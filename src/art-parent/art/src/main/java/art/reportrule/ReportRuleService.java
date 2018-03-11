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
package art.reportrule;

import art.dbutils.DbService;
import art.report.Report;
import art.report.ReportService;
import art.reportoptions.CloneOptions;
import art.rule.Rule;
import art.rule.RuleService;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Provides methods for retrieving, adding, updating and deleting report rules
 *
 * @author Timothy Anyona
 */
@Service
public class ReportRuleService {

	private static final Logger logger = LoggerFactory.getLogger(ReportRuleService.class);

	private final DbService dbService;
	private final RuleService ruleService;
	private final ReportService reportService;

	@Autowired
	public ReportRuleService(DbService dbService, RuleService ruleService,
			ReportService reportService) {

		this.dbService = dbService;
		this.ruleService = ruleService;
		this.reportService = reportService;
	}

	public ReportRuleService() {
		dbService = new DbService();
		ruleService = new RuleService();
		reportService = new ReportService();
	}

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_QUERY_RULES";

	/**
	 * Maps a resultset to an object
	 */
	private class ReportRuleMapper extends BasicRowProcessor {

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
			ReportRule reportRule = new ReportRule();

			reportRule.setReportRuleId(rs.getInt("QUERY_RULE_ID"));
			reportRule.setReportId(rs.getInt("QUERY_ID"));
			reportRule.setReportColumn(rs.getString("FIELD_NAME"));

			Rule rule = ruleService.getRule(rs.getInt("RULE_ID"));
			reportRule.setRule(rule);

