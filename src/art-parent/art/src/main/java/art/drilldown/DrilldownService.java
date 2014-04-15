/**
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or modify it under the
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
package art.drilldown;

import art.dbutils.DbService;
import art.report.Report;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Class with methods related to drilldowns
 *
 * @author Timothy Anyona
 */
@Service
public class DrilldownService {

	private static final Logger logger = LoggerFactory.getLogger(DrilldownService.class);

	@Autowired
	private DbService dbService;

	private final String SQL_SELECT_ALL = "SELECT ADQ.*,"
			+ " AQ.NAME AS DRILLDOWN_REPORT_NAME"
			+ " FROM ART_DRILLDOWN_QUERIES ADQ"
			+ " INNER JOIN ART_QUERIES AQ ON"
			+ " ADQ.DRILLDOWN_QUERY_ID=AQ.QUERY_ID";

	/**
	 * Class to map resultset to an object
	 */
	private class DrilldownMapper extends BasicRowProcessor {

		@Override
		public <T> List<T> toBeanList(ResultSet rs, Class<T> type) throws SQLException {
			List<T> list = new ArrayList<>();
			while (rs.next()) {
				list.add(toBean(rs, type));
			}
			return list;
		}

		@Override
		public <T> T toBean(ResultSet rs, Class<T> type) throws SQLException {
			Drilldown drilldown = new Drilldown();

			drilldown.setDrilldownId(rs.getInt("DRILLDOWN_ID"));
			drilldown.setPosition(rs.getInt("DRILLDOWN_QUERY_POSITION"));
			drilldown.setHeaderText(rs.getString("DRILLDOWN_TITLE"));
			drilldown.setLinkText(rs.getString("DRILLDOWN_TEXT"));
			drilldown.setReportFormat(rs.getString("OUTPUT_FORMAT"));
			drilldown.setOpenInNewWindow(rs.getBoolean("OPEN_IN_NEW_WINDOW"));

			drilldown.setParentReportId(rs.getInt("QUERY_ID"));

			Report drilldownReport = new Report();
			drilldownReport.setReportId(rs.getInt("DRILLDOWN_QUERY_ID"));
			drilldownReport.setName(rs.getString("DRILLDOWN_REPORT_NAME"));
			drilldown.setDrilldownReport(drilldownReport);

			return type.cast(drilldown);
		}
	}

	/**
	 * Get drilldowns for a given report
	 *
	 * @param parentReportId
	 * @return list of drilldowns for a given report, empty list otherwise
	 * @throws SQLException
	 */
	public List<Drilldown> getDrilldowns(int parentReportId) throws SQLException {
		logger.debug("Entering getDrilldowns");

		String sql = SQL_SELECT_ALL + " WHERE ADQ.QUERY_ID=?";
		ResultSetHandler<List<Drilldown>> h = new BeanListHandler<>(Drilldown.class, new DrilldownMapper());
		return dbService.query(sql, h, parentReportId);
	}

