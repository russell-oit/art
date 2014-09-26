/*
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.output;

import art.dbutils.DbConnections;
import art.dbutils.DbUtils;
import art.enums.ParameterDataType;
import art.report.Report;
import art.servlets.ArtConfig;
import art.utils.ArtUtils;
import art.runreport.ReportRunner;
import art.reportparameter.ReportParameter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRXhtmlExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.fill.JRAbstractLRUVirtualizer;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;
import net.sf.jasperreports.engine.fill.JRGzipVirtualizer;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.JRElementsVisitor;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSwapFile;
import net.sf.jasperreports.engine.util.JRVisitorSupport;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to generate reports using the JasperReports library
 *
 * @author Timothy Anyona
 */
public class JasperReportsOutput {

	private static final Logger logger = LoggerFactory.getLogger(JasperReportsOutput.class);
	String fullFileName;
	String reportFormat;
	String exportPath;
	PrintWriter htmlWriter;
	private final List<String> completedSubReports = new ArrayList<>(); //used with recursive compileReport call
	private ResultSet resultSet;

	/**
	 * @return the resultSet
	 */
	public ResultSet getResultSet() {
		return resultSet;
	}

	/**
	 * @param resultSet the resultSet to set
	 */
	public void setResultSet(ResultSet resultSet) {
		this.resultSet = resultSet;
	}

	/**
	 * Set html output object
	 *
	 * @param o html output object
	 */
	public void setWriter(PrintWriter o) {
		htmlWriter = o;
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
		return fullFileName;
	}

	/**
	 * Set the report format
	 *
	 * @param s report format
	 */
	public void setReportFormat(String s) {
		reportFormat = s;
	}

