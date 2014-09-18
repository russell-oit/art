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

import art.dbutils.DbConnections;
import art.report.ReportRunner;
import art.servlets.ArtConfig;
import art.utils.*;
import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import net.sf.jxls.report.ReportManager;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
	String fullOutputFileName = "-No File";
	String queryName;
	String fileUserName;
	String y_m_d;
	String h_m_s;
	String exportPath;
	PrintWriter htmlout;

	/**
	 * Set html output object
	 *
	 * @param o html output object
	 */
	public void setWriter(PrintWriter o) {
		htmlout = o;
	}

	/**
	 * Set the directory where the file is to be saved
	 *
	 * @param s directory where the file is to be saved
	 */
	public void setExportPath(String s) {
		exportPath = s;
	}

	/**
	 * Get the full filename where the output has been saved
	 *
	 * @return full filename where the output has been saved
	 */
	public String getFileName() {
		return fullOutputFileName;
	}

	/**
	 * Set query name to be used in file name
	 *
	 * @param s query name to be used in file name
	 */
	public void setQueryName(String s) {
		queryName = s;
	}

	/**
	 * Set username to be used in file name
	 *
	 * @param s username to be used in file name
	 */
	public void setFileUserName(String s) {
		fileUserName = s;
	}

	/**
	 * Generate output and set final filename
	 *
	 * @param rs query resultset
	 * @param queryId query id
	 * @param inlineParams inline parameters
	 * @param multiParams multi parameters
	 */
	public void createFile(ResultSet rs, int queryId, Map<String, String> inlineParams, Map<String, String[]> multiParams, Map<String, ArtQueryParam> htmlParams) {
		Connection connQuery = null;
		Connection connArt = null;

		try {
			String templateFileName;
			int datasourceId;
			String querySql;

			//get query datasource and template file name
			connArt = ArtConfig.getConnection();
			ArtQuery aq = new ArtQuery();
			aq.create(connArt, queryId);
			templateFileName = aq.getTemplate();
			datasourceId = aq.getDatabaseId();
			querySql = aq.getText();

			String templatesPath = ArtConfig.getTemplatesPath();
			String fullTemplateFileName = templatesPath + templateFileName;

			String interactiveLink;

			//only proceed if template file available
			File templateFile = new File(fullTemplateFileName);
			if (!templateFile.exists()) {
				//template file doesn't exist.
				logger.warn("Template file not found: {}", fullTemplateFileName);

				fullOutputFileName = "-Template file not found";

				//display error message instead of link when running query interactively
				interactiveLink = "Template file not found. Please contact the ART administrator.";
			} else {
				//set objects to be passed to jxls
				Map<String, Object> beans = new HashMap<String, Object>();

				//process multi parameters to obtain parameter labels instead of parameter identifiers
				HashMap<String, String> mParams = new HashMap<String, String>();
				ReportRunner pq = new ReportRunner();
				pq.setReportId(queryId);
				pq.setMultiParams(multiParams);
				mParams.putAll(pq.getJxlsMultiParams(querySql));

				//pass parameters 
				beans.putAll(inlineParams);
				beans.putAll(mParams);

				if (rs == null) {
					//pass connection to template query

					//use dynamic datasource if so configured
					String dynamicDatasource = null; //id or name of dynamic datasource

					if (htmlParams != null) {
						for (Map.Entry<String, ArtQueryParam> entry : htmlParams.entrySet()) {
							ArtQueryParam param = entry.getValue();
							String paramDataType = param.getParamDataType();

							if (StringUtils.equalsIgnoreCase(paramDataType, "DATASOURCE")) {
								//get dynamic connection to use
								Object paramValueObject = param.getParamValue();
								if (paramValueObject != null) {
									String paramValue = (String) paramValueObject;
									if (StringUtils.isNotBlank(paramValue)) {
										dynamicDatasource = paramValue;
									}
								}
								break;
							}
						}
					}

					if (dynamicDatasource != null) {
						if (NumberUtils.isNumber(dynamicDatasource)) {
							//use datasource id
							connQuery = DbConnections.getConnection(Integer.parseInt(dynamicDatasource));
						} else {
							//use datasource name
							connQuery = DbConnections.getConnection(dynamicDatasource);
						}
					} else {
						//not using dynamic datasource. use datasource defined on the query
						connQuery = DbConnections.getConnection(datasourceId);
					}
					ReportManager reportManager = new ArtJxlsReportManager(connQuery);
					beans.put("rm", reportManager);
				} else {
					//use recordset based on art query 
					ArtJxlsResultSetCollection rsc = new ArtJxlsResultSetCollection(rs, false, true);
					beans.put("results", rsc);
				}

				//Build output filename 
				Calendar cal = Calendar.getInstance();
				java.util.Date today = cal.getTime();

				String dateFormat = "yyyy_MM_dd";
				SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
				y_m_d = dateFormatter.format(today);

				String timeFormat = "HH_mm_ss";
				SimpleDateFormat timeFormatter = new SimpleDateFormat(timeFormat);
				h_m_s = timeFormatter.format(today);

				String extension = "." + FilenameUtils.getExtension(templateFileName);

				String fileName = fileUserName + "-" + queryName + "-" + y_m_d + "-" + h_m_s + ArtUtils.getRandomFileNameString() + extension;
				fileName = ArtUtils.cleanFileName(fileName); //replace characters that would make an invalid filename
				fullOutputFileName = exportPath + fileName;

				//generate output
				XLSTransformer transformer = new XLSTransformer();
				transformer.transformXLS(fullTemplateFileName, beans, fullOutputFileName);

				interactiveLink = "<a type=\"application/octet-stream\" href=\"../export/" + fileName + "\"> "
						+ fileName + "</a>";
			}

			//display link to access report if run interactively
			if (htmlout != null) {
				htmlout.println("<p><div align=\"center\"><table border=\"0\" width=\"90%\">");
				htmlout.println("<tr><td colspan=\"2\" class=\"data\" align=\"center\" >"
						+ interactiveLink
						+ "</td></tr>");
				htmlout.println("</table></div></p>");
			}

		} catch (Exception e) {
			logger.error("Error", e);
			if (htmlout != null) {
				//display error message on browser
				htmlout.println("<b>Error while generating report:</b> <p>" + e + "</p>");
			}
		} finally {
			try {
				if (connQuery != null) {
					connQuery.close();
				}
				if (connArt != null) {
					connArt.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}
	}
}