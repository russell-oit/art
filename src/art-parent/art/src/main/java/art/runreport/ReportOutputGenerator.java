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
package art.runreport;

import art.chart.Chart;
import art.chart.CategoryBasedChart;
import art.chart.ChartUtils;
import art.chart.PieChart;
import art.chart.SpeedometerChart;
import art.chart.TimeSeriesBasedChart;
import art.chart.XYChart;
import art.chart.XYZBasedChart;
import art.connectionpool.DbConnections;
import art.datasource.Datasource;
import art.dbutils.DatabaseUtils;
import art.drilldown.Drilldown;
import art.drilldown.DrilldownService;
import art.enums.ReportFormat;
import art.enums.ReportType;
import art.enums.ZipType;
import art.output.CsvOutputArt;
import art.output.CsvOutputUnivocity;
import art.output.DocxOutput;
import art.output.FixedWidthOutput;
import art.output.FreeMarkerOutput;
import art.output.StandardOutput;
import art.output.GroupHtmlOutput;
import art.output.GroupXlsxOutput;
import art.output.HtmlDataTableOutput;
import art.output.HtmlFancyOutput;
import art.output.HtmlGridOutput;
import art.output.HtmlPlainOutput;
import art.output.JasperReportsOutput;
import art.output.JsonOutput;
import art.output.JsonOutputResult;
import art.output.JxlsOutput;
import art.output.OdsOutput;
import art.output.OdtOutput;
import art.output.PdfOutput;
import art.output.ResultSetColumn;
import art.output.Rss20Output;
import art.output.SlkOutput;
import art.output.StandardOutputResult;
import art.output.ThymeleafOutput;
import art.output.TsvOutput;
import art.output.VelocityOutput;
import art.output.XDocReportOutput;
import art.output.XlsOutput;
import art.output.XlsxOutput;
import art.output.XmlOutput;
import art.report.ChartOptions;
import art.report.Report;
import art.report.ReportService;
import art.reportengine.ReportEngineOutput;
import art.reportoptions.C3Options;
import art.reportoptions.ChartJsOptions;
import art.reportoptions.CsvOutputArtOptions;
import art.reportoptions.CsvOutputUnivocityOptions;
import art.reportoptions.CsvServerOptions;
import art.reportoptions.DataTablesOptions;
import art.reportoptions.DatamapsOptions;
import art.reportoptions.JFreeChartOptions;
import art.reportoptions.MongoDbOptions;
import art.reportoptions.OrgChartOptions;
import art.reportoptions.ReportEngineOptions;
import art.reportoptions.WebMapOptions;
import art.reportparameter.ReportParameter;
import art.servlets.Config;
import art.user.User;
import art.utils.ArtHelper;
import art.utils.ArtUtils;
import art.utils.GroovySandbox;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import fr.opensagres.xdocreport.core.XDocReportException;
import freemarker.template.TemplateException;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.cewolfart.ChartValidationException;
import net.sf.cewolfart.DatasetProduceException;
import net.sf.cewolfart.PostProcessingException;
import net.sf.jasperreports.engine.JRException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.bson.types.ObjectId;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.kohsuke.groovy.sandbox.SandboxTransformer;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

/**
 * Generates report output
 *
 * @author Timothy Anyona
 */
public class ReportOutputGenerator {

	private static final Logger logger = LoggerFactory.getLogger(ReportOutputGenerator.class);

	//optional variables for generateOutput() method
	private HttpServletRequest request;
	private HttpServletResponse response;
	private ServletContext servletContext;
	private DrilldownService drilldownService;
	private boolean isJob = false;
	private boolean pdfPageNumbers = true;
	private String dynamicOpenPassword;
	private String dynamicModifyPassword;
	//global variables
	private ResultSet rs;
	private Report report;
	private ReportType reportType;
	private ReportRunner reportRunner;
	private Object groovyData;
	private Integer groovyDataSize;
	private Integer rowsRetrieved;
	private Locale locale;
	private PrintWriter writer;
	private String fullOutputFilename;
	private MessageSource messageSource;
	private ReportFormat reportFormat;
	private String contextPath;
	private String fileName;
	private List<ReportParameter> applicableReportParamsList;
	private ReportOptions reportOptions;
	private Map<String, ReportParameter> reportParamsMap;
	private ChartOptions parameterChartOptions;
	private User user;
	private Locale reportOutputLocale;

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
	 * @return the pdfPageNumbers
	 */
	public boolean isPdfPageNumbers() {
		return pdfPageNumbers;
	}

	/**
	 * @param pdfPageNumbers the pdfPageNumbers to set
	 */
	public void setPdfPageNumbers(boolean pdfPageNumbers) {
		this.pdfPageNumbers = pdfPageNumbers;
	}

	/**
	 * @return the isJob
	 */
	public boolean isIsJob() {
		return isJob;
	}

	/**
	 * @param isJob the isJob to set
	 */
	public void setIsJob(boolean isJob) {
		this.isJob = isJob;
	}

	/**
	 * @return the drilldownService
	 */
	public DrilldownService getDrilldownService() {
		return drilldownService;
	}

	/**
	 * @param drilldownService the drilldownService to set
	 */
	public void setDrilldownService(DrilldownService drilldownService) {
		this.drilldownService = drilldownService;
	}

	/**
	 * @return the request
	 */
	public HttpServletRequest getRequest() {
		return request;
	}

	/**
	 * @param request the request to set
	 */
	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	/**
	 * @return the response
	 */
	public HttpServletResponse getResponse() {
		return response;
	}

	/**
	 * @param response the response to set
	 */
	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	/**
	 * @return the servletContext
	 */
	public ServletContext getServletContext() {
		return servletContext;
	}

