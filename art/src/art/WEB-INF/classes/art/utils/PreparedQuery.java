/*
 * Copyright (C) 2001/2004  Enrico Liboni  - enrico@computer.org
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation;
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   (version 2) along with this program (see documentation directory);
 *   otherwise, have a look at http://www.gnu.org or write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
/**
 * PreparedQuery.java
 *
 * Caller:	QueryParameters, ArtJob Purpose:	get the prepared statement of the
 * selected query apply smartRules add multi params apply inline params apply
 * tags parse&apply dynamic SQL set bind parameters
 */
package art.utils;

import art.servlets.ArtDBCP;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Execute query
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class PreparedQuery {

	final static Logger logger = LoggerFactory.getLogger(PreparedQuery.class);
	final int MAX_RECURSIVE_LOOKUP = 20;
	String username; //used to check query access rights, in applying rule values and replacing :username tag
	int queryId;
	int queryDatabaseId = -1;
	StringBuilder sb;
	Map<String, String> bindParams;
	Map<String, String[]> multiParams;
	Map<String, String> inlineParams;
	TreeMap<Integer, String> treeInline; //  stores the inline values sorted by the ? in the SQL
	boolean adminSession = false;
	boolean useRules = false;
	boolean isLov = false;
	PreparedStatement psQuery; // this is the ps object produced by this query
	Connection connQuery; // this is the connection to the datasource for this query
	Connection conn; // connection to the art repository
	String preparedStatementSQL; //final sql statement. if query has inline parameters, sql will still have ?
	private String finalSQL = ""; //final sql statement. if query has inline parameters, sql will have query values
	String queryStatus;
	Map<String, List> jasperMultiParams; //hash map will contain multi parameter name and values instead of parameter id e.g. M_2 and string array of values. for jasper reports
	Map<String, Object> jasperInlineParams; //hash map will contain inline parameter label and value as corresponding object e.g. Double, Long. for jasper reports
	Map<String, String> jxlsMultiParams; //hash map will contain multi parameter label and values instead of parameter id e.g. M_2 and string array of values. for jxls reports
	int queryType; //to enable special handling of template queries where sql source is not executed
	boolean viewingTextObject = false; //flag used to check if user has rights to edit a text query
	Map<String, ArtQueryParam> htmlParams; //all the queries parameters, with the html name as the key
	private boolean recipientFilterPresent; //dynamic recipient filter label present
	private final String RECIPIENT_LABEL = "#recipient#"; //for dynamic recipients, label for recipient in data query
	private String recipientColumn;
	private String recipientId;
	private String recipientIdType = "VARCHAR";

	/**
	 *
	 */
	public PreparedQuery() {
		sb = new StringBuilder(1024 * 2); // assume the average query is < 2kb

		jasperInlineParams = new HashMap<String, Object>(); //save parameters in special hash map for jasper reports
		jasperMultiParams = new HashMap<String, List>(); //to populate hash map with multi parameter names and values
		jxlsMultiParams = new HashMap<String, String>(); //save parameters in special hash map for jxls reports        
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
	 * @return the finalSQL
	 */
	public String getFinalSQL() {
		return finalSQL;
	}

	/**
	 * @param finalSQL the finalSQL to set
	 */
	public void setFinalSQL(String finalSQL) {
		this.finalSQL = finalSQL;
	}

	/**
	 *
	 * @param value
	 */
	public void setHtmlParams(Map<String, ArtQueryParam> value) {
		htmlParams = value;
	}

	/**
	 *
	 * @return active status of query
	 */
	public String getQueryStatus() {
		return queryStatus;
	}

	/**
	 *
	 * @return sql to be executed by database with ? where inline parameters
	 * should be
	 */
	public String getPreparedStatementSQL() {
		return preparedStatementSQL;
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
	 * Set the query id to execute
	 *
	 * @param i
	 */
	public void setQueryId(int i) {
		queryId = i;
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
	 * Set the multi parameters. The hastable contains: <br>the multiple
	 * parameter name (prefixed with the M* string) as key <br>the array of
	 * values (String[])
	 *
	 * @param h
	 */
	public void setMultiParams(Map<String, String[]> h) {
		multiParams = h;
	}

	/**
	 * Set the map that contains the bind parameters. The map contains: <br>the
	 * bind parameter code (Py where y is the index of the ? or
	 * Py_year/Py_month/Py_day for dates) as key <br>the parameter value
	 * (String[])
	 *
	 * @param h
	 */
	public void setBindParams(Map<String, String> h) {
		bindParams = h;
	}

	/**
	 * Set the map that contains the general purpose parameters. <br>Art will
	 * substiture the general param label with the value specified by the user
	 * The hastable contains: <br>the parameter label (String) <br>the parameter
	 * value (String)
	 *
	 * @param h
	 */
	public void setInlineParams(Map<String, String> h) {
		inlineParams = h;
	}

	/**
	 *
	 * @return query's datasource
	 */
	public int getTargetDatasource() {
		return queryDatabaseId;
	}

	/**
	 *
	 * @return inline parameters
	 */
	public Map<String, String> getInlineParams() {
		return inlineParams;
	}

	/**
	 *
	 * @return multi parameters
	 */
	public Map<String, String[]> getMultiParams() {
		return multiParams;
	}

	/**
	 * Returns the SQL query with: :TAG applied Dynamic SQL applied inline
	 * parameters converted to bind multi parameters and rules applied.
	 */
	private String getQuerySQL() throws ArtException {

		/*
		 * Apply :Tags (:TAGS are substituted with their values)
		 */
		try {
			applyTags(sb); // work inline in the sb
		} catch (Exception e) {
			logger.error("Error", e);
			throw new ArtException("<p>Error applying tags to the query. Please contact the ART administrator. <br>Details:<code> " + e + "</code></p>");
		}

		/*
		 * Apply Dynamic SQL (parse the <IF> element - using inline parameters
		 * for EXP1, EXP2 and OP element if needed - to evaluate condition)
		 */
		try {
			applyDynamicSQL(sb); // work inline in the sb
		} catch (Exception e) {
			logger.error("Error", e);
			throw new ArtException("<p>Error applying dynamic SQL to the query. Please contact the ART administrator. <br>Details:<code> " + e + "</code></p>");
		}

		/*
		 * Apply Inline Parameters (this must come after applyDynamicSQL)
		 * (Inline parameters are replaced with parameter placeholders (?))
		 */
		try {
			applyInlineParameters(sb); // work inline in the sb
		} catch (Exception e) {
			logger.error("Error", e);
			throw new ArtException("<p>Error applying inline parameters to the query. Please contact the ART administrator. <br>Details:<code> " + e + "</code></p>");
		}


		/*
		 * Apply Multi Parameters to the query SQL (in the WHERE part new "AND
		 * param in (<values>)" conditions are added if the query uses multi
		 * params)
		 */
		try {
			applyMultiParameters(sb); // work inline in the sb
		} catch (Exception e) {
			logger.error("Error", e);
			throw new ArtException("<p>Error applying multi parameters to the query. Please contact the ART administrator. <br>Details:<code> " + e + "</code></p>");
		}

		//apply rules after inline and multi parameters to accomodate hardcoded rules label #rules#

		/*
		 * Apply rules to the SQL (in the WHERE part new "AND rule_column in
		 * (<values>)" conditions are added if the query uses rules)
		 */
		try {
			if (!applyRules(sb)) {
				throw new ArtException("<p>Error applying rules to the query. You likely have not been assigned values for the rules used by the query. Please contact the ART administrator. </p>");
			}
		} catch (Exception e) {
			throw new ArtException("<p>Error applying rules to the query. Please contact the ART administrator.<br>Details:<code> " + e + "</code></p>");
		}

		//handle dynamic recipient label
		applyDynamicRecipient(sb);

		logger.debug("Sql query now is:\n{}", sb.toString());

		return sb.toString();

	}

	private void applyDynamicRecipient(StringBuilder sb) {
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

				String querySql = sb.toString();
				String searchString = Pattern.quote(RECIPIENT_LABEL);
				replaceString = Matcher.quoteReplacement(replaceString);
				querySql = querySql.replaceAll("(?iu)" + searchString, replaceString);

				//update sb with new sql
				sb.replace(0, sb.length(), querySql);
			}
		}

		//ignore #recipient# label if it is still there
		String querySql = sb.toString();
		String searchString = Pattern.quote(RECIPIENT_LABEL);
		querySql = querySql.replaceAll("(?iu)" + searchString, "1=1");

		//update sb with new sql
		sb.replace(0, sb.length(), querySql);

	}

	//determine if the user can execute the query. Exception thrown if user can't excecute query
	private void verifyQueryAccess() throws ArtException {
		try {
			// Get the query SQL source from the ART Repository
			if (!getQuery()) {
				throw new ArtException("<p>Not able to get query. Are you sure you have been granted rights to execute this object?</p>");
			}

			if (StringUtils.equals(queryStatus, "D") && !adminSession) {
				throw new ArtException("<p>Query is disabled. Please contact the ART administrator. </p>");
			}
		} catch (Exception e) {
			throw new ArtException("Error getting the query from ART repository. Please contact the ART administrator. <br>Details:<code> " + e + "</code></p>");
		}
	}

	/**
	 * Set the parameters in the prepared statement. After this method, the
	 * prepared statement is ready to be executed on the target database
	 *
	 * @param ps
	 * @throws ArtException
	 */
	public void prepareStatement(PreparedStatement ps) throws ArtException {

		// Apply Inline Parameters to the prepared statement		 
		try {
			prepareInlineParameters(ps);
		} catch (Exception e) {
			logger.error("Error", e);
			throw new ArtException("<p>Error applying inline parameters to the query. Please contact the ART administrator. <br>Details:<code> " + e + "</code></p>");
		}

	}


	/*
	 * **************************************EXECUTE**********************************
	 */
	/**
	 * execute overload with a default resultset type
	 *
	 * @return <code>true</code> if successful
	 * @throws ArtException
	 */
	public boolean execute() throws ArtException {
		int resultSetType = ResultSet.TYPE_FORWARD_ONLY;
		return execute(resultSetType);
	}

	/**
	 * execute overload with a given resultset type
	 *
	 * @return <code>true</code> if successful
	 * @throws ArtException
	 */
	public boolean execute(int resultSetType) throws ArtException {
		return execute(resultSetType, false, false);
	}

	/**
	 * execute overload with use rules setting
	 *
	 * @return <code>true</code> if successful
	 * @throws ArtException
	 */
	public boolean execute(boolean newUseRules) throws ArtException {
		int resultSetType = ResultSet.TYPE_FORWARD_ONLY;
		return execute(resultSetType, true, newUseRules);
	}

	/**
	 * Execute the Query sql
	 *
	 * @param resultSetType
	 * @return <code>true</code> if successful
	 * @throws ArtException if error occurred while running the query or user
	 * doesn't have access to query
	 */
	public boolean execute(int resultSetType, boolean overrideUseRules, boolean newUseRules) throws ArtException {

		preparedStatementSQL = null;

		try {
			conn = ArtDBCP.getConnection();

			// Get some properties of the query
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT DATABASE_ID, QUERY_GROUP_ID, ACTIVE_STATUS, QUERY_TYPE, USES_RULES "
					+ " FROM ART_QUERIES  "
					+ " WHERE QUERY_ID = " + queryId);
			if (rs.next()) {
				queryDatabaseId = rs.getInt("DATABASE_ID");
				queryStatus = rs.getString("ACTIVE_STATUS");
				queryType = rs.getInt("QUERY_TYPE");

				int groupId = rs.getInt("QUERY_GROUP_ID");
				if (groupId == 0 || queryType == 119 || queryType == 120) {
					isLov = true;
				} else {
					isLov = false;
				}

				String usesRules = rs.getString("USES_RULES");
				if (StringUtils.equals(usesRules, "Y")) {
					useRules = true;
				} else {
					useRules = false;
				}
				//override use rules setting if required, especially for lovs
				if (overrideUseRules) {
					useRules = newUseRules;
				}
			}
			rs.close();
			st.close();

			//get the raw sql source and determine if the user has access to the query. exception will be thrown if user can't excecute query
			verifyQueryAccess();

			//Get the SQL String with rules, inline, multi params and tags already applied.
			//don't process the source for jasper, jxls template, static lov queries
			if (queryType != 115 && queryType != 117 && queryType != 120) {
				preparedStatementSQL = getQuerySQL();
			}

		} catch (Exception e) {
			logger.error("Error", e);
			throw new ArtException("Error while getting/building the query's SQL. " + e + "\nQuery SQL:\n" + preparedStatementSQL);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		//don't execute sql source for jasper report template query, jxls template query, mdx queries, static lov
		if (queryType == 115 || queryType == 117 || queryType == 112 || queryType == 113 || queryType == 114 || queryType == 120) {
			return true;
		}

		try {
			//use dynamic datasource if so configured
			boolean useDynamicDatasource = false;

			if (htmlParams != null) {
				//htmlparams passed from queryexecute or artjob has paramvalues set

				Iterator it = htmlParams.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry entry = (Map.Entry) it.next();
					ArtQueryParam param = (ArtQueryParam) entry.getValue();
					String paramDataType = param.getParamDataType();

					if (StringUtils.equalsIgnoreCase(paramDataType, "DATASOURCE")) {

						//get dynamic connection to use
						Object paramValueObject = param.getParamValue();
						if (paramValueObject != null) {
							String paramValue = (String) paramValueObject;
							if (StringUtils.isNotBlank(paramValue)) {
								useDynamicDatasource = true;
								if (NumberUtils.isNumber(paramValue)) {
									//use datasource id
									connQuery = ArtDBCP.getConnection(Integer.parseInt(paramValue));
								} else {
									//use datasource name
									connQuery = ArtDBCP.getConnection(paramValue);
								}
							}
						}
						break;
					}
				}
			}

			if (!useDynamicDatasource) {
				//not using dynamic datasource. use datasource defined on the query
				connQuery = ArtDBCP.getConnection(queryDatabaseId);
			}

			if (connQuery == null) {
				throw new ArtException("Could not get database connection.");
			}

			finalSQL = preparedStatementSQL; //set final sql in case error occurs before execute

			psQuery = connQuery.prepareStatement(preparedStatementSQL, resultSetType, ResultSet.CONCUR_READ_ONLY);

			prepareStatement(psQuery); // this applies the inline parameter placeholders

			return psQuery.execute();

		} catch (Exception e) {
			logger.error("Error", e);
			throw new ArtException("Error while running query. " + e + "<br>\nQuery SQL:\n" + finalSQL);
		}

	}

	/**
	 * Get the result set of this query
	 *
	 * @return the result set of this query
	 * @throws SQLException
	 */
	public ResultSet getResultSet() throws SQLException {
		return psQuery.getResultSet();
	}

	/**
	 * Get the last result set of this query if this is a series of statement,
	 * the latter rs is returned Anyway, for some drivers a
	 * java.sql.SQLException: Unsupported feature might be thrown
	 *
	 * @return last result set of this query
	 * @throws SQLException
	 */
	public ResultSet getLastResultSet() throws SQLException {
		ResultSet rs = psQuery.getResultSet();

		// Loop through the list of resultsets untill there are no more
		while (psQuery.getMoreResults(Statement.KEEP_CURRENT_RESULT) != false || psQuery.getUpdateCount() != -1) {
			if (rs != null) {
				rs.close();
			}
			rs = psQuery.getResultSet();
		}

		return rs; // return the last one
	}

	/**
	 * Get the number of rows updated by this query
	 *
	 * @return the number of rows updated by this query
	 * @throws SQLException
	 */
	public int getUpdateCount() throws SQLException {
		return psQuery.getUpdateCount();
	}

	/**
	 * Execute and get the result set for this query
	 *
	 * @return <code>true</code> if successful
	 * @throws SQLException
	 * @throws ArtException
	 */
	public ResultSet executeQuery() throws SQLException, ArtException {
		return executeQuery(false, false);
	}

	/**
	 * Execute and get the result set for this query
	 *
	 * @return <code>true</code> if successful
	 * @throws SQLException
	 * @throws ArtException
	 */
	public ResultSet executeQuery(boolean newUseRules) throws SQLException, ArtException {
		return executeQuery(true, newUseRules);
	}

	/**
	 * Execute and get the result set for this query
	 *
	 * @return <code>true</code> if successful
	 * @throws SQLException
	 * @throws ArtException
	 */
	public ResultSet executeQuery(boolean overrideUseRules, boolean newUseRules) throws SQLException, ArtException {
		execute(ResultSet.TYPE_FORWARD_ONLY, overrideUseRules, newUseRules);
		return getResultSet();
	}

	/**
	 * Execute and get the result set for an lov query
	 *
	 * @return values for an lov
	 * @throws SQLException
	 * @throws ArtException
	 */
	public Map<String, String> executeLovQuery() throws SQLException, ArtException {
		return executeLovQuery(false, false);
	}

	/**
	 * Execute and get the result set for an lov query
	 *
	 * @return values for an lov
	 * @throws SQLException
	 * @throws ArtException
	 */
	public Map<String, String> executeLovQuery(boolean newUseRules) throws SQLException, ArtException {
		return executeLovQuery(true, newUseRules);
	}

	/**
	 * Execute and get the result set for an lov query
	 *
	 * @return values for an lov
	 * @throws SQLException
	 * @throws ArtException
	 */
	public Map<String, String> executeLovQuery(boolean overrideUseRules, boolean newUseRules) throws SQLException, ArtException {
		Map<String, String> lov = new LinkedHashMap<String, String>();

		execute(ResultSet.TYPE_FORWARD_ONLY, overrideUseRules, newUseRules);

		if (queryType == 120) {
			//static lov. values coming from static values defined in sql source
			String items = sb.toString();
			String lines[] = items.split("\\r?\\n");
			for (String line : lines) {
				String[] values = line.trim().split("\\|");
				if (values.length == 1) {
					lov.put(values[0], values[0]);
				} else if (values.length == 2) {
					lov.put(values[0], values[1]);
				}
			}
		} else {
			//dynamic lov. values coming from sql query
			ResultSet rs = getResultSet();
			int columnCount = rs.getMetaData().getColumnCount();
			while (rs.next()) {
				if (columnCount == 1) {
					lov.put(rs.getString(1), rs.getString(1));
				} else if (columnCount == 2) {
					lov.put(rs.getString(1), rs.getString(2));
				}
			}
		}

		return lov;
	}

	/**
	 * Release resources (mainly, return the connection to the target database
	 * for this query) IT IS MANDATORY TO CALL THIS AFTER THE execute() IN ORDER
	 * TO RETURN THE CONNECTION TO THE POOL
	 */
	public void close() {
		// close resources and return connection to the pool
		try {
			if (psQuery != null) {
				psQuery.close();
			}
			if (connQuery != null) {
				connQuery.close();
			}
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}

	/*
	 * *******************************************************************************
	 */
	/**
	 * Determine if a user has access to an object
	 *
	 * @param uname
	 * @param qid
	 * @param admin <code>true</code> if this is an admin session
	 * @return <code>true</code> if user can execute the query
	 */
	public boolean canExecuteQuery(String uname, int qid, boolean admin) {
		username = uname;
		queryId = qid;
		adminSession = admin;

		return canExecuteQuery();
	}

	/**
	 * Determine if a user can edit a text query. User needs direct access to
	 * edit it
	 *
	 * @param uname
	 * @param qid
	 * @return <code>true</code> if user can edit the text query
	 */
	public boolean canEditTextObject(String uname, int qid) {
		username = uname;
		queryId = qid;
		adminSession = false;

		viewingTextObject = true;

		return canExecuteQuery();
	}

	/**
	 *
	 * @return <code>true</code> if user can execute this query
	 */
	public boolean canExecuteQuery() {
		boolean canExecute = false;
		boolean newConnection = false;

		try {
			if (conn == null) {
				conn = ArtDBCP.getConnection();
				newConnection = true;
			}

			if (getQuery()) {
				canExecute = true;
			}

		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (newConnection) {
					if (conn != null) {
						conn.close();
					}
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		return canExecute;
	}

	/**
	 * Get the SQL
	 */
	private boolean getQuery() throws SQLException {
		Statement st;
		ResultSet rs;

		int last_stmt_retrieved_rows = 0;

		// Statement to retrieve the query SQL source
		st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

		String stmt;
		PreparedStatement ps;

		//re-initialize sb
		sb = null;
		sb = new StringBuilder(1024 * 2);

		if (isLov || adminSession) {
			// don't check security for Lovs or during Admin session
			logger.debug("isLov = {}, adminSession = {}", isLov, adminSession);

			stmt = "SELECT AAS.SOURCE_INFO "
					+ "  FROM ART_ALL_SOURCES AAS "
					+ " WHERE AAS.OBJECT_ID = " + queryId + " "
					+ " ORDER BY LINE_NUMBER";

			rs = st.executeQuery(stmt);
			while (rs.next()) {
				sb.append(rs.getString(1));
				last_stmt_retrieved_rows++;
			}
			rs.close();
		} else {
			//User can execute query directly granted to him or his user group

			//try access based on user's right to query
			stmt = "SELECT AAS.SOURCE_INFO "
					+ "  FROM ART_ALL_SOURCES AAS, ART_USER_QUERIES AUQ "
					+ " WHERE AAS.OBJECT_ID = ? "
					+ " AND AUQ.USERNAME = ?"
					+ " AND AAS.OBJECT_ID = AUQ.QUERY_ID"
					+ " ORDER BY LINE_NUMBER";

			ps = conn.prepareStatement(stmt);
			ps.setInt(1, queryId);
			ps.setString(2, username);
			rs = ps.executeQuery();
			while (rs.next()) {
				sb.append(rs.getString(1));
				last_stmt_retrieved_rows++;
			}
			ps.close();
			rs.close();

			if (last_stmt_retrieved_rows == 0) {
				//user doesn't have direct access to query. check if he belongs to a user group which has direct access to the query
				stmt = "SELECT DISTINCT AAS.SOURCE_INFO, AAS.LINE_NUMBER "
						+ " FROM ART_ALL_SOURCES AAS, ART_USER_GROUP_QUERIES AUGQ "
						+ " WHERE AAS.OBJECT_ID=AUGQ.QUERY_ID "
						+ " AND AAS.OBJECT_ID = ? AND EXISTS "
						+ " (SELECT * FROM ART_USER_GROUP_ASSIGNMENT AUGA WHERE AUGA.USERNAME = ?"
						+ " AND AUGA.USER_GROUP_ID=AUGQ.USER_GROUP_ID)"
						+ " ORDER BY AAS.LINE_NUMBER";

				ps = conn.prepareStatement(stmt);
				ps.setInt(1, queryId);
				ps.setString(2, username);

				rs = ps.executeQuery();
				while (rs.next()) {
					sb.append(rs.getString(1));
					last_stmt_retrieved_rows++;
				}
				ps.close();
				rs.close();
			}

			//User can also execute all queries in a query group he has been assigned to
			//text querys must be assigned direct access
			if (!viewingTextObject) {
				if (last_stmt_retrieved_rows == 0) {
					//user doesn't belong to a group with direct access to the query. check if user has access to the query's group
					stmt = "SELECT AAS.SOURCE_INFO "
							+ " FROM ART_ALL_SOURCES AAS, ART_USER_QUERY_GROUPS AUQG, ART_QUERIES aq "
							+ " WHERE AAS.OBJECT_ID=aq.QUERY_ID AND aq.QUERY_GROUP_ID = AUQG.QUERY_GROUP_ID"
							+ " AND AAS.OBJECT_ID = ? AND AUQG.USERNAME= ? "
							+ " ORDER BY LINE_NUMBER";

					//try access based on user's right to query group
					ps = conn.prepareStatement(stmt);
					ps.setInt(1, queryId);
					ps.setString(2, username);
					rs = ps.executeQuery();
					while (rs.next()) {
						sb.append(rs.getString(1));
						last_stmt_retrieved_rows++;
					}
					rs.close();
				}

				if (last_stmt_retrieved_rows == 0) {
					//user doesn't have direct access to query group. check if he belongs to a user group which has direct access to the query group
					stmt = "SELECT DISTINCT AAS.SOURCE_INFO, AAS.LINE_NUMBER "
							+ " FROM ART_ALL_SOURCES AAS, ART_USER_GROUP_GROUPS AUGG, ART_QUERIES aq "
							+ " WHERE AAS.OBJECT_ID=aq.QUERY_ID AND aq.QUERY_GROUP_ID = AUGG.QUERY_GROUP_ID "
							+ " AND AAS.OBJECT_ID = ? AND EXISTS "
							+ " (SELECT * FROM ART_USER_GROUP_ASSIGNMENT AUGA WHERE AUGA.USERNAME = ? "
							+ " AND AUGA.USER_GROUP_ID = AUGG.USER_GROUP_ID) "
							+ " ORDER BY AAS.LINE_NUMBER";

					ps = conn.prepareStatement(stmt);
					ps.setInt(1, queryId);
					ps.setString(2, username);

					rs = ps.executeQuery();
					while (rs.next()) {
						sb.append(rs.getString(1));
						last_stmt_retrieved_rows++;
					}
					ps.close();
					rs.close();
				}
			}

		}

		//If the previous statement did not retrieve any rows, try to see if the query is public
		if (last_stmt_retrieved_rows == 0) {
			//no direct or group access. check if query is public. all users have access to public queries

			stmt = "SELECT AAS.SOURCE_INFO "
					+ "  FROM ART_ALL_SOURCES AAS, ART_USER_QUERIES AUQ "
					+ " WHERE AAS.OBJECT_ID = " + queryId + " "
					+ " AND AUQ.USERNAME= 'public_user' "
					+ " AND AAS.OBJECT_ID = AUQ.QUERY_ID "
					+ " ORDER BY LINE_NUMBER";

			rs = st.executeQuery(stmt);
			while (rs.next()) {
				sb.append(rs.getString(1));
				last_stmt_retrieved_rows++;
			}
			rs.close();
		}

		st.close();

		if (last_stmt_retrieved_rows == 0) {
			// this means we were not able to get the SQL, either because the query has not been granted to the user or it is not public
			return false;
		} else {
			//user has access to the query
			return true;
		}
	}

	/**
	 * Apply Rules v 0.5 - embedded in PreparedQuery v 0.4 - Return null instead
	 * of raising an exception if the usernames has not been granted to the rule
	 * v 0.3 - Handle "LOOKUP" rule type, with a recursive approach v 0.2 -
	 * Handle "ALL_ITEMS" rule value v 0.1 -
	 */
	private boolean applyRules(StringBuilder sb) throws SQLException {

		logger.debug("applyRules");

		if (!useRules) {
			//if use rules setting is overriden, i.e. it's false while the query has a #rules# label, remove label and put dummy condition
			String querySql = sb.toString();
			querySql = querySql.replaceAll("(?iu)#rules#", "1=1");

			//update sb with new sql
			sb.replace(0, sb.length(), querySql);

			return true; //don't process any further
		}

		String ruleName;
		String columnName;
		String columnDataType;
		Statement st; // Should I create a statement for each (nested) resultset???
		ResultSet rs;
		int insertPosLast = 0;

		boolean successfullyApplied = true; // use variable to return method value instead of having multiple return statements

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
		boolean usingLabelledRules = false;
		String querySql = sb.toString();
		int labelPosition = querySql.toLowerCase().indexOf("#rules#"); //use all lowercase to make find case insensitive
		if (labelPosition != -1) {
			usingLabelledRules = true;
		}

		// Get statement
		st = conn.createStatement();
		// Get rules for the current query
		rs = st.executeQuery("SELECT RULE_NAME, FIELD_NAME, FIELD_DATA_TYPE FROM ART_QUERY_RULES WHERE QUERY_ID=" + queryId);

		// for each rule build and add the AND column IN (list) string to the query
		// Note: if we don't have rules for this query, the sb is left untouched
		while (rs.next()) {
			count++;

			StringBuilder tmpSb;
			ruleName = rs.getString("RULE_NAME");
			columnName = rs.getString("FIELD_NAME");
			columnDataType = rs.getString("FIELD_DATA_TYPE");

			tmpSb = getRuleValues(conn, username, ruleName, 1, columnDataType);
			String groupValues = getGroupRuleValues(ruleName, columnName, columnDataType);
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
					successfullyApplied = false;
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
							labelledValues.append(" AND " + condition);
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
							sb.append(" AND " + condition);
						}
					}
				}
			}
		}

		rs.close();
		st.close();

		//replace all occurrences of labelled rule with rule values
		if (usingLabelledRules) {
			//replace rule values
			String replaceString = Matcher.quoteReplacement(labelledValues.toString()); //quote in case it contains special regex characters
			querySql = querySql.replaceAll("(?iu)#rules#", replaceString);
			//update sb with new sql
			sb.replace(0, sb.length(), querySql);
		}

		//return rules application status
		return successfullyApplied;
	}

	/**
	 * Get rule values for a user
	 *
	 * @param conn
	 * @param ruleUsername
	 * @param currentRule
	 * @param counter
	 * @return rule values
	 * @throws SQLException
	 */
	public StringBuilder getRuleValues(Connection conn, String ruleUsername, String currentRule, int counter, String columnDataType)
			throws SQLException {

		StringBuilder tmpSb = new StringBuilder(64);
		boolean isAllItemsForThisRule = false;

		// Exit after MAX_RECURSIVE_LOOKUP calls
		// this is to avoid a situation when user A lookups user B
		// and viceversa
		if (counter > MAX_RECURSIVE_LOOKUP) {
			logger.warn("TOO MANY LOOPS - exiting");
			return new StringBuilder("TOO MANY LOOPS");
		}

		// Retrieve user's rule value for this rule
		PreparedStatement ps;
		ResultSet rs;
		String sql;

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
					if (StringUtils.equals(columnDataType, "NUMBER") && NumberUtils.isNumber(ruleValue)) {
						//don't quote numbers
						tmpSb.append("," + ruleValue);
					} else {
						//escape and quote non-numbers
						tmpSb.append(",'" + escapeSql(ruleValue) + "'");
					}
				}
			} else {
				isAllItemsForThisRule = true;
				break;
			}
		}
		ps.close();
		rs.close();

		if (!isAllItemsForThisRule) {
			// return the <list> for the current rule and user
			return tmpSb;
		}

		return null;
	}

	/**
	 * Get rule values for the user's user groups
	 *
	 * @param columnName
	 * @param columnDataType
	 * @return
	 */
	private String getGroupRuleValues(String ruleName, String columnName, String columnDataType) throws SQLException {

		//get user's user groups
		PreparedStatement ps;
		ResultSet rs;
		String sql;

		sql = "SELECT USER_GROUP_ID "
				+ " FROM ART_USER_GROUP_ASSIGNMENT "
				+ " WHERE USERNAME=? ";

		ps = conn.prepareStatement(sql);
		ps.setString(1, username);

		rs = ps.executeQuery();

		int count = 0;
		StringBuilder valuesSb = new StringBuilder(512);

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
					valuesSb.append(" OR " + condition);
				}
			}
		}
		ps.close();
		rs.close();

		return valuesSb.toString();
	}

	// escape the ' char in a parameter value (used in multi params)
	private String escapeSql(String s) {
		return StringEscapeUtils.escapeSql(s);
	}

	/**
	 * Inline parameters are substituted with ?, a TreeMap (treeInline) is built
	 * to store the param position
	 */
	private void applyInlineParameters(StringBuilder sb) throws SQLException {
		// Change applied by Giacomo Ferrari on 2005-09-23
		//  to perform the padding during inline prameter replacement.
		//  in order to leave unchanged the original length of SQL string
		final String blanks = "                                                       "; //any length as long as we don't have a parameter label of longer length


		if (inlineParams == null) {
			return;
		}

		String paramLabel;
		String paramValue;
		int startPos;

		treeInline = new TreeMap<Integer, String>();

		if (queryType == 112 || queryType == 113 || queryType == 114) {
			//mdx query		
			String querySql = sb.toString();
			for (Map.Entry<String, String> entry : inlineParams.entrySet()) {
				paramLabel = entry.getKey();
				paramValue = entry.getValue();

				String searchString = Pattern.quote("#" + paramLabel + "#"); //quote in case it contains special regex characters
				String replaceString = Matcher.quoteReplacement(paramValue); //quote in case it contains special regex characters
				querySql = querySql.replaceAll("(?iu)" + searchString, replaceString); //(?iu) makes replace case insensitive across unicode characters
			}

			//update sb with new sql
			sb.replace(0, sb.length(), querySql);
		} else {
			//replace all inline parameters that use direct substitution first
			if (htmlParams == null) {
				ArtQuery aq = new ArtQuery();
				htmlParams = aq.getHtmlParams(queryId);
			}

			for (Map.Entry<String, String> entry : inlineParams.entrySet()) {
				paramLabel = entry.getKey();
				paramValue = entry.getValue();
				ArtQueryParam param = htmlParams.get("P_" + paramLabel);
				if (param != null) {
					if (param.usesDirectSubstitution()) {
						String querySql = sb.toString();

						//some precaution
						if (paramValue != null) { //can be null if htmlparams was null and was created here
							paramValue = paramValue.replace("'", "''").replace("--", "").replace(";", "");
						}

						String searchString = Pattern.quote("#" + paramLabel + "#"); //quote in case it contains special regex characters
						String replaceString = Matcher.quoteReplacement(paramValue); //quote in case it contains special regex characters
						querySql = querySql.replaceAll("(?iu)" + searchString, replaceString); //(?iu) makes replace case insensitive across unicode characters

						//update sb with new sql
						sb.replace(0, sb.length(), querySql);
					}
				}
			}

			//replace other inline parameters
			for (Map.Entry<String, String> entry : inlineParams.entrySet()) {
				paramLabel = entry.getKey();  // inline label without #

				//replace occurrences of param labels with ? one by one so that correct ps.set methods are used in prepareInlineParameters()
				//can't do replaceAll because there will be a mismatch with the ps.set methods

				startPos = sb.toString().indexOf("#" + paramLabel + "#"); //find #label#
				Object checker;

				// increased maximum to avoid loop from 30 to 200 by Giacomo Ferrari on 2005-09-23
				// while (startPos != -1 && i++<30) {
				while (startPos != -1 && startPos < sb.toString().length()) {
					checker = treeInline.get(new Integer(startPos));

					if (checker != null) {
						logger.warn("Another parameter already stored at position {}. Cannot store {}!", startPos, paramLabel);
					}

					treeInline.put(new Integer(startPos), paramLabel); // stores the param name and its position. The order of position will ensure correct substitution in prepareInlineParameters()

					logger.debug("Storing parameter {} found at position {}", paramLabel, startPos);

					// replace inline label with ' ? ' plus the correct number of blanks so that total string length is not changed
					// +2 is to consider the #, -3 is the chars used by ' ? ' replacement
					sb.replace(startPos, startPos + paramLabel.length() + 2, " ? " + blanks.substring(0, (paramLabel.length() + 2 - 3)));

					logger.debug("Sql string is \n{}", sb.toString());
					logger.debug("Sql string length is {}", sb.toString().length());

					// find another occurence of the same param
					startPos = sb.toString().indexOf("#" + paramLabel + "#", startPos + paramLabel.length() + 2);
				}
			}

		}
	}

	/**
	 * Dynamic SQL is parsed, evaluated and the sb is modified according
	 */
	private void applyDynamicSQL(StringBuilder sb) throws ArtException {
		applyDynamicSQL(sb, false);
	}

	/**
	 * Dynamic SQL is parsed, evaluated and the sb is modified according
	 */
	private void applyDynamicSQL(StringBuilder sb, boolean useFilterIfText) throws ArtException {
		String element, xmlText, exp1, exp2, op, tmp;
		XmlInfo xinfo;


		/*
		 * // <PROPS> element element= "PROPS"; xinfo =
		 * XmlParser.getXmlElementInfo(sb.toString(), element, 0); if ( xinfo !=
		 * null) { propsMap = new Hashtable(); String props = xinfo.getText();
		 * if (DEBUG) System.out.println("Art- PROPS tag detected: " + props);
		 * String[] lines = props.split("\n"); for (String s: lines) { String[]
		 * pair = s.split("="); if (pair.length == 2)
		 * propsMap.put(pair[0],pair[1]); } if (DEBUG) System.out.println("Art-
		 * PROPS tag detected - Map is:\n" + propsMap);
		 *
		 * // replace the code in the SQL with the "" text // +3 is to handle
		 * </ and > chars around the IF end tag
		 * sb.replace(xinfo.getStart(),xinfo.getEnd()+element.length()+3,""); if
		 * (DEBUG) System.out.println("Art- PROPS tag detected - Remaining SQL
		 * is:\n" + sb.toString());
		 *
		 * }
		 * // </PROPS>
		 */

		// <IF> element
		element = "IF";

		// XmlInfo stores the text between a tag as well as
		// the start and end position of the tag
		xinfo = XmlParser.getXmlElementInfo(sb.toString(), element, 0);

		while (xinfo != null) {
			xmlText = xinfo.getText(); // stores xml code between the IF element

			exp1 = XmlParser.getXmlElementValue(xmlText, "EXP1"); // get text between the EXP1 element, returns null if the element does not exists
			op = XmlParser.getXmlElementValue(xmlText, "OP");
			exp2 = XmlParser.getXmlElementValue(xmlText, "EXP2");

			// transform nulls to empty string
			if (exp1 == null) {
				exp1 = "";
			}
			if (exp2 == null) {
				exp2 = "";
			}
			if (op == null) {
				op = "";
			}

			//expression and expression values may be different
			String exp1Value = exp1;
			String exp2Value = exp2;
			String opValue = op;

			//get inline params
			if (inlineParams != null) {
				// Get inline param value for exp1 (if it is an inline param)
				if (exp1.startsWith("#") && exp1.endsWith("#") && exp1.length() > 2) {
					exp1Value = inlineParams.get(exp1.substring(1, exp1.length() - 1));
				}

				// Get inline param value for exp2 (if it is an inline param)
				if (exp2.startsWith("#") && exp2.endsWith("#") && exp2.length() > 2) {
					exp2Value = inlineParams.get(exp2.substring(1, exp2.length() - 1));
				}

				// Get inline param value for op (if it is an inline param)
				if (op.startsWith("#") && op.endsWith("#") && op.length() > 2) {
					opValue = inlineParams.get(op.substring(1, op.length() - 1));
				}
			}

			//enable use of same lov for chained and non-chained parameters			
			if (StringUtils.equals(exp1, "#filter#") && (StringUtils.equalsIgnoreCase(op, "is not null") || StringUtils.equalsIgnoreCase(op, "is not blank"))) {
				if (useFilterIfText) {
					exp1Value = "#filter#"; //any string. just so that if condition is returned
				} else if (!useFilterIfText && inlineParams == null) {
					exp1Value = ""; //empty string. so that else value is returned					
				}
			}

			if (evaluateIF(exp1Value, opValue, exp2Value)) {
				tmp = XmlParser.getXmlElementValue(xmlText, "TEXT");
			} else {
				tmp = XmlParser.getXmlElementValue(xmlText, "ELSETEXT");
				if (tmp == null) {
					tmp = "";
				}
			}

			// replace the code in the SQL with the text
			// +3 is to handle </ and > chars around the IF end tag
			sb.replace(xinfo.getStart(), xinfo.getEnd() + element.length() + 3, tmp);

			// check next element
			xinfo = XmlParser.getXmlElementInfo(sb.toString(), element, xinfo.getStart() + tmp.length());
		}
		// </IF>

	}

	/**
	 * Evaluate the IF element in Dynamic SQL
	 */
	private boolean evaluateIF(String exp1, String op, String exp2)
			throws ArtException {
		// transform null to empty strings
		if (op == null) {
			op = "";
		} else {
			op = op.trim().toLowerCase();
		}
		//although since exp1,exp2 come from parameter values, they can never be null. maybe only empty string
		if (exp1 == null) {
			exp1 = "";
		}
		if (exp2 == null) {
			exp2 = "";
		}

		//enable case sensitive comparisons
		String csExp1 = exp1;
		String csExp2 = exp2;

		//make operands lowercase for case insensitive comparisons
		exp1 = exp1.trim().toLowerCase();
		exp2 = exp2.trim().toLowerCase();

		//evaluate conditions
		if (op.equals("eq") || op.equals("equals")) { // -- equals
			return exp1.equals(exp2);

		} else if (op.equals("neq") || op.equals("not equals")) { // -- not equals
			return !exp1.equals(exp2);

		} else if (op.equals("la")) { // ----------------- less than  (alpha)
			return (exp1.compareTo(exp2) < 0 ? true : false);

		} else if (op.equals("ga")) { // ----------------- great than (alpha)
			return (exp1.compareTo(exp2) > 0 ? true : false);

		} else if (op.equals("ln")) { // ----------------- less than  (numbers)
			try {
				double e1 = Double.parseDouble(exp1);
				double e2 = Double.parseDouble(exp2);
				return (e1 < e2 ? true : false);
			} catch (Exception e) {
				logger.error("Error", e);
				throw new ArtException("<br>Not able to convert to a number &lt;EXP1&gt;" + exp1 + "&lt;/EXP1&gt; or &lt;EXP2&gt;" + exp2 + "&lt;/EXP2&gt;");
			}

		} else if (op.equals("gn")) { // ----------------- great than (numbers)
			try {
				double e1 = Double.parseDouble(exp1);
				double e2 = Double.parseDouble(exp2);
				return (e1 > e2 ? true : false);
			} catch (Exception e) {
				logger.error("Error", e);
				throw new ArtException("<br>Not able to convert to a number &lt;EXP1&gt;" + exp1 + "&lt;/EXP1&gt; or &lt;EXP2&gt;" + exp2 + "&lt;/EXP2&gt;");
			}

		} else if (op.equals("is blank") || op.equals("is null")) { // ------------ is empty string. "is null" for backward compatibility. exp can never be null string
			return exp1.equals("");

		} else if (op.equals("is not blank") || op.equals("is not null")) { // -------- is not empty string. "is not null" for backward compatibility			
			return !exp1.equals("");

		} else if (op.equals("starts with")) { // -------- startsWith
			return exp1.startsWith(exp2);

		} else if (op.equals("ends with")) { // ---------- ensWith
			return exp1.endsWith(exp2);

		} else if (op.equals("contains")) { // ----------- contains
			return (exp1.indexOf(exp2) != -1 ? true : false);

		} else if (op.equals("eq cs") || op.equals("equals cs")) { // -- equals case sensitive
			return csExp1.equals(csExp2);

		} else if (op.equals("neq cs") || op.equals("not equals cs")) { // -- not equals case sensitive
			return !csExp1.equals(csExp2);

		} else if (op.equals("la cs")) { // ----------------- less than  (alpha) case sensitive
			return (csExp1.compareTo(csExp2) < 0 ? true : false);

		} else if (op.equals("ga cs")) { // ----------------- great than (alpha) case sensitive
			return (csExp1.compareTo(csExp2) > 0 ? true : false);

		} else if (op.equals("starts with cs")) { // -------- startsWith case sensitive
			return csExp1.startsWith(csExp2);

		} else if (op.equals("ends with cs")) { // ---------- ensWith case sensitive
			return csExp1.endsWith(csExp2);

		} else if (op.equals("contains cs")) { // ----------- contains case sensitive
			return (csExp1.indexOf(csExp2) != -1 ? true : false);

		} else {
			throw new ArtException("<br>Not able to evaluate IF condition, the operator &lt;OP&gt;" + op + "&lt;/OP&gt; is not recognized");
		}

	}

	/**
	 * Process multi parameters and generate a hash map with parameter name and
	 * values. To be used for jasper reports
	 *
	 * @param querySql
	 * @return multi parameters to be used with jasper reports
	 */
	public Map<String, List> getJasperMultiParams(String querySql) {

		try {

			conn = ArtDBCP.getConnection();
			StringBuilder builder = new StringBuilder(1024 * 2);
			builder.append(querySql);

			applyMultiParameters(builder);

		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		return jasperMultiParams;
	}

	/**
	 * Process multi parameters and generate a map with parameter label and
	 * values. To be used for jxls reports
	 *
	 * @param querySql
	 * @return multi parameters to be used for jxls reports
	 */
	public Map<String, String> getJxlsMultiParams(String querySql) {

		try {

			conn = ArtDBCP.getConnection();
			StringBuilder builder = new StringBuilder(1024 * 2);
			builder.append(querySql);

			applyMultiParameters(builder);

		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		return jxlsMultiParams;
	}

	/**
	 * Multi parameters as applied to the SQL
	 */
	private void applyMultiParameters(StringBuilder sb) throws SQLException {

		logger.debug("applyMultiParameters");

		if (multiParams == null) {
			return;
		}

		Iterator it;

		// Enable looking up of param label (column name for non-labelled params) from the html name
		if (htmlParams == null) {
			ArtQuery aq = new ArtQuery();
			htmlParams = aq.getHtmlParams(queryId);
		}

		//check if query uses labelled multi parameters
		boolean hasLabelledMultiParams = false;
		String paramLabel;
		String querySql;

		querySql = sb.toString();
		it = htmlParams.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String htmlName = (String) entry.getKey();
			if (htmlName.startsWith("M_")) {
				//this is a multi parameter
				ArtQueryParam param = (ArtQueryParam) entry.getValue();
				paramLabel = param.getParamLabel();

				int foundPosition = querySql.toLowerCase().indexOf("#" + paramLabel.toLowerCase() + "#"); //use all lowercase to make find case insensitive
				if (foundPosition != -1) {
					hasLabelledMultiParams = true;
					break;
				}
			}
		}

		//process multi parameters
		if (hasLabelledMultiParams) {
			//process labelled multi parameters

			//replace multi parameters where a subset of values was selected
			it = multiParams.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				String paramId = (String) entry.getKey();
				String htmlName = "M_" + paramId; //may be M_1 etc - pre 2.2, or M_label
				String[] paramValues = (String[]) entry.getValue();

				List<String> paramValuesList = new ArrayList<String>(); //list of parameter values as is. used by jasper reports and mdx queries
				List<String> escapedValuesList = new ArrayList<String>(); //list of parameter values, possible escaped using single quotes. used by art queries and jxls queries

				String currentValue;
				String escapedValue;
				int parameterNumber;
				String paramDataType; //don't quote integer/number parameters i.e. where int_col in ('1','2') may not work on some databases e.g. hsqldb 2.x

				ArtQueryParam param = htmlParams.get(htmlName);
				paramDataType = param.getParamDataType();

				//build string of values to go into IN clause of sql
				for (parameterNumber = 0; parameterNumber < paramValues.length; parameterNumber++) {
					currentValue = paramValues[parameterNumber];

					//don't quote numbers. some databases won't do implicit conversion where column is numeric
					//confirm that they are numbers to avoid sql injection                        
					if (StringUtils.equals(paramDataType, "NUMBER") && NumberUtils.isNumber(currentValue)) {
						//don't quote numbers
						escapedValuesList.add(currentValue);
					} else {
						//escape and quote non-numbers
						escapedValue = escapeSql(currentValue);
						escapedValue = "'" + escapedValue + "'";
						escapedValuesList.add(escapedValue);
					}

					paramValuesList.add(currentValue);
				}
				//build comma separated list of values to use in the sql
				String finalEscapedValues = StringUtils.join(escapedValuesList, ",");

				//get param label. in case M_1 etc was used - pre 2.2                
				paramLabel = param.getParamLabel();

				//populate jasper multi parameters hash map
				jasperMultiParams.put(paramLabel, paramValuesList);

				//populate jxls multi parameters hash table
				jxlsMultiParams.put(paramLabel, finalEscapedValues);

				//replace all occurrences of labelled multi parameter with valid sql syntax
				String replaceValue;
				if (queryType == 112 || queryType == 113 || queryType == 114) {
					replaceValue = StringUtils.join(paramValuesList, ",");
				} else {
					replaceValue = finalEscapedValues;
				}
				String searchString = Pattern.quote("#" + paramLabel + "#"); //quote in case it contains special regex characters
				String replaceString = Matcher.quoteReplacement(replaceValue); //quote in case it contains special regex characters
				querySql = querySql.replaceAll("(?iu)" + searchString, replaceString); //(?iu) makes replace case insensitive across unicode characters
			}

			//replace any multi parameters that haven't been replaced yet. these are the ones where ALL_ITEMS was selected or all values are to be used
			it = htmlParams.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				String htmlName = (String) entry.getKey();
				if (htmlName.startsWith("M_")) {
					ArtQueryParam param = (ArtQueryParam) entry.getValue();
					paramLabel = param.getParamLabel();
					String paramDataType = param.getParamDataType();

					//check if parameter is yet to be replaced					
					if (StringUtils.containsIgnoreCase(querySql, "#" + paramLabel + "#")) {
						//replace parameter with all possible values
						List<String> finalValuesList = getAllParameterValues(paramLabel); //return all values from the parameter's lov query

						if (finalValuesList != null && finalValuesList.size() > 0) {

							List<String> paramValuesList = new ArrayList<String>(); //list of parameter values as is. used by jasper reports and mdx queries
							List<String> escapedValuesList = new ArrayList<String>(); //list of parameter values, possible escaped using single quotes. used by art queries and jxls queries

							String currentValue;
							String escapedValue;
							int i;
							//add all except last item
							for (i = 0; i < finalValuesList.size(); i++) {
								currentValue = finalValuesList.get(i);

								//don't quote numbers. some databases won't do implicit conversion where column is numeric
								//confirm that they are numbers to avoid sql injection                        
								if (StringUtils.equals(paramDataType, "NUMBER") && NumberUtils.isNumber(currentValue)) {
									escapedValuesList.add(currentValue);
								} else {
									//escape and quote non-numbers
									escapedValue = escapeSql(currentValue);
									escapedValue = "'" + escapedValue + "'";
									escapedValuesList.add(escapedValue);
								}

							}
							//build comma separated list of values to use in the sql
							String finalEscapedValues = StringUtils.join(escapedValuesList, ",");

							//populate jasper multi parameters hash map
							paramValuesList.addAll(finalValuesList);
							jasperMultiParams.put(paramLabel, paramValuesList);

							//populate jxls multi parameters hash table
							jxlsMultiParams.put(paramLabel, finalEscapedValues);

							//replace all occurrences of labelled multi parameter with valid sql syntax
							String replaceValue;
							if (queryType == 112 || queryType == 113 || queryType == 114) {
								replaceValue = StringUtils.join(finalValuesList, ",");
							} else {
								replaceValue = finalEscapedValues;
							}
							String searchString = Pattern.quote("#" + paramLabel + "#"); //quote in case it contains special regex characters
							String replaceString = Matcher.quoteReplacement(replaceValue); //quote in case it contains special regex characters
							querySql = querySql.replaceAll("(?iu)" + searchString, replaceString); //(?iu) makes replace case insensitive across unicode characters
						}
					}
				}
			}

			//update sb with new sql
			sb.replace(0, sb.length(), querySql);
		} else {
			//process non-labelled multi parameters
			//note that multi parameters with All selected (ALL_ITEMS) never get passed. (they don't exist in the multiParams map)
			it = multiParams.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				String paramId = (String) entry.getKey();
				String htmlName = "M_" + paramId;
				String[] paramValues = (String[]) entry.getValue();

				//get param label. for non-labelled params, this is the column name
				ArtQueryParam param = htmlParams.get(htmlName);
				paramLabel = param.getParamLabel();
				String paramDataType = param.getParamDataType();

				StringBuilder SqlAndParamIn = new StringBuilder(128);
				SqlAndParamIn.append(" AND " + paramLabel + " IN (");

				logger.debug("Number of parameters for {}/{} is {}", new Object[]{paramId, paramLabel, paramValues.length});

				String currentValue;
				String escapedValue;
				int parameterNumber;

				List<String> paramValuesList = new ArrayList<String>(); //list of parameter values as is. used by jasper reports and mdx queries
				List<String> escapedValuesList = new ArrayList<String>(); //list of parameter values, possible escaped using single quotes. used by art queries and jxls queries

				for (parameterNumber = 0; parameterNumber < paramValues.length; parameterNumber++) {
					currentValue = paramValues[parameterNumber];

					//don't quote numbers. some databases won't do implicit conversion where column is numeric
					//confirm that they are numbers to avoid sql injection                        
					if (StringUtils.equals(paramDataType, "NUMBER") && NumberUtils.isNumber(currentValue)) {
						escapedValuesList.add(currentValue);
					} else {
						//escape and quote non-numbers
						escapedValue = escapeSql(currentValue);
						escapedValue = "'" + escapedValue + "'";
						escapedValuesList.add(escapedValue);
					}

					paramValuesList.add(currentValue);
				}
				//build comma separated list of values to use in the sql
				String finalEscapedValues = StringUtils.join(escapedValuesList, ",");

				SqlAndParamIn.append(finalEscapedValues);
				SqlAndParamIn.append(") ");

				//populate jasper multi parameters hash map
				jasperMultiParams.put(paramLabel, paramValuesList);

				//populate jxls multi parameters hash table
				jxlsMultiParams.put(paramLabel, finalEscapedValues);

				/*
				 * The line: AND PARAM IN ( 'value1', 'value2', ... , 'valueN')
				 * is completed (and stored in SqlAndParamIn); we can add it to
				 * the prepared statement (only if allItems is false) We had to
				 * handle the case where a GROUP BY or a ORDER BY expression is
				 * present (the case in which we have a HAVING without GROUP BY
				 * is not considered).
				 */
				/**
				 * NOTE: the 'GROUP BY' and 'ORDER BY' string on the (main query
				 * of the) Prepared Statement must be in UPPERCASE and separated
				 * with a single blank. So nested queries should have the words
				 * 'GROUP BY' or 'ORDER BY' in lower case.
				 *
				 * NOTE2: the AND before the IN could be erroneous if we have
				 * nothing after the WHERE => workaround set a dummy condition
				 * WHERE 1 = 1
				 */
				int grb = sb.toString().lastIndexOf("GROUP BY");
				//int hvg = SqlQueryBuf.toString().lastIndexOf("HAVING");
				int orb = sb.toString().lastIndexOf("ORDER BY");

				if ((grb != -1) || (orb != -1)) {
					// We have a GROUP BY or an ORDER BY clause
					int pos = ((grb > orb) && (orb > 0) ? orb : (grb == -1 ? orb : grb));
					sb.insert(pos, SqlAndParamIn.toString());

					logger.debug("Multiple - IN inserted pos: {} because of GROUP BY or ORDER BY", pos);
				} else { // We can just append
					sb.append(SqlAndParamIn.toString());
					logger.debug("Multiple - IN appended");
				}
			}
		}
	}

	//return all values from the parameter's lov query
	private List<String> getAllParameterValues(String paramName) throws SQLException {

		List<String> finalValuesList = new ArrayList<String>();

		StringBuilder queryBuilder = new StringBuilder(512);

		int databaseId = 0;
		Connection connLov = null;
		PreparedStatement psLovQuery = null;
		ResultSet rsLovQuery = null;
		PreparedStatement psLovValues = null;
		ResultSet rsLovValues = null;

		try {
			//get the lov query's sql
			String sqlLovQuery = "SELECT AAS.SOURCE_INFO, AQ.DATABASE_ID, AQ.QUERY_TYPE, "
					+ " AQF.CHAINED_PARAM_POSITION, AQF.CHAINED_VALUE_POSITION "
					+ " FROM ART_QUERY_FIELDS AQF, ART_ALL_SOURCES AAS, ART_QUERIES AQ "
					+ " WHERE AQF.LOV_QUERY_ID = AAS.OBJECT_ID AND AAS.OBJECT_ID = AQ.QUERY_ID"
					+ " AND AQF.QUERY_ID = ? "
					+ " AND AQF.PARAM_TYPE = 'M' AND AQF.USE_LOV='Y' "
					+ " AND AQF.PARAM_LABEL = ?"
					+ " ORDER BY AAS.LINE_NUMBER";

			psLovQuery = conn.prepareStatement(sqlLovQuery);
			psLovQuery.setInt(1, queryId);
			psLovQuery.setString(2, paramName);

			rsLovQuery = psLovQuery.executeQuery();

			//build complete sql string for lov query
			int lovQueryType = 0;
			int chainedParamPosition = 0;
			int chainedValuePosition = 0;
			while (rsLovQuery.next()) {
				queryBuilder.append(rsLovQuery.getString("SOURCE_INFO"));
				databaseId = rsLovQuery.getInt("DATABASE_ID");
				lovQueryType = rsLovQuery.getInt("QUERY_TYPE");
				chainedParamPosition = rsLovQuery.getInt("CHAINED_PARAM_POSITION");
				chainedValuePosition = rsLovQuery.getInt("CHAINED_VALUE_POSITION");
			}

			if (queryBuilder.length() > 0) {
				//lov found. run lov to get and build all possible parameter values

				if (lovQueryType == 120) {
					//static lov. values coming from static values defined in sql source
					String items = queryBuilder.toString();
					String lines[] = items.split("\\r?\\n");
					for (String line : lines) {
						String[] values = line.trim().split("\\|");
						finalValuesList.add(values[0]);
					}
				} else {
					//dynamic lov

					//apply tags. in case they exist
					applyTags(queryBuilder);

					//apply dynamic sql. in case same lov used for chained and no-chained parameters
					try {
						applyDynamicSQL(queryBuilder, true); //return if text if #filter# is not null is in query
					} catch (ArtException e) {
						logger.error("Error", e);
					}

					//perform more replacements
					String lovSql = queryBuilder.toString();

					//replace rules if the label exists, with dummy condition. so that lov query executes without error
					lovSql = lovSql.replaceAll("(?iu)#rules#", "1=1");

					//replace #filter# parameter if it exists, for chained parameters
					int filterPosition;
					if (chainedParamPosition > 0 && chainedValuePosition > 0) {
						filterPosition = chainedValuePosition;
					} else {
						filterPosition = chainedParamPosition;
					}
					if (filterPosition > 0) {
						//parameter chained on another parameter. get filter parameter html name
						ArtQueryParam param = new ArtQueryParam();
						String filterLabel;
						String valueParamHtmlName = param.getHtmlName(queryId, filterPosition);
						//get filter value. 						
						if (StringUtils.startsWith(valueParamHtmlName, "P_")) {
							filterLabel = valueParamHtmlName.substring(2);
							String filterValue = inlineParams.get(filterLabel);

							String replaceString = Matcher.quoteReplacement(filterValue); //quote in case it contains special regex characters
							lovSql = lovSql.replaceAll("(?iu)#filter#", replaceString);
						} else if (StringUtils.startsWith(valueParamHtmlName, "M_")) {
							//filter value can actually never come from multi parameter. limitation of ajaxtags
							ArtQueryParam filterParam = htmlParams.get(valueParamHtmlName);
							if (filterParam != null) {
								filterLabel = filterParam.getParamLabel();
								String[] filterValues = multiParams.get(filterLabel);

								List<String> escapedValuesList = new ArrayList<String>(); //list of parameter values, possible escaped using single quotes. 

								String currentValue;
								String escapedValue;
								int parameterNumber;
								String paramDataType; //don't quote integer/number parameters i.e. where int_col in ('1','2') may not work on some databases e.g. hsqldb 2.x

								paramDataType = param.getParamDataType();

								//build string of values to go into IN clause of sql
								for (parameterNumber = 0; parameterNumber < filterValues.length; parameterNumber++) {
									currentValue = filterValues[parameterNumber];

									//don't quote numbers. some databases won't do implicit conversion where column is numeric
									//confirm that they are numbers to avoid sql injection                        
									if (StringUtils.equals(paramDataType, "NUMBER") && NumberUtils.isNumber(currentValue)) {
										escapedValuesList.add(currentValue);
									} else {
										//escape and quote non-numbers
										escapedValue = escapeSql(currentValue);
										escapedValue = "'" + escapedValue + "'";
										escapedValuesList.add(escapedValue);
									}
								}
								//build comma separated list of values to use in the sql
								String finalEscapedValues = StringUtils.join(escapedValuesList, ",");

								//replace #filter# with parameter values
								String replaceString = Matcher.quoteReplacement(finalEscapedValues); //quote in case it contains special regex characters
								lovSql = lovSql.replaceAll("(?iu)#filter#", replaceString);
							}
						}
					}

					connLov = ArtDBCP.getConnection(databaseId);
					psLovValues = connLov.prepareStatement(lovSql);
					rsLovValues = psLovValues.executeQuery();

					int rowCount = 0;
					while (rsLovValues.next()) {
						rowCount++;
						finalValuesList.add(rsLovValues.getString(1));
					}
				}
			}
		} finally {
			//close recordsets and lov query database connection
			if (psLovQuery != null) {
				psLovQuery.close();
			}
			if (rsLovQuery != null) {
				rsLovQuery.close();
			}
			if (psLovValues != null) {
				psLovValues.close();
			}
			if (rsLovValues != null) {
				rsLovValues.close();
			}
			if (connLov != null) {
				connLov.close();
			}
		}

		return finalValuesList;
	}

	/**
	 * Replace :TAGS
	 */
	private void applyTags(StringBuilder sb) {
		logger.debug("applyTags");

		/*
		 * Update query :TAG
		 */

		String querySql = sb.toString();


		/*
		 * :USERNAME substitution with logged username
		 */
		String searchString = Pattern.quote(":username"); //quote in case it contains special regex characters
		String replaceString = Matcher.quoteReplacement("'" + username + "'"); //quote in case it contains special regex characters
		querySql = querySql.replaceAll("(?iu)" + searchString, replaceString); //(?iu) makes replace case insensitive across unicode characters


		/*
		 * :DATE substitution with current date in 'YYYY-MM-DD' format
		 */
		java.util.Date today = new java.util.Date();

		String dateFormat = "yyyy-MM-dd";
		SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
		String date = dateFormatter.format(today);

		String timeFormat = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat timeFormatter = new SimpleDateFormat(timeFormat);
		String time = timeFormatter.format(today);

		querySql = querySql.replaceAll("(?iu):date", "'" + date + "'");
		querySql = querySql.replaceAll("(?iu):time", "'" + time + "'");


		//update sb with new sql
		sb.replace(0, sb.length(), querySql);

		logger.debug("Sql query now is:\n{}", sb);


		/*
		 * :DRILL(label, query_id, group_id, mode, concat, param1, param2, ...)
		 * automatic substitution
		 */

		/*
		 * Other ideas: DAY, MONTH, YEAR automatic substitution HOUR, MINUTE,
		 * YEAR automatic substitution ... automatic substitution con o senza
		 * apici etc
		 */

	}

	/**
	 * Called by the prepareStatement() method. The prepared statement is
	 * "fulfilled" with the parameters
	 */
	private void prepareInlineParameters(PreparedStatement ps) throws SQLException, ArtException {

		logger.debug("prepareInlineParameters");

		//set final sql. replace parameter placeholders ( ? ) with actual parameter values passed to database
		finalSQL = sb.toString();

		if (treeInline != null && !treeInline.isEmpty()) {

			Iterator paramNames = treeInline.values().iterator(); //contains parameters which were actually found and replaced in the sql source in the order they were found
			int i = 0; //parameter index/order of appearance
			String paramName, paramValue;

			java.util.Date dateValue = new java.util.Date();

			if (htmlParams == null) {
				ArtQuery aq = new ArtQuery();
				htmlParams = aq.getHtmlParams(queryId);
			}

			while (paramNames.hasNext()) {
				paramName = (String) paramNames.next();
				paramValue = inlineParams.get(paramName);
				ArtQueryParam aqp = htmlParams.get("P_" + paramName);
				String paramDataType = "VARCHAR";
				if (aqp != null) { //"filter" param used with chained params will not exist in htmlparams map
					paramDataType = aqp.getParamDataType();
				}

				i++; //increment parameter index

				logger.debug("Parameter name={}, index={}, data type={}, value={}", new Object[]{paramName, i, paramDataType, paramValue});

				if (paramDataType.equals("INTEGER")) {
					if (ps != null) { //ps can be null for jasper report
						//default empty strings to 0 so as not to have error when executing with default parameter values
						if (StringUtils.isBlank(paramValue)) {
							paramValue = "0";
						}
						ps.setInt(i, Integer.parseInt(paramValue));
					}
					jasperInlineParams.put(paramName, Long.parseLong(paramValue));
				} else if (paramDataType.equals("NUMBER")) {
					if (ps != null) {
						//default empty strings to 0 so as not to have error when executing with default parameter values
						if (StringUtils.isBlank(paramValue)) {
							paramValue = "0";
						}
						ps.setDouble(i, Double.parseDouble(paramValue));
					}
					jasperInlineParams.put(paramName, Double.parseDouble(paramValue));
				} else if (paramDataType.equals("DATE")) {
					dateValue = getDefaultValueDate(paramValue);
					if (ps != null) {
						ps.setDate(i, new java.sql.Date(dateValue.getTime()));
					}
					jasperInlineParams.put(paramName, dateValue);
				} else if (paramDataType.equals("DATETIME")) {
					dateValue = getDefaultValueDate(paramValue);
					if (ps != null) {
						ps.setTimestamp(i, new java.sql.Timestamp(dateValue.getTime()));
					}
					jasperInlineParams.put(paramName, dateValue);
				} else if (paramDataType.equals("VARCHAR") || paramDataType.equals("TEXT")) {
					if (ps != null) {
						ps.setString(i, paramValue);
					}
					jasperInlineParams.put(paramName, paramValue);
				}

				//set final sql. replace parameter placeholders ( ? ) with actual parameter values passed to database
				if (StringUtils.contains(paramDataType, "DATE")) {
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					finalSQL = StringUtils.replace(finalSQL, "?", "'" + df.format(dateValue) + "'", 1);
				} else if (StringUtils.equals(paramDataType, "INTEGER") || StringUtils.equals(paramDataType, "NUMBER")) {
					finalSQL = StringUtils.replace(finalSQL, "?", paramValue, 1);
				} else {
					finalSQL = StringUtils.replace(finalSQL, "?", "'" + escapeSql(paramValue) + "'", 1);
				}

			}

		}
	}

	/**
	 * Parse a string value that is supposed to: 1. be a valid date with format
	 * YYYY-MM-DD (returns it as it is) 2. be null, SYSDATE or NOW (returns
	 * current date in YYYY-MM-DD format) 3. follow the syntax "ADD
	 * DAYS|MONTHS|YEARS <integer>" (returns rolled date from current date in
	 * YYYY-MM-DD format)
	 *
	 * @param defaultValue
	 * @return date object that corresponds to the given string
	 */
	public static java.util.Date getDefaultValueDate(String defaultValue) {
		/*
		 * if default value has syntax "ADD DAYS|MONTHS|YEARS <integer>" or "Add
		 * day|MoN|Year <integer>" set default value as sysdate plus an offset
		 */

		if (defaultValue == null) {
			defaultValue = "";
		}

		if (defaultValue.toUpperCase().startsWith("ADD")) { // set an offset from today
			Calendar calendar = new GregorianCalendar();
			try {
				StringTokenizer st = new StringTokenizer(defaultValue.toUpperCase(), " ");
				if (st.hasMoreTokens()) {
					st.nextToken(); // skip 1st token
					String token = st.nextToken().trim(); // get 2nd token, i.e. one of DAYS, MONTHS or YEARS
					int field = (token.startsWith("YEAR") ? GregorianCalendar.YEAR : (token.startsWith("MON") ? GregorianCalendar.MONTH : GregorianCalendar.DAY_OF_MONTH));
					token = st.nextToken().trim(); // get last token, i.e. the offset (integer)
					int offset = Integer.parseInt(token);
					calendar.add(field, offset);
				}

				return calendar.getTime();

			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		//convert default date string as it is to a date
		String dateFormat;
		if (defaultValue.length() < 10) {
			dateFormat = "yyyy-M-d";
		} else if (defaultValue.length() == 10) {
			dateFormat = "yyyy-MM-dd";
		} else if (defaultValue.length() == 16) {
			dateFormat = "yyyy-MM-dd HH:mm";
		} else {
			dateFormat = "yyyy-MM-dd HH:mm:ss";
		}
		SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
		dateFormatter.setLenient(false); //don't allow invalid date strings to be coerced into valid dates

		java.util.Date dateValue;
		try {
			dateValue = dateFormatter.parse(defaultValue);
		} catch (ParseException e) {
			logger.debug("Defaulting {} to now", defaultValue, e);
			//string could not be converted to a valid date. default to now
			dateValue = new java.util.Date();
		}

		//return date
		return dateValue;

	}

	/**
	 * Process inline parameters and generate a hash map with parameter name and
	 * values. To be used for jasper reports
	 *
	 * @param querySql
	 * @return inline parameters to be used for jasper reports
	 */
	public Map<String, Object> getJasperInlineParams(String querySql) {

		try {

			conn = ArtDBCP.getConnection();
			StringBuilder builder = new StringBuilder(1024 * 2);
			builder.append(querySql);

			applyInlineParameters(builder);
			prepareInlineParameters(null);

		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		return jasperInlineParams;
	}
}
