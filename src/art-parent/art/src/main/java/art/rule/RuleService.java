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
package art.rule;

import art.dbutils.DbService;
import art.dbutils.DatabaseUtils;
import art.enums.ParameterDataType;
import art.user.User;
import art.utils.ActionResult;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
 * Provides methods for retrieving, adding, updating and deleting rules
 *
 * @author Timothy Anyona
 */
@Service
public class RuleService {

	private static final Logger logger = LoggerFactory.getLogger(RuleService.class);

	private final DbService dbService;
	
	@Autowired
	public RuleService(DbService dbService){
		this.dbService=dbService;
	}
	
	public RuleService(){
		dbService=new DbService();
	}

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_RULES";

	/**
	 * Maps a resultset to an object
	 */
	private class RuleMapper extends BasicRowProcessor {

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
			Rule rule = new Rule();

			rule.setRuleId(rs.getInt("RULE_ID"));
			rule.setName(rs.getString("RULE_NAME"));
			rule.setDescription(rs.getString("SHORT_DESCRIPTION"));
			rule.setDataType(ParameterDataType.toEnum(rs.getString("DATA_TYPE")));
			rule.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			rule.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
			rule.setCreatedBy(rs.getString("CREATED_BY"));
			rule.setUpdatedBy(rs.getString("UPDATED_BY"));

			return type.cast(rule);
		}
	}

	/**
	 * Returns all rules
	 *
	 * @return all rules
	 * @throws SQLException
	 */
	@Cacheable("rules")
	public List<Rule> getAllRules() throws SQLException {
		logger.debug("Entering getAllRules");

		ResultSetHandler<List<Rule>> h = new BeanListHandler<>(Rule.class, new RuleMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}
	
	/**
	 * Returns a rule
	 *
	 * @param id the rule id
	 * @return rule if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("rules")
	public Rule getRule(int id) throws SQLException {
		logger.debug("Entering getRule: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE RULE_ID=?";
		ResultSetHandler<Rule> h = new BeanHandler<>(Rule.class, new RuleMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Returns reports that use a given rule
	 *
	 * @param ruleId the rule id
	 * @return linked report names
	 * @throws SQLException
	 */
	public List<String> getLinkedReports(int ruleId) throws SQLException {
		logger.debug("Entering getLinkedReports: ruleId={}", ruleId);

		String sql = "SELECT AQ.NAME"
				+ " FROM ART_QUERY_RULES AQR"
				+ " INNER JOIN ART_QUERIES AQ ON"
				+ " AQR.QUERY_ID=AQ.QUERY_ID"
				+ " WHERE AQR.RULE_ID=?";

		ResultSetHandler<List<String>> h = new ColumnListHandler<>(1);
		return dbService.query(sql, h, ruleId);
	}

	/**
	 * Deletes a rule
	 *
	 * @param id the rule id
	 * @return ActionResult. if delete was not successful, data contains a list
	 * of linked reports which prevented the rule from being deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = "rules", allEntries = true)
	public ActionResult deleteRule(int id) throws SQLException {
		logger.debug("Entering deleteRule: id={}", id);

		ActionResult result = new ActionResult();

		//don't delete if important linked records exist
		List<String> linkedReports = getLinkedReports(id);
		if (!linkedReports.isEmpty()) {
			result.setData(linkedReports);
			return result;
		}

		String sql;

		sql = "DELETE FROM ART_USER_RULES WHERE RULE_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_GROUP_RULES WHERE RULE_ID=?";
		dbService.update(sql, id);

		//finally delete rule
		sql = "DELETE FROM ART_RULES WHERE RULE_ID=?";
		dbService.update(sql, id);

		result.setSuccess(true);
		
		return result;
	}

	/**
	 * Deletes multiple rules
	 *
	 * @param ids the ids of the rules to delete
	 * @return ActionResult. if delete was not successful, data contains ids of
	 * rules which were not deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = "rules", allEntries = true)
	public ActionResult deleteRules(Integer[] ids) throws SQLException {
		logger.debug("Entering deleteRules: ids={}", (Object) ids);

		ActionResult result = new ActionResult();
		List<Integer> nonDeletedRecords = new ArrayList<>();

		for (Integer id : ids) {
			ActionResult deleteResult = deleteRule(id);
			if (!deleteResult.isSuccess()) {
				nonDeletedRecords.add(id);
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
	 * Adds a new rule
	 *
	 * @param rule the rule to add
	 * @param actionUser the user who is performing the action
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "rules", allEntries = true)
	public synchronized int addRule(Rule rule, User actionUser) throws SQLException {
		logger.debug("Entering addRule: rule={}, actionUser={}", rule, actionUser);

		//generate new id
		String sql = "SELECT MAX(RULE_ID) FROM ART_RULES";
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

		rule.setRuleId(newId);

		boolean newRecord = true;
		saveRule(rule, newRecord, actionUser);

		return newId;
	}

	/**
	 * Updates an existing rule
	 *
	 * @param rule the updated rule
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	@CacheEvict(value = "rules", allEntries = true)
	public void updateRule(Rule rule, User actionUser) throws SQLException {
		logger.debug("Entering updateRule: rule={}, actionUser={}", rule, actionUser);

		boolean newRecord = false;
		saveRule(rule, newRecord, actionUser);
	}

	/**
	 * Saves a rule
	 *
	 * @param rule the rule to save
	 * @param newRecord whether this is a new record
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	private void saveRule(Rule rule, boolean newRecord, User actionUser) throws SQLException {
		logger.debug("Entering saveRule: rule={}, newRecord={},actionUser={}",
				rule, newRecord, actionUser);

		//set values for possibly null property objects
		String dataType;
		if (rule.getDataType() == null) {
			logger.warn("Data type not defined. Defaulting to varchar");
			dataType = ParameterDataType.Varchar.getValue();
		} else {
			dataType = rule.getDataType().getValue();
		}

		int affectedRows;
		if (newRecord) {
			String sql = "INSERT INTO ART_RULES"
					+ " (RULE_ID, RULE_NAME, SHORT_DESCRIPTION, DATA_TYPE,"
					+ " CREATION_DATE, CREATED_BY)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 6) + ")";

			Object[] values = {
				rule.getRuleId(),
				rule.getName(),
				rule.getDescription(),
				dataType,
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername()
			};

			affectedRows = dbService.update(sql, values);
		} else {
			String sql = "UPDATE ART_RULES SET RULE_NAME=?, SHORT_DESCRIPTION=?,"
					+ " DATA_TYPE=?, UPDATE_DATE=?, UPDATED_BY=?"
					+ " WHERE RULE_ID=?";

			Object[] values = {
				rule.getName(),
				rule.getDescription(),
				dataType,
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername(),
				rule.getRuleId()
			};

			affectedRows = dbService.update(sql, values);
		}

		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}, newRecord={}, rule={}",
					affectedRows, newRecord, rule);
		}
	}

	/**
	 * Returns the name of a given rule
	 *
	 * @param id the rule id
	 * @return the name of the given rule
	 * @throws SQLException
	 */
	@Cacheable("rules")
	public String getRuleName(int id) throws SQLException {
		logger.debug("Entering getRuleName: id={}", id);

		String sql = "SELECT RULE_NAME FROM ART_RULES WHERE RULE_ID=?";
		ResultSetHandler<String> h = new ScalarHandler<>(1);
		return dbService.query(sql, h, id);
	}
}
