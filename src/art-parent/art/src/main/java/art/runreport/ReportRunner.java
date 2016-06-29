/*
 * Copyright 2001-2016 Enrico Liboni <eliboni@users.sourceforge.net>
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
package art.runreport;

import art.connectionpool.DbConnections;
import art.dbutils.DatabaseUtils;
import art.enums.ParameterDataType;
import art.enums.ReportType;
import art.report.Report;
import art.reportparameter.ReportParameter;
import art.servlets.Config;
import art.utils.ArtUtils;
import art.utils.XmlInfo;
import art.utils.XmlParser;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs a report
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class ReportRunner {

	private static final Logger logger = LoggerFactory.getLogger(ReportRunner.class);
	private String username; //used in replacing :username tag
	private final StringBuilder querySb;
	private boolean useRules = false;
	private PreparedStatement psQuery; // this is the ps object produced by this query
	private Connection connQuery; // this is the connection to the datasource for this query
	private String finalSql; //final sql statement with parameters substituted
	private boolean recipientFilterPresent; //dynamic recipient filter label present
	private final String RECIPIENT_LABEL = "#recipient#"; //for dynamic recipients, label for recipient in data query
	private String recipientColumn;
	private String recipientId;
	private String recipientIdType = "VARCHAR";
	private int displayResultset;
	private int updateCount; //update count of display resultset
	private Report report;
	private Map<String, ReportParameter> reportParamsMap;
	private final List<Object> jdbcParams = new ArrayList<>();
	private ReportType reportType;

	public ReportRunner() {
		querySb = new StringBuilder(1024 * 2); // assume the average query is < 2kb
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
	 * Set the user who is executing the query
	 *
	 * @param s
	 */
	public void setUsername(String s) {
		username = s;
	}

	/**
	 * Processes the report source and applies tags, dynamic sql, parameters and
	 * rules
	 */
	private void processReportSource() throws SQLException {
		logger.debug("Entering processReportSource");

		//update querySb with report sql
		querySb.replace(0, querySb.length(), report.getReportSource());

		applyTags(querySb);
		applyDynamicSql(querySb);
		applyParameterPlaceholders(querySb);

		//handle dynamic recipient label
		applyDynamicRecipient(querySb);
		applyRules(querySb);

		logger.debug("Sql query now is:\n{}", querySb.toString());
	}

	/**
	 * Applies rules to the report source
	 *
	 * @param sb the report source
	 * @throws SQLException
	 */
	private void applyRules(StringBuilder sb) throws SQLException {
		logger.debug("Entering applyRules");

		if (!useRules) {
			//if use rules setting is overriden, i.e. it's false while the query has a #rules# label, remove label and put dummy condition
			String querySql = sb.toString();
			querySql = querySql.replaceAll("(?iu)#rules#", "1=1");

			//update sb with new sql
			sb.replace(0, sb.length(), querySql);

			return;
		}

		int insertPosLast = 0;

		// Determine if we have a GROUP BY or an ORDER BY
		int grb = sb.toString().lastIndexOf("GROUP BY");
		int orb = sb.toString().lastIndexOf("ORDER BY");
		if ((grb != -1) || (orb != -1)) {
			// We have a GROUP BY or an ORDER BY clause. This is the "negative" offset
			// that indicates where to insert the rule in the SQL statement
			insertPosLast = sb.length() - ((grb > orb) && (orb > 0) ? orb : (grb == -1 ? orb : grb));
		}

		//check if using labelled rules 
		int count = 0;
		StringBuilder labelledValues = new StringBuilder(1024);
		boolean usingLabelledRules;
		String querySql = sb.toString();
		if (StringUtils.containsIgnoreCase(querySql, "#rules#")) { //use all lowercase to make find case insensitive
			usingLabelledRules = true;
		} else {
			usingLabelledRules = false;
		}

		String ruleName;
		String columnName;
		String columnDataType;
		ResultSet rs = null;
		Connection conn = null;
		PreparedStatement ps = null;

		int queryId = report.getReportId();

		try {

			conn = DbConnections.getArtDbConnection();

			// Get rules for the current query
			String sql = "SELECT RULE_NAME, FIELD_NAME, FIELD_DATA_TYPE"
					+ " FROM ART_QUERY_RULES"
					+ " WHERE QUERY_ID=?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, queryId);
			rs = ps.executeQuery();

			// for each rule build and add the AND column IN (list) string to the query
			// Note: if we don't have rules for this query, the sb is left untouched
			while (rs.next()) {
				count++;

				StringBuilder tmpSb;
				ruleName = rs.getString("RULE_NAME");
				columnName = rs.getString("FIELD_NAME");
				columnDataType = rs.getString("FIELD_DATA_TYPE");

				tmpSb = getRuleValues(conn, username, ruleName, 1, columnDataType);
				String groupValues = getGroupRuleValues(conn, ruleName, columnName, columnDataType);
				if (tmpSb == null) { // it is null only if 	ALL_ITEMS
					//ALL_ITEMS. effectively means the rule doesn't apply
					if (usingLabelledRules) {
						//using labelled rules. don't append AND before the first rule value
						if (count == 1) {
							labelledValues.append(" 1=1 ");
						} else {
							labelledValues.append(" AND 1=1 ");
						}
					}
				} else {
					// Add the rule to the query 
					String values = tmpSb.toString();
					if (StringUtils.length(values) == 0 && StringUtils.length(groupValues) == 0) {
						//user doesn't have values set for at least one rule that the query uses. values needed for all rules
						break;
					} else {
						String condition = "";
						if (StringUtils.length(values) > 0) {
							condition = columnName + " in (" + values.substring(1) + ")";
						}
						String groupCondition = "";
						if (StringUtils.length(groupValues) > 0) {
							groupCondition = groupValues;
						}

						if (StringUtils.length(condition) > 0) {
							//rule values defined for user
							if (StringUtils.length(groupCondition) > 0) {
								groupCondition = " OR " + groupCondition;
							}
							condition = condition + groupCondition; // ( user values OR (user group values) )
						} else {
							//no rule values for user. use user group values
							condition = groupCondition;
						}

						condition = " ( " + condition + " ) "; //enclose this rule values in brackets to treat it as a single condition

						if (usingLabelledRules) {
							//using labelled rules. don't append AND before the first rule value
							// the tmpSb returned by getRuleValues begins with a ',' so we need a .substring(1)
							if (count == 1) {
								labelledValues.append(condition);
							} else {
								labelledValues.append(" AND ").append(condition);
							}
						} else {
						//append rule values for non-labelled rules

							// Add the rule to the query (handle GROUP_BY and ORDER BY)
							// NOTE: HAVING is not handled.
							// tmpSb.toSting().substring(1) is the <list> of allowed values for the current rule,
							// the tmpSb returned by getRuleValues begins with a ',' so we need a .substring(1)
							if (insertPosLast > 0) {
								// We have a GROUP BY or an ORDER BY clause
								// NOTE: sb changes dynamically

								sb.insert(sb.length() - insertPosLast, " AND " + condition);
							} else { //No group by or order by. We can just append
								sb.append(" AND ").append(condition);
							}
						}
					}
				}
			}

			//replace all occurrences of labelled rule with rule values
			if (usingLabelledRules) {
				//replace rule values
				String replaceString = Matcher.quoteReplacement(labelledValues.toString()); //quote in case it contains special regex characters
				querySql = querySql.replaceAll("(?iu)#rules#", replaceString);
				//update sb with new sql
				sb.replace(0, sb.length(), querySql);
			}
		} finally {
			DatabaseUtils.close(rs, ps, conn);
		}
	}

	/**
	 * Returns rule values for the given user and rule
	 *
	 * @param conn a connection to the art database
	 * @param ruleUsername the user name
	 * @param currentRule the rule name
	 * @param counter a counter for the recursion count
	 * @param columnDataType the column data type
	 * @return rule values, or null if all values are to be used
	 * @throws SQLException
	 */
	public StringBuilder getRuleValues(Connection conn, String ruleUsername,
			String currentRule, int counter, String columnDataType)
			throws SQLException {

		StringBuilder tmpSb = new StringBuilder(64);
		boolean isAllItemsForThisRule = false;
		final int MAX_RECURSIVE_LOOKUP = 20;

		// Exit after MAX_RECURSIVE_LOOKUP calls
		// this is to avoid a situation when user A lookups user B
		// and viceversa
		if (counter > MAX_RECURSIVE_LOOKUP) {
			logger.warn("TOO MANY LOOPS - exiting");
			return new StringBuilder("TOO MANY LOOPS");
		}

		// Retrieve user's rule value for this rule
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql;

		try {

			if (NumberUtils.isNumber(ruleUsername)) {
				//get values from user group
				sql = "SELECT RULE_VALUE, RULE_TYPE "
						+ " FROM ART_USER_GROUP_RULES "
						+ " WHERE USER_GROUP_ID = ? AND RULE_NAME = ?";

				ps = conn.prepareStatement(sql);
				ps.setInt(1, Integer.parseInt(ruleUsername));
				ps.setString(2, currentRule);
			} else {
				//get values from user
				sql = "SELECT RULE_VALUE, RULE_TYPE "
						+ " FROM ART_USER_RULES "
						+ " WHERE USERNAME = ? AND RULE_NAME = ?";

				ps = conn.prepareStatement(sql);
				ps.setString(1, ruleUsername);
				ps.setString(2, currentRule);
			}

			rs = ps.executeQuery();

			// Build the tmp string, handle ALL_ITEMS and
			// Recursively call applyRule() for LOOKUP
			//  Note: null TYPE is handled as EXACT
			while (rs.next() && !isAllItemsForThisRule) {
				String ruleValue = rs.getString("RULE_VALUE");
				if (!StringUtils.equals(ruleValue, "ALL_ITEMS")) {
					if (StringUtils.equals(rs.getString("RULE_TYPE"), "LOOKUP")) {
						// if type is lookup the VALUE is the name
						// to look up. Recursively call getRuleValues
						StringBuilder lookupSb = getRuleValues(conn, ruleValue, currentRule, ++counter, columnDataType);
						if (lookupSb == null) {
							//all values
							isAllItemsForThisRule = true;
							break;
						} else {
							String values = lookupSb.toString();
							if (StringUtils.equals(values, "TOO MANY LOOPS")) {
								values = "";
							}
							tmpSb.append(values);
						}
					} else { // Normal EXACT type
						if (StringUtils.equals(columnDataType, "NUMBER") && org.apache.commons.lang.math.NumberUtils.isNumber(ruleValue)) {
							//don't quote numbers
							tmpSb.append(",").append(ruleValue);
						} else {
							//escape and quote non-numbers
							tmpSb.append(",'").append(escapeSql(ruleValue)).append("'");
						}
					}
				} else {
					isAllItemsForThisRule = true;
					break;
				}
			}
		} finally {
			DatabaseUtils.close(rs, ps);
		}

		if (!isAllItemsForThisRule) {
			// return the <list> for the current rule and user
			return tmpSb;
		}

		return null;
	}

	/**
	 * Returns rule values for the user's user groups
	 *
	 * @param conn a connection to the art database
	 * @param ruleName the rule name
	 * @param columnName the column name
	 * @param columnDataType the columne data type
	 * @return the rule values
	 */
	private String getGroupRuleValues(Connection conn, String ruleName, String columnName,
			String columnDataType) throws SQLException {

		//get user's user groups
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql;
		StringBuilder valuesSb = new StringBuilder(512);

		try {

			sql = "SELECT USER_GROUP_ID "
					+ " FROM ART_USER_GROUP_ASSIGNMENT "
					+ " WHERE USERNAME=? ";

			ps = conn.prepareStatement(sql);
			ps.setString(1, username);

			rs = ps.executeQuery();

			int count = 0;

			while (rs.next()) {
				//for each group, get the group's rule values
				String userGroupId = rs.getString("USER_GROUP_ID");
				StringBuilder tmpSb = getRuleValues(conn, userGroupId, ruleName, 1, columnDataType);

				String condition;
				if (tmpSb == null) {
					//rule value defined for this group as ALL_ITEMS
					condition = " 1=1 ";
				} else {
					if (tmpSb.length() == 0) {
						//no values defined for this rule for this group
						condition = "";
					} else {
						//some values defined for this rule for this group
						String groupValues = tmpSb.toString().substring(1); //first character returned from getRuleValues is ,
						condition = columnName + " in(" + groupValues + ") ";
					}
				}

				//build group values string
				if (StringUtils.length(condition) > 0) {
					//some rule value defined for this group
					count++;

					if (count == 1) {
						valuesSb.append(condition);
					} else {
						valuesSb.append(" OR ").append(condition);
					}
				}
			}
		} finally {
			DatabaseUtils.close(rs, ps);
		}

		return valuesSb.toString();
	}

	/**
	 * Applies parameter placeholders to the report source
	 *
	 * @param sb the report source
	 */
	private void applyParameterPlaceholders(StringBuilder sb) {
		logger.debug("Entering applyParameterPlaceholders");

		if (reportParamsMap == null || reportParamsMap.isEmpty()) {
			return;
		}

		String querySql = sb.toString();

		//get and store param identifier order for use with jdbc preparedstatement
		if (!reportType.isOlap()) {
			Map<Integer, ReportParameter> jdbcParamOrder = new TreeMap<>(); //use treemap so that jdbc params are set in correct order
			for (Entry<String, ReportParameter> entry : reportParamsMap.entrySet()) {
				String paramName = entry.getKey();
				ReportParameter reportParam = entry.getValue();

				String paramIdentifier = "#" + paramName + "#";
				int index = StringUtils.indexOfIgnoreCase(querySql, paramIdentifier);
				while (index >= 0) {
					jdbcParamOrder.put(index, reportParam);
					index = StringUtils.indexOfIgnoreCase(querySql, paramIdentifier, index + paramIdentifier.length());
				}
			}

			jdbcParams.clear();
			for (ReportParameter reportParam : jdbcParamOrder.values()) {
				for (Object paramValue : reportParam.getActualParameterValues()) {
					addJdbcParam(paramValue, reportParam.getParameter().getDataType());
				}
			}
		}

		//replace direct substitution parameters
		if (Config.getCustomSettings().isEnableDirectParameterSubstitution()
				|| reportType.isOlap()) {
			RunReportHelper runReportHelper = new RunReportHelper();
			String placeholderPrefix = "!"; //use #!<param_name># syntax
			querySql = runReportHelper.performDirectParameterSubstitution(querySql, placeholderPrefix, reportParamsMap);
		}

		//replace jdbc parameter identifiers with ?
		for (Entry<String, ReportParameter> entry : reportParamsMap.entrySet()) {
			String paramName = entry.getKey();
			ReportParameter reportParam = entry.getValue();

			List<Object> actualParameterValues = reportParam.getActualParameterValues();

			if (actualParameterValues == null || actualParameterValues.isEmpty()) {
				continue;
			}

			String paramIdentifier = "#" + paramName + "#";
			String searchString = Pattern.quote(paramIdentifier); //quote in case it contains special regex characters
			String replaceString = Matcher.quoteReplacement(StringUtils.repeat("?", ",", reportParam.getActualParameterValues().size())); //quote in case it contains special regex characters

			querySql = querySql.replaceAll("(?iu)" + searchString, replaceString); //(?iu) makes replace case insensitive across unicode characters
		}

		//update querySb with new sql
		sb.replace(0, sb.length(), querySql);
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
			if (paramDataType == ParameterDataType.Date) {
				jdbcParams.add(DatabaseUtils.toSqlDate(dateValue));
			} else {
				jdbcParams.add(DatabaseUtils.toSqlTimestamp(dateValue));
			}
		} else {
			jdbcParams.add(paramValue);
		}
	}

	/**
	 * Applies dynamic recipients to the report source
	 *
	 * @param sb the report source
	 */
	private void applyDynamicRecipient(StringBuilder sb) {
		logger.debug("Entering applyDynamicRecipient");

		String querySql = sb.toString();

		if (recipientFilterPresent) {
			//replace #recipient# label with recipient values
			if (recipientColumn != null && recipientId != null) {
				String recipientValue;
				if (StringUtils.equalsIgnoreCase(recipientIdType, "NUMBER") && NumberUtils.isNumber(recipientId)) {
					//don't quote recipient id
					recipientValue = recipientId;
				} else {
					//quote recipient id
					recipientValue = "'" + escapeSql(recipientId) + "'";
				}
				String replaceString = recipientColumn + "=" + recipientValue;

				String searchString = Pattern.quote(RECIPIENT_LABEL);
				replaceString = Matcher.quoteReplacement(replaceString);
				querySql = querySql.replaceAll("(?iu)" + searchString, replaceString);
			}
		}

		//ignore #recipient# label if it is still there
		String searchString = Pattern.quote(RECIPIENT_LABEL);
		querySql = querySql.replaceAll("(?iu)" + searchString, "1=1");

		//update querySb with new sql
		sb.replace(0, sb.length(), querySql);
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
		execute(resultSetType, false, false);
	}

	/**
	 * Runs the report using a forward only cursor and the given use rules
	 * setting
	 *
	 * @param newUseRules the use rules setting to use
	 * @throws java.sql.SQLException
	 */
	public void execute(boolean newUseRules) throws SQLException {
		execute(ResultSet.TYPE_FORWARD_ONLY, true, newUseRules);
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

		reportType = report.getReportType();
		displayResultset = report.getDisplayResultset();
		useRules = report.isUsesRules();

		//override use rules setting if required, especially for lovs
		if (overrideUseRules) {
			useRules = newUseRules;
		}

		//Get the SQL String with rules, inline, multi params and tags already applied.
		//don't process the source for jasper, jxls template, static lov queries
		if (reportType != ReportType.JasperReportsTemplate
				&& reportType != ReportType.JxlsTemplate
				&& reportType != ReportType.LovStatic) {
			processReportSource();
		}

		//don't execute sql source for jasper report template query, jxls template query, mdx queries, static lov
		if (reportType == ReportType.JasperReportsTemplate
				|| reportType == ReportType.JxlsTemplate
				|| reportType == ReportType.Mondrian
				|| reportType == ReportType.MondrianXmla
				|| reportType == ReportType.SqlServerXmla
				|| reportType == ReportType.LovStatic) {
			return;
		}

		//use dynamic datasource if so configured
		RunReportHelper runReportHelper = new RunReportHelper();
		connQuery = runReportHelper.getEffectiveReportDatasource(report, reportParamsMap);

		String querySql = querySb.toString();

		Object[] paramValues = jdbcParams.toArray(new Object[0]);
		finalSql = generateFinalSql(querySql, paramValues);

		psQuery = connQuery.prepareStatement(querySql, resultSetType, ResultSet.CONCUR_READ_ONLY);

		DatabaseUtils.setValues(psQuery, paramValues);

		psQuery.execute();
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
		ResultSet rs = psQuery.getResultSet();
		updateCount = psQuery.getUpdateCount();

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

		if (reportType == ReportType.LovStatic) {
			//static lov. values coming from static values defined in sql source
			String items = querySb.toString();
			String lines[] = items.split("\\r?\\n"); //split by newline
			for (String line : lines) {
				String[] values = line.trim().split("\\|"); //split by |
				String dataValue = values[0];
				String displayValue = null;
				if (values.length > 1) {
					displayValue = values[1];
				}
				lovValues.put(dataValue, displayValue);
			}
		} else if (reportType == ReportType.LovDynamic) {
			//dynamic lov. values coming from sql query
			ResultSet rs = getResultSet();
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
		}

		return lovValues;
	}

	/**
	 * Releases resources (mainly, return the connection to the target database
	 * to the connection pool). IT IS MANDATORY TO CALL THIS AFTER THE execute()
	 * IN ORDER TO RETURN THE CONNECTION TO THE POOL.
	 */
	public void close() {
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
	 *
	 * @param sb the report source
	 */
	private void applyDynamicSql(StringBuilder sb) {
		logger.debug("Entering applyDynamicSql");

		String element = "IF";

		// XmlInfo stores the text between a tag as well as
		// the start and end position of the tag
		XmlInfo xinfo = XmlParser.getXmlElementInfo(sb.toString(), element, 0);

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
			sb.replace(xinfo.getStart(), xinfo.getEnd() + element.length() + 3, finalElementValue);

			// check next element
			xinfo = XmlParser.getXmlElementInfo(sb.toString(), element, xinfo.getStart() + finalElementValue.length());
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
				throw new IllegalStateException("Report parameters not available");
			}

			String paramName = exp.substring(1, exp.length() - 1);
			ReportParameter reportParam = reportParamsMap.get(paramName);
			if (reportParam == null) {
				throw new IllegalStateException("Parameter not found: " + paramName);
			}

			if (reportParam.getEffectiveActualParameterValue() instanceof Date) {
				Date dateValue = (Date) reportParam.getEffectiveActualParameterValue();
				expValue = ArtUtils.isoDateTimeMillisecondsFormatter.format(dateValue);
			} else {
				expValue = String.valueOf(reportParam.getActualParameterValues());
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
	 * @param sb the report source
	 */
	private void applyTags(StringBuilder sb) {
		logger.debug("Entering applyTags");

		String querySql = sb.toString();

		//replace :USERNAME with currently logged in user's username
		String replaceString = Matcher.quoteReplacement("'" + username + "'"); //quote in case it contains special regex characters
		querySql = querySql.replaceAll("(?iu):username", replaceString); //(?iu) makes replace case insensitive across unicode characters

		//replace :DATE with current date
		Date now = new Date();

		String dateFormat = "yyyy-MM-dd";
		SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
		String date = dateFormatter.format(now);
		querySql = querySql.replaceAll("(?iu):date", "'" + date + "'");

		//replace :TIME with current date and time
		String timeFormat = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat timeFormatter = new SimpleDateFormat(timeFormat);
		String time = timeFormatter.format(now);
		querySql = querySql.replaceAll("(?iu):time", "'" + time + "'");

		//update querySb with new sql
		sb.replace(0, sb.length(), querySql);

		logger.debug("Sql query now is:\n{}", sb);
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
	 * @param parameter the parameter value
	 * @return the formatted value
	 */
	private String formatParameter(Object parameter) {
		if (parameter == null) {
			return "NULL";
		} else {
			if (parameter instanceof String) {
				return "'" + ((String) parameter).replace("'", "''") + "'";
			} else if (parameter instanceof Timestamp) {
				return "'" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(parameter) + "'";
			} else if (parameter instanceof java.sql.Date) {
				return "'" + new SimpleDateFormat("yyyy-MM-dd").format(parameter) + "'";
			} else if (parameter instanceof Boolean) {
				return ((Boolean) parameter) ? "1" : "0";
			} else {
				return parameter.toString();
			}
		}
	}
}
