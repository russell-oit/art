/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.output;

import art.dbutils.DatabaseUtils;
import art.enums.ReportFormat;
import art.enums.ReportType;
import art.report.Report;
import art.reportparameter.ReportParameter;
import art.runreport.RunReportHelper;
import art.servlets.Config;
import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.ConverterTypeVia;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates output using the xdocreport library
 *
 * @author Timothy Anyona
 */
public class XDocReportOutput {

	private static final Logger logger = LoggerFactory.getLogger(XDocReportOutput.class);

	private Locale locale;

	/**
	 * @return the locale
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @param locale the locale to set
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * Generates report output
	 *
	 * @param report the report to use, not null
	 * @param reportParams the report parameters
	 * @param resultSet the resultset containing report data, not null
	 * @param reportFormat the report format for the report
	 * @param outputFileName the full output file name to use for the generated
	 * report
	 * @throws java.sql.SQLException
	 * @throws fr.opensagres.xdocreport.core.XDocReportException
	 * @throws java.io.IOException
	 */
	public void generateReport(Report report, List<ReportParameter> reportParams,
			ResultSet resultSet, ReportFormat reportFormat, String outputFileName)
			throws SQLException, XDocReportException, IOException {

		logger.debug("Entering generateReport");

		Objects.requireNonNull(report, "report must not be null");
		Objects.requireNonNull(resultSet, "resultset must not be null");
		Objects.requireNonNull(outputFileName, "outputFileName must not be null");
		Objects.requireNonNull(reportFormat, "reportFormat must not be null");

		Connection conn = null;

		try {

			String templateFileName = report.getTemplate();
			String templatesPath = Config.getTemplatesPath();
			String fullTemplateFileName = templatesPath + templateFileName;

			logger.debug("templateFileName='{}'", templateFileName);

			//need to explicitly check if template file is empty string
			//otherwise file.exists() will return true because fullTemplateFileName will just have the directory name
			if (StringUtils.isBlank(templateFileName)) {
				throw new IllegalArgumentException("Template file not specified");
			}

			//check if template file exists
			File templateFile = new File(fullTemplateFileName);
			if (!templateFile.exists()) {
				throw new IllegalStateException("Template file not found: " + templateFileName);
			}

			//load doc
			ReportType reportType = report.getReportType();
			TemplateEngineKind templateEngineKind;

			if (reportType.isXDocReportFreeMarker()) {
				templateEngineKind = TemplateEngineKind.Freemarker;
			} else if (reportType.isXDocReportVelocity()) {
				templateEngineKind = TemplateEngineKind.Velocity;
			} else {
				throw new IllegalArgumentException("Unexpected report type: " + reportType);
			}

			InputStream in = new FileInputStream(fullTemplateFileName);
			IXDocReport xdocReport = XDocReportRegistry.getRegistry().loadReport(in, templateEngineKind);

			//set objects to be passed to template
			IContext context = xdocReport.createContext();

			//pass report parameters
			if (reportParams != null) {
				for (ReportParameter reportParam : reportParams) {
					String paramName = reportParam.getParameter().getName();
					context.put(paramName, reportParam);
				}
			}

			context.put("params", reportParams);
			context.put("locale", getLocale());

			if (reportType.isXDocReportVelocity()) {
				NumberTool numberTool = new NumberTool();
				context.put("numberTool", numberTool);

				DateTool dateTool = new DateTool();
				context.put("dateTool", dateTool);
			}

			//pass report data
			boolean useLowerCaseProperties = false;
			boolean useColumnLabels = true;
			RowSetDynaClass rsdc = new RowSetDynaClass(resultSet, useLowerCaseProperties, useColumnLabels);
			context.put("results", rsdc.getRows());

			//add metadata to indicate results fields are list fields
			FieldsMetadata metadata = new FieldsMetadata();
			DynaProperty[] columns = rsdc.getDynaProperties();
			for (DynaProperty column : columns) {
				String metadataFieldName = "results." + column.getName();
				metadata.addFieldAsList(metadataFieldName);
			}
			xdocReport.setFieldsMetadata(metadata);

			RunReportHelper runReportHelper = new RunReportHelper();
			conn = runReportHelper.getEffectiveReportDatasource(report, reportParams);
			ArtJxlsJdbcHelper jdbcHelper = new ArtJxlsJdbcHelper(conn);
			context.put("jdbc", jdbcHelper);

			//create output
			try (OutputStream out = new FileOutputStream(new File(outputFileName))) {
				if ((reportType.isXDocReportDocx() && reportFormat == ReportFormat.docx)
						|| (reportType.isXDocReportOdt() && reportFormat == ReportFormat.odt)
						|| (reportType.isXDocReportPptx() && reportFormat == ReportFormat.pptx)) {
					//no conversion
					xdocReport.process(context, out);
				} else {
					Options options = null;
					if (reportType.isXDocReportDocx()) {
						switch (reportFormat) {
							case html:
								options = Options.getTo(ConverterTypeTo.XHTML).via(ConverterTypeVia.XWPF);
								break;
							case pdf:
								options = Options.getTo(ConverterTypeTo.PDF).via(ConverterTypeVia.XWPF);
								break;
							default:
								throw new IllegalArgumentException("Unexpected report format: " + reportFormat);
						}
					} else if (reportType.isXDocReportOdt()) {
						switch (reportFormat) {
							case html:
								options = Options.getTo(ConverterTypeTo.XHTML).via(ConverterTypeVia.ODFDOM);
								break;
							case pdf:
								options = Options.getTo(ConverterTypeTo.PDF).via(ConverterTypeVia.ODFDOM);
								break;
							default:
								throw new IllegalArgumentException("Unexpected report format: " + reportFormat);
						}
					} else {
						throw new IllegalArgumentException("Unexpected report type: " + reportType);
					}

					xdocReport.convert(context, options, out);
				}
			}

			if (reportFormat == ReportFormat.pdf) {
				PdfHelper pdfHelper = new PdfHelper();
				pdfHelper.addProtections(report, outputFileName);
			}

		} finally {
			DatabaseUtils.close(conn);
		}
	}
}
