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
package art.drilldown;

import art.dbutils.DbService;
import art.report.Report;
import art.report.ReportService;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provides methods for retrieving, adding, updating and deleting drilldowns
 *
 * @author Timothy Anyona
 */
@Service
public class DrilldownService {

	private static final Logger logger = LoggerFactory.getLogger(DrilldownService.class);

	private final DbService dbService;
	private final ReportService reportService;

	@Autowired
	public DrilldownService(DbService dbService, ReportService reportService) {
		this.dbService = dbService;
		this.reportService = reportService;
	}

	public DrilldownService() {
		dbService = new DbService();
		reportService = new ReportService();
	}

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_DRILLDOWN_QUERIES ADQ";

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

			Report drilldownReport = reportService.getReport(rs.getInt("DRILLDOWN_QUERY_ID"));
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
		int newId = dbService.getNewRecordId(sql);

		//generate new position
		sql = "SELECT MAX(DRILLDOWN_QUERY_POSITION)"
				+ " FROM ART_DRILLDOWN_QUERIES"
				+ " WHERE QUERY_ID=?";
		int newPosition = dbService.getNewRecordId(sql, parentReportId);

		drilldown.setPosition(newPosition);
		drilldown.setParentReportId(parentReportId);

		saveDrilldown(drilldown, newId);

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

		Integer newRecordId = null;
		saveDrilldown(drilldown, newRecordId);
	}

	/**
	 * Imports drilldown records
	 *
	 * @param drilldowns the drilldowns to import
	 * @param conn the connection to use. if autocommit is false, no commit is
	 * performed
	 * @throws SQLException
	 */
	public void importDrilldowns(List<Drilldown> drilldowns, Connection conn) throws SQLException {
		logger.debug("Entering importDrilldowns");

		if (drilldowns == null) {
			return;
		}

		String sql = "SELECT MAX(DRILLDOWN_ID) FROM ART_DRILLDOWN_QUERIES";
		int drilldownId = dbService.getMaxRecordId(sql);

		for (Drilldown drilldown : drilldowns) {
			drilldownId++;
			saveDrilldown(drilldown, drilldownId, conn);
		}
	}

	/**
	 * Saves a drilldown
	 *
	 * @param drilldown the drilldown to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @throws SQLException
	 */
	private void saveDrilldown(Drilldown drilldown, Integer newRecordId) throws SQLException {
		Connection conn = null;
		saveDrilldown(drilldown, newRecordId, conn);

	}

	/**
	 * Saves a drilldown
	 *
	 * @param drilldown the drilldown to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param conn the connection to use. if null, the art database will be used
	 * @throws SQLException
	 */
	private void saveDrilldown(Drilldown drilldown, Integer newRecordId,
			Connection conn) throws SQLException {

		logger.debug("Entering saveDrilldown: drilldown={}, newRecordId={}", drilldown, newRecordId);

		int affectedRows;

		boolean newRecord = false;
		if (newRecordId != null) {
			newRecord = true;
		}

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
				BooleanUtils.toInteger(drilldown.isOpenInNewWindow())
			};

			if (conn == null) {
				affectedRows = dbService.update(sql, values);
			} else {
				affectedRows = dbService.update(conn, sql, values);
			}
		} else {
			String sql = "UPDATE ART_DRILLDOWN_QUERIES SET DRILLDOWN_QUERY_ID=?,"
					+ " DRILLDOWN_TITLE=?, DRILLDOWN_TEXT=?,"
					+ " OUTPUT_FORMAT=?, OPEN_IN_NEW_WINDOW=?"
					+ " WHERE DRILLDOWN_ID=?"
					+ " AND QUERY_ID=? AND DRILLDOWN_QUERY_POSITION=?";

			Object[] values = {
				drilldown.getDrilldownReport().getReportId(),
				drilldown.getHeaderText(),
				drilldown.getLinkText(),
				drilldown.getReportFormat(),
				BooleanUtils.toInteger(drilldown.isOpenInNewWindow()),
				drilldown.getDrilldownId(),
				drilldown.getParentReportId(),
				drilldown.getPosition()
			};

			if (conn == null) {
				affectedRows = dbService.update(sql, values);
			} else {
				affectedRows = dbService.update(conn, sql, values);
			}
		}

		if (newRecordId != null) {
			drilldown.setDrilldownId(newRecordId);
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
