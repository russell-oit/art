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
import art.enums.ReportType;
import art.report.Report;
import art.reportoptions.JxlsOptions;
import art.reportparameter.ReportParameter;
import art.runreport.RunReportHelper;
import art.servlets.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.apache.commons.beanutils.RowSetDynaClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jxls.area.Area;
import org.jxls.builder.AreaBuilder;
import org.jxls.builder.xml.XmlAreaBuilder;
import org.jxls.common.Context;
import org.jxls.jdbc.JdbcHelper;
import org.jxls.transform.Transformer;
import org.jxls.util.JxlsHelper;
import org.jxls.util.TransformerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates formatted excel workbooks using the jxls library
 *
 * @author Timothy Anyona
 */
public class JxlsOutput {

	private static final Logger logger = LoggerFactory.getLogger(JxlsOutput.class);

	private ResultSet resultSet;
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
	 * @param resultSet the resultSet to set
	 */
	public void setResultSet(ResultSet resultSet) {
		this.resultSet = resultSet;
	}

	/**
	 * Generates report output
	 *
	 * @param report the report to use, not null
	 * @param reportParams the report parameters, not null
	 * @param outputFileName the full path of the output file, not null
	 * @throws java.sql.SQLException
	 * @throws java.io.IOException
	 * @throws org.apache.poi.openxml4j.exceptions.InvalidFormatException
	 */
	public void generateReport(Report report, List<ReportParameter> reportParams,
			String outputFileName)
			throws SQLException, IOException, InvalidFormatException {

		logger.debug("Entering generateReport");

		Objects.requireNonNull(report, "report must not be null");
		Objects.requireNonNull(reportParams, "reportParams must not be null");
		Objects.requireNonNull(outputFileName, "outputFileName must not be null");

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

			File templateFile = new File(fullTemplateFileName);
			if (!templateFile.exists()) {
				throw new IllegalStateException("Template file not found: " + templateFileName);
			}

			//set objects to be passed to jxls
			Context context = new Context();

			//pass report parameters
			for (ReportParameter reportParam : reportParams) {
				String paramName = reportParam.getParameter().getName();
				context.putVar(paramName, reportParam);
			}

			context.putVar("params", reportParams);
			context.putVar("locale", locale);

			String optionsString = report.getOptions();

			JxlsOptions options;
			if (StringUtils.isBlank(optionsString)) {
				options = new JxlsOptions();
			} else {
				ObjectMapper mapper = new ObjectMapper();
				options = mapper.readValue(optionsString, JxlsOptions.class);
			}

			String areaConfigFilename = options.getAreaConfigFile();

			if (StringUtils.isNotBlank(areaConfigFilename)) {
				String fullAreaConfigFilename = templatesPath + areaConfigFilename;
				File areaConfigFile = new File(fullAreaConfigFilename);
				if (!areaConfigFile.exists()) {
					throw new IllegalStateException("Area config file not found: " + areaConfigFilename);
				}
			}

			String fullAreaConfigFilename = templatesPath + areaConfigFilename;

			ReportType reportType = report.getReportType();
			if (reportType == ReportType.JxlsTemplate) {
				RunReportHelper runReportHelper = new RunReportHelper();
				conn = runReportHelper.getEffectiveReportDatasource(report, reportParams);
				JdbcHelper jdbcHelper = new JdbcHelper(conn);
				try (InputStream is = new FileInputStream(fullTemplateFileName)) {
					try (OutputStream os = new FileOutputStream(outputFileName)) {
						context.putVar("conn", conn);
						context.putVar("jdbc", jdbcHelper);
						if (StringUtils.isBlank(areaConfigFilename)) {
							JxlsHelper.getInstance().processTemplate(is, os, context);
						} else {
							processUsingXmlConfig(fullAreaConfigFilename, is, os, context);
						}
					}
				}
			} else {
				//use recordset based on art query
				boolean useLowerCaseProperties = false;
				boolean useColumnLabels = true;
				RowSetDynaClass rsdc = new RowSetDynaClass(resultSet, useLowerCaseProperties, useColumnLabels);
				context.putVar("results", rsdc.getRows());
				try (InputStream is = new FileInputStream(fullTemplateFileName)) {
					try (OutputStream os = new FileOutputStream(outputFileName)) {
						if (StringUtils.isBlank(areaConfigFilename)) {
							JxlsHelper.getInstance().processTemplate(is, os, context);
						} else {
							processUsingXmlConfig(fullAreaConfigFilename, is, os, context);
						}
					}
				}
			}
		} finally {
			DatabaseUtils.close(conn);
		}
	}

	/**
	 * Processes template using xml config to define areas
	 *
	 * @param fullAreaConfigFilename full file name of config file, including
	 * path
	 * @param is source template input stream
	 * @param os target file output stream
	 * @param context context
	 * @throws IOException
	 */
	private void processUsingXmlConfig(String fullAreaConfigFilename,
			final InputStream is, final OutputStream os, Context context) throws IOException {

		//http://jxls.sourceforge.net/samples/object_collection_xmlbuilder.html
		Transformer transformer = TransformerFactory.createTransformer(is, os);
		try (InputStream configInputStream = new FileInputStream(fullAreaConfigFilename)) {
			AreaBuilder areaBuilder = new XmlAreaBuilder(configInputStream, transformer);
			List<Area> xlsAreaList = areaBuilder.build();
			for (Area xlsArea : xlsAreaList) {
				xlsArea.applyAt(xlsArea.getStartCellRef(), context);
			}
			transformer.write();
		}
	}
}
