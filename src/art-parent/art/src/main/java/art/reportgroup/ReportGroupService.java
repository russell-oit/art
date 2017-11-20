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
package art.reportgroup;

import art.dbutils.DbService;
import art.dbutils.DatabaseUtils;
import art.enums.AccessLevel;
import art.user.User;
import art.utils.ActionResult;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Provides methods for retrieving, adding, updating and deleting report groups
 *
 * @author Timothy Anyona
 */
@Service
public class ReportGroupService {

	private static final Logger logger = LoggerFactory.getLogger(ReportGroupService.class);

	private final DbService dbService;

	@Autowired
	public ReportGroupService(DbService dbService) {
		this.dbService = dbService;
	}

	public ReportGroupService() {
		dbService = new DbService();
	}

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_QUERY_GROUPS AQG";

	/**
	 * Maps a resultset to an object
	 */
	private class ReportGroupMapper extends BasicRowProcessor {

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
			ReportGroup group = new ReportGroup();

			group.setReportGroupId(rs.getInt("QUERY_GROUP_ID"));
			group.setName(rs.getString("NAME"));
			group.setDescription(rs.getString("DESCRIPTION"));
			group.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			group.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
			group.setCreatedBy(rs.getString("CREATED_BY"));
			group.setUpdatedBy(rs.getString("UPDATED_BY"));

