/**
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
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
 * Provides methods for retrieveing, adding, updating and deleting drilldowns
 *
 * @author Timothy Anyona
 */
@Service
public class DrilldownService {

	private static final Logger logger = LoggerFactory.getLogger(DrilldownService.class);

	private final DbService dbService;

	@Autowired
	public DrilldownService(DbService dbService) {
		this.dbService = dbService;
	}

	public DrilldownService() {
		dbService = new DbService();
	}

	private final String SQL_SELECT_ALL = "SELECT ADQ.*,"
			+ " AQ.NAME AS DRILLDOWN_REPORT_NAME"
			+ " FROM ART_DRILLDOWN_QUERIES ADQ"
			+ " INNER JOIN ART_QUERIES AQ ON"
			+ " ADQ.DRILLDOWN_QUERY_ID=AQ.QUERY_ID";

	/**
	 * Maps a resultset to an object
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
	 * Returns drilldowns for a given report
	 *
	 * @param parentReportId the report id
	 * @return drilldowns for the given report
	 * @throws SQLException
	 */
	public List<Drilldown> getDrilldowns(int parentReportId) throws SQLException {
		logger.debug("Entering getDrilldowns: parentReportId={}", parentReportId);

		String sql = SQL_SELECT_ALL + " WHERE ADQ.QUERY_ID=?";
		ResultSetHandler<List<Drilldown>> h = new BeanListHandler<>(Drilldown.class, new DrilldownMapper());
		return dbService.query(sql, h, parentReportId);
	}

	/**
	 * Returns a drilldown
	 *
	 * @param id the drilldown id
	 * @return drilldown if found, null otherwise
	 * @throws SQLException
	 */
	public Drilldown getDrilldown(int id) throws SQLException {
		logger.debug("Entering getDrilldown: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE DRILLDOWN_ID=?";
		ResultSetHandler<Drilldown> h = new BeanHandler<>(Drilldown.class, new DrilldownMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Deletes a drilldown
	 *
	 * @param id the drilldown id
	 * @throws SQLException
	 */
	public void deleteDrilldown(int id) throws SQLException {
		logger.debug("Entering deleteDrilldown: id={}", id);

		String sql;

		sql = "DELETE FROM ART_DRILLDOWN_QUERIES WHERE DRILLDOWN_ID=?";
		dbService.update(sql, id);
	}

	/**
	 * Deletes multiple drilldowns
	 *
	 * @param ids the ids of the drilldowns to delete
	 * @throws SQLException
	 */
	public void deleteDrilldowns(Integer[] ids) throws SQLException {
		logger.debug("Entering deleteDrilldowns: ids={}", (Object) ids);

		String sql;

		sql = "DELETE FROM ART_DRILLDOWN_QUERIES"
				+ " WHERE DRILLDOWN_ID IN(" + StringUtils.repeat("?", ",", ids.length) + ")";
		dbService.update(sql, (Object[]) ids);
	}

	/**
	 * Adds a new drilldown to the database
	 *
	 * @param drilldown the drilldown to add
	 * @param parentReportId the parent report id
	 * @return new drilldown's id
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

		drilldown.setDrilldownId(newId);
		drilldown.setPosition(newPosition);
		drilldown.setParentReportId(parentReportId);

		saveDrilldown(drilldown, true);

		return newId;
	}

	/**
	 * Updates an existing drilldown
	 *
	 * @param drilldown the updated drilldown
	 * @throws SQLException
	 */
	public void updateDrilldown(Drilldown drilldown) throws SQLException {
		logger.debug("Entering updateDrilldown: drilldown={}", drilldown);

		saveDrilldown(drilldown, false);
	}

	/**
	 * Saves a drilldown
	 *
	 * @param drilldown the drilldown to save
	 * @param newRecord whether this is a new drilldown
	 * @throws SQLException
	 */
	private void saveDrilldown(Drilldown drilldown, boolean newRecord) throws SQLException {
		logger.debug("Entering saveDrilldown: drilldown={}", drilldown);

		int affectedRows;
		if (newRecord) {
			String sql = "INSERT INTO ART_DRILLDOWN_QUERIES"
					+ " (DRILLDOWN_ID, QUERY_ID, DRILLDOWN_QUERY_ID, DRILLDOWN_QUERY_POSITION,"
					+ " DRILLDOWN_TITLE, DRILLDOWN_TEXT, OUTPUT_FORMAT, OPEN_IN_NEW_WINDOW)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 8) + ")";

			Object[] values = {
				drilldown.getDrilldownId(),
				drilldown.getParentReportId(),
				drilldown.getDrilldownReport().getReportId(),
				drilldown.getPosition(),
				drilldown.getHeaderText(),
				drilldown.getLinkText(),
				drilldown.getReportFormat(),
				drilldown.isOpenInNewWindow()
			};

			affectedRows = dbService.update(sql, values);
		} else {
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

			affectedRows = dbService.update(sql, values);
		}

		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}, newRecord={}, drilldown={}",
					affectedRows, newRecord, drilldown);
		}
	}

	/**
	 * Moves a drilldown to a different position
	 *
	 * @param id the drilldown id
	 * @param fromPosition the current position
	 * @param toPosition the new position
	 * @param direction "forward" or "back"
	 * @param parentReportId the drilldown's parent report id
	 * @throws SQLException
	 */
	public void moveDrilldown(int id, int fromPosition, int toPosition, String direction,
			int parentReportId) throws SQLException {

		logger.debug("Entering moveDrilldown: id={}, fromPosition={}, toPosition={},"
				+ " direction='{}', parentReportId={}", id, fromPosition, toPosition,
				direction, parentReportId);

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
		} else if (StringUtils.equals(direction, "forward")) {
			//toPosition > fromPosition
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
