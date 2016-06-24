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
package art.runreport;

import art.connectionpool.DbConnections;
import art.datasource.Datasource;
import art.enums.AccessLevel;
import art.enums.ParameterDataType;
import art.enums.ReportType;
import art.parameter.Parameter;
import art.report.Report;
import art.report.ReportService;
import art.reportparameter.ReportParameter;
import art.servlets.Config;
import art.user.User;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;

/**
 * Provides utility functions used to aid running of reports
 *
 * @author Timothy Anyona
 */
public class RunReportHelper {

	private static final Logger logger = LoggerFactory.getLogger(RunReportHelper.class);

	/**
	 * Returns the connection to use for running the given report
	 *
	 * @param report the report
	 * @param reportParams the report parameters
	 * @return the connection to use for running the report
	 * @throws SQLException
	 */
	public Connection getEffectiveReportDatasource(Report report,
			Collection<ReportParameter> reportParams) throws SQLException {

		logger.debug("Entering getEffectiveReportDatasource: report={}", report);

		Connection conn;

		Integer dynamicDatasourceId = null;
		if (reportParams != null) {
			for (ReportParameter reportParam : reportParams) {
				if (reportParam.getParameter().getDataType() == ParameterDataType.Datasource) {
					dynamicDatasourceId = (Integer) reportParam.getEffectiveActualParameterValue();
					break;
				}
			}
		}

		if (dynamicDatasourceId == null) {
			//use datasource defined on the report
			Datasource reportDatasource = report.getDatasource();
			conn = DbConnections.getConnection(reportDatasource.getDatasourceId());
		} else {
			//use datasource indicated in parameter
			conn = DbConnections.getConnection(dynamicDatasourceId);
		}

		return conn;
	}

	/**
	 * Returns the connection to use for running the given report
	 *
	 * @param report the report
	 * @param reportParamsMap the report parameters
	 * @return the connection to use for running the report
	 * @throws SQLException
	 */
	public Connection getEffectiveReportDatasource(Report report,
			Map<String, ReportParameter> reportParamsMap) throws SQLException {

		Collection<ReportParameter> reportParams = null;
		if (reportParamsMap != null) {
			reportParams = reportParamsMap.values();
		}

		return getEffectiveReportDatasource(report, reportParams);
	}