			return type.cast(group);
		}
	}

	/**
	 * Returns report groups that are available for a given user
	 *
	 * @param username the user's username
	 * @return available report groups
	 * @throws SQLException
	 */
	@Cacheable("reportGroups")
	public List<ReportGroup> getAvailableReportGroups(String username) throws SQLException {
		logger.debug("Entering getAvailableReportGroups: username='{}'", username);

		//union will return distinct results
		//get groups that user has explicit rights to see
		String sql = "SELECT AQG.* "
				+ " FROM ART_USER_QUERY_GROUPS AUQG , ART_QUERY_GROUPS AQG"
				+ " WHERE AUQG.USERNAME=?"
				+ " AND AUQG.QUERY_GROUP_ID = AQG.QUERY_GROUP_ID"
				+ " UNION "
				//add groups to which the user has access through his user group 
				+ " SELECT AQG.* "
				+ " FROM ART_USER_GROUP_GROUPS AUGG, ART_QUERY_GROUPS AQG"
				+ " WHERE AUGG.QUERY_GROUP_ID=AQG.QUERY_GROUP_ID"
				+ " AND EXISTS (SELECT * FROM ART_USER_GROUP_ASSIGNMENT AUGA"
				+ " WHERE AUGA.USERNAME = ? AND AUGA.QUERY_GROUP_ID = AUGG.QUERY_GROUP_ID)"
				+ " UNION "
				//add groups where user has right to query but not to group
				+ " SELECT AQG.* "
				+ " FROM ART_USER_QUERIES AUQ, ART_QUERIES AQ,"
				+ " ART_REPORT_REPORT_GROUPS ARRG, ART_QUERY_GROUPS AQG,"
				+ " WHERE AUQ.QUERY_ID=AQ.QUERY_ID"
				+ " AND AQ.QUERY_ID=ARRG.REPORT_ID"
				+ " AND ARRG.REPORT_GROUP_ID=AQG.QUERY_GROUP_ID"
				+ " AND AUQ.USERNAME = ? AND AQG.QUERY_GROUP_ID<>0"
				+ " AND AQ.QUERY_TYPE<>119 AND AQ.QUERY_TYPE<>120"
				+ " UNION "
				//add groups where user's group has rights to the query
				+ " SELECT AQG.QUERY_GROUP_ID, AQG.NAME, AQG.DESCRIPTION"
				+ " FROM ART_USER_GROUP_QUERIES AUGQ, ART_QUERIES AQ,"
				+ " ART_REPORT_REPORT_GROUPS ARRG, ART_QUERY_GROUPS AQG,"
				+ " WHERE AUGQ.QUERY_ID=AQ.QUERY_ID"
				+ " AND AQ.QUERY_ID=ARRG.REPORT_ID"
				+ " AND ARRG.REPORT_GROUP_ID=AQG.QUERY_GROUP_ID"
				+ " AND AQG.QUERY_GROUP_ID<>0 AND AQ.QUERY_TYPE<>119 AND AQ.QUERY_TYPE<>120"
				+ " AND EXISTS (SELECT * FROM ART_USER_GROUP_ASSIGNMENT AUGA"
				+ " WHERE AUGA.USERNAME = ? AND AUGA.QUERY_GROUP_ID = AUGQ.QUERY_GROUP_ID)";

		Object[] values = {username, username, username, username};

		ResultSetHandler<List<ReportGroup>> h = new BeanListHandler<>(ReportGroup.class, new ReportGroupMapper());
		return dbService.query(sql, h, values);
	}

	/**
	 * Returns all report groups
	 *
	 * @return all report groups
	 * @throws SQLException
	 */
	@Cacheable("reportGroups")
	public List<ReportGroup> getAllReportGroups() throws SQLException {
		logger.debug("Entering getAllReportGroups");

		ResultSetHandler<List<ReportGroup>> h = new BeanListHandler<>(ReportGroup.class, new ReportGroupMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Returns report groups that an admin can use, according to his access
	 * level
	 *
	 * @param user the admin user
	 * @return available report groups
	 * @throws SQLException
	 */
	@Cacheable("reportGroups")
	public List<ReportGroup> getAdminReportGroups(User user) throws SQLException {
		logger.debug("Entering getAdminReportGroups: user={}", user);

		if (user == null || user.getAccessLevel() == null) {
			return Collections.emptyList();
		}

		logger.debug("user.getAccessLevel()={}", user.getAccessLevel());

		ResultSetHandler<List<ReportGroup>> h = new BeanListHandler<>(ReportGroup.class, new ReportGroupMapper());
		if (user.getAccessLevel().getValue() >= AccessLevel.StandardAdmin.getValue()) {
			//standard admins and above can work with everything
			return dbService.query(SQL_SELECT_ALL, h);
		} else {
			String sql = "SELECT AQG.*"
					+ " FROM ART_QUERY_GROUPS AQG, ART_ADMIN_PRIVILEGES AAP "
					+ " WHERE AQG.QUERY_GROUP_ID = AAP.VALUE_ID "
					+ " AND AAP.PRIVILEGE = 'GRP' "
					+ " AND AAP.USER_ID = ? ";
			return dbService.query(sql, h, user.getUserId());
		}
	}

	/**
	 * Returns a report group
	 *
	 * @param id the report group id
	 * @return report group if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("reportGroups")
	public ReportGroup getReportGroup(int id) throws SQLException {
		logger.debug("Entering getReportGroup: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE QUERY_GROUP_ID=?";
		ResultSetHandler<ReportGroup> h = new BeanHandler<>(ReportGroup.class, new ReportGroupMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Deletes a report group
	 *
	 * @param id the report group id
	 * @return ActionResult. if not successful, data contains a list of linked
	 * reports which prevented the report group from being deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = {"reportGroups", "reports"}, allEntries = true)
	public ActionResult deleteReportGroup(int id) throws SQLException {
		logger.debug("Entering deleteReportGroup: id={}", id);

		ActionResult result = new ActionResult();

		//don't delete if important linked records exist
		List<String> linkedReports = getLinkedReports(id);
		if (!linkedReports.isEmpty()) {
			result.setData(linkedReports);
			return result;
		}

		String sql;

		//delete foreign key records
		sql = "DELETE FROM ART_USER_QUERY_GROUPS WHERE QUERY_GROUP_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_GROUP_GROUPS WHERE QUERY_GROUP_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_REPORT_REPORT_GROUPS WHERE REPORT_GROUP_ID=?";
		dbService.update(sql, id);

		//finally delete report group
		sql = "DELETE FROM ART_QUERY_GROUPS WHERE QUERY_GROUP_ID=?";
		dbService.update(sql, id);

		result.setSuccess(true);

		return result;
	}

	/**
	 * Deletes multiple report groups
	 *
	 * @param ids the ids of the report groups to delete
	 * @return ActionResult. if not successful, data contains ids of the report
	 * groups that were not deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = {"reportGroups", "reports"}, allEntries = true)
	public ActionResult deleteReportGroups(Integer[] ids) throws SQLException {
		logger.debug("Entering deleteReportGrousp: ids={}", (Object) ids);

		ActionResult result = new ActionResult();
		List<String> nonDeletedRecords = new ArrayList<>();

		for (Integer id : ids) {
			ActionResult deleteResult = deleteReportGroup(id);
			if (!deleteResult.isSuccess()) {
				@SuppressWarnings("unchecked")
				List<String> linkedReports = (List<String>) deleteResult.getData();
				String value = String.valueOf(id) + " - (" + StringUtils.join(linkedReports, ", ") + ")";
				nonDeletedRecords.add(value);
			}
		}

		if (nonDeletedRecords.isEmpty()) {
			result.setSuccess(true);
		} else {
			result.setData(nonDeletedRecords);
		}

		return result;
	}

	/**
	 * Adds a new report group to the database
	 *
	 * @param group the report group to add
	 * @param actionUser the user who is performing the action
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "reportGroups", allEntries = true)
	public synchronized int addReportGroup(ReportGroup group, User actionUser) throws SQLException {
		logger.debug("Entering addReportGroup: group={}, actionUser={}", group, actionUser);

		//generate new id
		String sql = "SELECT MAX(QUERY_GROUP_ID) FROM ART_QUERY_GROUPS";
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

		saveReportGroup(group, newId, actionUser);

		return newId;
	}

	/**
	 * Updates an existing report group
	 *
	 * @param group the updated report group
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	@CacheEvict(value = {"reportGroups", "reports"}, allEntries = true)
	public void updateReportGroup(ReportGroup group, User actionUser) throws SQLException {
		logger.debug("Entering updateReportGroup: group={}, actionUser={}", group, actionUser);

		Integer newRecordId = null;
		saveReportGroup(group, newRecordId, actionUser);
	}

	/**
	 * Saves a report group
	 *
	 * @param group the report group
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	private void saveReportGroup(ReportGroup group, Integer newRecordId, User actionUser) throws SQLException {
		logger.debug("Entering saveReportGroup: group={}, newRecordId={}, actionUser={}",
				group, newRecordId, actionUser);

		int affectedRows;

		boolean newRecord = false;
		if (newRecordId != null) {
			newRecord = true;
		}

		if (newRecord) {
			String sql = "INSERT INTO ART_QUERY_GROUPS"
					+ " (QUERY_GROUP_ID, NAME, DESCRIPTION, CREATION_DATE, CREATED_BY)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 5) + ")";

			Object[] values = {
				newRecordId,
				group.getName(),
				group.getDescription(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername()
			};

			affectedRows = dbService.update(sql, values);
		} else {
			String sql = "UPDATE ART_QUERY_GROUPS SET NAME=?, DESCRIPTION=?,"
					+ " UPDATE_DATE=?, UPDATED_BY=?"
					+ " WHERE QUERY_GROUP_ID=?";

			Object[] values = {
				group.getName(),
				group.getDescription(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername(),
				group.getReportGroupId()
			};

			affectedRows = dbService.update(sql, values);
		}

		if (newRecordId != null) {
			group.setReportGroupId(newRecordId);
		}

		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}, newRecord={}, group={}",
					affectedRows, newRecord, group);
		}
	}

	/**
	 * Returns reports that are in a given report group
	 *
	 * @param reportGroupId the report group id
	 * @return linked report names
	 * @throws SQLException
	 */
	public List<String> getLinkedReports(int reportGroupId) throws SQLException {
		logger.debug("Entering getLinkedReports: reportGroupId={}", reportGroupId);

		String sql = "SELECT AQ.NAME"
				+ " FROM ART_QUERIES AQ"
				+ " INNER JOIN ART_REPORT_REPORT_GROUPS ARRG"
				+ " ON AQ.QUERY_ID=ARRG=REPORT_ID"
				+ " WHERE ARRG.REPORT_GROUP_ID=?";

		ResultSetHandler<List<String>> h = new ColumnListHandler<>("NAME");
		return dbService.query(sql, h, reportGroupId);
	}

	/**
	 * Returns the report groups that the given report belongs to
	 *
	 * @param reportId the report id
	 * @return the report's report groups
	 * @throws SQLException
	 */
	@Cacheable("reportGroups")
	public List<ReportGroup> getReportGroupsForReport(int reportId) throws SQLException {
		logger.debug("Entering getReportGroupsForReport: reportId={}", reportId);

		String sql = SQL_SELECT_ALL
				+ " INNER JOIN ART_REPORT_REPORT_GROUPS ARRG "
				+ " ON ARRG.REPORT_GROUP_ID=AQG.QUERY_GROUP_ID"
				+ " WHERE ARRG.REPORT_ID=?";
		ResultSetHandler<List<ReportGroup>> h = new BeanListHandler<>(ReportGroup.class, new ReportGroupMapper());
		return dbService.query(sql, h, reportId);
	}

}
