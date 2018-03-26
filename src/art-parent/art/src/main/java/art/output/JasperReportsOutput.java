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

import art.datasource.Datasource;
import art.dbutils.DatabaseUtils;
import art.enums.DatasourceType;
import art.enums.ReportFormat;
import art.enums.ReportType;
import art.report.Report;
import art.reportparameter.ReportParameter;
import art.runreport.RunReportHelper;
import art.servlets.Config;
import com.jaspersoft.mongodb.connection.MongoDbConnection;
import groovy.sql.GroovyRowResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.fill.JRAbstractLRUVirtualizer;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;
import net.sf.jasperreports.engine.fill.JRGzipVirtualizer;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.JRElementsVisitor;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSwapFile;
import net.sf.jasperreports.engine.util.JRVisitorSupport;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates reports using the JasperReports library
 *
 * @author Timothy Anyona
 */
public class JasperReportsOutput {

	private static final Logger logger = LoggerFactory.getLogger(JasperReportsOutput.class);

	private final List<String> completedSubReports = new ArrayList<>(); //used with recursive compileReport call
	private ResultSet resultSet;
	private String dynamicOpenPassword;
	private String dynamicModifyPassword;
	private Object data;

	/**
	 * @return the data
	 */
	public Object getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(Object data) {
		this.data = data;
	}

	/**
	 * @return the dynamicOpenPassword
	 */
	public String getDynamicOpenPassword() {
		return dynamicOpenPassword;
	}

	/**
	 * @param dynamicOpenPassword the dynamicOpenPassword to set
	 */
	public void setDynamicOpenPassword(String dynamicOpenPassword) {
		this.dynamicOpenPassword = dynamicOpenPassword;
	}

	/**
	 * @return the dynamicModifyPassword
	 */
	public String getDynamicModifyPassword() {
		return dynamicModifyPassword;
	}

	/**
	 * @param dynamicModifyPassword the dynamicModifyPassword to set
	 */
	public void setDynamicModifyPassword(String dynamicModifyPassword) {
		this.dynamicModifyPassword = dynamicModifyPassword;
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
	 * @param reportParams the report parameters to use, not null
	 * @param reportFormat the report format to use, not null
	 * @param outputFileName the full path of the output file to use, not null
	 * @throws java.io.IOException
	 * @throws java.sql.SQLException
	 * @throws net.sf.jasperreports.engine.JRException
	 */
	public void generateReport(Report report, List<ReportParameter> reportParams,
			ReportFormat reportFormat, String outputFileName)
			throws IOException, SQLException, JRException {

		logger.debug("Entering generateReport");

		Objects.requireNonNull(report, "report must not be null");
		Objects.requireNonNull(reportParams, "reportParams must not be null");
		Objects.requireNonNull(reportFormat, "reportFormat must not be null");
		Objects.requireNonNull(outputFileName, "outputFileName must not be null");

		//use JRAbstractLRUVirtualizer instead of JRVirtualizer to have access to setReadOnly() method
		JRAbstractLRUVirtualizer jrVirtualizer = null;

		try {
			String templateFileName = report.getTemplate();
			String baseTemplateFileName = FilenameUtils.getBaseName(templateFileName);
			String templatesPath = Config.getTemplatesPath();
			String jasperFilePath = templatesPath + baseTemplateFileName + ".jasper";
			String jrxmlFilePath = templatesPath + baseTemplateFileName + ".jrxml";

			File jasperFile = new File(jasperFilePath);
			File jrxmlFile = new File(jrxmlFilePath);

			if (!jasperFile.exists() && !jrxmlFile.exists()) {
				throw new IllegalStateException("Template file not found: " + baseTemplateFileName);
			}

			//compile report and subreports if necessary
			compileReport(baseTemplateFileName);

			//create object for storing all jasper reports parameters - query parameters, virtualizers, etc
			Map<String, Object> jasperReportsParams = new HashMap<>();

			//pass query parameters
			for (ReportParameter reportParam : reportParams) {
				jasperReportsParams.put(reportParam.getParameter().getName(), reportParam.getEffectiveActualParameterValue());
			}

			//pass virtualizer if it's to be used
			jrVirtualizer = createVirtualizer();
			if (jrVirtualizer != null) {
				jasperReportsParams.put(JRParameter.REPORT_VIRTUALIZER, jrVirtualizer);
			}

			//fill report with data
			JasperPrint jasperPrint;
			ReportType reportType = report.getReportType();
			if (reportType == ReportType.JasperReportsTemplate) {
				// What kind of a datasource are we using?
				Datasource ds = report.getDatasource();
				if (ds.getDatasourceType() == DatasourceType.MongoDB) {
					try (MongoDbConnection conn = new MongoDbConnection(ds.getUrl(), ds.getUsername(), ds.getPassword())) {
						jasperPrint = JasperFillManager.fillReport(jasperFilePath, jasperReportsParams, conn);
					}
				} else {
					Connection conn = null;
					try {
						RunReportHelper runReportHelper = new RunReportHelper();
						conn = runReportHelper.getEffectiveReportDatasource(report, reportParams);
						jasperPrint = JasperFillManager.fillReport(jasperFilePath, jasperReportsParams, conn);
					} finally {
						DatabaseUtils.close(conn);
					}
				}
			} else {
				if (data == null) {
					jasperPrint = JasperFillManager.fillReport(jasperFilePath, jasperReportsParams, new JRResultSetDataSource(resultSet));
				} else {
					List<Map<String, ?>> finalData = new ArrayList<>();
					@SuppressWarnings("unchecked")
					List<? extends Object> dataList = (List<? extends Object>) data;
					if (CollectionUtils.isNotEmpty(dataList)) {
						Object sample = dataList.get(0);
						if (sample instanceof GroovyRowResult) {
							for (Object row : dataList) {
								//https://6by9.wordpress.com/2012/10/13/groovyrowresult-as-a-hashmap/
								GroovyRowResult rowResult = (GroovyRowResult) row;
								Map<String, Object> rowMap = new LinkedHashMap<>();
								for (Object columnName : rowResult.keySet()) {
									rowMap.put(String.valueOf(columnName), rowResult.get(columnName));
								}
								finalData.add(rowMap);
							}
						} else if (sample instanceof Map) {
							for (Object row : dataList) {
								@SuppressWarnings("unchecked")
								Map<String, Object> rowMap = (Map<String, Object>) row;
								finalData.add(rowMap);
							}
						} else {
							throw new IllegalArgumentException("Unexpected data type: " + sample.getClass().getCanonicalName());
						}
					}

					jasperPrint = JasperFillManager.fillReport(jasperFilePath, jasperReportsParams, new JRMapCollectionDataSource(finalData));
				}
			}

			//set virtualizer as read only to optimize performance
			//must be set after print object has been generated
			if (jrVirtualizer != null) {
				jrVirtualizer.setReadOnly(true);
			}

			//export report
			//https://stackoverflow.com/questions/27779612/export-jasperreports-in-html-format
			switch (reportFormat) {
				case pdf:
					//https://stackoverflow.com/questions/32318421/how-can-i-export-to-pdf-in-jasperreports-6-1-alternate-of-using-jrpdfexporter-s
					//http://community.jaspersoft.com/questions/1038606/jasperreports-37-63-migration-how-replace-pdffont-font-mapping-code
					//http://chager.de/encrypting-and-restricting-pdf-reports-build-with-jasperreports/
					JRPdfExporter pdfExporter = new JRPdfExporter();

					pdfExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
					pdfExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputFileName));
					pdfExporter.exportReport();

					PdfHelper pdfHelper = new PdfHelper();
					pdfHelper.addProtections(report, outputFileName, dynamicOpenPassword, dynamicModifyPassword);
					break;
				case html:
					HtmlExporter htmlExporter = new HtmlExporter();

					htmlExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
					htmlExporter.setExporterOutput(new SimpleHtmlExporterOutput(outputFileName));
					htmlExporter.exportReport();
					break;
				case xls:
					JRXlsExporter xlsExporter = new JRXlsExporter();

					SimpleXlsReportConfiguration xlsConfiguration = new SimpleXlsReportConfiguration();
					xlsConfiguration.setOnePagePerSheet(Boolean.FALSE);
					xlsConfiguration.setRemoveEmptySpaceBetweenRows(Boolean.TRUE);
					xlsExporter.setConfiguration(xlsConfiguration);

					xlsExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
					xlsExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputFileName));
					xlsExporter.exportReport();
					break;
				case xlsx:
					JRXlsxExporter xlsxExporter = new JRXlsxExporter();

