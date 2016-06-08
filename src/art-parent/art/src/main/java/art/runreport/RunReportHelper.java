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
import art.enums.ParameterDataType;
import art.report.Report;
import art.reportparameter.ReportParameter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Provides utility functions used to aid running of reports
 *
 * @author Timothy Anyona
 */
public class RunReportHelper {

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
}