	/**
	 * @param servletContext the servletContext to set
	 */
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	/**
	 * Generates report output
	 *
	 * @param report the report to use
	 * @param reportRunner the report runner to use
	 * @param reportFormat the report format
	 * @param locale the locale to use
	 * @param paramProcessorResult the parameter processor result
	 * @param writer the output writer to use
	 * @param fullOutputFilename the full path of the output file name
	 * @param user the user under who's permissions the report is being
	 * generated
	 * @param messageSource the messagesource to use
	 * @return the output result
	 * @throws Exception
	 */
	public ReportOutputGeneratorResult generateOutput(Report report, ReportRunner reportRunner,
			ReportFormat reportFormat, Locale locale,
			ParameterProcessorResult paramProcessorResult,
			PrintWriter writer, String fullOutputFilename, User user, MessageSource messageSource)
			throws Exception {

		logger.debug("Entering generateOutput");

		if (!isJob) {
			Objects.requireNonNull(request, "request must not be null");
			Objects.requireNonNull(response, "response must not be null");
			Objects.requireNonNull(servletContext, "servletContext must not be null");
			Objects.requireNonNull(drilldownService, "drilldownService must not be null");
		}

		ReportOutputGeneratorResult outputResult = new ReportOutputGeneratorResult();
		outputResult.setSuccess(true);

		this.report = report;
		reportType = report.getReportType();
		this.reportRunner = reportRunner;
		this.locale = locale;
		this.writer = writer;
		this.fullOutputFilename = fullOutputFilename;
		this.messageSource = messageSource;
		this.reportFormat = reportFormat;
		this.user = user;

		groovyData = reportRunner.getGroovyData();
		if (groovyData != null) {
			@SuppressWarnings("unchecked")
			List<? extends Object> dataList = (List<? extends Object>) groovyData;
			groovyDataSize = dataList.size();
		}

		if (request != null) {
			contextPath = request.getContextPath();
		}

		fileName = FilenameUtils.getName(fullOutputFilename);

		try {
			reportParamsMap = paramProcessorResult.getReportParamsMap();
			List<ReportParameter> reportParamsList = paramProcessorResult.getReportParamsList();
			reportOptions = paramProcessorResult.getReportOptions();
			parameterChartOptions = paramProcessorResult.getChartOptions();

			//for pdf dashboards, more parameters may be passed than are relevant for a report
			applicableReportParamsList = new ArrayList<>();
			for (ReportParameter reportParam : reportParamsList) {
				if (report.getReportId() == reportParam.getReport().getReportId()) {
					applicableReportParamsList.add(reportParam);
				}
			}

			String reportLocale = report.getLocale();
			if (StringUtils.isBlank(reportLocale)) {
				reportOutputLocale = locale;
			} else {
				reportOutputLocale = ArtUtils.getLocaleFromString(reportLocale);
			}

			//generate report output
			if (reportType.isJasperReports() || reportType.isJxls()) {
				if (reportType.isJasperReports()) {
					generateJasperReport();
				} else if (reportType.isJxls()) {
					generateJxlsOutput();
				}

				if (groovyDataSize == null) {
					rowsRetrieved = getResultSetRowCount(rs);
				} else {
					rowsRetrieved = groovyDataSize;
				}

				if (!isJob) {
					displayFileLink(fileName);
				}
			} else if (reportType == ReportType.Group) {
				generateGroupReport();
			} else if (reportType.isChart()) {
				generateChartReport();
			} else if (reportType.isStandardOutput()) {
				generateStandardReport(outputResult);
			} else if (reportType == ReportType.FreeMarker) {
				generateFreeMarkerOutput();
			} else if (reportType == ReportType.Thymeleaf) {
				generateThymeleafReport();
			} else if (reportType == ReportType.Velocity) {
				generateVelocityReport();
			} else if (reportType.isXDocReport()) {
				generateXDocReport();
			} else if (reportType == ReportType.ReactPivot) {
				generateReactPivotReport();
			} else if (reportType.isPivotTableJs()) {
				generatePivotTableJsOutput();
			} else if (reportType.isDygraphs()) {
				generateDygraphReport();
			} else if (reportType.isDataTables()) {
				generateDataTablesOutput();
			} else if (reportType == ReportType.FixedWidth) {
				generateFixedWidthReport();
			} else if (reportType == ReportType.CSV) {
				generateCsvReport();
			} else if (reportType == ReportType.C3) {
				generateC3Report();
			} else if (reportType == ReportType.ChartJs) {
				generateChartJsReport();
			} else if (reportType.isDatamaps()) {
				generateDatamapReport();
			} else if (reportType.isWebMap()) {
				if (isJob) {
					throw new IllegalStateException("Report type not supported for jobs: " + reportType);
				}

				rs = reportRunner.getResultSet();

				JsonOutput jsonOutput = new JsonOutput();
				JsonOutputResult jsonOutputResult;
				if (groovyData == null) {
					jsonOutputResult = jsonOutput.generateOutput(rs);
				} else {
					jsonOutputResult = jsonOutput.generateOutput(groovyData, report);
				}
				String jsonData = jsonOutputResult.getJsonData();
				rowsRetrieved = jsonOutputResult.getRowCount();

				String templateFileName = report.getTemplate();
				String jsTemplatesPath = Config.getJsTemplatesPath();
				String fullTemplateFileName = jsTemplatesPath + templateFileName;

				logger.debug("templateFileName='{}'", templateFileName);

				//need to explicitly check if template file is empty string
				//otherwise file.exists() will return true because fullTemplateFileName will just have the directory name
				if (StringUtils.isBlank(templateFileName)) {
					throw new IllegalArgumentException("Template file not specified");
				}

				File templateFile = new File(fullTemplateFileName);
				if (!templateFile.exists()) {
					throw new IllegalStateException("Template file not found: " + fullTemplateFileName);
				}

				WebMapOptions options;
				String optionsString = report.getOptions();
				if (StringUtils.isBlank(optionsString)) {
					options = new WebMapOptions();
				} else {
					ObjectMapper mapper = new ObjectMapper();
					options = mapper.readValue(optionsString, WebMapOptions.class);
				}

				String cssFileName = options.getCssFile();
				if (StringUtils.isNotBlank(cssFileName)) {
					String fullCssFileName = jsTemplatesPath + cssFileName;

					File cssFile = new File(fullCssFileName);
					if (!cssFile.exists()) {
						throw new IllegalStateException("Css file not found: " + fullCssFileName);
					}
				}

				String dataFileName = options.getDataFile();
				if (StringUtils.isNotBlank(dataFileName)) {
					String fullDataFileName = jsTemplatesPath + dataFileName;
					File dataFile = new File(fullDataFileName);
					if (!dataFile.exists()) {
						throw new IllegalStateException("Data file not found: " + fullDataFileName);
					}
				}

				List<String> jsFileNames = options.getJsFiles();
				if (CollectionUtils.isNotEmpty(jsFileNames)) {
					for (String jsFileName : jsFileNames) {
						if (StringUtils.isNotBlank(jsFileName)) {
							String fullJsFileName = jsTemplatesPath + jsFileName;
							File jsFile = new File(fullJsFileName);
							if (!jsFile.exists()) {
								throw new IllegalStateException("Js file not found: " + fullJsFileName);
							}
						}
					}
				}

				List<String> cssFileNames = options.getCssFiles();
				if (CollectionUtils.isNotEmpty(cssFileNames)) {
					for (String listCssFileName : cssFileNames) {
						if (StringUtils.isNotBlank(listCssFileName)) {
							String fullListCssFileName = jsTemplatesPath + listCssFileName;
							File listCssFile = new File(fullListCssFileName);
							if (!listCssFile.exists()) {
								throw new IllegalStateException("Css file not found: " + fullListCssFileName);
							}
						}
					}
				}

				String mapId = "map-" + RandomStringUtils.randomAlphanumeric(5);
				request.setAttribute("mapId", mapId);
				request.setAttribute("options", options);
				request.setAttribute("data", jsonData);
				request.setAttribute("templateFileName", templateFileName);

				switch (reportType) {
					case Leaflet:
						servletContext.getRequestDispatcher("/WEB-INF/jsp/showLeaflet.jsp").include(request, response);
						break;
					case OpenLayers:
						servletContext.getRequestDispatcher("/WEB-INF/jsp/showOpenLayers.jsp").include(request, response);
						break;
					default:
						throw new IllegalArgumentException("Unexpected report type: " + reportType);
				}
			} else if (reportType == ReportType.MongoDB) {
				//https://learnxinyminutes.com/docs/groovy/
				//http://groovy-lang.org/index.html
				//http://docs.groovy-lang.org/next/html/documentation/
				//https://www.tutorialspoint.com/mongodb/mongodb_java.htm
				//https://avaldes.com/java-connecting-to-mongodb-3-2-examples/
				//https://avaldes.com/mongodb-java-crud-operations-example-tutorial/
				//http://www.mkyong.com/mongodb/java-mongodb-query-document/
				//http://o7planning.org/en/10289/java-and-mongodb-tutorial
				//http://www.developer.com/java/ent/using-mongodb-in-a-java-ee7-framework.html
				//http://zetcode.com/db/mongodbjava/
				//http://www.mastertheintegration.com/nosql-databases/mongodb/mongodb-java-driver-3-0-quick-reference.html
				//https://mongodb.github.io/mongo-java-driver/3.4/driver/getting-started/quick-start/
				//https://github.com/mongolab/mongodb-driver-examples/blob/master/java/JavaSimpleExample.java
				//https://github.com/ihr/jongo-by-example/blob/master/src/test/java/org/ingini/mongodb/jongo/example/aggregation/TestAggregationFramework.java
				//https://stackoverflow.com/questions/24370456/groovy-script-sandboxing-use-groovy-timecategory-syntax-from-java-as-string/24374237
				//http://groovy-lang.org/integrating.html
				//http://blog.xebia.com/jongo/
				//http://ingini.org/2013/04/03/mongodb-with-jongo-sleeves-up/
				//https://stackoverflow.com/questions/37155718/mapping-a-java-object-with-a-mongodb-document-using-jongo
				//https://github.com/bguerout/jongo/issues/254
				//http://www.developer.com/java/ent/using-mongodb-in-a-java-ee7-framework.html
				//https://stackoverflow.com/questions/7567378/access-a-java-class-from-within-groovy
				//https://stackoverflow.com/questions/4912400/what-packages-does-1-java-and-2-groovy-automatically-import
				//https://www.spigotmc.org/wiki/mongodb-with-morphia/
				//http://www.foobaracademy.com/morphia-hello-world-example/
				//http://www.carfey.com/blog/using-mongodb-with-morphia/
				//http://www.obsidianscheduler.com/blog/evolving-document-structures-with-morphia-and-mongodb/
				//https://www.javacodegeeks.com/2011/11/using-mongodb-with-morphia.html
				//http://javabeat.net/using-morphia-java-library-for-mongodb/
				//http://www.scalabiliti.com/blog/mongodb_and_morphia_performance
				//https://city81.blogspot.co.ke/2012/07/using-morphia-to-map-java-objects-in.html
				//http://www.thejavageek.com/2015/08/24/save-entity-using-morphia/
				//https://sleeplessinslc.blogspot.co.ke/2010/10/mongodb-with-morphia-example.html
				//http://jameswilliams.be/blog/2010/05/05/Using-MongoDB-with-Morphia-and-Groovy.html
				//https://mongodb.github.io/morphia/1.3/getting-started/quick-tour/
				//https://github.com/mongodb/morphia/blob/1.3.x/morphia/src/examples/java/org/mongodb/morphia/example/QuickTour.java
				//https://www.javacodegeeks.com/2015/09/mongodb-and-java-tutorial.html
				//https://mdahlman.wordpress.com/2011/09/02/cool-reporting-on-mongodb/
				//https://mdahlman.wordpress.com/2011/09/02/simple-reporting-on-mongodb/
				CompilerConfiguration cc = new CompilerConfiguration();
				cc.addCompilationCustomizers(new SandboxTransformer());

				Map<String, Object> variables = new HashMap<>();
				variables.putAll(reportParamsMap);

				MongoClient mongoClient = null;
				Datasource datasource = report.getDatasource();
				if (datasource != null) {
					mongoClient = DbConnections.getMongodbConnection(datasource.getDatasourceId());
				}
				variables.put("mongoClient", mongoClient);

				Binding binding = new Binding(variables);

				GroovyShell shell = new GroovyShell(binding, cc);

				GroovySandbox sandbox = null;
				if (Config.getCustomSettings().isEnableGroovySandbox()) {
					sandbox = new GroovySandbox();
					sandbox.register();
				}

				//get report source with direct parameters, rules etc applied
				String reportSource = reportRunner.getQuerySql();
				Object result;
				try {
					result = shell.evaluate(reportSource);
				} finally {
					if (sandbox != null) {
						sandbox.unregister();
					}
				}
				if (result != null) {
					if (result instanceof List) {
						String optionsString = report.getOptions();
						List<String> optionsColumnNames = null;
						List<Map<String, String>> columnDataTypes = null;
						MongoDbOptions options;
						if (StringUtils.isBlank(optionsString)) {
							options = new MongoDbOptions();
						} else {
							ObjectMapper mapper = new ObjectMapper();
							options = mapper.readValue(optionsString, MongoDbOptions.class);
							optionsColumnNames = options.getColumns();
							columnDataTypes = options.getColumnDataTypes();
						}

						@SuppressWarnings("unchecked")
						List<Object> resultList = (List<Object>) result;
						List<ResultSetColumn> columns = new ArrayList<>();
						String resultString = null;
						if (!resultList.isEmpty()) {
							if (CollectionUtils.isEmpty(optionsColumnNames)) {
								Object sample = resultList.get(0);
								//https://stackoverflow.com/questions/6133660/recursive-beanutils-describe
								//https://www.leveluplunch.com/java/examples/convert-object-bean-properties-map-key-value/
								//https://stackoverflow.com/questions/26071530/jackson-convert-object-to-map-preserving-date-type
								//http://cassiomolin.com/converting-pojo-map-vice-versa-jackson/
								//http://www.makeinjava.com/convert-list-objects-tofrom-json-java-jackson-objectmapper-example/
								ObjectMapper mapper = new ObjectMapper();
								@SuppressWarnings("unchecked")
								Map<String, Object> map = mapper.convertValue(sample, Map.class);
								for (Entry<String, Object> entry : map.entrySet()) {
									String name = entry.getKey();
									Object value = entry.getValue();
									String type = "string";
									if (value instanceof Number) {
										type = "numeric";
									}
									ResultSetColumn column = new ResultSetColumn();
									column.setName(name);
									column.setType(type);
									columns.add(column);
								}
							} else {
								for (String columnName : optionsColumnNames) {
									ResultSetColumn column = new ResultSetColumn();
									column.setName(columnName);
									column.setType("string");
									columns.add(column);
								}
							}

							if (CollectionUtils.isNotEmpty(columnDataTypes)) {
								for (ResultSetColumn column : columns) {
									String dataColumnName = column.getName();
									for (Map<String, String> columnDataTypeDefinition : columnDataTypes) {
										Entry<String, String> entry = columnDataTypeDefinition.entrySet().iterator().next();
										String columnName = entry.getKey();
										String dataType = entry.getValue();
										if (StringUtils.equalsIgnoreCase(columnName, dataColumnName)) {
											column.setType(dataType);
											break;
										}
									}
								}
							}

							List<String> finalColumnNames = new ArrayList<>();
							for (ResultSetColumn column : columns) {
								String columnName = column.getName();
								finalColumnNames.add(columnName);
							}

							//_id is a complex object so we have to iterate and replace it with the toString() representation
							//otherwise we would just call resultString = ArtUtils.objectToJson(resultList); directly and not have to create a new list
							List<Map<String, Object>> finalResultList = new ArrayList<>();
							for (Object object : resultList) {
								Map<String, Object> row = new LinkedHashMap<>();
								if (object instanceof Map) {
									ObjectMapper mapper = new ObjectMapper();
									@SuppressWarnings("unchecked")
									Map<String, Object> map2 = mapper.convertValue(object, Map.class);
									for (String columnName : finalColumnNames) {
										Object value = map2.get(columnName);
										Object finalValue;
										if (value == null) {
											finalValue = "";
										} else if (value instanceof ObjectId) {
											ObjectId objectId = (ObjectId) value;
											finalValue = objectId.toString();
										} else {
											finalValue = value;
										}
										row.put(columnName, finalValue);
									}
								} else {
									//https://stackoverflow.com/questions/3333974/how-to-loop-over-a-class-attributes-in-java
									Class<?> c = object.getClass();
									BeanInfo beanInfo = Introspector.getBeanInfo(c, Object.class);
									for (PropertyDescriptor propertyDesc : beanInfo.getPropertyDescriptors()) {
										String propertyName = propertyDesc.getName();
										if (StringUtils.equals(propertyName, "metaClass")) {
											//don't include
										} else {
											if (ArtUtils.containsIgnoreCase(finalColumnNames, propertyName)) {
												Object value = propertyDesc.getReadMethod().invoke(object);
												Object finalValue;
												if (value instanceof ObjectId) {
													ObjectId objectId = (ObjectId) value;
													finalValue = objectId.toString();
												} else {
													finalValue = value;
												}
												row.put(propertyName, finalValue);
											}
										}
									}
								}
								finalResultList.add(row);
							}

							//https://stackoverflow.com/questions/20355261/how-to-deserialize-json-into-flat-map-like-structure
							//https://github.com/wnameless/json-flattener
							resultString = ArtUtils.objectToJson(finalResultList);
						}

						request.setAttribute("data", resultString);
						request.setAttribute("columns", columns);
						request.setAttribute("reportType", reportType);
						request.setAttribute("options", options);

						String languageTag = locale.toLanguageTag();
						request.setAttribute("languageTag", languageTag);
						String localeString = locale.toString();
						request.setAttribute("locale", localeString);
						servletContext.getRequestDispatcher("/WEB-INF/jsp/showDataTables.jsp").include(request, response);
					} else {
						writer.print(result);
					}
				}
			} else if (reportType.isOrgChart()) {
				if (isJob) {
					throw new IllegalStateException("OrgChart report types not supported for jobs");
				}

				request.setAttribute("reportType", reportType);

				String jsonData;
				switch (reportType) {
					case OrgChartDatabase:
						rs = reportRunner.getResultSet();

						JsonOutput jsonOutput = new JsonOutput();
						JsonOutputResult jsonOutputResult;
						if (groovyData == null) {
							jsonOutputResult = jsonOutput.generateOutput(rs);
						} else {
							jsonOutputResult = jsonOutput.generateOutput(groovyData, report);
						}
						jsonData = jsonOutputResult.getJsonData();
						rowsRetrieved = jsonOutputResult.getRowCount();
						break;
					case OrgChartJson:
					case OrgChartList:
					case OrgChartAjax:
						jsonData = report.getReportSource();
						break;
					default:
						throw new IllegalArgumentException("Unexpected OrgChart report type: " + reportType);
				}

				jsonData = Encode.forJavaScript(jsonData);
				request.setAttribute("data", jsonData);

				String jsTemplatesPath = Config.getJsTemplatesPath();

				OrgChartOptions options;
				String optionsString = report.getOptions();
				if (StringUtils.isBlank(optionsString)) {
					options = new OrgChartOptions();
				} else {
					ObjectMapper mapper = new ObjectMapper();
					options = mapper.readValue(optionsString, OrgChartOptions.class);
				}

				String cssFileName = options.getCssFile();
				if (StringUtils.isNotBlank(cssFileName)) {
					String fullCssFileName = jsTemplatesPath + cssFileName;

					File cssFile = new File(fullCssFileName);
					if (!cssFile.exists()) {
						throw new IllegalStateException("Css file not found: " + fullCssFileName);
					}
				}

				String templateFileName = report.getTemplate();

				logger.debug("templateFileName='{}'", templateFileName);

				if (StringUtils.isNotBlank(templateFileName)) {
					String fullTemplateFileName = jsTemplatesPath + templateFileName;
					File templateFile = new File(fullTemplateFileName);
					if (!templateFile.exists()) {
						throw new IllegalStateException("Template file not found: " + fullTemplateFileName);
					}
				}

				String optionsJson = ArtUtils.objectToJson(options);
				optionsJson = Encode.forJavaScript(optionsJson);
				String containerId = "container-" + RandomStringUtils.randomAlphanumeric(5);
				request.setAttribute("containerId", containerId);
				request.setAttribute("optionsJson", optionsJson);
				request.setAttribute("options", options);
				request.setAttribute("templateFileName", templateFileName);
				servletContext.getRequestDispatcher("/WEB-INF/jsp/showOrgChart.jsp").include(request, response);
			} else if (reportType.isReportEngine()) {
				StandardOutput standardOutput = getStandardOutputInstance(reportFormat, isJob, report);

				standardOutput.setWriter(writer);
				standardOutput.setFullOutputFileName(fullOutputFilename);
				standardOutput.setReportParamsList(applicableReportParamsList); //used to show selected parameters and drilldowns
				standardOutput.setShowSelectedParameters(reportOptions.isShowSelectedParameters());
				standardOutput.setLocale(locale);
				standardOutput.setReportName(report.getLocalizedName(locale));
				standardOutput.setMessageSource(messageSource);
				standardOutput.setIsJob(isJob);
				standardOutput.setPdfPageNumbers(pdfPageNumbers);
				standardOutput.setReport(report);
				standardOutput.setDynamicOpenPassword(dynamicOpenPassword);
				standardOutput.setDynamicModifyPassword(dynamicModifyPassword);

				if (request != null) {
					standardOutput.setContextPath(contextPath);

					if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
						standardOutput.setAjax(true);
					}
				}

				//generate output
				rs = reportRunner.getResultSet();

				ReportEngineOutput reportEngineOutput = new ReportEngineOutput(standardOutput);
				reportEngineOutput.setResultSet(rs);
				reportEngineOutput.setData(groovyData);

				String options = report.getOptions();
				ReportEngineOptions reportEngineOptions;
				if (StringUtils.isBlank(options)) {
					reportEngineOptions = new ReportEngineOptions();
				} else {
					reportEngineOptions = ArtUtils.jsonToObject(options, ReportEngineOptions.class);
				}

				if (reportEngineOptions.isPivot()) {
					reportEngineOutput.generatePivotOutput(reportType);
				} else {
					reportEngineOutput.generateTabularOutput(reportType);
				}

				if (!reportFormat.isHtml() && standardOutput.outputHeaderAndFooter() && !isJob) {
					displayFileLink(fileName);
				}
			} else {
				throw new IllegalArgumentException("Unexpected report type: " + reportType);
			}
		} finally {
			DatabaseUtils.close(rs);
		}