					SimpleXlsxReportConfiguration xlsxConfiguration = new SimpleXlsxReportConfiguration();
					xlsxConfiguration.setOnePagePerSheet(Boolean.FALSE);
					xlsxConfiguration.setRemoveEmptySpaceBetweenRows(Boolean.TRUE);
					xlsxExporter.setConfiguration(xlsxConfiguration);

					xlsxExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
					xlsxExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputFileName));
					xlsxExporter.exportReport();
					break;
				case docx:
					JRDocxExporter docxExporter = new JRDocxExporter();

					docxExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
					docxExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputFileName));
					docxExporter.exportReport();
					break;
				case odt:
					JROdtExporter odtExporter = new JROdtExporter();

					odtExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
					odtExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputFileName));
					odtExporter.exportReport();
					break;
				case ods:
					JROdsExporter odsExporter = new JROdsExporter();

					odsExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
					odsExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputFileName));
					odsExporter.exportReport();
					break;
				default:
					throw new IllegalArgumentException("Unexpected report format: " + reportFormat);
			}
		} finally {
			if (jrVirtualizer != null) {
				jrVirtualizer.cleanup();
			}
		}
	}

	/**
	 * Creates a jasper reports virtualizer using settings in the
	 * jasperreports.properties file. Swap virtualizer will be used if none is
	 * configured.
	 *
	 * @return created virtualizer or null if virtualizer property in the file
	 * is set to "none"
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	private JRAbstractLRUVirtualizer createVirtualizer() throws IOException {
		logger.debug("Entering createVirtualizer");

		//use JRAbstractLRUVirtualizer instead of JRVirtualizer in order to
		//provide access to setReadOnly() method
		JRAbstractLRUVirtualizer jrVirtualizer;

		//set virtualizer properties, if virtualizer is to be used
		Properties properties = new Properties();
		String propertiesFilePath = Config.getClassesPath() + "jasperreports.properties";
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

	/**
	 * Compiles a report and all its subreports
	 *
	 * @param baseFileName report file name without the extension
	 */
	private void compileReport(String baseFileName) throws JRException {
		logger.debug("Entering compileReport: baseFileName='{}'", baseFileName);

		String templatesPath = Config.getTemplatesPath();
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
				String subreportName = subreport.getExpression().getText().replace("\"", ""); //file name is quoted
				subreportName = StringUtils.substringBeforeLast(subreportName, ".");
				//Sometimes the same subreport can be used multiple times, but
				//there is no need to compile multiple times
				if (completedSubReports.contains(subreportName)) {
					return;
				}
				completedSubReports.add(subreportName);

				//recursively compile any reports within the subreport
				//see https://stackoverflow.com/questions/10265576/java-retain-information-in-recursive-function
				try {
					compileReport(subreportName);
				} catch (JRException ex) {
					throw new RuntimeException(ex);
				}
			}
		});
	}
}