			return type.cast(reportRule);
		}
	}

	/**
	 * Returns report rules for a given report
	 *
	 * @param reportId the report id
	 * @return report rules for the given report
	 * @throws SQLException
	 */
	@Cacheable("rules")
	public List<ReportRule> getReportRules(int reportId) throws SQLException {
		logger.debug("Entering getReportRules: reportId={}", reportId);

		String sql = SQL_SELECT_ALL + " WHERE QUERY_ID=?";
		ResultSetHandler<List<ReportRule>> h = new BeanListHandler<>(ReportRule.class, new ReportRuleMapper());
		return dbService.query(sql, h, reportId);
	}

	/**
	 * Returns the report rules for a given report, returning the parent's
	 * report rules if applicable for clone reports
	 *
	 * @param reportId the report id
	 * @return report rules for the given report
	 * @throws SQLException
	 */
	@Cacheable("rules")
	public List<ReportRule> getEffectiveReportRules(int reportId) throws SQLException {
		logger.debug("Entering getEffectiveReportRules: reportId={}", reportId);

		Report report = reportService.getReport(reportId);
		if (report != null) {
			int sourceReportId = report.getSourceReportId();
			CloneOptions cloneOptions = report.getCloneOptions();
			if (cloneOptions == null) {
				cloneOptions = new CloneOptions();
			}
			if (sourceReportId > 0 && cloneOptions.isUseParentRules()) {
				reportId = sourceReportId;
			}
		}

		String sql = SQL_SELECT_ALL + " WHERE QUERY_ID=?";
		ResultSetHandler<List<ReportRule>> h = new BeanListHandler<>(ReportRule.class, new ReportRuleMapper());
		return dbService.query(sql, h, reportId);
	}

	/**
	 * Returns a report rule
	 *
	 * @param id the report rule id
	 * @return report rule if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("rules")
	public ReportRule getReportRule(int id) throws SQLException {
		logger.debug("Entering getReportRule: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE QUERY_RULE_ID=?";
		ResultSetHandler<ReportRule> h = new BeanHandler<>(ReportRule.class, new ReportRuleMapper());
		return dbService.query(sql, h, id);
	}
	
		/**
	 * Returns a report rule
	 *
	 * @param reportId the report id
	 * @param ruleId the rule id
	 * @return report rule if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("rules")
	public ReportRule getReportRule(int reportId, int ruleId) throws SQLException {
		logger.debug("Entering getReportRule: reportId={}, ruleId={}",
				reportId, ruleId);

		String sql = SQL_SELECT_ALL + " WHERE QUERY_ID=? AND RULE_ID=?";
		ResultSetHandler<ReportRule> h = new BeanHandler<>(ReportRule.class, new ReportRuleMapper());
		return dbService.query(sql, h, reportId, ruleId);
	}

	/**
	 * Deletes a report rule
	 *
	 * @param id the report rule id
	 * @throws SQLException
	 */
	@CacheEvict(value = "rules", allEntries = true)
	public void deleteReportRule(int id) throws SQLException {
		logger.debug("Entering deleteReportRule: id={}", id);

		String sql;

		sql = "DELETE FROM ART_QUERY_RULES WHERE QUERY_RULE_ID=?";
		dbService.update(sql, id);
	}

	/**
	 * Deletes multiple report rules
	 *
	 * @param ids the report rule ids
	 * @throws SQLException
	 */
	@CacheEvict(value = "rules", allEntries = true)
	public void deleteReportRules(Integer[] ids) throws SQLException {
		logger.debug("Entering deleteReportRules: ids={}", (Object) ids);

		String sql;

		sql = "DELETE FROM ART_QUERY_RULES WHERE"
				+ " QUERY_RULE_ID IN(" + StringUtils.repeat("?", ",", ids.length) + ")";

		dbService.update(sql, (Object[]) ids);
	}

	/**
	 * Adds a new report rule to the database
	 *
	 * @param reportRule the report rule
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "rules", allEntries = true)
	public synchronized int addReportRule(ReportRule reportRule) throws SQLException {
		logger.debug("Entering addReportRule: reportRule={}", reportRule);

		//generate new id
		String sql = "SELECT MAX(QUERY_RULE_ID) FROM ART_QUERY_RULES";
		int newId = dbService.getNewRecordId(sql);

		saveReportRule(reportRule, newId);

		return newId;
	}

	/**
	 * Updates an existing report rule
	 *
	 * @param reportRule the updated report rule
	 * @throws SQLException
	 */
	@CacheEvict(value = "rules", allEntries = true)
	public void updateReportRule(ReportRule reportRule) throws SQLException {
		logger.debug("Entering updateReportRule: reportRule={}", reportRule);

		Integer newRecordId = null;
		saveReportRule(reportRule, newRecordId);
	}

	/**
	 * Saves a report rule
	 *
	 * @param reportRule the report rule to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @throws SQLException
	 */
	public void saveReportRule(ReportRule reportRule, Integer newRecordId) throws SQLException {
		Connection conn = null;
		saveReportRule(reportRule, newRecordId, conn);
	}

	/**
	 * Saves a report rule
	 *
	 * @param reportRule the report rule to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param conn the connection to use. if null, the art database will be used
	 * @throws SQLException
	 */
	@CacheEvict(value = "rules", allEntries = true)
	public void saveReportRule(ReportRule reportRule, Integer newRecordId,
			Connection conn) throws SQLException {

		logger.debug("Entering saveReportRule: reportRule={}, newRecordId={}",
				reportRule, newRecordId);

		int affectedRows;

		boolean newRecord = false;
		if (newRecordId != null) {
			newRecord = true;
		}

		if (newRecord) {
			String sql = "INSERT INTO ART_QUERY_RULES"
					+ " (QUERY_RULE_ID, QUERY_ID, RULE_ID, RULE_NAME, FIELD_NAME)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 5) + ")";

			Object[] values = {
				newRecordId,
				reportRule.getReportId(),
				reportRule.getRule().getRuleId(),
				reportRule.getRule().getName(), //remove once rule_name is removed
				reportRule.getReportColumn()
			};

			if (conn == null) {
				affectedRows = dbService.update(sql, values);
			} else {
				affectedRows = dbService.update(conn, sql, values);
			}
		} else {
			String sql = "UPDATE ART_QUERY_RULES SET QUERY_ID=?, RULE_ID=?,"
					+ " RULE_NAME=?, FIELD_NAME=?"
					+ " WHERE QUERY_RULE_ID=?";

			Object[] values = {
				reportRule.getReportId(),
				reportRule.getRule().getRuleId(),
				reportRule.getRule().getName(), //remove once rule_name is removed
				reportRule.getReportColumn(),
				reportRule.getReportRuleId()
			};

			if (conn == null) {
				affectedRows = dbService.update(sql, values);
			} else {
				affectedRows = dbService.update(conn, sql, values);
			}
		}

		if (newRecordId != null) {
			reportRule.setReportRuleId(newRecordId);
		}

		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}, newRecord={}, reportRule={}",
					affectedRows, newRecord, reportRule);
		}
	}
}
