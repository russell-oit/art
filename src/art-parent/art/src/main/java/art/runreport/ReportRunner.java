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
package art.runreport;

import art.connectionpool.DbConnections;
import art.datasource.Datasource;
import art.datasource.DatasourceOptions;
import art.dbutils.DatabaseUtils;
import art.enums.DatabaseProtocol;
import art.enums.ParameterDataType;
import art.enums.ParameterType;
import art.enums.ReportType;
import art.job.Job;
import art.report.Report;
import art.reportoptions.ViewOptions;
import art.reportparameter.ReportParameter;
import art.reportrule.ReportRule;
import art.reportrule.ReportRuleService;
import art.rule.Rule;
import art.ruleValue.RuleValueService;
import art.selfservice.SelfServiceOptions;
import art.servlets.Config;
import art.user.User;
import art.usergroup.UserGroup;
import art.utils.ArtUtils;
import art.utils.ExpressionHelper;
import art.utils.XmlInfo;
import art.utils.XmlParser;
import com.itfsw.query.builder.SqlQueryBuilderFactory;
import com.itfsw.query.builder.support.builder.SqlBuilder;
import com.itfsw.query.builder.support.model.result.SqlQueryResult;
import groovy.lang.GString;
import groovy.sql.GroovyRowResult;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

/**
 * Runs a report
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class ReportRunner {

	private static final Logger logger = LoggerFactory.getLogger(ReportRunner.class);

	private final StringBuilder querySb;
	private boolean useRules = false;
	private Statement psQuery; // this is the ps object produced by this query
	private Connection connQuery; // this is the connection to the datasource for this query
	private String finalSql; //final sql statement with parameters substituted
	private boolean recipientFilterPresent; //dynamic recipient filter label present
	private final String RECIPIENT_LABEL = "#recipient#"; //for dynamic recipients, label for recipient in data query
	private String recipientColumn;
	private String recipientId;
	private String recipientIdType = "VARCHAR";
	private int updateCount; //update count of display resultset
	private Report report;
	private Map<String, ReportParameter> reportParamsMap;
	private final List<Object> jdbcParams = new ArrayList<>();
	private User user;
	private boolean postgreSqlFetchSizeApplied;
	private final String QUESTION_PLACEHOLDER = "[ART_QUESTION_MARK_PLACEHOLDER]";
	private Object groovyData;
	private Job job;
	private Integer limit;
	private boolean useViewColumns;
	public static final int RETURN_ALL_RECORDS = -1;
	public static final int RETURN_ZERO_RECORDS = 0;
	private String runId;
	private MultiValueMap<String, MultipartFile> multiFileMap;
	private String localRunId;

	public ReportRunner() {
		querySb = new StringBuilder(1024 * 2); // assume the average query is < 2kb
	}

	/**
	 * @return the multiFileMap
	 */
	public MultiValueMap<String, MultipartFile> getMultiFileMap() {
		return multiFileMap;
	}

	/**
	 * @param multiFileMap the multiFileMap to set
	 */
	public void setMultiFileMap(MultiValueMap<String, MultipartFile> multiFileMap) {
		this.multiFileMap = multiFileMap;
	}

	/**
	 * @return the runId
	 */
	public String getRunId() {
		return runId;
	}

	/**
	 * @param runId the runId to set
	 */
	public void setRunId(String runId) {
		this.runId = runId;
	}

	/**
	 * @return the useViewColumns
	 */
	public boolean isUseViewColumns() {
		return useViewColumns;
	}

	/**
	 * @param useViewColumns the useViewColumns to set
	 */
	public void setUseViewColumns(boolean useViewColumns) {
		this.useViewColumns = useViewColumns;
	}

	/**
	 * @return the limit
	 */
	public Integer getLimit() {
		return limit;
	}

	/**
	 * @param limit the limit to set
	 */
	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	/**
	 * @return the job
	 */
	public Job getJob() {
		return job;
	}

	/**
	 * @param job the job to set
	 */
	public void setJob(Job job) {
		this.job = job;
	}

	/**
	 * @return the groovyData
	 */
	public Object getGroovyData() {
		return groovyData;
	}

	/**
	 * @param groovyData the groovyData to set
	 */
	public void setGroovyData(Object groovyData) {
		this.groovyData = groovyData;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * Set the value of reportParamsMap
	 *
	 * @param reportParamsMap new value of reportParamsMap
	 */
	public void setReportParamsMap(Map<String, ReportParameter> reportParamsMap) {
		this.reportParamsMap = reportParamsMap;
	}

	/**
	 * @return the report
	 */
	public Report getReport() {
		return report;
	}

	/**
	 * @param report the report to set
	 */
	public void setReport(Report report) {
		this.report = report;
	}

	/**
	 * @return the recipientIdType
	 */
	public String getRecipientIdType() {
		return recipientIdType;
	}

	/**
	 * @param recipientIdType the recipientIdType to set
	 */
	public void setRecipientIdType(String recipientIdType) {
		this.recipientIdType = recipientIdType;
	}

	/**
	 * @return the recipientColumn
	 */
	public String getRecipientColumn() {
		return recipientColumn;
	}

	/**
	 * @param recipientColumn the recipientColumn to set
	 */
	public void setRecipientColumn(String recipientColumn) {
		this.recipientColumn = recipientColumn;
	}

	/**
	 * @return the recipientId
	 */
	public String getRecipientId() {
		return recipientId;
	}

	/**
	 * @param recipientId the recipientId to set
	 */
	public void setRecipientId(String recipientId) {
		this.recipientId = recipientId;
	}

	/**
	 * Get the value of recipientFilterPresent
	 *
	 * @return the value of recipientFilterPresent
	 */
	public boolean isRecipientFilterPresent() {
		return recipientFilterPresent;
	}

	/**
	 * Set the value of recipientFilterPresent
	 *
	 * @param recipientFilterPresent new value of recipientFilterPresent
	 */
	public void setRecipientFilterPresent(boolean recipientFilterPresent) {
		this.recipientFilterPresent = recipientFilterPresent;
	}

	/**
	 * @return the finalSql
	 */
	public String getFinalSql() {
		return finalSql;
	}

	/**
	 * @param finalSql the finalSql to set
	 */
	public void setFinalSql(String finalSql) {
		this.finalSql = finalSql;
	}

	/**
	 * Processes the report source and applies tags, dynamic sql, parameters and
	 * rules
	 *
	 * @param report the report for which to process the source
	 * @param user the user who is running the report. may be null if the report
	 * doesn't use rules, or if the source doesn't contain the :username: tag
	 * which needs to be replaced
	 * @param reportParamsMap the report parameters. may be null if the report
	 * doesn't contain direct report parameters which need to be replaced, or if
	 * it doesn't contain dynamic sql tags that use parameters
	 * @return processed report source
	 * @throws java.sql.SQLException
	 */
	public String processReportSource(Report report, User user,
			Map<String, ReportParameter> reportParamsMap) throws SQLException {

		Objects.requireNonNull(report, "report must not be null");

		this.report = report;
		this.user = user;
		this.reportParamsMap = reportParamsMap;

		useRules = report.isUsesRules();

		return processReportSource();
	}

	/**
	 * Processes the report source and applies tags, dynamic sql, parameters and
	 * rules
	 *
	 * @return processed report source
	 * @throws java.sql.SQLException
	 */
	private String processReportSource() throws SQLException {
		logger.debug("Entering processReportSource");

		//update querySb with report sql
		String reportSource = report.getReportSource();
		if (reportSource == null) {
			throw new RuntimeException("Report source not available");
		}
		querySb.replace(0, querySb.length(), reportSource);

		applyTags();
		applyFieldExpressions();
		applyDynamicSql();
		applyDirectSubstitution();
		applyGroovySnippets();
		applyUsesGroovy();
		applyParameterPlaceholders(); //question placeholder put here
		applyDynamicRecipient();
		applySelfServiceFields(); //must come after applyParameterPlaceholders()

		if (!report.getReportType().isJPivot()) {
			applyRulesToQuery();
		}

		//generate final sql and revert question placeholder
		String querySql = querySb.toString();

		Object[] paramValues = jdbcParams.toArray(new Object[0]);
		finalSql = generateFinalSql(querySql, paramValues);

		querySql = StringUtils.replaceIgnoreCase(querySql, QUESTION_PLACEHOLDER, "?");
		finalSql = StringUtils.replaceIgnoreCase(finalSql, QUESTION_PLACEHOLDER, "?");

		//update querySb with new sql
		querySb.replace(0, querySb.length(), querySql);

		logger.debug("Sql query finally is: \n{}", querySb);

		String processedSource = querySb.toString();

		return processedSource;
	}

	/**
	 * Applies field expressions i.e. username, date, datetime
	 *
	 * @throws SQLException
	 */
	private void applyFieldExpressions() throws SQLException {
		logger.debug("Entering applyFieldExpressions");

		String querySql = querySb.toString();
		ExpressionHelper expressionHelper = new ExpressionHelper();

		String username = null;
		if (user != null) {
			username = user.getUsername();
		}

		try {
			querySql = expressionHelper.processFields(querySql, username);
		} catch (ParseException ex) {
			throw new SQLException(ex);
		}

		//update sb with new sql
		querySb.replace(0, querySb.length(), querySql);
	}

	/**
	 * Applies groovy snippets
	 *
	 */
	private void applyGroovySnippets() {
		logger.debug("Entering applyGroovySnippets");

		String querySql = querySb.toString();
		ExpressionHelper expressionHelper = new ExpressionHelper();

		querySql = expressionHelper.processGroovy(querySql, reportParamsMap);

		//update sb with new sql
		querySb.replace(0, querySb.length(), querySql);
	}

	/**
	 * Applies groovy to the report source if the report is configured to use
	 * groovy
	 *
	 * @throws SQLException
	 */
	private void applyUsesGroovy() {
		logger.debug("Entering applyUsesGroovy");

		if (report.isEffectiveUseGroovy() || report.getReportType() == ReportType.MongoDB) {
			String querySql = querySb.toString();
			logger.debug("Groovy source before evaluation: \n{}", querySql);

			ExpressionHelper expressionHelper = new ExpressionHelper();
			Object result = expressionHelper.runGroovyExpression(querySql, report, reportParamsMap, multiFileMap);

			groovyData = null;
			if (result != null) {
				if (result instanceof String || result instanceof GString) {
					querySql = String.valueOf(result);
				} else {
					groovyData = result;
				}
			}

			//update sb with new sql
			querySb.replace(0, querySb.length(), querySql);
		}
	}

	/**
	 * Applies rules to the report source
	 *
	 * @throws SQLException
	 */
	private void applyRulesToQuery() throws SQLException {
		logger.debug("Entering applyRulesToQuery");

		String querySql = querySb.toString();

		if (!useRules) {
			//if use rules setting is overriden, i.e. it's false while the query has a #rules# label, remove label and put dummy condition
			querySql = querySql.replaceAll("(?iu)#rules#", "1=1");
			//update sb with new sql
			querySb.replace(0, querySb.length(), querySql);
			return;
		}

		ReportRuleService reportRuleService = new ReportRuleService();

		int reportId = report.getReportId();
		List<ReportRule> reportRules = reportRuleService.getEffectiveReportRules(reportId);

		RuleValueService ruleValueService = new RuleValueService();
		int count = 0;
		StringBuilder labelledValues = new StringBuilder(1024);

		if (CollectionUtils.isNotEmpty(reportRules) && user == null) {
			throw new RuntimeException("Report has rules but no user supplied");
		}

		// for each rule build and add the AND column IN (list) string to the query
		// Note: if we don't have rules for this query, the sb is left untouched
		for (ReportRule reportRule : reportRules) {
			count++;
			Rule rule = reportRule.getRule();
			int userId = user.getUserId();
			int ruleId = rule.getRuleId();
			List<String> userRuleValues = ruleValueService.getUserRuleValues(userId, ruleId);
			List<String> userGroupRuleValues = new ArrayList<>();
			for (UserGroup userGroup : user.getUserGroups()) {
				userGroupRuleValues.addAll(ruleValueService.getUserGroupRuleValues(userGroup.getUserGroupId(), ruleId));
			}

			if (userRuleValues.contains("ALL_ITEMS") || userGroupRuleValues.contains("ALL_ITEMS")) {
				//ALL_ITEMS. effectively means the rule doesn't apply
				//using labelled rules. don't append AND before the first rule value
				if (count == 1) {
					labelledValues.append(" 1=1 ");
				} else {
					labelledValues.append(" AND 1=1 ");
				}
			} else if (userRuleValues.isEmpty() && userGroupRuleValues.isEmpty()) {
				//user doesn't have rule value for this rule
				//rule values needed for all rules
				throw new RuntimeException("No values defined for rule: " + rule.getName());
			} else {
				String condition = "";
				String columnName = reportRule.getReportColumn();
				ParameterDataType dataType = rule.getDataType();
				if (!userRuleValues.isEmpty()) {
					List<String> finalUserRuleValues = new ArrayList<>();
					for (String ruleValue : userRuleValues) {
						ruleValue = escapeSql(ruleValue);
						if (dataType.isNumeric()) {
							finalUserRuleValues.add(ruleValue);
						} else {
							finalUserRuleValues.add("'" + ruleValue + "'");
						}
					}
					String finalUserRuleValuesString = StringUtils.join(finalUserRuleValues, ",");
					condition = columnName + " in (" + finalUserRuleValuesString + ")";
				}

				String groupCondition = "";
				if (!userGroupRuleValues.isEmpty()) {
					List<String> finalUserGroupRuleValues = new ArrayList<>();
					for (String ruleValue : userGroupRuleValues) {
						ruleValue = escapeSql(ruleValue);
						if (dataType.isNumeric()) {
							finalUserGroupRuleValues.add(ruleValue);
						} else {
							finalUserGroupRuleValues.add("'" + ruleValue + "'");
						}
					}
					String finalUserGroupRuleValuesString = StringUtils.join(finalUserGroupRuleValues, ",");
					groupCondition = columnName + " in (" + finalUserGroupRuleValuesString + ")";
				}

				if (StringUtils.isNotEmpty(condition)) {
					//rule values defined for user
					if (StringUtils.isNotEmpty(groupCondition)) {
						groupCondition = " OR " + groupCondition;
					}
					condition = condition + groupCondition; // ( user values OR (user group values) )
				} else {
					//no rule values for user. use user group values
					condition = groupCondition;
				}

				condition = " ( " + condition + " ) "; //enclose this rule values in brackets to treat it as a single condition

				//using labelled rules. don't append AND before the first rule value
				// the tmpSb returned by getRuleValues begins with a ',' so we need a .substring(1)
				if (count == 1) {
					labelledValues.append(condition);
				} else {
					labelledValues.append(" AND ").append(condition);
				}
			}
		}

		//replace all occurrences of labelled rule with rule values
		String searchString = "#rules#";
		String replaceString = labelledValues.toString();
		querySql = StringUtils.replaceIgnoreCase(querySql, searchString, replaceString);

		//update sb with new sql
		querySb.replace(0, querySb.length(), querySql);
	}

	/**
	 * Applies rules to static lov values
	 *
	 * @param sb the report source
	 * @throws SQLException
	 */
	private Map<Object, String> applyRulesToStaticLov(Map<Object, String> lovValues) throws SQLException {
		logger.debug("Entering applyRulesToStaticLov");

		if (!useRules) {
			return lovValues;
		}

		Map<Object, String> finalLovValues = new LinkedHashMap<>();

		ReportRuleService reportRuleService = new ReportRuleService();

		int reportId = report.getReportId();
		List<ReportRule> reportRules = reportRuleService.getEffectiveReportRules(reportId);

		RuleValueService ruleValueService = new RuleValueService();

		// for each rule build and add the AND column IN (list) string to the query
		// Note: if we don't have rules for this query, the sb is left untouched
		for (ReportRule reportRule : reportRules) {
			Rule rule = reportRule.getRule();
			List<String> userRuleValues = ruleValueService.getUserRuleValues(user.getUserId(), rule.getRuleId());
			List<String> userGroupRuleValues = new ArrayList<>();
			for (UserGroup userGroup : user.getUserGroups()) {
				userGroupRuleValues.addAll(ruleValueService.getUserGroupRuleValues(userGroup.getUserGroupId(), rule.getRuleId()));
			}

			if (userRuleValues.contains("ALL_ITEMS") || userGroupRuleValues.contains("ALL_ITEMS")) {
				//ALL_ITEMS. effectively means the rule doesn't apply
				//do nothing
			} else {
				for (Entry<Object, String> entry : lovValues.entrySet()) {
					String staticValue = (String) entry.getKey();
					String displayValue = entry.getValue();

					for (String ruleValue : userRuleValues) {
						if (StringUtils.equals(staticValue, ruleValue)) {
							finalLovValues.put(staticValue, displayValue);
						}
					}

					for (String ruleValue : userGroupRuleValues) {
						if (StringUtils.equals(staticValue, ruleValue)) {
							finalLovValues.put(staticValue, displayValue);
						}
					}

				}
			}
		}

		if (finalLovValues.isEmpty()) {
			//use all values
			finalLovValues.putAll(lovValues);
		}

		return finalLovValues;
	}

	/**
	 * Applies direct substitution parameters in the report source
	 */
	private void applyDirectSubstitution() {
		logger.debug("Entering applyDirectSubstitution");

		if (MapUtils.isEmpty(reportParamsMap)) {
			return;
		}

		String querySql = querySb.toString();

		//get and store param identifier order for use with jdbc preparedstatement
		ReportType reportType = report.getReportType();

		//replace direct substitution parameters
		if (Config.getCustomSettings().isEnableDirectParameterSubstitution()
				|| reportType.isJPivot()) {
			RunReportHelper runReportHelper = new RunReportHelper();
			String placeholderPrefix = "!";
			querySql = runReportHelper.performDirectParameterSubstitution(querySql, placeholderPrefix, reportParamsMap);
		}

		//update querySb with new sql
		querySb.replace(0, querySb.length(), querySql);

		logger.debug("Sql query now is: \n{}", querySb);
	}

	/**
	 * Applies parameter placeholders to the report source
	 */
	private void applyParameterPlaceholders() {
		logger.debug("Entering applyParameterPlaceholders");

		if (MapUtils.isEmpty(reportParamsMap)) {
			return;
		}

		String querySql = querySb.toString();

		//get and store param identifier order for use with jdbc preparedstatement
		Map<Integer, JdbcParameterDetails> jdbcParamOrder = new TreeMap<>(); //use treemap so that jdbc params are set in correct order
		ReportType reportType = report.getReportType();
		if (!reportType.isJPivot()) {
			for (Entry<String, ReportParameter> entry : reportParamsMap.entrySet()) {
				String paramName = entry.getKey();
				ReportParameter reportParam = entry.getValue();

				String paramIdentifier = "#" + paramName + "#";
				int index = StringUtils.indexOfIgnoreCase(querySql, paramIdentifier);
				while (index >= 0) {
					List<Object> finalValues = reportParam.getActualParameterValues();
					JdbcParameterDetails jdbcParamDetails = new JdbcParameterDetails(reportParam);
					jdbcParamDetails.setFinalValues(finalValues);
					jdbcParamOrder.put(index, jdbcParamDetails);
					index = StringUtils.indexOfIgnoreCase(querySql, paramIdentifier, index + paramIdentifier.length());
				}
			}

			setXParameterOrder("in", querySql, jdbcParamOrder);
			setXParameterOrder("notin", querySql, jdbcParamOrder);
			setXParameterOrder("equal", querySql, jdbcParamOrder);
			setXParameterOrder("notequal", querySql, jdbcParamOrder);
		}

		//replace literal ? in query with a placeholder, before substituting query parameters with ?
		//to enable show sql to give correct results when ? literals exist in the query
		//https://sourceforge.net/p/art/discussion/352129/thread/ee7c78d4/#2c1f/6b3b
		querySql = StringUtils.replaceIgnoreCase(querySql, "?", QUESTION_PLACEHOLDER);

		//replace jdbc parameter identifiers with ?
		for (Entry<String, ReportParameter> entry : reportParamsMap.entrySet()) {
			String paramName = entry.getKey();
			ReportParameter reportParam = entry.getValue();

			List<Object> actualParameterValues = reportParam.getActualParameterValues();
			if (!actualParameterValues.isEmpty()) {
				String searchString = "#" + paramName + "#";
				String replaceString = StringUtils.repeat("?", ",", actualParameterValues.size());
				querySql = StringUtils.replaceIgnoreCase(querySql, searchString, replaceString);
			}
		}

		//replace x parameter identifiers with ?
		Set<String> xParamDefinitions = new HashSet<>();
		getXParameterDefinitions("in", querySql, xParamDefinitions);
		getXParameterDefinitions("notin", querySql, xParamDefinitions);
		getXParameterDefinitions("equal", querySql, xParamDefinitions);
		getXParameterDefinitions("notequal", querySql, xParamDefinitions);

		for (String xParamDefinition : xParamDefinitions) {
			String xInParamPrefix = "$x{in";
			String xNotInParamPrefix = "$x{notin";
			String xEqualParamPrefix = "$x{equal";
			String xNotEqualParamPrefix = "$x{notequal";
			String xParamPrefix;
			String sqlCommand;
			String sqlNullCommand;

			if (StringUtils.startsWithIgnoreCase(xParamDefinition, xInParamPrefix)) {
				xParamPrefix = xInParamPrefix;
				sqlCommand = "IN";
				sqlNullCommand = "IS NULL";
			} else if (StringUtils.startsWithIgnoreCase(xParamDefinition, xNotInParamPrefix)) {
				xParamPrefix = xNotInParamPrefix;
				sqlCommand = "NOT IN";
				sqlNullCommand = "IS NOT NULL";
			} else if (StringUtils.startsWithIgnoreCase(xParamDefinition, xEqualParamPrefix)) {
				xParamPrefix = xEqualParamPrefix;
				sqlCommand = "=?";
				sqlNullCommand = "IS NULL";
			} else if (StringUtils.startsWithIgnoreCase(xParamDefinition, xNotEqualParamPrefix)) {
				xParamPrefix = xNotEqualParamPrefix;
				sqlCommand = "<>?";
				sqlNullCommand = "IS NOT NULL";
			} else {
				throw new IllegalArgumentException("Unexpected X parameter definition: " + xParamDefinition);
			}

			String beginSeparator = StringUtils.substring(xParamDefinition, xParamPrefix.length()).trim();
			String separator = StringUtils.left(beginSeparator, 1);
			String contents = StringUtils.substringBetween(xParamDefinition, "{", "}");
			String[] components = StringUtils.split(contents, separator);
			String column = components[1].trim();
			String paramName = components[2].trim();

			ReportParameter reportParam = reportParamsMap.get(paramName);
			if (reportParam == null) {
				throw new IllegalArgumentException("X parameter not found: " + paramName);
			}

			String finalString = "";
			List<Object> actualParameterValues = reportParam.getActualParameterValues();
			List<Object> finalValues = new ArrayList<>();

			if (StringUtils.equalsAny(xParamPrefix, xInParamPrefix, xNotInParamPrefix)) {
				StringBuilder finalSb = new StringBuilder();
				finalSb.append("(");
				//https://stackoverflow.com/questions/11512034/does-java-util-list-isempty-check-if-the-list-itself-is-null
				if (actualParameterValues.isEmpty()) {
					finalSb.append("1=1");
				} else {
					//oracle has a maximum list literal count of 1000 items e.g. in IN clauses
					//https://sourceforge.net/p/art/discussion/352129/thread/518e3b41/
					//https://stackoverflow.com/questions/2895342/java-how-can-i-split-an-arraylist-in-multiple-small-arraylists?noredirect=1&lq=1
					//https://stackoverflow.com/questions/4697187/need-for-a-jasperreports-null-parameter-value-to-show-all
					//https://community.jaspersoft.com/blog/tip-how-handle-null-values-when-passing-nothing-single-select-query-parameter-jasper-design
					//https://community.jaspersoft.com/documentation/tibco-jaspersoft-studio-user-guide/v60/using-parameters-queries
					//https://stackoverflow.com/questions/6362112/in-clause-with-null-or-is-null
					//https://stackoverflow.com/questions/8737837/query-sql-server-with-in-null-not-working
					//https://stackoverflow.com/questions/129077/null-values-inside-not-in-clause
					//https://stackoverflow.com/questions/46958023/java-stream-divide-into-two-lists-by-boolean-predicate
					//https://stackoverflow.com/questions/27993604/whats-the-purpose-of-partitioningby
					//https://stackoverflow.com/questions/7230315/how-to-remove-null-from-an-array-in-java
					List<String> conditions = new ArrayList<>();
					Map<Boolean, List<Object>> partitionedValues = actualParameterValues.stream().collect(Collectors.partitioningBy(Objects::isNull));
					List<Object> nullValues = partitionedValues.get(true);
					List<Object> nonNullValues = partitionedValues.get(false);
					if (!nullValues.isEmpty()) {
						String condition = column + " " + sqlNullCommand;
						conditions.add(condition);
					}

					finalValues = nonNullValues;

					final int MAX_LITERAL_COUNT = 1000;
					List<List<Object>> listParts = ListUtils.partition(nonNullValues, MAX_LITERAL_COUNT);
					for (List<Object> listPart : listParts) {
						String condition = column + " " + sqlCommand
								+ "(" + StringUtils.repeat("?", ",", listPart.size()) + ")";
						conditions.add(condition);
					}
					finalSb.append(StringUtils.join(conditions, " OR "));
				}
				finalSb.append(")");
				finalString = finalSb.toString();
			} else if (StringUtils.equalsAny(xParamPrefix, xEqualParamPrefix, xNotEqualParamPrefix)) {
				Object value = actualParameterValues.get(0);
				if (value == null) {
					finalString = column + " " + sqlNullCommand;
				} else {
					finalString = column + sqlCommand;
					finalValues.add(value);
				}
			}

			for (JdbcParameterDetails jdbcParamDetails : jdbcParamOrder.values()) {
				if (jdbcParamDetails.isxParameter()
						&& reportParam.equals(jdbcParamDetails.getReportParam())) {
					jdbcParamDetails.setFinalValues(finalValues);
				}
			}

			querySql = StringUtils.replaceIgnoreCase(querySql, xParamDefinition, finalString);
		}

		jdbcParams.clear();
		for (JdbcParameterDetails jdbcParamDetails : jdbcParamOrder.values()) {
			ReportParameter reportParam = jdbcParamDetails.getReportParam();
			for (Object paramValue : jdbcParamDetails.getFinalValues()) {
				logger.debug("{} - {}", reportParam, paramValue);
				addJdbcParam(paramValue, reportParam.getParameter().getDataType());
			}
		}

		//update querySb with new sql
		querySb.replace(0, querySb.length(), querySql);
	}

	/**
	 * Gets x parameter definitions contained in the sql query
	 *
	 * @param command the x parameter command
	 * @param querySql the query sql
	 * @param xParamDefinitions the object that will be set with found x
	 * parameter definitions
	 */
	private void getXParameterDefinitions(String command, String querySql, Set<String> xParamDefinitions) {
		String xParamPrefix = "$x{" + command;
		int index = StringUtils.indexOfIgnoreCase(querySql, xParamPrefix);
		while (index >= 0) {
			int end = StringUtils.indexOfIgnoreCase(querySql, "}", index + xParamPrefix.length());
			end += 1;
			String xParamDefinition = StringUtils.substring(querySql, index, end);
			xParamDefinitions.add(xParamDefinition);
			index = StringUtils.indexOfIgnoreCase(querySql, xParamPrefix, end);
		}
	}

	/**
	 * Sets the jdbc parameter order of the parameter defined using x syntax
	 *
	 * @param command the x parameter command
	 * @param querySql the query sql
	 * @param jdbcParamOrder the jdbc parameter order map
	 * @throws IllegalArgumentException
	 */
	private void setXParameterOrder(String command, String querySql,
			Map<Integer, JdbcParameterDetails> jdbcParamOrder) throws IllegalArgumentException {

		//search for x-parameter definitions
		//http://community.jaspersoft.com/questions/516502/x-and-p
		//http://community.jaspersoft.com/documentation/tibco-jaspersoft-studio-user-guide/v60/using-parameters-queries
		//https://reportserver.net/en/guides/admin/chapters/using-parameters/
		//http://community.jaspersoft.com/questions/532261/x-clause-define-columnname-expression
		String xParamPrefix = "$x{" + command;
		int index = StringUtils.indexOfIgnoreCase(querySql, xParamPrefix);
		while (index >= 0) {
			int end = StringUtils.indexOfIgnoreCase(querySql, "}", index + xParamPrefix.length());
			end += 1;
			String xParamDefinition = StringUtils.substring(querySql, index, end);
			String beginSeparator = StringUtils.substring(xParamDefinition, xParamPrefix.length()).trim();
			String separator = StringUtils.left(beginSeparator, 1);
			String contents = StringUtils.substringBetween(xParamDefinition, "{", "}");
			String[] components = StringUtils.split(contents, separator);
			String paramName = components[2].trim();

			ReportParameter reportParam = reportParamsMap.get(paramName);
			if (reportParam == null) {
				throw new IllegalArgumentException("X parameter not found: " + paramName);
			}

			JdbcParameterDetails jdbcParamDetails = new JdbcParameterDetails(reportParam);
			jdbcParamDetails.setxParameter(true);
			jdbcParamOrder.put(index, jdbcParamDetails);

			index = StringUtils.indexOfIgnoreCase(querySql, xParamPrefix, end);
		}
	}

	/**
	 * Adds parameters to be used when executing the report via jdbc. Date
	 * parameters are converted to the appropriate java.sql.Date or
	 * java.sql.Timestamp
	 *
	 * @param paramValue the parameter value
	 * @param paramDataType the parameter data type
	 */
	private void addJdbcParam(Object paramValue, ParameterDataType paramDataType) {
		if (paramValue instanceof Date) {
			Date dateValue = (Date) paramValue;
			switch (paramDataType) {
				case Date:
					jdbcParams.add(DatabaseUtils.toSqlDate(dateValue));
					break;
				case Time:
					jdbcParams.add(DatabaseUtils.toSqlTime(dateValue));
					break;
				default:
					jdbcParams.add(DatabaseUtils.toSqlTimestamp(dateValue));
					break;
			}
		} else {
			jdbcParams.add(paramValue);
		}
	}

	/**
	 * Applies dynamic recipients to the report source
	 */
	private void applyDynamicRecipient() {
		logger.debug("Entering applyDynamicRecipient");

		String querySql = querySb.toString();

		if (recipientFilterPresent) {
			//replace #recipient# label with recipient values
			if (recipientColumn != null && recipientId != null) {
				String recipientValue;
				if (StringUtils.equalsIgnoreCase(recipientIdType, "number") && NumberUtils.isCreatable(recipientId)) {
					//don't quote recipient id
					recipientValue = recipientId;
				} else {
					//quote recipient id
					recipientValue = "'" + escapeSql(recipientId) + "'";
				}
				String replaceString = recipientColumn + "=" + recipientValue;

				querySql = StringUtils.replaceIgnoreCase(querySql, RECIPIENT_LABEL, replaceString);
			}
		}

		//ignore #recipient# label if it is still there
		String replaceString = "1=1";
		querySql = StringUtils.replaceIgnoreCase(querySql, RECIPIENT_LABEL, replaceString);

		//update querySb with new sql
		querySb.replace(0, querySb.length(), querySql);
	}

	/**
	 * Applies self service fields
	 *
	 * @throws SQLException
	 */
	private void applySelfServiceFields() throws SQLException {
		logger.debug("Entering applySelfServiceFields");

		try {
			if (!report.isViewOrSelfService()) {
				return;
			}

			Datasource datasource = report.getDatasource();
			if (datasource == null) {
				throw new RuntimeException("Datasource not specified");
			}

			String options = datasource.getOptions();
			DatasourceOptions datasourceOptions;
			if (StringUtils.isBlank(options)) {
				datasourceOptions = new DatasourceOptions();
			} else {
				datasourceOptions = ArtUtils.jsonToObject(options, DatasourceOptions.class);
			}
			DatabaseProtocol databaseProtocol = datasource.getEffectiveDatabaseProtocol();

			ViewOptions viewOptions = report.getGeneralOptions().getView();
			if (viewOptions == null) {
				viewOptions = new ViewOptions();
			}

			final String COLUMNS_PLACEHOLDER = "#columns#";
			final String CONDITION_PLACEHOLDER = "#condition#";
			final String LIMIT_PLACEHOLDER = "#limitClause#";

			String querySql = querySb.toString();

			if (!StringUtils.containsIgnoreCase(querySql, COLUMNS_PLACEHOLDER)) {
				throw new RuntimeException(COLUMNS_PLACEHOLDER + " placeholder not found");
			} else if (!StringUtils.containsIgnoreCase(querySql, CONDITION_PLACEHOLDER)) {
				throw new RuntimeException(CONDITION_PLACEHOLDER + " placeholder not found");
			} else if (!StringUtils.containsIgnoreCase(querySql, LIMIT_PLACEHOLDER)) {
				throw new RuntimeException(LIMIT_PLACEHOLDER + " placeholder not found");
			}

			String columnsString;
			String conditionString = null;
			String selfServiceOptionsString = report.getSelfServiceOptions();
			if (StringUtils.isBlank(selfServiceOptionsString) || useViewColumns) {
				columnsString = viewOptions.getColumns();
			} else {
				SelfServiceOptions selfServiceOptions = ArtUtils.jsonToObjectIgnoreUnknown(selfServiceOptionsString, SelfServiceOptions.class);
				columnsString = selfServiceOptions.getColumnsString();

				Object jqueryRule = selfServiceOptions.getJqueryRule();
				if (jqueryRule != null && StringUtils.containsIgnoreCase(querySql, CONDITION_PLACEHOLDER)) {
					SqlQueryBuilderFactory sqlQueryBuilderFactory = new SqlQueryBuilderFactory();
					SqlBuilder sqlBuilder = sqlQueryBuilderFactory.builder();
					String jqueryRuleString = ArtUtils.objectToJson(jqueryRule);
					SqlQueryResult sqlQueryResult = sqlBuilder.build(jqueryRuleString);
					conditionString = sqlQueryResult.getQuery();
					List<Object> values = sqlQueryResult.getParams();
					for (Object value : values) {
						jdbcParams.add(value);
					}
				}
			}

			if (columnsString == null) {
				columnsString = "*";
			}

			querySql = StringUtils.replaceIgnoreCase(querySql, COLUMNS_PLACEHOLDER, columnsString);

			if (conditionString == null) {
				conditionString = "1=1";
			}
			querySql = StringUtils.replaceIgnoreCase(querySql, CONDITION_PLACEHOLDER, conditionString);

			String reportLimitClause = viewOptions.getLimitClause();
			String datasourceLimitClause = datasourceOptions.getLimitClause();
			String databaseLimitClause = databaseProtocol.limitClause();
			String limitClause = reportLimitClause;
			if (StringUtils.isBlank(limitClause)) {
				limitClause = datasourceLimitClause;
			}
			if (StringUtils.isBlank(limitClause)) {
				limitClause = databaseLimitClause;
			}

			Integer reportLimit = viewOptions.getLimit();
			Integer datasourceLimit = datasourceOptions.getLimit();
			Integer runLimit = report.getLimit();

			Integer finalLimit = null;
			if (limit == null) {
				if (runLimit == null) {
					if (report.getReportType() == ReportType.View) {
						finalLimit = reportLimit;
						if (finalLimit == null) {
							finalLimit = datasourceLimit;
						}

						if (finalLimit == null) {
							final Integer DEFAULT_VIEW_LIMIT = 10;
							finalLimit = DEFAULT_VIEW_LIMIT;
						}
					}
				} else {
					finalLimit = runLimit;
				}
			} else {
				finalLimit = limit;
			}

			if (finalLimit == null || finalLimit < 0) {
				querySql = StringUtils.removeIgnoreCase(querySql, LIMIT_PLACEHOLDER);
			} else {
				String finalLimitClause = StringUtils.replace(limitClause, "{0}", String.valueOf(finalLimit));
				querySql = StringUtils.replaceIgnoreCase(querySql, LIMIT_PLACEHOLDER, finalLimitClause);
			}

			//update querySb with new sql
			querySb.replace(0, querySb.length(), querySql);
		} catch (IOException ex) {
			throw new SQLException(ex);
		}
	}

	/**
	 * Runs the report using a forward only cursor
	 *
	 * @throws java.sql.SQLException
	 */
	public void execute() throws SQLException {
		execute(ResultSet.TYPE_FORWARD_ONLY);
	}

	/**
	 * Runs the report using the given resultset type
	 *
	 * @param resultSetType the resultset type
	 * @throws java.sql.SQLException
	 */
	public void execute(int resultSetType) throws SQLException {
		boolean overrideUseRules = false;
		boolean newUseRules = false;
		execute(resultSetType, overrideUseRules, newUseRules);
	}

	/**
	 * Runs the report using a forward only cursor and the given use rules
	 * setting
	 *
	 * @param newUseRules the use rules setting to use
	 * @throws java.sql.SQLException
	 */
	public void execute(boolean newUseRules) throws SQLException {
		boolean overrideUseRules = true;
		execute(ResultSet.TYPE_FORWARD_ONLY, overrideUseRules, newUseRules);
	}

	/**
	 * Runs the report using the given resultset type and new use rules setting
	 *
	 * @param resultSetType the resultset type to use
	 * @param overrideUseRules whether to override the report's use rule setting
	 * @param newUseRules the new use rules setting
	 * @throws java.sql.SQLException
	 */
	public void execute(int resultSetType, boolean overrideUseRules, boolean newUseRules)
			throws SQLException {

		if (report == null) {
			throw new RuntimeException("report is null");
		}

		int reportId = report.getReportId();
		long currentRunning = Config.getRunningReportCount(reportId);
		int maxRunning = report.getMaxRunning();
		int maxRunningPerUser = report.getMaxRunningPerUser();
		long currentRunningForUser;
		if (user == null) {
			currentRunningForUser = 0;
		} else {
			currentRunningForUser = Config.getRunningReportCount(reportId, user.getUserId());
		}

		if ((maxRunning > 0 && currentRunning >= maxRunning)
				|| (maxRunningPerUser > 0 && currentRunningForUser >= maxRunningPerUser)) {
			throw new RuntimeException("Maximum reports running for this report. Try again later.");
		} else {
			localRunId = ArtUtils.getUniqueId(reportId);

			ReportRunDetails reportRunDetails = new ReportRunDetails();
			reportRunDetails.setReport(report);
			reportRunDetails.setJob(job);
			User runDetailsUser;
			if (user == null) {
				runDetailsUser = new User();
			} else {
				runDetailsUser = user;
			}
			reportRunDetails.setUser(runDetailsUser);
			reportRunDetails.setRunId(localRunId);

			Config.addRunningReport(reportRunDetails);
		}

		useRules = report.isUsesRules();

		//override use rules setting if required, especially for lovs
		if (overrideUseRules) {
			useRules = newUseRules;
		}

		//Get the SQL String with rules, inline, multi params and tags already applied.
		processReportSource();

		//don't execute sql source for report types that don't have runnable sql
		ReportType reportType = report.getReportType();
		if (!reportType.isJdbcRunnableByArt() || groovyData != null) {
			return;
		}

		//use dynamic datasource if so configured
		if (reportType == ReportType.LovDynamic && !report.isLovUseDynamicDatasource()) {
			Datasource reportDatasource = report.getDatasource();
			if (reportDatasource != null) {
				connQuery = DbConnections.getConnection(reportDatasource.getDatasourceId());
			}
		} else {
			RunReportHelper runReportHelper = new RunReportHelper();
			connQuery = runReportHelper.getEffectiveReportConnection(report, reportParamsMap);
		}

		if (connQuery == null) {
			if (reportType.isXDocReport()) {
				//xdocreport may only have template queries
				return;
			} else {
				throw new RuntimeException("Datasource not found");
			}
		}

		int fetchSize = report.getFetchSize();
		boolean applyFetchSize = false;

		Datasource datasource = report.getDatasource();
		String jdbcUrl = null;
		if (datasource != null) {
			jdbcUrl = datasource.getUrl();
		}

		if (fetchSize > 0) {
			applyFetchSize = true;
			if (StringUtils.startsWith(jdbcUrl, "jdbc:postgresql")) {
				postgreSqlFetchSizeApplied = true;
				connQuery.setAutoCommit(false);
			} else if (StringUtils.startsWith(jdbcUrl, "jdbc:mysql")) {
				fetchSize = Integer.MIN_VALUE;
			}
		}

		String querySql = querySb.toString();

		//https://stackoverflow.com/questions/45576157/does-resultset-returned-by-redshift-supports-resetting-iterator
		//https://stackoverflow.com/questions/40496316/sqlfeaturenotsupportedexception-on-amazon-redshift
		//http://www.java2s.com/Code/Java/Database-SQL-JDBC/DeterminingIfaDatabaseSupportsScrollableResultSets.htm
		DatabaseMetaData dmd = connQuery.getMetaData();
		logger.debug("dmd.supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE) = {}", dmd.supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE));
		if (!dmd.supportsResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE)) {
			if (reportType.requiresScrollableResultSet()) {
				throw new RuntimeException("Report type requires scrollable resultset but database doesn't support this");
			}
			resultSetType = ResultSet.TYPE_FORWARD_ONLY;
		}

		boolean bigQueryStarschema = false;
		if (StringUtils.startsWith(jdbcUrl, "jdbc:BQDriver:")) {
			bigQueryStarschema = true;
		}

		//https://github.com/jonathanswenson/bqjdbc/issues/61
		//https://stackoverflow.com/questions/3271249/difference-between-statement-and-preparedstatement
		//https://stackoverflow.com/questions/45972001/when-is-it-better-to-use-a-statement-over-a-preparedstatement
		boolean usingPlainStatement = false;
		if (bigQueryStarschema && jdbcParams.isEmpty()) {
			usingPlainStatement = true;
			psQuery = connQuery.createStatement(resultSetType, ResultSet.CONCUR_READ_ONLY);
		} else {
			psQuery = connQuery.prepareStatement(querySql, resultSetType, ResultSet.CONCUR_READ_ONLY);
		}

		if (applyFetchSize) {
			psQuery.setFetchSize(fetchSize);
		}

		if (!usingPlainStatement) {
			Object[] paramValues = jdbcParams.toArray(new Object[0]);

			DatabaseUtils.setValues((PreparedStatement) psQuery, paramValues);
		}

		//https://www.programcreek.com/java-api-examples/?class=java.sql.Statement&method=setQueryTimeout
		//https://docs.oracle.com/javase/7/docs/api/java/sql/Statement.html#setQueryTimeout(int)
		Integer queryTimeoutSeconds = report.getGeneralOptions().getQueryTimeoutSeconds();
		if (queryTimeoutSeconds != null) {
			if (queryTimeoutSeconds < 0) {
				throw new RuntimeException("Invalid query timeout value: " + queryTimeoutSeconds);
			} else {
				psQuery.setQueryTimeout(queryTimeoutSeconds);
			}
		}

		if (StringUtils.isNotBlank(runId)) {
			ReportRunDetails reportRunDetails = new ReportRunDetails();
			reportRunDetails.setReport(report);
			reportRunDetails.setJob(job);
			reportRunDetails.setUser(user);
			reportRunDetails.setRunId(runId);
			reportRunDetails.setStartTime(new Date());

			Config.addRunningQuery(reportRunDetails, psQuery);
		}

		if (usingPlainStatement) {
			psQuery.execute(querySql);
		} else {
			((PreparedStatement) psQuery).execute();
		}
	}

	/**
	 * Returns the query sql
	 *
	 * @return
	 */
	public String getQuerySql() {
		return querySb.toString();
	}

	/**
	 * Returns the resultset produced for this report
	 *
	 * @return the resultset produced for this report
	 * @throws SQLException
	 */
	public ResultSet getResultSet() throws SQLException {
		if (psQuery == null) {
			return null;
		}

		ResultSet rs = psQuery.getResultSet();
		updateCount = psQuery.getUpdateCount();

		int displayResultset = report.getDisplayResultset();
		if (displayResultset == -1) {
			//use the select statement. will use first select statement. having several selects isn't useful
			if (rs == null) {
				//first statement was not a select statement. iterate through other resultsets until we get a select statement
				while (psQuery.getMoreResults() != false || psQuery.getUpdateCount() != -1) {
					rs = psQuery.getResultSet();
					updateCount = psQuery.getUpdateCount();
					if (rs != null) {
						//we have found a select statement (resultset object)
						break;
					}
				}
			}
		} else if (displayResultset == -2) {
			//use last statement. driver must be jdbc 3.0 compliant (and above)
			//otherwise this will result in endless loop
			final int MAX_LOOPS = 20;
			int count = 0;
			while (psQuery.getMoreResults(Statement.KEEP_CURRENT_RESULT) != false
					|| psQuery.getUpdateCount() != -1) {

				count++;

				if (rs != null) {
					rs.close();
				}
				rs = psQuery.getResultSet();
				updateCount = psQuery.getUpdateCount();

				if (count > MAX_LOOPS) {
					logger.warn("MAX_LOOPS reached. Report Id = {}", report.getReportId());
					break;
				}
			}
		} else if (displayResultset > 1) {
			//use specific statment. statement 2, 3, etc. statement 1 already retrieved by initial getresultset call
			int count = 1;
			while (psQuery.getMoreResults() != false || psQuery.getUpdateCount() != -1) {
				count++;
				rs = psQuery.getResultSet();
				updateCount = psQuery.getUpdateCount();

				if (count == displayResultset) {
					break;
				}
			}
		}

		return rs;
	}

	/**
	 * Returns the number of rows updated by this report
	 *
	 * @return the number of rows updated by this report
	 * @throws SQLException
	 */
	public int getUpdateCount() throws SQLException {
		return updateCount;
	}

	/**
	 * Runs this report and returns the generated resultset
	 *
	 * @return the generated resultset for this report
	 * @throws SQLException
	 */
	public ResultSet executeQuery() throws SQLException {
		return executeQuery(false, false);
	}

	/**
	 * Runs this report and returns the generated resultset, using the given use
	 * rules setting
	 *
	 * @param newUseRules the use rules setting to use
	 * @return the generated resultset
	 * @throws SQLException
	 */
	public ResultSet executeQuery(boolean newUseRules) throws SQLException {
		return executeQuery(true, newUseRules);
	}

	/**
	 * Runs this report and returns the generated resultset, using the given use
	 * rules setting and using a forward only cursor
	 *
	 * @param overrideUseRules whether to override the report's use rules
	 * setting
	 * @param newUseRules the new use rules setting to use
	 * @return the generated resultset
	 * @throws SQLException
	 */
	public ResultSet executeQuery(boolean overrideUseRules, boolean newUseRules) throws SQLException {
		execute(ResultSet.TYPE_FORWARD_ONLY, overrideUseRules, newUseRules);
		return getResultSet();
	}

	/**
	 * Runs an lov report and returns the lov values (value and label)
	 *
	 * @return the lov values
	 * @throws SQLException
	 */
	public Map<String, String> getLovValues() throws SQLException {
		return getLovValues(false, false);
	}

	/**
	 * Runs an lov report and returns the lov values, using the given use rules
	 * setting
	 *
	 * @param newUseRules the use rules setting to use
	 * @return lov values
	 * @throws SQLException
	 */
	public Map<String, String> getLovValues(boolean newUseRules) throws SQLException {
		return getLovValues(true, newUseRules);
	}

	/**
	 * Runs an lov report and returns the lov values, using the given use rules
	 * setting
	 *
	 * @param overrideUseRules whether to override the report's use rules
	 * setting
	 * @param newUseRules the new use rules setting
	 * @return lov values
	 * @throws SQLException
	 */
	public Map<String, String> getLovValues(boolean overrideUseRules, boolean newUseRules)
			throws SQLException {

		Map<String, String> lovValues = new LinkedHashMap<>();

		Map<Object, String> lovValuesAsObjects = getLovValuesAsObjects(overrideUseRules, newUseRules);

		for (Entry<Object, String> entry : lovValuesAsObjects.entrySet()) {
			Object dataValue = entry.getKey();
			String displayValue = entry.getValue();

			String stringValue;
			if (dataValue instanceof Date) {
				Date dateValue = (Date) dataValue;
				stringValue = ArtUtils.isoDateTimeMillisecondsFormatter.format(dateValue);
			} else {
				stringValue = String.valueOf(dataValue);
			}

			lovValues.put(stringValue, displayValue);
		}

		return lovValues;
	}

	/**
	 * Runs an lov report and returns the lov values (value and label)
	 *
	 * @return the lov values
	 * @throws SQLException
	 */
	public Map<Object, String> getLovValuesAsObjects() throws SQLException {
		return getLovValuesAsObjects(false, false);
	}

	/**
	 * Runs an lov report and returns the lov values (value and label), using
	 * the given use rules setting
	 *
	 * @param newUseRules the use rules setting to use
	 * @return lov values
	 * @throws SQLException
	 */
	public Map<Object, String> getLovValuesAsObjects(boolean newUseRules) throws SQLException {
		return getLovValuesAsObjects(true, newUseRules);
	}

	/**
	 * Runs an lov report and returns the lov values (value and label), using
	 * the given use rules setting
	 *
	 * @param overrideUseRules whether to override the report's use rules
	 * setting
	 * @param newUseRules the new use rules setting
	 * @return lov values
	 * @throws SQLException
	 */
	public Map<Object, String> getLovValuesAsObjects(boolean overrideUseRules, boolean newUseRules)
			throws SQLException {

		Map<Object, String> lovValues = new LinkedHashMap<>();

		execute(ResultSet.TYPE_FORWARD_ONLY, overrideUseRules, newUseRules);

		ReportType reportType = report.getReportType();
		if (reportType == ReportType.LovStatic) {
			//static lov. values coming from static values defined in sql source
			String items = querySb.toString();
			String lines[] = items.split("\\r?\\n"); //split by newline
			for (String line : lines) {
				String[] values = line.trim().split("\\|"); //split by |
				String dataValue = values[0];
				String displayValue = dataValue;
				if (values.length > 1) {
					displayValue = values[1];
				}
				lovValues.put(dataValue, displayValue);
			}

			//apply rules
			lovValues = applyRulesToStaticLov(lovValues);
		} else if (reportType == ReportType.LovDynamic) {
			//dynamic lov. values coming from sql query
			ResultSet rs = getResultSet();
			if (rs != null) {
				try {
					int columnCount = rs.getMetaData().getColumnCount();
					while (rs.next()) {
						//use getObject(). for dates, using getString() will return
						//different strings for different databases and drivers
						//https://stackoverflow.com/questions/8229727/how-to-get-jdbc-date-format
						//https://stackoverflow.com/questions/14700962/default-jdbc-date-format-when-reading-date-as-a-string-from-resultset
						Object dataValue = rs.getObject(1);
						String displayValue;
						if (columnCount > 1) {
							displayValue = rs.getString(2);
						} else {
							displayValue = rs.getString(1);
						}

						lovValues.put(dataValue, displayValue);
					}
				} finally {
					DatabaseUtils.close(rs);
				}
			} else if (groovyData != null) {
				//https://stackoverflow.com/questions/22768663/how-to-know-if-a-java-object-is-a-list
				List<? extends Object> dataList;
				if (groovyData instanceof List) {
					@SuppressWarnings("unchecked")
					List<? extends Object> dataListTemp = (List<? extends Object>) groovyData;
					dataList = dataListTemp;
				} else {
					List<Object> dataListTemp = new ArrayList<>();
					dataListTemp.add(groovyData);
					dataList = dataListTemp;
				}

				if (CollectionUtils.isNotEmpty(dataList)) {
					for (Object row : dataList) {
						if (row instanceof GroovyRowResult) {
							GroovyRowResult rowResult = (GroovyRowResult) row;
							int columnCount = rowResult.size();
							Object dataValue = rowResult.getAt(0);
							String displayValue;
							if (columnCount > 1) {
								displayValue = String.valueOf(rowResult.getAt(1));
							} else {
								displayValue = String.valueOf(dataValue);
							}
							lovValues.put(dataValue, displayValue);
						} else if (row instanceof DynaBean) {
							DynaBean rowBean = (DynaBean) row;
							DynaProperty[] columns = rowBean.getDynaClass().getDynaProperties();
							int columnCount = columns.length;
							String columnOneName = columns[0].getName();
							Object dataValue = rowBean.get(columnOneName);
							String displayValue;
							if (columnCount > 1) {
								String columnTwoName = columns[1].getName();
								displayValue = String.valueOf(rowBean.get(columnTwoName));
							} else {
								displayValue = String.valueOf(dataValue);
							}
							lovValues.put(dataValue, displayValue);
						} else if (row instanceof Map) {
							@SuppressWarnings("unchecked")
							Map<? extends Object, String> rowMap = (Map<? extends Object, String>) row;
							lovValues.putAll(rowMap);
						} else {
							String displayValue = String.valueOf(row);
							lovValues.put(row, displayValue);
						}
					}
				}
			}
		}

		return lovValues;
	}

	/**
	 * Releases resources (mainly, return the connection to the target database
	 * to the connection pool). IT IS MANDATORY TO CALL THIS AFTER THE execute()
	 * IN ORDER TO RETURN THE CONNECTION TO THE POOL.
	 */
	public void close() {
		if (postgreSqlFetchSizeApplied) {
			try {
				connQuery.setAutoCommit(true);
			} catch (SQLException ex) {
				logger.error("Error", ex);
			}
		}

		if (StringUtils.isNotBlank(runId)) {
			Config.removeRunningQuery(runId);
		}

		if (StringUtils.isNotBlank(localRunId)) {
			Config.removeRunningReport(localRunId);
		}

		DatabaseUtils.close(psQuery, connQuery);
	}

	/**
	 * Escape the ' char in a parameter value
	 *
	 * @param value the value
	 * @return the escaped value
	 */
	private String escapeSql(String value) {
		String escaped = null;
		if (value != null) {
			escaped = StringUtils.replace(value, "'", "''");
		}
		return escaped;
	}

	/**
	 * Applies dynamic sql to the report source
	 */
	private void applyDynamicSql() {
		logger.debug("Entering applyDynamicSql");

		String element = "IF";

		String querySql = querySb.toString();

		// XmlInfo stores the text between a tag as well as
		// the start and end position of the tag
		XmlInfo xinfo = XmlParser.getXmlElementInfo(querySql, element, 0);

		while (xinfo != null) {
			String xmlText = xinfo.getText(); // stores xml code between the IF element

			String exp1 = XmlParser.getXmlElementValue(xmlText, "EXP1"); // get text between the EXP1 element, returns null if the element does not exists
			String op = XmlParser.getXmlElementValue(xmlText, "OP");
			String exp2 = XmlParser.getXmlElementValue(xmlText, "EXP2");

			//get effective operand/operator values in case operand/operator uses a report parameter
			String exp1Value = getDynamicSqlExpressionValue(exp1);
			String exp2Value = getDynamicSqlExpressionValue(exp2);
			String opValue = getDynamicSqlExpressionValue(op);

			String finalElementValue;
			if (evaluateIF(exp1Value, opValue, exp2Value)) {
				finalElementValue = XmlParser.getXmlElementValue(xmlText, "TEXT");
			} else {
				finalElementValue = XmlParser.getXmlElementValue(xmlText, "ELSETEXT");
			}

			if (finalElementValue == null) {
				finalElementValue = "";
			}

			// replace the code in the SQL with the text
			// +3 is to handle </ and > chars around the closing tag
			querySb.replace(xinfo.getStart(), xinfo.getEnd() + element.length() + 3, finalElementValue);

			// check next element
			xinfo = XmlParser.getXmlElementInfo(querySb.toString(), element, xinfo.getStart() + finalElementValue.length());
		}
	}

	/**
	 * Returns a dynamic expression value
	 *
	 * @param exp the expression
	 * @return the value
	 */
	private String getDynamicSqlExpressionValue(String exp) {
		String expValue;
		if (StringUtils.startsWith(exp, "#") && StringUtils.endsWith(exp, "#") && StringUtils.length(exp) > 2) {
			//expression is a report parameter. get the value to use
			if (reportParamsMap == null) {
				throw new RuntimeException("Report parameters not available");
			}

			String paramName = exp.substring(1, exp.length() - 1);
			ReportParameter reportParam = reportParamsMap.get(paramName);
			if (reportParam == null) {
				throw new RuntimeException("Parameter not found: " + paramName);
			}

			Object actualParameterValue = reportParam.getEffectiveActualParameterValue();
			if (reportParam.getParameter().getParameterType() == ParameterType.MultiValue) {
				expValue = StringUtils.join(actualParameterValue, ",");
			} else if (actualParameterValue instanceof Date) {
				Date dateValue = (Date) actualParameterValue;
				expValue = ArtUtils.isoDateTimeMillisecondsFormatter.format(dateValue);
			} else {
				expValue = String.valueOf(actualParameterValue);
			}
		} else {
			//expression isn't a report parameter. use as is
			expValue = exp;
		}

		return expValue;
	}

	/**
	 * Evaluates the IF element in a dynamic sql expression
	 */
	private boolean evaluateIF(String exp1, String op, String exp2) {
		if (exp1 == null) {
			exp1 = "";
		}

		if (exp2 == null) {
			exp2 = "";
		}

		//enable case sensitive comparisons
		String csExp1 = exp1;
		String csExp2 = exp2;

		//trim and make operands lowercase for case insensitive comparisons
		exp1 = exp1.trim().toLowerCase(Locale.ENGLISH);
		exp2 = exp2.trim().toLowerCase(Locale.ENGLISH);

		//evaluate conditions
		if (StringUtils.equalsIgnoreCase(op, "eq") || StringUtils.equalsIgnoreCase(op, "equals")) { // -- equals
			return exp1.equals(exp2);

		} else if (StringUtils.equalsIgnoreCase(op, "neq") || StringUtils.equalsIgnoreCase(op, "not equals")) { // -- not equals
			return !exp1.equals(exp2);

		} else if (StringUtils.equalsIgnoreCase(op, "la")) { //less than  (alphanumeric)
			return (exp1.compareTo(exp2) < 0 ? true : false);

		} else if (StringUtils.equalsIgnoreCase(op, "ga")) { //greater than (alphanumeric)
			return (exp1.compareTo(exp2) > 0 ? true : false);

		} else if (StringUtils.equalsIgnoreCase(op, "ln")) { //less than (numeric)
			double e1 = Double.parseDouble(exp1);
			double e2 = Double.parseDouble(exp2);
			return (e1 < e2 ? true : false);

		} else if (StringUtils.equalsIgnoreCase(op, "gn")) { //greater than (numeric)
			double e1 = Double.parseDouble(exp1);
			double e2 = Double.parseDouble(exp2);
			return (e1 > e2 ? true : false);

		} else if (StringUtils.equalsIgnoreCase(op, "is blank") || StringUtils.equalsIgnoreCase(op, "is null")) { //is empty string. "is null" for backward compatibility
			return exp1.equals("");

		} else if (StringUtils.equalsIgnoreCase(op, "is not blank") || StringUtils.equalsIgnoreCase(op, "is not null")) { //is not empty string. "is not null" for backward compatibility			
			return !exp1.equals("");

		} else if (StringUtils.equalsIgnoreCase(op, "starts with")) {
			return exp1.startsWith(exp2);

		} else if (StringUtils.equalsIgnoreCase(op, "ends with")) {
			return exp1.endsWith(exp2);

		} else if (StringUtils.equalsIgnoreCase(op, "contains")) {
			return (exp1.contains(exp2) ? true : false);

		} else if (StringUtils.equalsIgnoreCase(op, "eq cs") || StringUtils.equalsIgnoreCase(op, "equals cs")) { // equals case sensitive
			return csExp1.equals(csExp2);

		} else if (StringUtils.equalsIgnoreCase(op, "neq cs") || StringUtils.equalsIgnoreCase(op, "not equals cs")) { // not equals case sensitive
			return !csExp1.equals(csExp2);

		} else if (StringUtils.equalsIgnoreCase(op, "la cs")) { // less than (alphanumeric) case sensitive
			return (csExp1.compareTo(csExp2) < 0 ? true : false);

		} else if (StringUtils.equalsIgnoreCase(op, "ga cs")) { // great than (alphanumeric) case sensitive
			return (csExp1.compareTo(csExp2) > 0 ? true : false);

		} else if (StringUtils.equalsIgnoreCase(op, "starts with cs")) { // starts with case sensitive
			return csExp1.startsWith(csExp2);

		} else if (StringUtils.equalsIgnoreCase(op, "ends with cs")) { // ends with case sensitive
			return csExp1.endsWith(csExp2);

		} else if (StringUtils.equalsIgnoreCase(op, "contains cs")) { // contains case sensitive
			return (csExp1.contains(csExp2) ? true : false);

		} else {
			throw new IllegalArgumentException("Unknown operand: " + op);
		}
	}

	/**
	 * Applies :TAGS in the report source. This includes :USERNAME, :TIME, :DATE
	 *
	 */
	private void applyTags() {
		logger.debug("Entering applyTags");

		String querySql = querySb.toString();

		//replace :USERNAME: with currently logged in user's username
		if (user != null) {
			String username = user.getUsername();
			querySql = StringUtils.replaceIgnoreCase(querySql, ":username:", "'" + username + "'");
		}

		//replace :DATE: with current date
		Date now = new Date();

		SimpleDateFormat dateFormatter = new SimpleDateFormat(ArtUtils.ISO_DATE_FORMAT);
		String date = dateFormatter.format(now);
		querySql = StringUtils.replaceIgnoreCase(querySql, ":date:", "'" + date + "'"); //postgresql has casting syntax like ::date

		//replace :TIME: with current date and time
		SimpleDateFormat timeFormatter = new SimpleDateFormat(ArtUtils.ISO_DATE_TIME_SECONDS_FORMAT);
		String time = timeFormatter.format(now);
		querySql = StringUtils.replaceIgnoreCase(querySql, ":time:", "'" + time + "'");

		//replace :reportId:
		querySql = StringUtils.replace(querySql, ":reportId:", String.valueOf(report.getReportId()));

		//replace :jobId:
		if (job != null) {
			querySql = StringUtils.replace(querySql, ":jobId:", String.valueOf(job.getJobId()));
		}

		//update querySb with new sql
		querySb.replace(0, querySb.length(), querySql);

		logger.debug("Sql query now is: \n{}", querySb);
	}

	/**
	 * Generates the final sql that is executed
	 *
	 * @param sqlQuery the sql query with parameter placeholders
	 * @param parameters the parameter values
	 * @return final sql
	 */
	private String generateFinalSql(String sqlQuery, Object... parameters) {
		//https://stackoverflow.com/questions/2683214/get-query-from-java-sql-preparedstatement

		logger.debug("Entering generateFinalSql");

		String[] parts = sqlQuery.split("\\?");
		StringBuilder sb = new StringBuilder();

		// This might be wrong if some '?' are used as litteral '?'
		for (int i = 0; i < parts.length; i++) {
			String part = parts[i];
			sb.append(part);
			if (i < parameters.length) {
				sb.append(formatParameter(parameters[i]));
			}
		}

		return sb.toString();
	}

	/**
	 * Formats a parameter value for use in the final sql string
	 *
	 * @param value the parameter value
	 * @return the formatted value
	 */
	private String formatParameter(Object value) {
		if (value == null) {
			return "NULL";
		} else {
			if (value instanceof String) {
				return "'" + ((String) value).replace("'", "''") + "'";
			} else if (value instanceof Timestamp) {
				return "'" + ArtUtils.isoDateTimeMillisecondsFormatter.format(value) + "'";
			} else if (value instanceof java.sql.Date) {
				return "'" + ArtUtils.isoDateFormatter.format(value) + "'";
			} else if (value instanceof Time) {
				return "'" + String.valueOf(value) + "'";
			} else if (value instanceof Boolean) {
				Boolean booleanValue = (Boolean) value;
				return booleanValue ? "1" : "0";
			} else {
				return String.valueOf(value);
			}
		}
	}
}
