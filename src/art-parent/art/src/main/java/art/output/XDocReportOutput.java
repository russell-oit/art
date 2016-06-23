/*
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.output;

import art.enums.ReportFormat;
import art.enums.ReportType;
import art.report.Report;
import art.reportparameter.ReportParameter;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates output using the xdocreport library
 *
 * @author Timothy Anyona
 */
public class XDocReportOutput {

	private static final Logger logger = LoggerFactory.getLogger(XDocReportOutput.class);

	/**
	 * Generates report output
	 *
	 * @param report the report to use, not null
	 * @param reportParams the report parameters
	 * @param resultSet the resultset containing report data, not null
	 * @param reportFormat
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

		String templateFileName = report.getTemplate();
		String templatesPath = Config.getTemplatesPath();
		String fullTemplateFileName = templatesPath + templateFileName;

		//check if template file exists
		File templateFile = new File(fullTemplateFileName);
		if (!templateFile.exists()) {
			throw new IllegalStateException("Template file not found: " + templateFileName);
		}

		//load doc
		ReportType reportType = report.getReportType();
		TemplateEngineKind templateEngineKind;
		switch (reportType) {
			case XDocReportFreeMarkerDocx:
			case XDocReportFreeMarkerOdt:
				templateEngineKind = TemplateEngineKind.Freemarker;
				break;
			case XDocReportVelocityDocx:
			case XDocReportVelocityOdt:
				templateEngineKind = TemplateEngineKind.Velocity;
				break;
			default:
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

		//pass report data
		boolean useLowerCaseProperties = false;
		boolean useColumnLabels = true;
		RowSetDynaClass rsdc = new RowSetDynaClass(resultSet, useLowerCaseProperties, useColumnLabels);
		context.put("results", rsdc.getRows());

		//add metadata for iteration of results using list syntax
		FieldsMetadata metadata = new FieldsMetadata();
		DynaProperty[] columns = rsdc.getDynaProperties();
		for (DynaProperty column : columns) {
			String metadataFieldName = "results." + column.getName();
			metadata.addFieldAsList(metadataFieldName);
		}
		xdocReport.setFieldsMetadata(metadata);

		//create output
		try (OutputStream out = new FileOutputStream(new File(outputFileName))) {
			if ((reportType.isXDocReportDocx() && reportFormat == ReportFormat.docx)
					|| (reportType.isXDocReportOdt() && reportFormat == ReportFormat.odt)) {
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
	}
}