	/**
	 * Get a drilldown
	 *
	 * @param id
	 * @return populated object if found, null otherwise
	 * @throws SQLException
	 */
	public Drilldown getDrilldown(int id) throws SQLException {
		logger.debug("Entering getDrilldown: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE DRILLDOWN_ID=?";
		ResultSetHandler<Drilldown> h = new BeanHandler<>(Drilldown.class, new DrilldownMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Delete a drilldown
	 *
	 * @param id
	 * @throws SQLException
	 */
	public void deleteDrilldown(int id) throws SQLException {
		logger.debug("Entering deleteDrilldown: id={}", id);

		String sql;

		//finally delete drilldown
		sql = "DELETE FROM ART_DRILLDOWN_QUERIES WHERE DRILLDOWN_ID=?";
		dbService.update(sql, id);
	}

	/**
	 * Add a new drilldown to the database
	 *
	 * @param drilldown
	 * @param parentReportId
	 * @return new record id
	 * @throws SQLException
	 */
	public synchronized int addDrilldown(Drilldown drilldown, int parentReportId) throws SQLException {
		logger.debug("Entering addDrilldown: drilldown={}", drilldown);

		//generate new id
		String sql = "SELECT MAX(DRILLDOWN_ID) FROM ART_DRILLDOWN_QUERIES";
		ResultSetHandler<Integer> h = new ScalarHandler<>();
		Integer maxId = dbService.query(sql, h);
		logger.debug("maxId={}", maxId);

		int newId;
		if (maxId == null || maxId < 0) {
			//no records in the table, or only hardcoded records
			newId = 1;
		} else {
			newId = maxId + 1;
		}
		logger.debug("newId={}", newId);

		//generate new position
		sql = "SELECT MAX(DRILLDOWN_QUERY_POSITION)"
				+ " FROM ART_DRILLDOWN_QUERIES"
				+ " WHERE QUERY_ID=?";
		ResultSetHandler<Integer> h2 = new ScalarHandler<>();
		Integer maxPosition = dbService.query(sql, h2, parentReportId);
		logger.debug("maxPosition={}", maxPosition);

		int newPosition;
		if (maxPosition == null || maxPosition < 0) {
			//no records in the table, or only hardcoded records
			newPosition = 1;
		} else {
			newPosition = maxPosition + 1;
		}
		logger.debug("newPosition={}", newPosition);

		sql = "INSERT INTO ART_DRILLDOWN_QUERIES"
				+ " (DRILLDOWN_ID, QUERY_ID, DRILLDOWN_QUERY_ID, DRILLDOWN_QUERY_POSITION,"
				+ " DRILLDOWN_TITLE, DRILLDOWN_TEXT, OUTPUT_FORMAT, OPEN_IN_NEW_WINDOW)"
				+ " VALUES(" + StringUtils.repeat("?", ",", 8) + ")";

		Object[] values = {
			newId,
			parentReportId,
			drilldown.getDrilldownReport().getReportId(),
			newPosition,
			drilldown.getHeaderText(),
			drilldown.getLinkText(),
			drilldown.getReportFormat(),
			drilldown.isOpenInNewWindow()
		};

		dbService.update(sql, values);

		return newId;
	}

	/**
	 * Update an existing drilldown
	 *
	 * @param drilldown
	 * @throws SQLException
	 */
	public void updateDrilldown(Drilldown drilldown) throws SQLException {
		logger.debug("Entering updateDrilldown: drilldown={}", drilldown);

		String sql = "UPDATE ART_DRILLDOWN_QUERIES SET QUERY_ID=?, DRILLDOWN_QUERY_ID=?,"
				+ " DRILLDOWN_QUERY_POSITION=?, DRILLDOWN_TITLE=?, DRILLDOWN_TEXT=?,"
				+ " OUTPUT_FORMAT=?, OPEN_IN_NEW_WINDOW=?"
				+ " WHERE DRILLDOWN_ID=?";

		Object[] values = {
			drilldown.getParentReportId(),
			drilldown.getDrilldownReport().getReportId(),
			drilldown.getPosition(),
			drilldown.getHeaderText(),
			drilldown.getLinkText(),
			drilldown.getReportFormat(),
			drilldown.isOpenInNewWindow(),
			drilldown.getDrilldownId()
		};

		dbService.update(sql, values);
	}

	/**
	 * Move a drilldown to a different position
	 *
	 * @param id
	 * @param fromPosition
	 * @param toPosition
	 * @param direction
	 * @param parentReportId
	 * @throws SQLException
	 */
	public void moveDrilldown(int id, int fromPosition, int toPosition, String direction,
			int parentReportId) throws SQLException {

		String sql;

		//https://datatables.net/forums/discussion/comment/55311#Comment_55311
		if (StringUtils.equals(direction, "back")) {
			//toPosition < fromPosition
			int finalPosition = toPosition + 1;

			sql = "UPDATE ART_DRILLDOWN_QUERIES"
					+ " SET DRILLDOWN_QUERY_POSITION=0"
					+ " WHERE DRILLDOWN_QUERY_POSITION=?"
					+ " AND QUERY_ID=?";
			dbService.update(sql, toPosition, parentReportId);

			sql = "UPDATE ART_DRILLDOWN_QUERIES"
					+ " SET DRILLDOWN_QUERY_POSITION=?"
					+ " WHERE DRILLDOWN_ID=?"
					+ " AND QUERY_ID=?";
			dbService.update(sql, toPosition, id, parentReportId);

			sql = "UPDATE ART_DRILLDOWN_QUERIES"
					+ " SET DRILLDOWN_QUERY_POSITION=DRILLDOWN_QUERY_POSITION + 1"
					+ " WHERE (?<=DRILLDOWN_QUERY_POSITION"
					+ " AND DRILLDOWN_QUERY_POSITION<=?)"
					+ " AND DRILLDOWN_ID<>? AND DRILLDOWN_QUERY_POSITION<>0"
					+ " AND QUERY_ID=?";
			dbService.update(sql, toPosition, fromPosition, id, parentReportId);

			sql = "UPDATE ART_DRILLDOWN_QUERIES"
					+ " SET DRILLDOWN_QUERY_POSITION=?"
					+ " WHERE DRILLDOWN_QUERY_POSITION=0"
					+ " AND QUERY_ID=?";
			dbService.update(sql, finalPosition, parentReportId);
		} else {
			//"forward". toPosition > fromPosition
			int finalPosition = toPosition - 1;

			sql = "UPDATE ART_DRILLDOWN_QUERIES"
					+ " SET DRILLDOWN_QUERY_POSITION=0"
					+ " WHERE DRILLDOWN_QUERY_POSITION=?"
					+ " AND QUERY_ID=?";
			dbService.update(sql, toPosition, parentReportId);

			sql = "UPDATE ART_DRILLDOWN_QUERIES"
					+ " SET DRILLDOWN_QUERY_POSITION=?"
					+ " WHERE DRILLDOWN_ID=?"
					+ " AND QUERY_ID=?";
			dbService.update(sql, toPosition, id, parentReportId);

			sql = "UPDATE ART_DRILLDOWN_QUERIES"
					+ " SET DRILLDOWN_QUERY_POSITION=DRILLDOWN_QUERY_POSITION - 1"
					+ " WHERE (?<=DRILLDOWN_QUERY_POSITION"
					+ " AND DRILLDOWN_QUERY_POSITION<=?)"
					+ " AND DRILLDOWN_ID<>? AND DRILLDOWN_QUERY_POSITION<>0"
					+ " AND QUERY_ID=?";
			dbService.update(sql, fromPosition, toPosition, id, parentReportId);

			sql = "UPDATE ART_DRILLDOWN_QUERIES"
					+ " SET DRILLDOWN_QUERY_POSITION=?"
					+ " WHERE DRILLDOWN_QUERY_POSITION=0"
					+ " AND QUERY_ID=?";
			dbService.update(sql, finalPosition, parentReportId);
		}

		//http://www.codeproject.com/Articles/331986/Table-Row-Drag-and-Drop-in-ASP-NET-MVC-JQuery-Data
		//logic doesn't work here because position is part of primary key and updates fail because of duplicate primary keys
//		//move other drilldowns
//		if (StringUtils.equals(direction, "back")) {
//			//toPosition < fromPosition
//			sql = "UPDATE ART_DRILLDOWN_QUERIES"
//					+ " SET DRILLDOWN_QUERY_POSITION=DRILLDOWN_QUERY_POSITION + 1"
//					+ " WHERE ?<=DRILLDOWN_QUERY_POSITION"
//					+ " AND DRILLDOWN_QUERY_POSITION<=?"
//					+ " AND QUERY_ID=?";
//			dbService.update(sql, toPosition, fromPosition, parentReportId);
//		} else {
//			//"forward". toPosition > fromPosition
//			sql = "UPDATE ART_DRILLDOWN_QUERIES"
//					+ " SET DRILLDOWN_QUERY_POSITION=DRILLDOWN_QUERY_POSITION + 1"
//					+ " WHERE ?<=DRILLDOWN_QUERY_POSITION"
//					+ " AND DRILLDOWN_QUERY_POSITION<=?"
//					+ " AND QUERY_ID=?";
//			dbService.update(sql, fromPosition, toPosition, parentReportId);
//		}
//
//		//move this drilldown
//		sql = "UPDATE ART_DRILLDOWN_QUERIES"
//				+ " SET DRILLDOWN_QUERY_POSITION=?"
//				+ " WHERE DRILLDOWN_ID=?";
//		dbService.update(sql, toPosition);
	}

}
