/*
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
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

import art.dbutils.DatabaseUtils;
import art.enums.ParameterDataType;
import art.enums.ParameterType;
import art.enums.ReportType;
import art.parameter.Parameter;
import art.report.Report;
import art.reportparameter.ReportParameter;
import art.servlets.Config;
import art.utils.ArtException;
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
import java.util.HashMap;
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
 * Execute query
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class ReportRunner {

	private static final Logger logger = LoggerFactory.getLogger(ReportRunner.class);
	String username; //used in replacing :username tag
	int reportId;
	StringBuilder querySb;
	boolean adminSession = false;
	boolean useRules = false;
	PreparedStatement psQuery; // this is the ps object produced by this query
	Connection connQuery; // this is the connection to the datasource for this query
	private String finalSql; //final sql statement with parameters substituted
	private boolean recipientFilterPresent; //dynamic recipient filter label present
	private final String RECIPIENT_LABEL = "#recipient#"; //for dynamic recipients, label for recipient in data query
	private String recipientColumn;
	private String recipientId;
	private String recipientIdType = "VARCHAR";
	int displayResultset;
	int updateCount; //update count of display resultset
	private Report report;
	private String[] filterValues; //value of filter used with chained parameters
	private Map<String, ReportParameter> reportParamsMap;
	private List<Object> jdbcParams = new ArrayList<>();
	ReportType reportType;

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
	 * Get the value of filterValues
	 *
	 * @return the value of filterValues
	 */
	public String[] getFilterValues() {
		return filterValues;
	}

	/**
	 * Set the value of filterValues
	 *
	 * @param filterValues new value of filterValues
	 */
	public void setFilterValues(String[] filterValues) {
		this.filterValues = filterValues;
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
	 * Set the report id to execute
	 *
	 * @param i
	 */
	public void setReportId(int i) {
		reportId = i;
	}

	/**
	 * Set this flag to true to skip privileges checks when getting the SQL
	 * Default is false.
	 *
	 * @param b
	 */
	public void setAdminSession(boolean b) {
		adminSession = b;
	}

	/**
	 * Process the report source and apply tags, dynamic sql and parameters
	 */
	private void processReportSource() {

		//update querySb with report sql
		querySb.replace(0, querySb.length(), report.getReportSource());

		applyTags(querySb);
		applyDynamicSql(querySb);
		applyParameterPlaceholders(querySb);

		//handle dynamic recipient label
		applyDynamicRecipient(querySb);

		logger.debug("Sql query now is:\n{}", querySb.toString());
	}

	private void applyParameterPlaceholders(StringBuilder sb) {
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
			for (Entry<String, ReportParameter> entry : reportParamsMap.entrySet()) {
				String paramName = entry.getKey();
				ReportParameter reportParam = entry.getValue();

				String paramIdentifier = "#!" + paramName + "#";
				String searchString = Pattern.quote(paramIdentifier); //quote in case it contains special regex characters
				String replaceString = Matcher.quoteReplacement(String.valueOf(reportParam.getActualParameterValues())); //quote in case it contains special regex characters
				querySql = querySql.replaceAll("(?iu)" + searchString, replaceString); //(?iu) makes replace case insensitive across unicode characters
			}
		}

		//replace jdbc parameter identifiers with ?
		for (Entry<String, ReportParameter> entry : reportParamsMap.entrySet()) {
			String paramName = entry.getKey();
			ReportParameter reportParam = entry.getValue();

			String paramIdentifier = "#" + paramName + "#";
			String searchString = Pattern.quote(paramIdentifier); //quote in case it contains special regex characters
			String replaceString = Matcher.quoteReplacement(StringUtils.repeat("?", ",", reportParam.getActualParameterValues().size())); //quote in case it contains special regex characters

			querySql = querySql.replaceAll("(?iu)" + searchString, replaceString); //(?iu) makes replace case insensitive across unicode characters
		}

		//update querySb with new sql
		sb.replace(0, sb.length(), querySql);
	}

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

	private void applyDynamicRecipient(StringBuilder sb) {

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
	 * execute overload with a default resultset type
	 *
	 */
	public void execute() throws SQLException {
		execute(ResultSet.TYPE_FORWARD_ONLY);
	}

	/**
	 * execute overload with a given resultset type
	 *
	 */
	public void execute(int resultSetType) throws SQLException {
		execute(resultSetType, false, false);
	}

	/**
	 * execute overload with use rules setting
	 *
	 */
	public void execute(boolean newUseRules) throws SQLException {
		execute(ResultSet.TYPE_FORWARD_ONLY, true, newUseRules);
	}

	/**
	 * Execute the Query sql
	 *
	 * @param resultSetType
	 */
	public void execute(int resultSetType, boolean overrideUseRules, boolean newUseRules) throws SQLException {

		reportType = report.getReportType();
		displayResultset = report.getDisplayResultset();
		useRules = report.isUsesFilters();

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
	 * Get the result set to use for this query. query sql may have several
	 * statements
	 *
	 * @return the result set of this query
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
	 * Get the number of rows updated by this query
	 *
	 * @return the number of rows updated by this query
	 * @throws SQLException
	 */
	public int getUpdateCount() throws SQLException {
		return updateCount;
	}

	/**
	 * Execute and get the result set for this query
	 *
	 * @return <code>true</code> if successful
	 * @throws SQLException
	 */
	public ResultSet executeQuery() throws SQLException {
		return executeQuery(false, false);
	}

	/**
	 * Execute and get the result set for this query
	 *
	 * @return <code>true</code> if successful
	 * @throws SQLException
	 */
	public ResultSet executeQuery(boolean newUseRules) throws SQLException {
		return executeQuery(true, newUseRules);
	}

	/**
	 * Execute and get the result set for this query
	 *
	 * @return <code>true</code> if successful
	 * @throws SQLException
	 * @throws ArtException
	 */
	public ResultSet executeQuery(boolean overrideUseRules, boolean newUseRules) throws SQLException {
		execute(ResultSet.TYPE_FORWARD_ONLY, overrideUseRules, newUseRules);
		return getResultSet();
	}

	/**
	 * Run lov report and return the lov values (value and label)
	 *
	 * @return values for an lov
	 * @throws SQLException
	 */
	public Map<Object, String> getLovValues() throws SQLException {
		return getLovValues(false, false);
	}

	/**
	 * Run lov report and return the lov values (value and label)
	 *
	 * @param newUseRules
	 * @return values for an lov
	 * @throws SQLException
	 */
	public Map<Object, String> getLovValues(boolean newUseRules) throws SQLException {
		return getLovValues(true, newUseRules);
	}

	/**
	 * Run lov report and return the lov values (value and label)
	 *
	 * @param overrideUseRules
	 * @param newUseRules
	 * @return values for an lov
	 * @throws SQLException
	 */
	public Map<Object, String> getLovValues(boolean overrideUseRules, boolean newUseRules) throws SQLException {
		Map<Object, String> lovValues = new HashMap<>();

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
			int columnCount = rs.getMetaData().getColumnCount();
			while (rs.next()) {
				//use getObject(). for dates, using getString() will return
				//different strings for different databases and drivers
				//https://stackoverflow.com/questions/8229727/how-to-get-jdbc-date-format
				//https://stackoverflow.com/questions/14700962/default-jdbc-date-format-when-reading-date-as-a-string-from-resultset
				Object dataValue = rs.getObject(1);
				String displayValue = null;
				if (columnCount > 1) {
					displayValue = rs.getString(2);
				}
				lovValues.put(dataValue, displayValue);
			}
		}

		return lovValues;
	}

	/**
	 * Release resources (mainly, return the connection to the target database
	 * for this query) IT IS MANDATORY TO CALL THIS AFTER THE execute() IN ORDER
	 * TO RETURN THE CONNECTION TO THE POOL
	 */
	public void close() {
		DatabaseUtils.close(psQuery, connQuery);
	}

	// escape the ' char in a parameter value (used in multi params)
	private String escapeSql(String s) {
		String escaped = null;
		if (s != null) {
			escaped = StringUtils.replace(s, "'", "''");
		}
		return escaped;
	}

	/**
	 * Dynamic SQL is parsed, evaluated and the querySb is modified according
	 */
	private void applyDynamicSql(StringBuilder sb) {
		applyDynamicSql(sb, false);
	}

	/**
	 * Dynamic SQL is parsed, evaluated and the querySb is modified according
	 */
	private void applyDynamicSql(StringBuilder sb, boolean usingFilter) {
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

			//enable use of same lov for chained and non-chained parameters			
			if (StringUtils.equals(exp1, "#filter#") && (StringUtils.equalsIgnoreCase(op, "is not null") || StringUtils.equalsIgnoreCase(op, "is not blank"))) {
				if (!usingFilter) {
					//determine if we have filter
					if (reportParamsMap != null) {
						if (reportParamsMap.get("filter") != null) {
							usingFilter = true;
						}
					}
				}

				if (usingFilter) {
					exp1Value = "#filter#"; //any string. just so that if condition is returned
				} else {
					exp1Value = ""; //empty sting. so that else value is returned
				}
			}

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

			//TODO use explicit format for dates instead of String.valueOf() or use passed parameter value?
			expValue = String.valueOf(reportParam.getActualParameterValues());
		} else {
			//expression isn't a report parameter. use as is
			expValue = exp;
		}

		return expValue;
	}

	/**
	 * Evaluate the IF element in Dynamic SQL
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
	 * Replace :TAGS
	 */
	private void applyTags(StringBuilder sb) {
		logger.debug("applyTags");

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

	private String generateFinalSql(String sqlQuery, Object... parameters) {
		//https://stackoverflow.com/questions/2683214/get-query-from-java-sql-preparedstatement

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