	/**
	 * Generate report output and set final filename
	 *
	 * @param report
	 * @param reportParams
	 */
	public void generateReport(Report report, Map<String, ReportParameter> reportParams) {
		Objects.requireNonNull(report, "Report must not be null");

		Connection connQuery = null;

		JRAbstractLRUVirtualizer jrVirtualizer = null;

		try {
			String templateFileName = report.getTemplate();
			int datasourceId = report.getDatasource().getDatasourceId();
			String querySql = report.getReportSource();

			String baseFileName = FilenameUtils.getBaseName(templateFileName);
			String templatesPath = ArtConfig.getTemplatesPath();
			String jasperFilePath = templatesPath + baseFileName + ".jasper";
			String jrxmlFilePath = templatesPath + baseFileName + ".jrxml";

			File jasperFile = new File(jasperFilePath);
			File jrxmlFile = new File(jrxmlFilePath);

			String interactiveLink;

			//only proceed if template file available
			if (!jasperFile.exists() && !jrxmlFile.exists()) {
				//template file doesn't exist.
				logger.warn("Template file not found: {}.jrxml", templatesPath + baseFileName);

				fullFileName = "-Template file not found";

				//display error message instead of link when running query interactively
				interactiveLink = "Template file not found";
			} else {
				//compile report and subreports if necessary
				compileReport(baseFileName);

				//create object for storing all jasper reports parameters - query parameters, virtualizers, etc
				Map<String, Object> jasperReportsParams = new HashMap<>();

				//prepare virtualizer if it's to be used
				jrVirtualizer = createVirtualizer();
				if (jrVirtualizer != null) {
					jasperReportsParams.put(JRParameter.REPORT_VIRTUALIZER, jrVirtualizer);
				}

				//process parameters to obtain appropriate jasper reports data type objects
				ReportRunner reportRunner = new ReportRunner();
				jasperReportsParams.putAll(reportRunner.getJasperReportsParameters(report, reportParams));

				//fill report with data
				JasperPrint jasperPrint;
				if (resultSet == null) {
					//use template query

					//use dynamic datasource if so configured
					String dynamicDatasource = null; //id or name of dynamic datasource

					if (reportParams != null) {
						for (Entry<String, ReportParameter> entry : reportParams.entrySet()) {
							ReportParameter reportParam = entry.getValue();
							ParameterDataType paramDataType = reportParam.getParameter().getDataType();

							if (paramDataType == ParameterDataType.Datasource) {
								//get dynamic connection to use
								String[] paramValues = reportParam.getPassedParameterValues();
								if (paramValues != null) {
									String paramValue = paramValues[0];
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
					jasperPrint = JasperFillManager.fillReport(jasperFilePath, jasperReportsParams, connQuery);
				} else {
					//use recordset from art query
					JRResultSetDataSource ds;
					ds = new JRResultSetDataSource(resultSet);
					jasperPrint = JasperFillManager.fillReport(jasperFilePath, jasperReportsParams, ds);
				}

				//set virtualizer as read only to optimize performance
				//must be set after print object has been generated
				if (jrVirtualizer != null) {
					jrVirtualizer.setReadOnly(true);
				}

				// Build output filename base
				String fileName = ArtUtils.getUniqueFileName(report.getReportId(), reportFormat);
				fullFileName = exportPath + fileName;

				//export report
				if (StringUtils.equals(reportFormat, "pdf")) {
					JasperExportManager.exportReportToPdfFile(jasperPrint, fullFileName);
				} else if (StringUtils.equals(reportFormat, "html")) {
					JRXhtmlExporter exporter = new JRXhtmlExporter();

					exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
					exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, fullFileName);

					exporter.exportReport();
				} else if (StringUtils.equals(reportFormat, "xls")) {
					JRXlsExporter exporter = new JRXlsExporter();

					exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
					exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, fullFileName);
					exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
					exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);

					exporter.exportReport();
				} else if (StringUtils.equals(reportFormat, "xlsx")) {
					JRXlsxExporter exporter = new JRXlsxExporter();

					exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
					exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, fullFileName);
					exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
					exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);

					exporter.exportReport();
				}

				interactiveLink = "<a type=\"application/octet-stream\" href=\"../export/" + fileName + "\"> "
						+ fileName + "</a>";
			}

			//display link to access report if run interactively
			if (htmlWriter != null) {
				htmlWriter.println("<p><div align=\"center\"><table border=\"0\" width=\"90%\">");
				htmlWriter.println("<tr><td colspan=\"2\" class=\"data\" align=\"center\" >"
						+ interactiveLink
						+ "</td></tr>");
				htmlWriter.println("</table></div></p>");
			}
		} catch (Exception ex) {
			logger.error("Error", ex);
			if (htmlWriter != null) {
				//display error message on browser
				htmlWriter.println("<b>Error while generating report:</b> <p>" + ex + "</p>");
			}
		} finally {
			DbUtils.close(connQuery);

			if (jrVirtualizer != null) {
				jrVirtualizer.cleanup();
			}
		}
	}

	/**
	 * Create a jasper reports virtualizer using settings in the
	 * jasperreports.properties file. swap virtualizer will be used if none is
	 * configured.
	 *
	 * @return created virtualizer or null if virtualizer property in the file
	 * is set to "none"
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	private JRAbstractLRUVirtualizer createVirtualizer() throws IOException {
		//use JRAbstractLRUVirtualizer instead of JRVirtualizer so as to be able to call setReadOnly method
		JRAbstractLRUVirtualizer jrVirtualizer;

		//set virtualizer properties, if virtualizer is to be used
		Properties properties = new Properties();
		String propertiesFilePath = ArtConfig.getClassesPath() + "jasperreports.properties";
		File propertiesFile = new File(propertiesFilePath);
		if (propertiesFile.exists()) {
			try (FileInputStream o = new FileInputStream(propertiesFilePath)) {
				properties.load(o);
			}
		}

		//finalize properties object
		//use values from the properties file if they exist, otherwise use defaults
		final String VIRTUALIZER = "virtualizer";
		final String SWAP_MAX_SIZE = "swap.maxSize";
		final String SWAP_BLOCK_SIZE = "swap.blockSize";
		final String SWAP_MIN_GROW_COUNT = "swap.minGrowCount";
		final String SWAP_DIRECTORY = "swap.directory";
		final String FILE_MAX_SIZE = "file.maxSize";
		final String FILE_DIRECTORY = "file.directory";
		final String GZIP_MAX_SIZE = "gzip.maxSize";
		final String DIRECTORY = "directory";
		final String MAX_SIZE = "maxSize";

		if (properties.getProperty(VIRTUALIZER) == null) {
			properties.setProperty(VIRTUALIZER, "swap");
		}
		if (properties.getProperty(SWAP_MAX_SIZE) == null) {
			properties.setProperty(SWAP_MAX_SIZE, "300");
		}
		if (properties.getProperty(SWAP_BLOCK_SIZE) == null) {
			properties.setProperty(SWAP_BLOCK_SIZE, "4096");
		}
		if (properties.getProperty(SWAP_MIN_GROW_COUNT) == null) {
			properties.setProperty(SWAP_MIN_GROW_COUNT, "1024");
		}
		if (properties.getProperty(SWAP_DIRECTORY) == null) {
			properties.setProperty(SWAP_DIRECTORY, System.getProperty("java.io.tmpdir"));
		}
		if (properties.getProperty(FILE_MAX_SIZE) == null) {
			properties.setProperty(FILE_MAX_SIZE, "300");
		}
		if (properties.getProperty(FILE_DIRECTORY) == null) {
			properties.setProperty(FILE_DIRECTORY, System.getProperty("java.io.tmpdir"));
		}
		if (properties.getProperty(GZIP_MAX_SIZE) == null) {
			properties.setProperty(GZIP_MAX_SIZE, "300");
		}

		//use virtualizer if required
		String virtualizer = properties.getProperty(VIRTUALIZER);
		logger.debug("virtualizer='{}'", virtualizer);

		if (StringUtils.equalsIgnoreCase(virtualizer, "none")) {
			jrVirtualizer = null;
		} else if (StringUtils.equalsIgnoreCase(virtualizer, "file")) {
			int maxSize = NumberUtils.toInt(properties.getProperty(FILE_MAX_SIZE));
			jrVirtualizer = new JRFileVirtualizer(maxSize, properties.getProperty(FILE_DIRECTORY));
		} else if (StringUtils.equalsIgnoreCase(virtualizer, "gzip")) {
			int maxSize = NumberUtils.toInt(properties.getProperty(GZIP_MAX_SIZE));
			jrVirtualizer = new JRGzipVirtualizer(maxSize);
		} else {
			//use swap virtualizer by default
			int maxSize = NumberUtils.toInt(properties.getProperty(SWAP_MAX_SIZE));
			int blockSize = NumberUtils.toInt(properties.getProperty(SWAP_BLOCK_SIZE));
			int minGrowCount = NumberUtils.toInt(properties.getProperty(SWAP_MIN_GROW_COUNT));
			JRSwapFile swapFile = new JRSwapFile(properties.getProperty(SWAP_DIRECTORY), blockSize, minGrowCount);
			jrVirtualizer = new JRSwapFileVirtualizer(maxSize, swapFile);
		}

		return jrVirtualizer;
	}

	public int getIntOrError(String value) {
		if (NumberUtils.isNumber(value)) {
			return NumberUtils.toInt(value);
		} else {
			throw new IllegalArgumentException("Number expected. Found '" + value + "'");
		}
	}

	/**
	 * Compile a report and all it's subreports
	 *
	 * @param baseFileName report file name without the extension
	 */
	private void compileReport(String baseFileName) {

		//see https://stackoverflow.com/questions/10265576/java-retain-information-in-recursive-function
		try {
			String templatesPath = ArtConfig.getTemplatesPath();
			String jasperFilePath = templatesPath + baseFileName + ".jasper";
			String jrxmlFilePath = templatesPath + baseFileName + ".jrxml";

			File jasperFile = new File(jasperFilePath);
			File jrxmlFile = new File(jrxmlFilePath);

			//compile report if .jasper doesn't exist or is outdated
			if (!jasperFile.exists() || (jasperFile.lastModified() < jrxmlFile.lastModified())) {
				JasperCompileManager.compileReportToFile(jrxmlFilePath, jasperFilePath);
			}
			//load report object
			JasperReport jasperReport = (JasperReport) JRLoader.loadObjectFromFile(jasperFilePath);

			//Compile sub reports
			JRElementsVisitor.visitReport(jasperReport, new JRVisitorSupport() {
				@Override
				public void visitSubreport(JRSubreport subreport) {
					try {
						String subreportName = subreport.getExpression().getText().replace("\"", ""); //file name is quoted
						subreportName = StringUtils.substringBeforeLast(subreportName, ".");
						//Sometimes the same subreport can be used multiple times, but
						//there is no need to compile multiple times
						if (completedSubReports.contains(subreportName)) {
							return;
						}
						completedSubReports.add(subreportName);

						//recursively compile any reports within the subreport
						compileReport(subreportName);
					} catch (Exception e) {
						logger.error("Error", e);
					}
				}
			});

		} catch (JRException ex) {
			logger.error("Error", ex);
		}
	}
}
