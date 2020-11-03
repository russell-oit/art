/*
 * ART. A Reporting Tool.
 * Copyright (C) 2020 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.startcondition;

import art.dbutils.DatabaseUtils;
import art.report.Report;
import art.report.ReportService;
import art.runreport.ReportRunner;
import art.utils.ArtUtils;
import art.utils.ExpressionHelper;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Provides methods for evaluating a start condition
 *
 * @author Timothy Anyona
 */
public class StartConditionHelper {

	/**
	 * Returns <code>true</code> if the start condition is fulfilled
	 *
	 * @param condition the condition
	 * @return <code>true</code> if the start condition is fulfilled
	 * @throws SQLException
	 */
	public boolean evaluate(String condition) throws SQLException {
		if (NumberUtils.isCreatable(condition)) {
			int reportId = NumberUtils.toInt(condition);
			ReportService reportService = new ReportService();
			Report report = reportService.getReport(reportId);
			if (report == null) {
				throw new RuntimeException("Start condition report not found: " + reportId);
			}

			String runId = ArtUtils.getUniqueId(reportId);

			ReportRunner reportRunner = new ReportRunner();
			reportRunner.setReport(report);
			reportRunner.setRunId(runId);

			try {
				reportRunner.execute();
				ResultSet rs = null;
				try {
					rs = reportRunner.getResultSet();
					if (rs.next()) {
						int value = rs.getInt(1);
						if (value > 0) {
							return true;
						}
					}
				} finally {
					DatabaseUtils.close(rs);
				}
			} finally {
				reportRunner.close();
			}
		} else {
			ExpressionHelper expressionHelper = new ExpressionHelper();
			Object result = expressionHelper.runGroovyExpression(condition);
			if (result != null && result instanceof Boolean) {
				return (Boolean) result;
			}
		}

		return false;
	}

}