		outputResult.setRowCount(rowsRetrieved);

		return outputResult;
	}

	/**
	 * Instantiates an appropriate chart object, sets some parameters and
	 * prepares the chart dataset
	 *
	 * @param outputReport the chart's report
	 * @param outputResultSet the resultset that has the chart data
	 * @param swapAxes whether to swap the values of the x and y axes
	 * @param outputGroovyData data to use for the chart, if not using a
	 * resultset
	 * @param includeDataInOutput whether resultset data should be included in
	 * the output
	 * @return the prepared chart
	 * @throws SQLException
	 */
	private Chart prepareChart(Report outputReport, ResultSet outputResultSet,
			boolean swapAxes, Object outputGroovyData, boolean includeDataInOutput)
			throws SQLException, IOException {

		ReportType outputReportType = outputReport.getReportType();
		Chart chart = getChartInstance(outputReportType);

		ChartOptions effectiveChartOptions = getEffectiveChartOptions(outputReport, parameterChartOptions, reportFormat);

		String shortDescription = outputReport.getLocalizedShortDescription(locale);
		RunReportHelper runReportHelper = new RunReportHelper();
		shortDescription = runReportHelper.performDirectParameterSubstitution(shortDescription, reportParamsMap);

		chart.setReportType(outputReportType);
		chart.setLocale(locale);
		chart.setChartOptions(effectiveChartOptions);
		chart.setTitle(shortDescription);
		chart.setXAxisLabel(outputReport.getxAxisLabel());
		chart.setYAxisLabel(outputReport.getyAxisLabel());
		chart.setSwapAxes(swapAxes);
		chart.setIncludeDataInOutput(includeDataInOutput);

		String optionsString = outputReport.getOptions();
		JFreeChartOptions options;
		if (StringUtils.isBlank(optionsString)) {
			options = new JFreeChartOptions();
		} else {
			ObjectMapper mapper = new ObjectMapper();
			options = mapper.readValue(optionsString, JFreeChartOptions.class);
		}
		chart.setExtraOptions(options);

		Drilldown drilldown = null;
		if (reportFormat == ReportFormat.html) {
			int reportId = outputReport.getReportId();
			List<Drilldown> drilldowns = drilldownService.getDrilldowns(reportId);
			if (!drilldowns.isEmpty()) {
				drilldown = drilldowns.get(0);
			}
		}

		if (outputGroovyData == null) {
			chart.prepareDataset(outputResultSet, drilldown, applicableReportParamsList);
		} else {
			chart.prepareDataset(outputGroovyData, drilldown, applicableReportParamsList);
		}

		return chart;
	}

	/**
	 * Returns an appropriate instance of a chart object based on the given
	 * report type
	 *
	 * @param reportType the report type
	 * @return an appropriate instance of a chart object
	 * @throws IllegalArgumentException
	 */
	private Chart getChartInstance(ReportType reportType) throws IllegalArgumentException {
		logger.debug("Entering getChartInstance: reportType={}", reportType);

		Chart chart;
		switch (reportType) {
			case Pie2DChart:
			case Pie3DChart:
				chart = new PieChart(reportType);
				break;
			case SpeedometerChart:
				chart = new SpeedometerChart();
				break;
			case XYChart:
				chart = new XYChart();
				break;
			case TimeSeriesChart:
			case DateSeriesChart:
				chart = new TimeSeriesBasedChart(reportType);
				break;
			case LineChart:
			case HorizontalBar2DChart:
			case HorizontalBar3DChart:
			case VerticalBar2DChart:
			case VerticalBar3DChart:
			case StackedHorizontalBar2DChart:
			case StackedHorizontalBar3DChart:
			case StackedVerticalBar2DChart:
			case StackedVerticalBar3DChart:
				chart = new CategoryBasedChart(reportType);
				break;
			case BubbleChart:
			case HeatmapChart:
				chart = new XYZBasedChart(reportType);
				break;
			default:
				throw new IllegalArgumentException("Unexpected chart report type: " + reportType);
		}

		return chart;
	}

	/**
	 * Returns a standard output instance based on the given report format
	 *
	 * @param reportFormat the report format
	 * @param isJob whether this is a job or an interactive report
	 * @param report the report that is being run
	 * @return the standard output instance
	 * @throws IllegalArgumentException
	 * @throws java.io.IOException
	 */
	public StandardOutput getStandardOutputInstance(ReportFormat reportFormat, boolean isJob,
			Report report) throws IllegalArgumentException, IOException {

		logger.debug("Entering getStandardOutputInstance: reportFormat={}, isJob={}, report={}", reportFormat, isJob, report);

		StandardOutput standardOutput;

		String xlsDateFormat;
		String reportDateFormat = report.getDateFormat();
		if (StringUtils.isBlank(reportDateFormat)) {
			xlsDateFormat = null;
		} else {
			xlsDateFormat = reportDateFormat;
		}

		String xlsNumberFormat;
		String reportNumberFormat = report.getNumberFormat();
		if (StringUtils.isBlank(reportNumberFormat)) {
			xlsNumberFormat = null;
		} else {
			xlsNumberFormat = reportNumberFormat;
		}

		switch (reportFormat) {
			case htmlPlain:
				standardOutput = new HtmlPlainOutput(isJob);
				break;
			case htmlFancy:
				standardOutput = new HtmlFancyOutput();
				break;
			case htmlGrid:
				standardOutput = new HtmlGridOutput();
				break;
			case htmlDataTable:
				standardOutput = new HtmlDataTableOutput();
				break;
			case pdf:
				standardOutput = new PdfOutput();
				break;
			case xml:
				standardOutput = new XmlOutput();
				break;
			case rss20:
				standardOutput = new Rss20Output();
				break;
			case xls:
				standardOutput = new XlsOutput(xlsDateFormat, xlsNumberFormat);
				break;
			case xlsZip:
				standardOutput = new XlsOutput(xlsDateFormat, xlsNumberFormat, ZipType.Zip);
				break;
			case xlsx:
				standardOutput = new XlsxOutput(xlsDateFormat, xlsNumberFormat);
				break;
			case slk:
				standardOutput = new SlkOutput();
				break;
			case slkZip:
				standardOutput = new SlkOutput(ZipType.Zip);
				break;
			case tsv:
				standardOutput = new TsvOutput();
				break;
			case tsvZip:
				standardOutput = new TsvOutput(ZipType.Zip);
				break;
			case tsvGz:
				standardOutput = new TsvOutput(ZipType.Gzip);
				break;
			case docx:
				standardOutput = new DocxOutput();
				break;
			case odt:
				standardOutput = new OdtOutput();
				break;
			case ods:
				standardOutput = new OdsOutput();
				break;
			case csv:
			case csvZip:
				CsvOutputArtOptions options;
				String optionsString = report.getOptions();
				if (StringUtils.isBlank(optionsString)) {
					options = new CsvOutputArtOptions(); //has default values set
				} else {
					ObjectMapper mapper = new ObjectMapper();
					options = mapper.readValue(optionsString, CsvOutputArtOptions.class);
				}

				ZipType zipType = ZipType.None;
				if (reportFormat == ReportFormat.csvZip) {
					zipType = ZipType.Zip;
				}

				standardOutput = new CsvOutputArt(options, zipType);
				break;
			default:
				throw new IllegalArgumentException("Unexpected standard output report format: " + reportFormat);
		}

		return standardOutput;
	}

	/**
	 * Outputs a file link to the web browser
	 *
	 * @param fileName the file name
	 * @throws IOException
	 * @throws ServletException
	 */
	private void displayFileLink(String fileName) throws IOException, ServletException {
		if (request == null || servletContext == null) {
			return;
		}

		//display link to access report
		request.setAttribute("fileName", fileName);
		servletContext.getRequestDispatcher("/WEB-INF/jsp/showFileLink.jsp").include(request, response);
	}

	/**
	 * Returns the row count for a given resultset
	 *
	 * @param rs the resultset
	 * @return the row count
	 */
	private Integer getResultSetRowCount(ResultSet rs) {
		Integer rowCount = null;

		try {
			if (rs != null && !rs.isClosed()) {
				int rsType = rs.getType();
				if (rsType == ResultSet.TYPE_SCROLL_INSENSITIVE || rsType == ResultSet.TYPE_SCROLL_SENSITIVE) {
					//resultset is scrollable
					rs.last();
					rowCount = rs.getRow();
					rs.beforeFirst();
				}
			}
		} catch (SQLException ex) {
			logger.error("Error", ex);
		}

		return rowCount;
	}

	/**
	 * Returns the final chart options to use based on the given chart options
	 * and the report options
	 *
	 * @param report the report
	 * @param parameterChartOptions the passed chart options
	 * @return the final chart options
	 */
	public ChartOptions getEffectiveChartOptions(Report report, ChartOptions parameterChartOptions) {
		ReportFormat reportFormat = null;
		return getEffectiveChartOptions(report, parameterChartOptions, reportFormat);
	}

	/**
	 * Returns the final chart options to use based on the given chart options,
	 * report and report format
	 *
	 * @param report the report
	 * @param parameterChartOptions the passed chart options
	 * @param reportFormat the report format
	 * @return the final chart options
	 */
	private ChartOptions getEffectiveChartOptions(Report report, ChartOptions parameterChartOptions,
			ReportFormat reportFormat) {

		ChartOptions reportChartOptions = report.getChartOptions();
		ChartOptions effectiveChartOptions = parameterChartOptions;

		Integer width = parameterChartOptions.getWidth();
		if (width == null || width <= 0) {
			width = reportChartOptions.getWidth();
		}
		if (width == null || width <= 0) {
			final Integer DEFAULT_WIDTH = 500;
			effectiveChartOptions.setWidth(DEFAULT_WIDTH);
		} else {
			effectiveChartOptions.setWidth(width);
		}

		Integer height = parameterChartOptions.getHeight();
		if (height == null || height <= 0) {
			height = reportChartOptions.getHeight();
		}
		if (height == null || height <= 0) {
			final Integer DEFAULT_HEIGHT = 300;
			effectiveChartOptions.setHeight(DEFAULT_HEIGHT);
		} else {
			effectiveChartOptions.setHeight(height);
		}

		Double yAxisMin = parameterChartOptions.getyAxisMin();
		if (yAxisMin == null) {
			yAxisMin = reportChartOptions.getyAxisMin();
		}
		if (yAxisMin == null) {
			final Double DEFAULT_Y_AXIS_MIN = 0D;
			effectiveChartOptions.setyAxisMin(DEFAULT_Y_AXIS_MIN);
		} else {
			effectiveChartOptions.setyAxisMin(yAxisMin);
		}

		Double yAxisMax = parameterChartOptions.getyAxisMax();
		if (yAxisMax == null) {
			yAxisMax = reportChartOptions.getyAxisMax();
		}
		if (yAxisMax == null) {
			final Double DEFAULT_Y_AXIS_MAX = 0D;
			effectiveChartOptions.setyAxisMax(DEFAULT_Y_AXIS_MAX);
		} else {
			effectiveChartOptions.setyAxisMax(yAxisMax);
		}

		Integer rotateAt = parameterChartOptions.getRotateAt();
		if (rotateAt == null) {
			rotateAt = reportChartOptions.getRotateAt();
		}
		if (rotateAt == null) {
			final Integer DEFAULT_ROTATE_AT = 0;
			effectiveChartOptions.setRotateAt(DEFAULT_ROTATE_AT);
		} else {
			effectiveChartOptions.setRotateAt(rotateAt);
		}

		Integer removeAt = parameterChartOptions.getRemoveAt();
		if (removeAt == null) {
			removeAt = reportChartOptions.getRemoveAt();
		}
		if (removeAt == null) {
			final Integer DEFAULT_REMOVE_AT = 0;
			effectiveChartOptions.setRemoveAt(DEFAULT_REMOVE_AT);
		} else {
			effectiveChartOptions.setRemoveAt(removeAt);
		}

		String backgroundColor = parameterChartOptions.getBackgroundColor();
		if (StringUtils.isBlank(backgroundColor)) {
			backgroundColor = reportChartOptions.getBackgroundColor();
		}
		if (StringUtils.isBlank(backgroundColor)) {
			final String DEFAULT_BACKGROUND_COLOR = ArtUtils.WHITE_HEX_COLOR_CODE;
			effectiveChartOptions.setBackgroundColor(DEFAULT_BACKGROUND_COLOR);
		} else {
			effectiveChartOptions.setBackgroundColor(backgroundColor);
		}

		ArtHelper artHelper = new ArtHelper();
		ReportType reportType = report.getReportType();

		Boolean showLegend = parameterChartOptions.getShowLegend();
		if (showLegend == null) {
			showLegend = reportChartOptions.getShowLegend();
		}
		if (showLegend == null) {
			boolean defaultShowLegendOption = artHelper.getDefaultShowLegendOption(reportType);
			effectiveChartOptions.setShowLegend(defaultShowLegendOption);
		} else {
			effectiveChartOptions.setShowLegend(showLegend);
		}

		Boolean showLabels = parameterChartOptions.getShowLabels();
		if (showLabels == null) {
			showLabels = reportChartOptions.getShowLabels();
		}
		if (showLabels == null) {
			boolean defaultShowLabelsOption = artHelper.getDefaultShowLabelsOption(reportType);
			effectiveChartOptions.setShowLabels(defaultShowLabelsOption);
		} else {
			effectiveChartOptions.setShowLabels(showLabels);
		}

		Boolean showPoints = parameterChartOptions.getShowPoints();
		if (showPoints == null) {
			showPoints = reportChartOptions.getShowPoints();
		}
		if (showPoints == null) {
			effectiveChartOptions.setShowPoints(false);
		} else {
			effectiveChartOptions.setShowPoints(showPoints);
		}

		Boolean showData = parameterChartOptions.getShowData();
		if (showData == null) {
			showData = reportChartOptions.getShowData();
		}
		if (showData == null) {
			effectiveChartOptions.setShowData(false);
		} else {
			effectiveChartOptions.setShowData(showData);
		}

		//set default label format.
		//{2} for category based charts
		//{0} ({2}) for pie chart html output
		//{0} = {1} ({2}) for pie chart png and pdf output
		String labelFormat = parameterChartOptions.getLabelFormat();
		if (StringUtils.isBlank(labelFormat)) {
			effectiveChartOptions.setLabelFormat(reportChartOptions.getLabelFormat());
		}
		labelFormat = effectiveChartOptions.getLabelFormat();
		if (StringUtils.isBlank(labelFormat)) {
			if (reportType == ReportType.Pie2DChart || reportType == ReportType.Pie3DChart) {
				if (reportFormat == null || reportFormat == ReportFormat.html) {
					labelFormat = "{0} ({2})";
				} else {
					labelFormat = "{0} = {1} ({2})";
				}
			} else {
				labelFormat = "{2}";
			}
			effectiveChartOptions.setLabelFormat(labelFormat);
		}

		return effectiveChartOptions;
	}

	/**
	 * Generates pivot table js output
	 *
	 * @throws SQLException
	 * @throws IOException
	 * @throws ServletException
	 */
	private void generatePivotTableJsOutput() throws SQLException, IOException, ServletException {
		logger.debug("Entering generatePivotTableJsOutput");

		if (isJob) {
			throw new IllegalStateException("PivotTable.js output not supported for jobs");
		}

		request.setAttribute("reportType", reportType);

		if (reportType == ReportType.PivotTableJs) {
			rs = reportRunner.getResultSet();

			JsonOutput jsonOutput = new JsonOutput();
			JsonOutputResult jsonOutputResult;
			if (groovyData == null) {
				jsonOutputResult = jsonOutput.generateOutput(rs);
			} else {
				jsonOutputResult = jsonOutput.generateOutput(groovyData, report);
			}
			String jsonData = jsonOutputResult.getJsonData();
			jsonData = Encode.forJavaScript(jsonData);
			rowsRetrieved = jsonOutputResult.getRowCount();
			request.setAttribute("input", jsonData);
		}

		String templateFileName = report.getTemplate();
		String jsTemplatesPath = Config.getJsTemplatesPath();
		String fullTemplateFileName = jsTemplatesPath + templateFileName;

		logger.debug("templateFileName='{}'", templateFileName);

		//template file not mandatory
		if (StringUtils.isNotBlank(templateFileName)) {
			File templateFile = new File(fullTemplateFileName);
			if (!templateFile.exists()) {
				throw new IllegalStateException("Template file not found: " + fullTemplateFileName);
			}
			request.setAttribute("templateFileName", templateFileName);
		}

		if (reportType == ReportType.PivotTableJsCsvServer) {
			String optionsString = report.getOptions();

			if (StringUtils.isBlank(optionsString)) {
				throw new IllegalArgumentException("Options not specified");
			}

			ObjectMapper mapper = new ObjectMapper();
			CsvServerOptions options = mapper.readValue(optionsString, CsvServerOptions.class);
			String dataFileName = options.getDataFile();

			logger.debug("dataFileName='{}'", dataFileName);

			//need to explicitly check if file name is empty string
			//otherwise file.exists() will return true because fullDataFileName will just have the directory name
			if (StringUtils.isBlank(dataFileName)) {
				throw new IllegalArgumentException("Data file not specified");
			}

			String fullDataFileName = jsTemplatesPath + dataFileName;

			File dataFile = new File(fullDataFileName);
			if (!dataFile.exists()) {
				throw new IllegalStateException("Data file not found: " + fullDataFileName);
			}

			request.setAttribute("dataFileName", dataFileName);
		}

		String localeString = locale.toString();

		String languageFileName = "pivot." + localeString + ".js";

		String languageFilePath = Config.getAppPath() + File.separator
				+ "js" + File.separator
				+ "pivottable-2.7.0" + File.separator
				+ languageFileName;

		File languageFile = new File(languageFilePath);

		if (languageFile.exists()) {
			request.setAttribute("locale", localeString);
		}

		String outputDivId = "pivotTableJsOutput-" + RandomStringUtils.randomAlphanumeric(5);
		request.setAttribute("outputDivId", outputDivId);
		servletContext.getRequestDispatcher("/WEB-INF/jsp/showPivotTableJs.jsp").include(request, response);
	}

	/**
	 * Generates standard output reports
	 *
	 * @param outputResult the output result object to update if there is an
	 * error
	 * @throws SQLException
	 * @throws IOException
	 * @throws ServletException
	 */
	private void generateStandardReport(ReportOutputGeneratorResult outputResult)
			throws SQLException, IOException, ServletException {

		logger.debug("Entering generateStandardReport");

		if (reportFormat.isJson()) {
			generateStandardReportJsonOutput();
		} else if (reportFormat == ReportFormat.pivotTableJs) {
			ReportType originalReportType = reportType;
			reportType = ReportType.PivotTableJs;
			generatePivotTableJsOutput();
			reportType = originalReportType;
		} else {
			generateStandardOutput(outputResult);
		}
	}

	/**
	 * Generates standard output
	 *
	 * @param outputResult the output result object to update if there is an
	 * error
	 * @throws SQLException
	 * @throws IOException
	 * @throws ServletException
	 */
	private void generateStandardOutput(ReportOutputGeneratorResult outputResult)
			throws SQLException, IOException, ServletException {

		logger.debug("Entering generateStandardOutput");

		StandardOutput standardOutput = getStandardOutputInstance(reportFormat, isJob, report);

		standardOutput.setWriter(writer);
		standardOutput.setFullOutputFileName(fullOutputFilename);
		standardOutput.setReportParamsList(applicableReportParamsList); //used to show selected parameters and drilldowns
		standardOutput.setShowSelectedParameters(reportOptions.isShowSelectedParameters());
		standardOutput.setLocale(locale);
		standardOutput.setReportName(report.getLocalizedName(locale));
		standardOutput.setMessageSource(messageSource);
		standardOutput.setIsJob(isJob);
		standardOutput.setPdfPageNumbers(pdfPageNumbers);
		standardOutput.setReport(report);
		standardOutput.setDynamicOpenPassword(dynamicOpenPassword);
		standardOutput.setDynamicModifyPassword(dynamicModifyPassword);

		if (request != null) {
			standardOutput.setContextPath(contextPath);

			if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
				standardOutput.setAjax(true);
			}
		}

		//generate output
		rs = reportRunner.getResultSet();

		StandardOutputResult standardOutputResult;
		if (reportType.isCrosstab()) {
			if (groovyData == null) {
				standardOutputResult = standardOutput.generateCrosstabOutput(rs, reportFormat, report);
			} else {
				standardOutputResult = standardOutput.generateCrosstabOutput(groovyData, reportFormat, report);
			}
		} else {
			if (reportFormat.isHtml() && !isJob) {
				//only drill down for html output. drill down query launched from hyperlink                                            
				standardOutput.setDrilldowns(drilldownService.getDrilldowns(report.getReportId()));
			}

			//https://stackoverflow.com/questions/16675191/get-full-url-and-query-string-in-servlet-for-both-http-and-https-requests
			if (request != null) {
				String requestBaseUrl = request.getScheme() + "://"
						+ request.getServerName()
						+ ("http".equals(request.getScheme()) && request.getServerPort() == 80 || "https".equals(request.getScheme()) && request.getServerPort() == 443 ? "" : ":" + request.getServerPort())
						+ request.getContextPath();
				standardOutput.setRequestBaseUrl(requestBaseUrl);
			}

			if (groovyData == null) {
				standardOutputResult = standardOutput.generateTabularOutput(rs, reportFormat, report);
			} else {
				standardOutputResult = standardOutput.generateTabularOutput(groovyData, reportFormat, report);
			}
		}

		if (standardOutputResult.isSuccess()) {
			if (!reportFormat.isHtml() && standardOutput.outputHeaderAndFooter() && !isJob) {
				displayFileLink(fileName);
			}

			rowsRetrieved = standardOutputResult.getRowCount();
		} else {
			outputResult.setSuccess(false);
			outputResult.setMessage(standardOutputResult.getMessage());
		}
	}

	/**
	 * Generates standard report json report format output
	 *
	 * @throws SQLException
	 * @throws IOException
	 */
	private void generateStandardReportJsonOutput() throws SQLException, IOException {
		logger.debug("Entering generateStandardReportJsonOutput");

		rs = reportRunner.getResultSet();

		JsonOutput jsonOutput = new JsonOutput();
		jsonOutput.setPrettyPrint(reportOptions.isPrettyPrint());

		JsonOutputResult jsonOutputResult;
		if (groovyData == null) {
			jsonOutputResult = jsonOutput.generateOutput(rs);
		} else {
			jsonOutputResult = jsonOutput.generateOutput(groovyData, report);
		}
		String jsonString = jsonOutputResult.getJsonData();
		rowsRetrieved = jsonOutputResult.getRowCount();

		switch (reportFormat) {
			case jsonBrowser:
				//https://stackoverflow.com/questions/14533530/how-to-show-pretty-print-json-string-in-a-jsp-page
				writer.print("<pre>" + jsonString + "</pre>");
				break;
			default:
				writer.print(jsonString);
		}

		writer.flush();
	}

	/**
	 * Generates a jasperreports report
	 *
	 * @throws SQLException
	 * @throws IOException
	 * @throws JRException
	 */
	private void generateJasperReport() throws SQLException, IOException, JRException {
		logger.debug("Entering generateJasperReport");

		JasperReportsOutput jrOutput = new JasperReportsOutput();
		jrOutput.setDynamicOpenPassword(dynamicOpenPassword);
		jrOutput.setDynamicModifyPassword(dynamicModifyPassword);
		if (reportType == ReportType.JasperReportsArt) {
			rs = reportRunner.getResultSet();
			jrOutput.setResultSet(rs);
			jrOutput.setData(groovyData);
		}

		jrOutput.generateReport(report, applicableReportParamsList, reportFormat, fullOutputFilename);
	}

	/**
	 * Generates jxls output
	 *
	 * @throws SQLException
	 * @throws IOException
	 * @throws InvalidFormatException
	 * @throws GeneralSecurityException
	 */
	private void generateJxlsOutput() throws SQLException, IOException,
			InvalidFormatException, GeneralSecurityException {

		logger.debug("Entering generateJxlsOutput");

		JxlsOutput jxlsOutput = new JxlsOutput();
		jxlsOutput.setLocale(locale);
		jxlsOutput.setDynamicOpenPassword(dynamicOpenPassword);
		jxlsOutput.setDynamicModifyPassword(dynamicModifyPassword);
		if (reportType == ReportType.JxlsArt) {
			rs = reportRunner.getResultSet();
			jxlsOutput.setResultSet(rs);
			jxlsOutput.setData(groovyData);
		}

		jxlsOutput.generateReport(report, applicableReportParamsList, fullOutputFilename);
	}

	/**
	 * Generates group output
	 *
	 * @throws SQLException
	 * @throws IOException
	 * @throws ServletException
	 */
	private void generateGroupReport() throws SQLException, IOException, ServletException {
		logger.debug("Entering generateGroupReport");

		rs = reportRunner.getResultSet();

		int splitColumnOption = reportOptions.getSplitColumn();
		int splitColumn;
		if (splitColumnOption > 0) {
			//option has been specified. override report setting
			splitColumn = splitColumnOption;
		} else {
			splitColumn = report.getGroupColumn();
		}

		switch (reportFormat) {
			case html:
				GroupHtmlOutput groupHtmlOutput = new GroupHtmlOutput();
				if (groovyData == null) {
					rowsRetrieved = groupHtmlOutput.generateReport(rs, splitColumn, writer, contextPath);
				} else {
					rowsRetrieved = groupHtmlOutput.generateReport(groovyData, splitColumn, writer, contextPath);
				}
				break;
			case xlsx:
				GroupXlsxOutput groupXlsxOutput = new GroupXlsxOutput();
				String reportName = report.getLocalizedName(locale);
				if (groovyData == null) {
					rowsRetrieved = groupXlsxOutput.generateReport(rs, splitColumn, report, reportName, fullOutputFilename);
				} else {
					rowsRetrieved = groupXlsxOutput.generateReport(groovyData, splitColumn, report, reportName, fullOutputFilename);
				}

				if (!isJob) {
					displayFileLink(fileName);
				}
				break;
			default:
				throw new IllegalArgumentException("Unexpected group report format: " + reportFormat);
		}
	}

	/**
	 * Outputs a chart report
	 *
	 * @throws SQLException
	 * @throws IOException
	 * @throws DatasetProduceException
	 * @throws ChartValidationException
	 * @throws PostProcessingException
	 * @throws ServletException
	 */
	private void generateChartReport() throws SQLException, IOException,
			DatasetProduceException, ChartValidationException,
			PostProcessingException, ServletException {

		logger.debug("Entering generateStandardChart");

		rs = reportRunner.getResultSet();

		ChartUtils.prepareTheme(Config.getSettings().getPdfFontName());

		boolean showData = false;
		if (BooleanUtils.isTrue(parameterChartOptions.getShowData())
				&& (reportFormat == ReportFormat.html || reportFormat == ReportFormat.pdf)) {
			showData = true;
		}

		boolean swapAxes = reportOptions.isSwapAxes();
		Chart chart = prepareChart(report, rs, swapAxes, groovyData, showData);

		//add secondary charts
		String secondaryChartSetting = report.getSecondaryCharts();
		secondaryChartSetting = StringUtils.deleteWhitespace(secondaryChartSetting);
		String[] secondaryChartIds = StringUtils.split(secondaryChartSetting, ",");
		if (secondaryChartIds != null) {
			List<Chart> secondaryCharts = new ArrayList<>();
			ReportService reportService = new ReportService();
			for (String secondaryChartIdString : secondaryChartIds) {
				int secondaryChartId = Integer.parseInt(secondaryChartIdString);
				Report secondaryReport = reportService.getReport(secondaryChartId);
				ReportRunner secondaryReportRunner = new ReportRunner();
				secondaryReportRunner.setUser(user);
				secondaryReportRunner.setReport(secondaryReport);
				secondaryReportRunner.setReportParamsMap(reportParamsMap);
				ResultSet secondaryResultSet = null;
				try {
					secondaryReportRunner.execute();
					secondaryResultSet = secondaryReportRunner.getResultSet();
					Object secondaryGroovyData = secondaryReportRunner.getGroovyData();
					swapAxes = false;
					Chart secondaryChart = prepareChart(secondaryReport, secondaryResultSet, swapAxes, secondaryGroovyData, showData);
					secondaryCharts.add(secondaryChart);
				} finally {
					DatabaseUtils.close(secondaryResultSet);
					secondaryReportRunner.close();
				}
			}
			chart.setSecondaryCharts(secondaryCharts);
		}

		//store data for potential use in html and pdf output
		List<String> dataColumnNames = null;
		Object chartGroovyData = null;
		boolean showResultSetData = false;
		if (showData) {
			if (groovyData == null) {
				showResultSetData = true;
				dataColumnNames = chart.getResultSetColumnNames();
			} else {
				GroovyDataDetails dataDetails = RunReportHelper.getGroovyDataDetails(groovyData, report);
				dataColumnNames = dataDetails.getColumnNames();
				chartGroovyData = groovyData;
			}
		}

		if (isJob) {
			chart.generateFile(reportFormat, fullOutputFilename, report, pdfPageNumbers, dynamicOpenPassword, dynamicModifyPassword, chartGroovyData, showResultSetData);
		} else {
			if (reportFormat == ReportFormat.html) {
				request.setAttribute("chart", chart);

				String htmlElementId = "chart-" + report.getReportId();
				request.setAttribute("htmlElementId", htmlElementId);

				servletContext.getRequestDispatcher("/WEB-INF/jsp/showChart.jsp").include(request, response);

				if (dataColumnNames != null) {
					request.setAttribute("columnNames", dataColumnNames);
					if (groovyData == null) {
						request.setAttribute("data", chart.getResultSetData());
					} else {
						request.setAttribute("data", groovyData);
					}
					servletContext.getRequestDispatcher("/WEB-INF/jsp/showChartData.jsp").include(request, response);
				}
			} else {
				chart.generateFile(reportFormat, fullOutputFilename, report, pdfPageNumbers, dynamicOpenPassword, dynamicModifyPassword, chartGroovyData, showResultSetData);
				displayFileLink(fileName);
			}

			if (groovyDataSize == null) {
				rowsRetrieved = chart.getResultSetRecordCount();
			} else {
				rowsRetrieved = groovyDataSize;
			}
		}
	}

	/**
	 * Generates output for a freemarker report
	 *
	 * @throws SQLException
	 * @throws IOException
	 * @throws TemplateException
	 */
	private void generateFreeMarkerOutput() throws SQLException, IOException, TemplateException {
		logger.debug("Entering generateFreeMarkerOutput");

		rs = reportRunner.getResultSet();

		FreeMarkerOutput freemarkerOutput = new FreeMarkerOutput();
		freemarkerOutput.setContextPath(contextPath);
		freemarkerOutput.setLocale(locale);
		freemarkerOutput.setResultSet(rs);
		freemarkerOutput.setData(groovyData);
		freemarkerOutput.generateOutput(report, writer, applicableReportParamsList);

		if (groovyDataSize == null) {
			rowsRetrieved = getResultSetRowCount(rs);
		} else {
			rowsRetrieved = groovyDataSize;
		}
	}

	/**
	 * Generates a thymeleaf report
	 *
	 * @throws SQLException
	 * @throws IOException
	 */
	private void generateThymeleafReport() throws SQLException, IOException {
		logger.debug("Entering generateThymeleafReport");

		rs = reportRunner.getResultSet();

		ThymeleafOutput thymeleafOutput = new ThymeleafOutput();
		thymeleafOutput.setContextPath(contextPath);
		thymeleafOutput.setLocale(locale);
		thymeleafOutput.setResultSet(rs);
		thymeleafOutput.setData(groovyData);
		thymeleafOutput.generateOutput(report, writer, applicableReportParamsList);

		if (groovyDataSize == null) {
			rowsRetrieved = getResultSetRowCount(rs);
		} else {
			rowsRetrieved = groovyDataSize;
		}
	}

	/**
	 * Generates a velocity report
	 *
	 * @throws SQLException
	 * @throws IOException
	 */
	private void generateVelocityReport() throws SQLException, IOException {
		logger.debug("Entering generateVelocityReport");

		rs = reportRunner.getResultSet();

		VelocityOutput velocityOutput = new VelocityOutput();
		velocityOutput.setContextPath(contextPath);
		velocityOutput.setLocale(locale);
		velocityOutput.setResultSet(rs);
		velocityOutput.setData(groovyData);
		velocityOutput.generateOutput(report, writer, applicableReportParamsList);

		if (groovyDataSize == null) {
			rowsRetrieved = getResultSetRowCount(rs);
		} else {
			rowsRetrieved = groovyDataSize;
		}
	}

	/**
	 * Generates an xdocreport report
	 *
	 * @throws SQLException
	 * @throws IOException
	 * @throws XDocReportException
	 * @throws ServletException
	 */
	private void generateXDocReport() throws SQLException, IOException,
			XDocReportException, ServletException {

		logger.debug("Entering generateXDocReport");

		rs = reportRunner.getResultSet();

		XDocReportOutput xdocReportOutput = new XDocReportOutput();
		xdocReportOutput.setLocale(locale);
		xdocReportOutput.setResultSet(rs);
		xdocReportOutput.setData(groovyData);
		xdocReportOutput.generateReport(report, applicableReportParamsList, reportFormat, fullOutputFilename);

		if (groovyDataSize == null) {
			rowsRetrieved = getResultSetRowCount(rs);
		} else {
			rowsRetrieved = groovyDataSize;
		}

		if (!isJob) {
			displayFileLink(fileName);
		}
	}

	/**
	 * Generates a react pivot report
	 *
	 * @throws SQLException
	 * @throws IOException
	 * @throws ServletException
	 */
	private void generateReactPivotReport() throws SQLException, IOException, ServletException {
		logger.debug("Entering generateReactPivotReport");

		if (isJob) {
			throw new IllegalStateException("ReactPivot report type not supported for jobs");
		}

		rs = reportRunner.getResultSet();

		JsonOutput jsonOutput = new JsonOutput();
		JsonOutputResult jsonOutputResult;
		if (groovyData == null) {
			jsonOutputResult = jsonOutput.generateOutput(rs);
		} else {
			jsonOutputResult = jsonOutput.generateOutput(groovyData, report);
		}
		String jsonData = jsonOutputResult.getJsonData();
		jsonData = Encode.forJavaScript(jsonData);
		rowsRetrieved = jsonOutputResult.getRowCount();

		String templateFileName = report.getTemplate();
		String jsTemplatesPath = Config.getJsTemplatesPath();
		String fullTemplateFileName = jsTemplatesPath + templateFileName;

		logger.debug("templateFileName='{}'", templateFileName);

		//need to explicitly check if template file is empty string
		//otherwise file.exists() will return true because fullTemplateFileName will just have the directory name
		if (StringUtils.isBlank(templateFileName)) {
			throw new IllegalArgumentException("Template file not specified");
		}

		File templateFile = new File(fullTemplateFileName);
		if (!templateFile.exists()) {
			throw new IllegalStateException("Template file not found: " + fullTemplateFileName);
		}

		String outputDivId = "reactPivotOutput-" + RandomStringUtils.randomAlphanumeric(5);
		request.setAttribute("outputDivId", outputDivId);
		request.setAttribute("templateFileName", templateFileName);
		request.setAttribute("rows", jsonData);
		servletContext.getRequestDispatcher("/WEB-INF/jsp/showReactPivot.jsp").include(request, response);
	}

	/**
	 * Generates a dygraphs report
	 *
	 * @throws SQLException
	 * @throws IOException
	 * @throws ServletException
	 */
	private void generateDygraphReport() throws SQLException, IOException, ServletException {
		logger.debug("Entering generateDygraphReport");

		if (isJob) {
			throw new IllegalStateException("Dygraphs report types not supported for jobs");
		}

		request.setAttribute("reportType", reportType);

		if (reportType == ReportType.Dygraphs) {
			rs = reportRunner.getResultSet();

			CsvOutputUnivocity csvOutputUnivocity = new CsvOutputUnivocity();
			//use appropriate date formats to ensure correct interpretation by browsers
			//http://blog.dygraphs.com/2012/03/javascript-and-dates-what-mess.html
			//http://dygraphs.com/date-formats.html
			String dateFormat = "yyyy/MM/dd";
			String dateTimeFormat = "yyyy/MM/dd HH:mm";
			CsvOutputUnivocityOptions csvOptions = new CsvOutputUnivocityOptions();
			csvOptions.setDateFormat(dateFormat);
			csvOptions.setDateTimeFormat(dateTimeFormat);

			String csvString;
			try (StringWriter stringWriter = new StringWriter()) {
				csvOutputUnivocity.setResultSet(rs);
				csvOutputUnivocity.setData(groovyData);
				csvOutputUnivocity.generateOutput(stringWriter, csvOptions, Locale.ENGLISH);
				csvString = stringWriter.toString();
			}

			if (groovyDataSize == null) {
				rowsRetrieved = getResultSetRowCount(rs);
			} else {
				rowsRetrieved = groovyDataSize;
			}

			//need to escape string for javascript, otherwise you get Unterminated string literal error
			//https://stackoverflow.com/questions/5016517/error-using-javascript-and-jsp-string-with-space-gives-unterminated-string-lit
			String escapedCsvString = Encode.forJavaScript(csvString);
			request.setAttribute("csvData", escapedCsvString);
		}

		String templateFileName = report.getTemplate();
		String jsTemplatesPath = Config.getJsTemplatesPath();
		String fullTemplateFileName = jsTemplatesPath + templateFileName;

		logger.debug("templateFileName='{}'", templateFileName);

		//template file not mandatory
		if (StringUtils.isNotBlank(templateFileName)) {
			File templateFile = new File(fullTemplateFileName);
			if (!templateFile.exists()) {
				throw new IllegalStateException("Template file not found: " + fullTemplateFileName);
			}
			request.setAttribute("templateFileName", templateFileName);
		}

		if (reportType == ReportType.DygraphsCsvServer) {
			String optionsString = report.getOptions();

			if (StringUtils.isBlank(optionsString)) {
				throw new IllegalArgumentException("Options not specified");
			}

			ObjectMapper mapper = new ObjectMapper();
			CsvServerOptions options = mapper.readValue(optionsString, CsvServerOptions.class);
			String dataFileName = options.getDataFile();

			logger.debug("dataFileName='{}'", dataFileName);

			//need to explicitly check if file name is empty string
			//otherwise file.exists() will return true because fullDataFileName will just have the directory name
			if (StringUtils.isBlank(dataFileName)) {
				throw new IllegalArgumentException("Data file not specified");
			}

			String fullDataFileName = jsTemplatesPath + dataFileName;

			File dataFile = new File(fullDataFileName);
			if (!dataFile.exists()) {
				throw new IllegalStateException("Data file not found: " + fullDataFileName);
			}

			request.setAttribute("dataFileName", dataFileName);
		}

		String outputDivId = "dygraphsOutput-" + RandomStringUtils.randomAlphanumeric(5);
		request.setAttribute("outputDivId", outputDivId);
		servletContext.getRequestDispatcher("/WEB-INF/jsp/showDygraphs.jsp").include(request, response);
	}

	/**
	 * Generates datatables reports
	 *
	 * @throws SQLException
	 * @throws IOException
	 * @throws ServletException
	 */
	private void generateDataTablesOutput() throws SQLException, IOException, ServletException {
		logger.debug("Entering generateDataTablesOutput");

		if (isJob) {
			throw new IllegalStateException("DataTables report types not supported for jobs");
		}

		request.setAttribute("reportType", reportType);

		if (reportType == ReportType.DataTables) {
			rs = reportRunner.getResultSet();

			JsonOutput jsonOutput = new JsonOutput();
			JsonOutputResult jsonOutputResult;
			if (groovyData == null) {
				jsonOutputResult = jsonOutput.generateOutput(rs);
			} else {
				jsonOutputResult = jsonOutput.generateOutput(groovyData, report);
			}
			String jsonData = jsonOutputResult.getJsonData();
			jsonData = Encode.forJavaScript(jsonData);
			List<ResultSetColumn> columns = jsonOutputResult.getColumns();
			request.setAttribute("data", jsonData);
			request.setAttribute("columns", columns);
		}

		String templateFileName = report.getTemplate();
		String jsTemplatesPath = Config.getJsTemplatesPath();
		String fullTemplateFileName = jsTemplatesPath + templateFileName;

		logger.debug("templateFileName='{}'", templateFileName);

		//template file not mandatory
		if (StringUtils.isNotBlank(templateFileName)) {
			File templateFile = new File(fullTemplateFileName);
			if (!templateFile.exists()) {
				throw new IllegalStateException("Template file not found: " + fullTemplateFileName);
			}
			request.setAttribute("templateFileName", templateFileName);
		}

		String optionsString = report.getOptions();
		DataTablesOptions options;
		if (StringUtils.isBlank(optionsString)) {
			options = new DataTablesOptions();
		} else {
			ObjectMapper mapper = new ObjectMapper();
			options = mapper.readValue(optionsString, DataTablesOptions.class);
		}
		request.setAttribute("options", options);

		if (reportType == ReportType.DataTablesCsvServer) {
			if (StringUtils.isBlank(optionsString)) {
				throw new IllegalArgumentException("Options not specified");
			}

			String dataFileName = options.getDataFile();

			logger.debug("dataFileName='{}'", dataFileName);

			//need to explicitly check if file name is empty string
			//otherwise file.exists() will return true because fullDataFileName will just have the directory name
			if (StringUtils.isBlank(dataFileName)) {
				throw new IllegalArgumentException("Data file not specified");
			}

			String fullDataFileName = jsTemplatesPath + dataFileName;

			File dataFile = new File(fullDataFileName);
			if (!dataFile.exists()) {
				throw new IllegalStateException("Data file not found: " + fullDataFileName);
			}

			request.setAttribute("dataFileName", dataFileName);
		}

		String outputDivId = "dataTablesOutput-" + RandomStringUtils.randomAlphanumeric(5);
		String tableId = "tableData-" + RandomStringUtils.randomAlphanumeric(5);
		String languageTag = locale.toLanguageTag();
		String localeString = locale.toString();
		request.setAttribute("outputDivId", outputDivId);
		request.setAttribute("tableId", tableId);
		request.setAttribute("languageTag", languageTag);
		request.setAttribute("locale", localeString);
		servletContext.getRequestDispatcher("/WEB-INF/jsp/showDataTables.jsp").include(request, response);
	}

	/**
	 * Generates a fixed width report
	 *
	 * @throws SQLException
	 * @throws IOException
	 * @throws ServletException
	 */
	private void generateFixedWidthReport() throws SQLException, IOException, ServletException {
		logger.debug("Entering generateFixedWidthReport");

		rs = reportRunner.getResultSet();

		FixedWidthOutput fixedWidthOutput = new FixedWidthOutput();
		fixedWidthOutput.setResultSet(rs);
		fixedWidthOutput.setData(groovyData);
		fixedWidthOutput.generateOutput(writer, report, reportFormat, fullOutputFilename, reportOutputLocale);

		if (groovyDataSize == null) {
			rowsRetrieved = getResultSetRowCount(rs);
		} else {
			rowsRetrieved = groovyDataSize;
		}

		if (!isJob && !reportFormat.isHtml()) {
			displayFileLink(fileName);
		}
	}

	/**
	 * Generates output for a csv report
	 *
	 * @throws SQLException
	 * @throws IOException
	 * @throws ServletException
	 */
	private void generateCsvReport() throws SQLException, IOException, ServletException {
		logger.debug("Entering generateCsvReport");

		rs = reportRunner.getResultSet();

		CsvOutputUnivocity csvOutput = new CsvOutputUnivocity();
		csvOutput.setResultSet(rs);
		csvOutput.setData(groovyData);
		csvOutput.generateOutput(writer, report, reportFormat, fullOutputFilename, reportOutputLocale);

		if (groovyDataSize == null) {
			rowsRetrieved = getResultSetRowCount(rs);
		} else {
			rowsRetrieved = groovyDataSize;
		}

		if (!isJob && !reportFormat.isHtml()) {
			displayFileLink(fileName);
		}
	}

	/**
	 * Generates a c3.js report
	 *
	 * @throws SQLException
	 * @throws IOException
	 * @throws ServletException
	 */
	private void generateC3Report() throws SQLException, IOException, ServletException {
		logger.debug("Entering generateC3Report");

		if (isJob) {
			throw new IllegalStateException("C3.js report type not supported for jobs");
		}

		rs = reportRunner.getResultSet();

		JsonOutput jsonOutput = new JsonOutput();
		JsonOutputResult jsonOutputResult;
		if (groovyData == null) {
			jsonOutputResult = jsonOutput.generateOutput(rs);
		} else {
			jsonOutputResult = jsonOutput.generateOutput(groovyData, report);
		}
		String jsonData = jsonOutputResult.getJsonData();
		jsonData = Encode.forJavaScript(jsonData);
		rowsRetrieved = jsonOutputResult.getRowCount();

		String templateFileName = report.getTemplate();
		String jsTemplatesPath = Config.getJsTemplatesPath();
		String fullTemplateFileName = jsTemplatesPath + templateFileName;

		logger.debug("templateFileName='{}'", templateFileName);

		//need to explicitly check if template file is empty string
		//otherwise file.exists() will return true because fullTemplateFileName will just have the directory name
		if (StringUtils.isBlank(templateFileName)) {
			throw new IllegalArgumentException("Template file not specified");
		}

		File templateFile = new File(fullTemplateFileName);
		if (!templateFile.exists()) {
			throw new IllegalStateException("Template file not found: " + fullTemplateFileName);
		}

		String optionsString = report.getOptions();
		if (StringUtils.isNotBlank(optionsString)) {
			ObjectMapper mapper = new ObjectMapper();
			C3Options options = mapper.readValue(optionsString, C3Options.class);
			String cssFileName = options.getCssFile();

			logger.debug("cssFileName='{}'", cssFileName);

			//need to explicitly check if file name is empty string
			//otherwise file.exists() will return true because fullDataFileName will just have the directory name
			if (StringUtils.isNotBlank(cssFileName)) {
				String fullCssFileName = jsTemplatesPath + cssFileName;

				File cssFile = new File(fullCssFileName);
				if (!cssFile.exists()) {
					throw new IllegalStateException("Css file not found: " + fullCssFileName);
				}

				request.setAttribute("cssFileName", cssFileName);
			}
		}

		String chartId = "chart-" + RandomStringUtils.randomAlphanumeric(5);
		request.setAttribute("chartId", chartId);
		request.setAttribute("templateFileName", templateFileName);
		request.setAttribute("data", jsonData);
		servletContext.getRequestDispatcher("/WEB-INF/jsp/showC3.jsp").include(request, response);
	}

	/**
	 * Generate a chart.js report
	 *
	 * @throws SQLException
	 * @throws IOException
	 * @throws ServletException
	 */
	private void generateChartJsReport() throws SQLException, IOException, ServletException {
		logger.debug("Entering generateChartJsReport");

		if (isJob) {
			throw new IllegalStateException("Chart.js report type not supported for jobs");
		}

		rs = reportRunner.getResultSet();

		JsonOutput jsonOutput = new JsonOutput();
		JsonOutputResult jsonOutputResult;
		if (groovyData == null) {
			jsonOutputResult = jsonOutput.generateOutput(rs);
		} else {
			jsonOutputResult = jsonOutput.generateOutput(groovyData, report);
		}
		String jsonData = jsonOutputResult.getJsonData();
		jsonData = Encode.forJavaScript(jsonData);
		rowsRetrieved = jsonOutputResult.getRowCount();

		String templateFileName = report.getTemplate();
		String jsTemplatesPath = Config.getJsTemplatesPath();
		String fullTemplateFileName = jsTemplatesPath + templateFileName;

		logger.debug("templateFileName='{}'", templateFileName);

		//need to explicitly check if template file is empty string
		//otherwise file.exists() will return true because fullTemplateFileName will just have the directory name
		if (StringUtils.isBlank(templateFileName)) {
			throw new IllegalArgumentException("Template file not specified");
		}

		File templateFile = new File(fullTemplateFileName);
		if (!templateFile.exists()) {
			throw new IllegalStateException("Template file not found: " + fullTemplateFileName);
		}

		ChartJsOptions options;
		String optionsString = report.getOptions();
		if (StringUtils.isBlank(optionsString)) {
			options = new ChartJsOptions();
		} else {
			ObjectMapper mapper = new ObjectMapper();
			options = mapper.readValue(optionsString, ChartJsOptions.class);
		}

		String chartId = "chart-" + RandomStringUtils.randomAlphanumeric(5);
		request.setAttribute("chartId", chartId);
		request.setAttribute("options", options);
		request.setAttribute("templateFileName", templateFileName);
		request.setAttribute("data", jsonData);
		servletContext.getRequestDispatcher("/WEB-INF/jsp/showChartJs.jsp").include(request, response);
	}

	/**
	 * Generates a datamaps report
	 * 
	 * @throws SQLException
	 * @throws IOException
	 * @throws ServletException 
	 */
	private void generateDatamapReport() throws SQLException, IOException, ServletException {
		logger.debug("Entering generateDatamapReport");
		
		if (isJob) {
			throw new IllegalStateException("Datamaps report types not supported for jobs");
		}

		request.setAttribute("reportType", reportType);

		if (reportType == ReportType.Datamaps) {
			rs = reportRunner.getResultSet();

			JsonOutput jsonOutput = new JsonOutput();
			JsonOutputResult jsonOutputResult;
			if (groovyData == null) {
				jsonOutputResult = jsonOutput.generateOutput(rs);
			} else {
				jsonOutputResult = jsonOutput.generateOutput(groovyData, report);
			}
			String jsonData = jsonOutputResult.getJsonData();
			rowsRetrieved = jsonOutputResult.getRowCount();
			request.setAttribute("data", jsonData);
		}

		String templateFileName = report.getTemplate();
		String jsTemplatesPath = Config.getJsTemplatesPath();
		String fullTemplateFileName = jsTemplatesPath + templateFileName;

		logger.debug("templateFileName='{}'", templateFileName);

		//need to explicitly check if template file is empty string
		//otherwise file.exists() will return true because fullTemplateFileName will just have the directory name
		if (StringUtils.isBlank(templateFileName)) {
			throw new IllegalArgumentException("Template file not specified");
		}

		File templateFile = new File(fullTemplateFileName);
		if (!templateFile.exists()) {
			throw new IllegalStateException("Template file not found: " + fullTemplateFileName);
		}

		DatamapsOptions options;
		String optionsString = report.getOptions();
		if (StringUtils.isBlank(optionsString)) {
			options = new DatamapsOptions();
		} else {
			ObjectMapper mapper = new ObjectMapper();
			options = mapper.readValue(optionsString, DatamapsOptions.class);
		}

		String datamapsJsFileName = options.getDatamapsJsFile();

		if (StringUtils.isBlank(datamapsJsFileName)) {
			throw new IllegalArgumentException("Datamaps js file not specified");
		}

		String fullDatamapsJsFileName = jsTemplatesPath + datamapsJsFileName;
		File datamapsJsFile = new File(fullDatamapsJsFileName);
		if (!datamapsJsFile.exists()) {
			throw new IllegalStateException("Datamaps js file not found: " + fullDatamapsJsFileName);
		}

		String dataFileName = options.getDataFile();
		if (StringUtils.isNotBlank(dataFileName)) {
			String fullDataFileName = jsTemplatesPath + dataFileName;
			File dataFile = new File(fullDataFileName);
			if (!dataFile.exists()) {
				throw new IllegalStateException("Data file not found: " + fullDataFileName);
			}
		}

		String mapFileName = options.getMapFile();
		if (StringUtils.isNotBlank(mapFileName)) {
			String fullMapFileName = jsTemplatesPath + mapFileName;

			File mapFile = new File(fullMapFileName);
			if (!mapFile.exists()) {
				throw new IllegalStateException("Map file not found: " + fullMapFileName);
			}
		}

		String cssFileName = options.getCssFile();
		if (StringUtils.isNotBlank(cssFileName)) {
			String fullCssFileName = jsTemplatesPath + cssFileName;

			File cssFile = new File(fullCssFileName);
			if (!cssFile.exists()) {
				throw new IllegalStateException("Css file not found: " + fullCssFileName);
			}
		}

		String containerId = "container-" + RandomStringUtils.randomAlphanumeric(5);
		request.setAttribute("containerId", containerId);
		request.setAttribute("options", options);
		request.setAttribute("templateFileName", templateFileName);
		servletContext.getRequestDispatcher("/WEB-INF/jsp/showDatamaps.jsp").include(request, response);
	}

}
