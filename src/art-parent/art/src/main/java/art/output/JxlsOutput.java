/*
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.output;

import art.dbutils.DatabaseUtils;
import art.enums.ReportType;
import art.report.Report;
import art.reportparameter.ReportParameter;
import art.runreport.RunReportHelper;
import art.servlets.Config;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.sf.jxls.exception.ParsePropertyException;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to generate formatted excel workbooks using the jxls library. Doesn't
 * implement artOutputInterface like other output classes
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
	 * Generate report output
	 *
	 * @param report
	 * @param reportParams
	 * @param reportType
	 * @param outputFileName
	 * @throws java.sql.SQLException
	 * @throws java.io.IOException
	 * @throws org.apache.poi.openxml4j.exceptions.InvalidFormatException
	 */
	public void generateReport(Report report, List<ReportParameter> reportParams,
			ReportType reportType, String outputFileName)
			throws SQLException, ParsePropertyException, IOException, InvalidFormatException {
		
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
			Map<String, Object> jxlsParams = new HashMap<>();

			//pass query parameters
			for (ReportParameter reportParam : reportParams) {
				jxlsParams.put(reportParam.getParameter().getName(), reportParam.getEffectiveActualParameterValue());
			}

			if (reportType == ReportType.JxlsTemplate) {
				RunReportHelper runReportHelper = new RunReportHelper();
				conn = runReportHelper.getEffectiveReportDatasource(report, reportParams);
				JxlsReportManager reportManager = new JxlsReportManager(conn);
				jxlsParams.put("rm", reportManager);
			} else {
				//use recordset based on art query 
				RowSetDynaClass rsdc = new RowSetDynaClass(resultSet, false, true); //use lowercase properties = false, use column labels =true
				jxlsParams.put("results", rsdc.getRows());
			}

			//generate output
			XLSTransformer transformer = new XLSTransformer();
			transformer.transformXLS(fullTemplateFileName, jxlsParams, outputFileName);
		} finally {
			DatabaseUtils.close(conn);
		}
	}
}
