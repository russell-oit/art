/**
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
package art.utils;

import art.enums.ParameterType;
import art.parameter.Parameter;
import art.reportparameter.ReportParameter;
import art.reportparameter.ReportParameterService;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes report parameters contained in a http request and populates maps with
 * their values
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class ParameterProcessor {

	private static final Logger logger = LoggerFactory.getLogger(ParameterProcessor.class);

	/**
	 * Process the http request and fill objects with parameter values.
	 *
	 * @param request http request
	 * @param reportId report id
	 * @param reportParams new hashmap to store all the report's defined
	 * parameters
	 * @throws java.sql.SQLException
	 */
	public void processParameters(HttpServletRequest request, int reportId,
			Map<String, ReportParameter> reportParams) throws SQLException {

		logger.debug("Entering processParameters: reportId={}", reportId);

		//get list of all defined report parameters
		ReportParameterService reportParameterService = new ReportParameterService();
		List<ReportParameter> reportParamsList = reportParameterService.getReportParameters(reportId);

		for (ReportParameter reportParam : reportParamsList) {
			//set default parameter values. so that they don't have to be specified on the url
			Parameter param = reportParam.getParameter();
			logger.debug("param={}", param);

			String defaultValue = param.getDefaultValue();
			logger.debug("defaultValue='{}'", defaultValue);

			if (defaultValue != null) {
				String defaultValues[] = defaultValue.split("\\r?\\n");
				reportParam.setParameterValues(defaultValues);
			}

			//build map for easier lookup
			reportParams.put(param.getName(), reportParam);
		}

		//set parameter values from url
		Enumeration<String> htmlParamNames = request.getParameterNames();

		while (htmlParamNames.hasMoreElements()) {
			String htmlParamName = htmlParamNames.nextElement();
			logger.debug("htmlParamName='{}'", htmlParamName);

			if (htmlParamName.startsWith("p-")) { //use startswith instead of substring(0,2) because chrome passes a parameter "-" which causes StringIndexOutOfBoundsException. reported by yidong123
				//this is a report parameter. set it's value
				String[] paramValues = request.getParameterValues(htmlParamName);

				String paramName = htmlParamName.substring(2);
				logger.debug("paramName='{}'", paramName);
				
				ReportParameter reportParam = reportParams.get(paramName);
				logger.debug("reportParam={}", reportParam);

				if (reportParam != null) {
					//check if this is a multi parameter that doesn't use an lov
					//multi param that doesn't use an lov contains values separated by newlines
					Parameter param = reportParam.getParameter();
					logger.debug("param={}", param);

					if (param.getParameterType() == ParameterType.Multi && !param.isUseLov()) {
						String firstValue = paramValues[0];
						logger.debug("firstValue='{}'", firstValue);

						String values[] = firstValue.split("\\r?\\n");
						reportParam.setParameterValues(values);
					} else {
						reportParam.setParameterValues(paramValues);
					}
				}
			}
		}
	}
}
