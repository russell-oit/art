/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.output;

import art.dbutils.DatabaseUtils;
import art.enums.ReportType;
import art.report.Report;
import art.reportparameter.ReportParameter;
import art.runreport.RunReportHelper;
import art.servlets.Config;
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
import java.util.Objects;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jxls.common.Context;
import org.jxls.jdbc.JdbcHelper;
import org.jxls.util.JxlsHelper;
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

			//check if template file exists
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

			ReportType reportType = report.getReportType();
			if (reportType == ReportType.JxlsTemplate) {
				RunReportHelper runReportHelper = new RunReportHelper();
				conn = runReportHelper.getEffectiveReportDatasource(report, reportParams);
				JdbcHelper jdbcHelper = new JdbcHelper(conn);
				try (InputStream is = new FileInputStream(fullTemplateFileName)) {
					try (OutputStream os = new FileOutputStream(outputFileName)) {
						context.putVar("conn", conn);
						context.putVar("jdbc", jdbcHelper);
						JxlsHelper.getInstance().processTemplate(is, os, context);
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
						JxlsHelper.getInstance().processTemplate(is, os, context);
					}
				}
			}
		} finally {
			DatabaseUtils.close(conn);
		}
	}
}