	/**
	 * Sets request attributes relevant for the select parameters portion of the
	 * run report page
	 *
	 * @param model the model to use
	 * @param report the report that is being run
	 * @param request the http request
	 * @param session the http session
	 * @param reportService the report service to use
	 * @throws ParseException
	 * @throws SQLException
	 */
	public void setSelectReportParameterAttributes(Model model, Report report,
			HttpServletRequest request, HttpSession session, ReportService reportService)
			throws ParseException, SQLException {

		logger.debug("Entering setSelectReportParameterAttributes: report={}", report);

		request.setAttribute("report", report);

		//prepare report parameters
		ParameterProcessor paramProcessor = new ParameterProcessor();
		ParameterProcessorResult paramProcessorResult = paramProcessor.processHttpParameters(request);

		Map<String, ReportParameter> reportParamsMap = paramProcessorResult.getReportParamsMap();
		List<ReportParameter> reportParamsList = paramProcessorResult.getReportParamsList();

		for (ReportParameter reportParam : reportParamsList) {
			Parameter param = reportParam.getParameter();
			if (param.isUseLov()) {
				//get all possible lov values.
				//don't run chained parameters. their values will be
				//loaded dynamically depending on parent and depends paremeter values
				if (!reportParam.isChained()) {
					ReportRunner lovReportRunner = null;
					try {
						lovReportRunner = new ReportRunner();
						int lovReportId = param.getLovReportId();
						Report lovReport = reportService.getReport(lovReportId);
						lovReportRunner.setReport(lovReport);
						lovReportRunner.setReportParamsMap(reportParamsMap);
						boolean applyFilters = false; //don't apply filters so as to get all values
						Map<Object, String> lovValues = lovReportRunner.getLovValuesAsObjects(applyFilters);
						reportParam.setLovValues(lovValues);
						Map<String, String> lovValuesAsString = reportParam.convertLovValuesFromObjectToString(lovValues);
						reportParam.setLovValuesAsString(lovValuesAsString);
					} finally {
						if (lovReportRunner != null) {
							lovReportRunner.close();
						}
					}
				}
			}
		}

		//create map in order to display parameters by position
		Map<Integer, ReportParameter> reportParams = new TreeMap<>();
		for (ReportParameter reportParam : reportParamsList) {
			reportParams.put(reportParam.getPosition(), reportParam);
		}

		request.setAttribute("reportParams", reportParams);

		ReportType reportType = report.getReportType();

		boolean enableReportFormats;
		switch (reportType) {
			case Dashboard:
			case Mondrian:
			case MondrianXmla:
			case SqlServerXmla:
			case Update:
			case Text:
			case TabularHtml:
			case JxlsArt:
			case JxlsTemplate:
			case FreeMarker:
				enableReportFormats = false;
				break;
			default:
				enableReportFormats = true;
				List<String> reportFormats = getAvailableReportFormats(report.getReportType());
				request.setAttribute("reportFormats", reportFormats);
		}
		request.setAttribute("enableReportFormats", enableReportFormats);

		User sessionUser = (User) session.getAttribute("sessionUser");
		int accessLevel = sessionUser.getAccessLevel().getValue();

		boolean enableSchedule;
		if (accessLevel >= AccessLevel.ScheduleUser.getValue()
				&& Config.getSettings().isSchedulingEnabled()) {

			switch (reportType) {
				case Dashboard:
				case Mondrian:
				case MondrianXmla:
				case SqlServerXmla:
				case Text:
					enableSchedule = false;
					break;
				default:
					enableSchedule = true;
			}
		} else {
			enableSchedule = false;
		}
		request.setAttribute("enableSchedule", enableSchedule);

		boolean enableShowSql;
		boolean enableShowSelectedParameters;

		switch (reportType) {
			case Dashboard:
			case Mondrian:
			case MondrianXmla:
			case SqlServerXmla:
			case JasperReportsTemplate:
			case JxlsTemplate:
			case Text:
				enableShowSql = false;
				enableShowSelectedParameters = false;
				break;
			default:
				if (accessLevel >= AccessLevel.JuniorAdmin.getValue()) {
					enableShowSql = true;
				} else {
					enableShowSql = false;
				}

				if (!reportParamsList.isEmpty()) {
					enableShowSelectedParameters = true;
				} else {
					enableShowSelectedParameters = false;
				}
		}
		request.setAttribute("enableShowSql", enableShowSql);
		request.setAttribute("enableShowSelectedParameters", enableShowSelectedParameters);

		request.setAttribute("isChart", reportType.isChart());

		boolean enableRunInline;

		switch (reportType) {
			case Dashboard:
			case Mondrian:
			case MondrianXmla:
			case SqlServerXmla:
				enableRunInline = false;
				break;
			default:
				enableRunInline = true;
		}
		request.setAttribute("enableRunInline", enableRunInline);

		boolean enablePrint;

		switch (reportType) {
			case Dashboard:
			case Mondrian:
			case MondrianXmla:
			case SqlServerXmla:
			case JxlsArt:
			case JxlsTemplate:
			case JasperReportsArt:
			case JasperReportsTemplate:
				enablePrint = false;
				break;
			default:
				if (reportType.isChart()) {
					enablePrint = false;
				} else {
					enablePrint = true;
				}
		}
		request.setAttribute("enablePrint", enablePrint);
	}

	/**
	 * Returns the available report formats for the given report type
	 *
	 * @param reportType the report type
	 * @return the available report formats
	 */
	private List<String> getAvailableReportFormats(ReportType reportType) {
		logger.debug("Entering getAvailableReportFormats: reportType={}", reportType);

		List<String> formats = new ArrayList<>();

		if (reportType.isChart()) {
			formats.add("html");
			formats.add("pdf");
			formats.add("png");
		} else {
			switch (reportType) {
				case Tabular:
				case Crosstab:
					String formatsString = Config.getSettings().getReportFormats();
					String[] formatsArray = StringUtils.split(formatsString, ",");
					formats = Arrays.asList(formatsArray);
					break;
				case JasperReportsArt:
				case JasperReportsTemplate:
					formats.add("pdf");
					formats.add("xls");
					formats.add("xlsx");
					formats.add("html");
					break;
				case Group:
					formats.add("html");
					formats.add("xlsx");
					break;
				case XDocReportFreeMarkerDocx:
				case XDocReportVelocityDocx:
					formats.add("docx");
					formats.add("pdf");
					formats.add("html");
					break;
				case XDocReportFreeMarkerOdt:
				case XDocReportVelocityOdt:
					formats.add("odt");
					formats.add("pdf");
					formats.add("html");
					break;
				case XDocReportFreeMarkerPptx:
				case XDocReportVelocityPptx:
					formats.add("pptx");
					break;
				default:
					throw new IllegalArgumentException("Unexpected report type: " + reportType);
			}
		}

		return formats;
	}
}
