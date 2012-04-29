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
/** PreparedQuery.java
 *
 * Caller:	QueryParameters, ArtJob
 * Purpose:	get the prepared statement  of the selected query
 *              apply smartRules
 *              add multi params
 *              apply inline params
 *              apply tags
 *              parse&apply dynamic SQL
 *		set bind parameters
 */
package art.utils;

import art.servlets.ArtDBCP;

import java.sql.*;
import java.util.*;
import java.text.*;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

/**
 * Execute query
 * 
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class PreparedQuery {

    final static Logger logger = LoggerFactory.getLogger(PreparedQuery.class);
    final int MAX_RECURSIVE_LOOKUP = 20;
    String username;
    int queryId;
    int queryDatabaseId = -1;
    StringBuffer sb;
    Map<String, String> bindParams;
    Map<String, String[]> multiParams;
    Map<String, String> inlineParams;
    TreeMap<Integer, String> treeInline; //  stores the inline values sorted by the ? in the SQL
    boolean adminSession = false;
    boolean useSmartRules = true;
    boolean isLov = false;
    PreparedStatement psQuery; // this is the ps object produced by this query
    Connection connQuery; // this is the connection to the target database for this query
    Connection conn; // connection to the art repository
    String preparedStatementSQL; //final sql statement. if query has inline parameters, sql will still have ?
    String queryStatus;
    Map<String, List> jasperMultiParams; //hash map will contain multi parameter name and values instead of parameter id e.g. M_2 and string array of values. for jasper reports
    Map<String, Object> jasperInlineParams; //hash map will contain inline parameter name and value as corresponding object e.g. Double, Long. for jasper reports
    Map<String, String> jxlsMultiParams; //hash map will contain multi parameter name and values instead of parameter id e.g. M_2 and string array of values. for jxls reports
    int queryType; //to enable special handling of template queries where sql source is not executed
    boolean viewingTextObject = false; //flag used to check if user has rights to edit a text object
    Map<String, ArtQueryParam> htmlParams; //all the queries parameters, with the html name as the key

    /**
     * 
     */
    public PreparedQuery() {
        sb = new StringBuffer(1024 * 2); // assume the average query is < 2kb

        jasperInlineParams = new HashMap<String, Object>(); //save parameters in special hash map for jasper reports
        jasperMultiParams = new HashMap<String, List>(); //to populate hash map with multi parameter names and values
        jxlsMultiParams = new HashMap<String, String>(); //save parameters in special hash map for jxls reports        
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
     * @return sql to be executed by database with ? where inline parameters should be
     */
    public String getPreparedStatementSQL() {
        return preparedStatementSQL;
    }

    /** Set the user who is executing the query
     * 
     * @param s 
     */
    public void setUsername(String s) {
        username = s;
    }

    /** Set the query id to execute
     * 
     * @param i 
     */
    public void setQueryId(int i) {
        queryId = i;
    }

    /** Set this flag to true to skip privileges checks when getting the SQL
     *  Default is false.
     * 
     * @param b 
     */
    public void isAdminSession(boolean b) {
        adminSession = b;
    }

    /** Set this flag to false to skip rules (used in lov)
     *  Default is true.
     * 
     * @param b 
     */
    public void isUseSmartRules(boolean b) {
        useSmartRules = b;
    }

    /** Set the multi parameters.
    The hastable contains:
    <br>the multiple parameter name (prefixed with the M* string) as key
    <br>the array of values (String[])
     * 
     * @param h 
     */
    public void setMultiParams(Map<String, String[]> h) {
        multiParams = h;
    }

    /** Set the map that contains the bind parameters.
    The map contains:
    <br>the bind parameter code (Py where y is the index of the ? or Py_year/Py_month/Py_day for dates) as key
    <br>the parameter value (String[])
     * 
     * @param h 
     */
    public void setBindParams(Map<String, String> h) {
        bindParams = h;
    }

    /** Set the map that contains the general purpose parameters.
    <br>Art will substiture the general param label with the
    value specified by the user
    The hastable contains:
    <br>the parameter label (String)
    <br>the parameter value (String)
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

    /** Returns the SQL query with:
     *   :TAG applied
     *   Dynamic SQL applied
     *   inline parameters converted to bind
     *   multi parameters and rules applied.
     */
    private String getQuerySQL() throws ArtException {

        /* Apply :Tags
         * (:TAG are substituted with their values)
         */
        try {
            applyTags(sb); // work inline in the sb
        } catch (Exception e) {
            logger.error("Error", e);
            throw new ArtException("<p>Error applying tags to the query. Please contact the ART administrator. <br>Details:<code> " + e + "</code></p>");
        }

        /* Apply Dynamic SQL
         * (parse the <IF> element - using inline parameters for EXP1, EXP2 and OP element if needed - to evaluate condition)
         */
        try {
            applyDynamicSQL(sb); // work inline in the sb
        } catch (Exception e) {
            logger.error("Error", e);
            throw new ArtException("<p>Error applying dynamic SQL to the query. Please contact the ART administrator. <br>Details:<code> " + e + "</code></p>");
        }

        /* Apply Inline Parameters (this must come after applyDynamicSQL)
         * (Inline parameters are transformed to BIND ones (?))
         */
        try {
            if (inlineParams != null) {
                prepareInlineParameters(sb); // work inline in the sb
            }
        } catch (Exception e) {
            logger.error("Error", e);
            throw new ArtException("<p>Error applying inline parameters to the query. Please contact the ART administrator. <br>Details:<code> " + e + "</code></p>");
        }


        /* Apply Multi Parameters to the query SQL
         * (in the WHERE part new "AND param in (<values>)" conditions are addedd if the query uses multi params)
         */
        try {
            if (multiParams != null) {
                applyMultiParameters(sb); // work inline in the sb
            }
        } catch (Exception e) {
            logger.error("Error", e);
            throw new ArtException("<p>Error applying multi parameters to the query. Please contact the ART administrator. <br>Details:<code> " + e + "</code></p>");
        }

        //apply rules after inline and multi parameters to accomodate hardcoded rules label #rules#

        /* Apply smart rules to the SQL
         * (in the WHERE part new "AND param in (<values>)" conditions are addedd if the query uses rules)
         */
        if (useSmartRules) {
            try {
                if (!applySmartRules(sb)) {
                    throw new ArtException("<p>Error applying rules to the query. You likely have not been assigned values for the rules used by the query. Please contact the ART administrator. </p>");
                }
            } catch (Exception e) {
                throw new ArtException("<p>Error applying rules to the query. Please contact the ART administrator.<br>Details:<code> " + e + "</code></p>");
            }
        }

        logger.debug("Sql query now is:\n{}", sb.toString());

        return sb.toString();

    }

    //determine if the user can execute the query. Exception thrown if user can't excecute query
    private void verifyQueryAccess() throws ArtException {
        try {
            // Get the query SQL source from the ART Repository
            if (!getQuery()) {
                throw new ArtException("<p>Not able to get query. Are you sure you have been granted rights to execute this object?</p>");
            }

            if (queryStatus.equals("D") && !adminSession) {
                throw new ArtException("<p>Query is disabled. Please contact the ART administrator. </p>");
            }
        } catch (Exception e) {
            throw new ArtException("Error getting the query from ART repository. Please contact the ART administrator. <br>Details:<code> " + e + "</code></p>");
        }
    }

    /** Fulfil the prepared statement with the bind parameters
    After this method, the prepared statement is ready to be executed on the target database
     * 
     * @param ps 
     * @throws ArtException 
     */
    public void prepareStatement(PreparedStatement ps) throws ArtException {

        // Apply Inline Parameters (or old Bind Parameters) to
        // the  prepared statement
        try {
            applyInlineParameters(ps); // work inline in the sb
        } catch (Exception e) {
            logger.error("Error", e);
            throw new ArtException("<p>Error applying bind parameters to the query. Please contact the ART administrator. <br>Details:<code> " + e + "</code></p>");
        }

    }


    /* **************************************EXECUTE********************************** */
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
     * Execute the Query sql
     * 
     * @param resultSetType 
     * @return <code>true</code> if successful
     * @throws ArtException if error occurred while running the query or user doesn't have access to query
     */
    public boolean execute(int resultSetType) throws ArtException {

        preparedStatementSQL = null;

        try {
            conn = ArtDBCP.getConnection();

            // Get some properties of the query
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT DATABASE_ID, QUERY_GROUP_ID, ACTIVE_STATUS, QUERY_TYPE "
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
            connQuery = ArtDBCP.getConnection(queryDatabaseId);

            if (connQuery == null) {
                throw new ArtException("Could not get database connection.");
            }

            psQuery = connQuery.prepareStatement(preparedStatementSQL, resultSetType, ResultSet.CONCUR_READ_ONLY);

            prepareStatement(psQuery); // this applies all the bind parameters (inline)

            return psQuery.execute();

        } catch (Exception e) {
            logger.error("Error", e);
            throw new ArtException("Error while running query. " + e + "<br>\nQuery SQL:\n" + preparedStatementSQL);
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
     * Get the last result set of this query
     * if this is a series of statement, the latter rs is returned
     * Anyway, for some drivers a java.sql.SQLException: Unsupported feature
     * might be thrown
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
        execute(ResultSet.TYPE_FORWARD_ONLY);
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
        Map<String, String> lov = new LinkedHashMap<String, String>();

        execute(ResultSet.TYPE_FORWARD_ONLY);

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
     * Release resources (mainly, return the connection to the target database for this query)
     * IT IS MANDATORY TO CALL THIS AFTER THE execute() IN ORDER TO RETURN
     * THE CONNECTION TO THE POOL
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

    /* ******************************************************************************* */
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
     * Determine if a user can edit a text object. User needs direct access to edit it
     * 
     * @param uname
     * @param qid
     * @return <code>true</code> if user can edit the text object
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

    /** Get the SQL */
    private boolean getQuery() throws SQLException {
        Statement st = null;
        ResultSet rs = null;

        int last_stmt_retrieved_rows = 0;

        // Statement to retrieve the query SQL source
        st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

        String stmt;
        PreparedStatement ps;

        //re-initialize sb
        sb = null;
        sb = new StringBuffer(1024 * 2);

        if (isLov || adminSession) {
            // don't check security for Lovs or during Admin session

            stmt = "SELECT AAS.TEXT_INFO "
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
            stmt = "SELECT AAS.TEXT_INFO "
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
                stmt = "SELECT DISTINCT AAS.TEXT_INFO, AAS.LINE_NUMBER "
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

            //User can also execute all queries in an object group he has been assigned to
            //text objects must be assigned direct access
            if (!viewingTextObject) {
                if (last_stmt_retrieved_rows == 0) {
                    //user doesn't belong to a group with direct access to the query. check if user has access to the query's group
                    stmt = "SELECT AAS.TEXT_INFO "
                            + "  FROM ART_ALL_SOURCES AAS, ART_USER_QUERY_GROUPS AUQG "
                            + " WHERE AAS.OBJECT_ID = ? "
                            + " AND AUQG.USERNAME= ? "
                            + " AND AAS.OBJECT_GROUP_ID = AUQG.QUERY_GROUP_ID"
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
                    stmt = "SELECT DISTINCT AAS.TEXT_INFO, AAS.LINE_NUMBER "
                            + " FROM ART_ALL_SOURCES AAS, ART_USER_GROUP_GROUPS AUGG "
                            + " WHERE AAS.OBJECT_GROUP_ID = AUGG.QUERY_GROUP_ID "
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

            stmt = "SELECT AAS.TEXT_INFO "
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
     * Apply Smart Rules
     * v 0.5 - embedded in PreparedQueryd
     * v 0.4 - Return null instead of raising an exception
     *         if the usernames has not been granted to the rule
     * v 0.3 - Handle "LOOKUP" rule type, with a recursive approach
     * v 0.2 - Handle "ALL_ITEMS" rule value
     * v 0.1 -
     */
    private boolean applySmartRules(StringBuffer sb)
            throws SQLException {
        String currentRule, currentParamName;
        Statement st; // Should I create a statement for each (nested) resultset???
        ResultSet rsRules;
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
        StringBuilder labelledValues = new StringBuilder(1024 * 2);
        boolean usingLabelledRules = false;
        String querySql = sb.toString();
        int labelPosition = querySql.toLowerCase().indexOf("#rules#"); //use all lowercase to make find case insensitive
        if (labelPosition != -1) {
            usingLabelledRules = true;
        }

        // Get statement
        st = conn.createStatement();
        // Get rules for the current query
        rsRules = st.executeQuery("SELECT RULE_NAME, FIELD_NAME FROM ART_QUERY_RULES WHERE QUERY_ID=" + queryId);

        // for each rule build and add the AND column IN (list) string to the query
        // Note: if we don't have rules for this query, the sb is left untouched
        while (rsRules.next()) {
            count++;

            StringBuffer tmpSb = new StringBuffer(64);
            currentRule = rsRules.getString("RULE_NAME");
            currentParamName = rsRules.getString("FIELD_NAME");
            tmpSb = getRuleValuesList(conn, username, currentRule, 1);
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
                // Add the rule to the query (handle GROUP_BY and ORDER BY)
                // NOTE: HAVING is not handled.
                // tmpSb.toSting().substring(1) is the <list> of allowed values for the current rule,
                // the tmpSb returned by applyRule begins with a ',' so we need a .substring(1)

                String values = tmpSb.toString();
                if (values == null || (values != null && values.length() == 0)) {
                    //user doesn't have values set for at least one rule that the query uses. values needed for all rules
                    successfullyApplied = false;
                    break;
                } else {
                    if (usingLabelledRules) {
                        //using labelled rules. don't append AND before the first rule value
                        if (count == 1) {
                            labelledValues.append(currentParamName + " in (" + values.substring(1) + ") ");
                        } else {
                            labelledValues.append(" AND " + currentParamName + " in (" + values.substring(1) + ") ");
                        }
                    } else {
                        //append rule values for non-labelled rules
                        if (insertPosLast > 0) {
                            // We have a GROUP BY or an ORDER BY clause
                            // NOTE: sb changes dynamically

                            sb.insert(sb.length() - insertPosLast, " AND "
                                    + currentParamName + " in ( " + values.substring(1) + " ) ");
                        } else { //No group by or order by. We can just append
                            sb.append(" AND " + currentParamName + " in ( " + values.substring(1) + " ) ");
                        }
                    }
                }
            }
        }

        rsRules.close();
        st.close();

        //replace all occurrences of labelled rule with rule values
        if (usingLabelledRules) {
            //replace rule values
            querySql = querySql.replaceAll("(?i)#rules#", labelledValues.toString()); //(?i) makes regex case insensitive. first parameter of replaceall is a regex expression.
            //update sb with new sql
            sb.replace(0, sb.length(), querySql);
        }

        //return rules application status
        return successfullyApplied;
    }

    /**
     * Apply a rule for a user
     * 
     * @param conn
     * @param ruleUsername
     * @param currentRule
     * @param counter
     * @return rule values
     * @throws SQLException
     */
    public StringBuffer getRuleValuesList(Connection conn, String ruleUsername, String currentRule, int counter)
            throws SQLException {

        StringBuffer tmpSb = new StringBuffer(64);
        boolean isAllItemsForThisRule = false;

        // Exit after MAX_RECURSIVE_LOOKUP calls
        // this is to avoid a situation when user A lookups user B
        // and viceversa
        if (counter > MAX_RECURSIVE_LOOKUP) {
            logger.warn("TOO MANY LOOPS - exiting");
            return null;
        }

        // Retrieve user's rule value for this rule
        // select value from art_user_rules where username = [username] and rule = [rule]
        PreparedStatement ps;
        ResultSet rsRuleValues;
        String sql = "SELECT RULE_VALUE, RULE_TYPE "
                + " FROM ART_USER_RULES "
                + " WHERE USERNAME = ? AND RULE_NAME = ?";

        ps = conn.prepareStatement(sql);
        ps.setString(1, ruleUsername);
        ps.setString(2, currentRule);

        rsRuleValues = ps.executeQuery();

        // Build the tmp string, handle ALL_ITEMS and
        // Recursively call applyRule() for LOOKUP
        //  Note: null TYPE is handled as EXACT

        while (rsRuleValues.next() && !isAllItemsForThisRule) {
            if (!rsRuleValues.getString("RULE_VALUE").equals("ALL_ITEMS")) {
                if (rsRuleValues.getString("RULE_TYPE").equals("LOOKUP")) {
                    // if type is lookup the VALUE is the name
                    // to look up. Recursively call applyRule
                    tmpSb.append(getRuleValuesList(conn, rsRuleValues.getString("RULE_VALUE"), currentRule, ++counter).toString());
                } else { // Normal EXACT type
                    tmpSb.append(",'" + escapeSql(rsRuleValues.getString("RULE_VALUE")) + "'");
                }
            } else {
                isAllItemsForThisRule = true;
            }
        }
        ps.close();
        rsRuleValues.close();

        if (!isAllItemsForThisRule) {
            // return the <list> for the current rule and user
            return tmpSb;
        }
        return null;
    }

    /*
     *   END Smart Rules Code ;)
     *
     *****************/
    // escape the ' char in a parameter value (used in multi params)
    private String escapeSql(String s) {
        return StringEscapeUtils.escapeSql(s);
    }

    /** Inline parameters are substituted with ?,
     *   a TreeMap (treeInline) is built to store the param position     
     */
    private void prepareInlineParameters(StringBuffer sb) throws SQLException {
        // Change applied by Giacomo Ferrari on 2005-09-23
        //  to perform the padding during inline prameter replacement.
        //  in order to leave unchanged the original length of SQL string
        final String blanks = "                                                       "; //any length as long as we don't have a parameter label of longer length

        String paramName;
        int startPos;

        treeInline = new TreeMap<Integer, String>();

        String querySql = sb.toString();
        Iterator it = inlineParams.entrySet().iterator();

        if (queryType == 112 || queryType == 113 || queryType == 114) {
            //mdx query		
            String paramValue;
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                paramName = (String) entry.getKey();
                paramValue = inlineParams.get(paramName);
                querySql = querySql.replaceAll("(?i)#" + paramName + "#", paramValue); //(?i) makes regex case insensitive. first parameter of replaceall is a regex expression.
            }

            //update sb with new sql
            sb.replace(0, sb.length(), querySql);
        } else {
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                paramName = (String) entry.getKey();  // inline label without #

                //replace occurrences of param labels with ? one by one so that correct ps.set methods are used in applyInlineParameters()
                //can't do replaceAll because there will be a mismatch with the ps.set methods

                startPos = sb.toString().indexOf("#" + paramName + "#"); //find #label#
                Object checker = null;

                // increased maximum to avoid loop from 30 to 200 by Giacomo Ferrari on 2005-09-23
                // while (startPos != -1 && i++<30) {
                while (startPos != -1 && startPos < sb.toString().length()) {
                    checker = treeInline.get(new Integer(startPos));

                    //if(DEBUG && checker!=null ) System.err.println(sNAME + ": ERROR: another parameter already stored at position " + startPos + ". Cannot store " +paramName+"! " );
                    if (checker != null) {
                        logger.warn("Another parameter already stored at position {}. Cannot store {}!", startPos, paramName);
                    }

                    treeInline.put(new Integer(startPos), paramName); // stores the param name and its position. The order of position will ensure correct substitution in applyInlineParameters()

                    //if(DEBUG) System.err.println(sNAME + ":Storing parameter "+paramName+" found at position: " + startPos);
                    logger.debug("Storing parameter {} found at position {}", paramName, startPos);

                    // replace inline label with ' ? ' plus the correct number of blanks so that total string length is not changes
                    // row replaced by the following one by Giacomo Ferrari on 2005-09-23
                    // sb.replace(startPos, startPos+paramName.length()+2, " ? "); //parseStringSQL(paramValue));
                    sb.replace(startPos, startPos + paramName.length() + 2, " ? " + blanks.substring(0, (paramName.length() + 2 - 3)));
                    // +2 is to consider the #, -3 is the chars used by ' ? ' replacement

                    //if(DEBUG) System.err.println(sNAME + ": SQL is: \n--------------------" + sb.toString() + "\n--------------------");
                    //if(DEBUG) System.err.println(sNAME + ": SQL string length is: " + sb.toString().length());
                    logger.debug("Sql string length is {}", sb.toString().length());

                    // find another occurence of the same param
                    startPos = sb.toString().indexOf("#" + paramName + "#", startPos + paramName.length() + 2);
                }

            }

        }
    }

    /** Dynamic SQL is parsed, evaluated and the sb is modified according
     */
    private void applyDynamicSQL(StringBuffer sb)
            throws ArtException {
        String element, xmlText, exp1, exp2, op, tmp;
        XmlInfo xinfo;

        /*
        // <PROPS> element
        element= "PROPS";
        xinfo = XmlParser.getXmlElementInfo(sb.toString(), element, 0);
        if ( xinfo != null) {
        propsMap = new Hashtable();
        String props = xinfo.getText();
        if (DEBUG) System.out.println("Art- PROPS tag detected: " + props);
        String[] lines = props.split("\n");
        for (String s: lines) {
        String[] pair = s.split("=");
        if (pair.length == 2) propsMap.put(pair[0],pair[1]);
        }
        if (DEBUG) System.out.println("Art- PROPS tag detected - Map is:\n" + propsMap);
        
        // replace the code in the SQL with the "" text
        // +3 is to handle </ and > chars around the IF end tag
        sb.replace(xinfo.getStart(),xinfo.getEnd()+element.length()+3,"");
        if (DEBUG) System.out.println("Art- PROPS tag detected - Remaining SQL is:\n" + sb.toString());
        
        }
        // </PROPS>
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

            //get inline params
            if (inlineParams != null) {
                // Get inline param value for exp1 (if it is an inline param)
                if (exp1.startsWith("#") && exp1.endsWith("#") && exp1.length() > 2) {
                    exp1 = inlineParams.get(exp1.substring(1, exp1.length() - 1));
                }

                // Get inline param value for exp2 (if it is an inline param)
                if (exp2.startsWith("#") && exp2.endsWith("#") && exp2.length() > 2) {
                    exp2 = inlineParams.get(exp2.substring(1, exp2.length() - 1));
                }

                // Get inline param value for op (if it is an inline param)
                if (op.startsWith("#") && op.endsWith("#") && op.length() > 2) {
                    op = inlineParams.get(op.substring(1, op.length() - 1));
                }
            }

            if (evaluateIF(exp1, op, exp2)) {
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

    /** Evaluate the IF element in Dynamic SQL
     */
    private boolean evaluateIF(String exp1, String op, String exp2)
            throws ArtException {
        // transform null in empty strings
        exp1 = (exp1 == null ? "" : exp1.trim().toLowerCase());
        exp2 = (exp2 == null ? "" : exp2.trim().toLowerCase());
        op = (op == null ? "" : op.trim().toLowerCase());

        if (op.equals("eq") || op.equals("equals")) { // -- equals
            return exp1.equals(exp2);

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

        } else if (op.equals("is null")) { // ------------ is null
            return exp1.equals("");

        } else if (op.equals("is not null")) { // -------- is not null
            return !exp1.equals("");

        } else if (op.equals("starts with")) { // -------- startsWith
            return exp1.startsWith(exp2);

        } else if (op.equals("ends with")) { // ---------- ensWith
            return exp1.endsWith(exp2);

        } else if (op.equals("contains")) { // ----------- contains
            return (exp1.indexOf(exp2) != -1 ? true : false);

        } else {
            throw new ArtException("<br>Not able to evaluate IF condition, the operator &lt;OP&gt;" + op + "&lt;/OP&gt; is not recognized");
        }

    }

    /**
     * Process multi parameters and generate a hash map with parameter name and values.
     * To be used for jasper reports
     * 
     * @param querySql
     * @return multi parameters to be used with jasper reports
     */
    public Map<String, List> getJasperMultiParams(String querySql) {

        try {

            conn = ArtDBCP.getConnection();
            StringBuffer buffer = new StringBuffer(1024 * 2);
            buffer.append(querySql);

            applyMultiParameters(buffer);

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
     * Process multi parameters and generate a map with parameter name and values.
     * To be used for jxls reports
     * 
     * @param querySql
     * @return multi parameters to be used for jxls reports
     */
    public Map<String, String> getJxlsMultiParams(String querySql) {

        try {

            conn = ArtDBCP.getConnection();
            StringBuffer buffer = new StringBuffer(1024 * 2);
            buffer.append(querySql);

            applyMultiParameters(buffer);

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

    /** Multi parameters as applied to the SQL
     */
    private void applyMultiParameters(StringBuffer sb) throws SQLException {

        Iterator it;

        // Enable looking up of param label (column name for non-labelled params) from the html name
        if (htmlParams == null) {
            ArtQuery aq = new ArtQuery();
            htmlParams = aq.getHtmlParams(queryId);
        }


        //check if query uses labelled multi parameters
        boolean hasLabelledMultiParams = false;
        String paramLabel;
        int foundPosition;
        String querySql;
        List<String> finalValuesList;
        List<String> paramValuesList = new ArrayList<String>(); //parameter values list that will be used by jasper reports

        querySql = sb.toString();
        it = htmlParams.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String htmlName = (String) entry.getKey();
            if (htmlName.startsWith("M_")) {
                //this is a multi parameter
                ArtQueryParam param = (ArtQueryParam) entry.getValue();
                paramLabel = param.getParamLabel();

                foundPosition = querySql.toLowerCase().indexOf("#" + paramLabel.toLowerCase() + "#"); //use all lowercase to make find case insensitive
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

                StringBuilder finalValuesBuffer = new StringBuilder(512);
                StringBuilder mdxValues = new StringBuilder(512);

                String currentValue;
                String escapedValue;
                int parameterNumber;
                String paramType; //don't quote integer/number parameters i.e. where int_col in ('1','2') may not work on some databases e.g. hsqldb 2.x

                ArtQueryParam param = htmlParams.get(htmlName);
                paramType = param.getFieldClass();

                //build string of values to go into IN clause of sql
                for (parameterNumber = 0; parameterNumber < (paramValues.length - 1); parameterNumber++) {
                    currentValue = paramValues[parameterNumber];

                    //don't quote numbers. some databases won't do implicit conversion where column is numeric
                    //confirm that they are numbers to avoid sql injection                        
                    if ("NUMBER".equals(paramType) && NumberUtils.isNumber(currentValue)) {
                        finalValuesBuffer.append(currentValue);
                        finalValuesBuffer.append(",");
                    } else {
                        //escape and quote non-numbers
                        escapedValue = escapeSql(currentValue);
                        finalValuesBuffer.append("'");
                        finalValuesBuffer.append(escapedValue);
                        finalValuesBuffer.append("',");
                    }

                    paramValuesList.add(currentValue);

                    mdxValues.append(currentValue);
                    mdxValues.append(",");
                }
                //add last value
                currentValue = paramValues[parameterNumber];
                if ("NUMBER".equals(paramType) && NumberUtils.isNumber(currentValue)) {
                    finalValuesBuffer.append(currentValue);
                } else {
                    //escape and quote non-numbers
                    escapedValue = escapeSql(currentValue);
                    finalValuesBuffer.append("'");
                    finalValuesBuffer.append(escapedValue);
                    finalValuesBuffer.append("'");
                }
                paramValuesList.add(currentValue);
                mdxValues.append(currentValue);

                //get param label. in case M_1 etc was used - pre 2.2                
                paramLabel = param.getParamLabel();

                //populate jasper multi parameters hash map
                jasperMultiParams.put(paramLabel, paramValuesList);

                //populate jxls multi parameters hash table
                jxlsMultiParams.put(paramLabel, finalValuesBuffer.toString());

                //replace all occurrences of labelled multi parameter with valid sql syntax
                if (queryType == 112 || queryType == 113 || queryType == 114) {
                    querySql = querySql.replaceAll("(?i)#" + paramLabel + "#", mdxValues.toString()); //(?i) makes regex case insensitive. first parameter of replaceall is a regex expression.
                } else {
                    querySql = querySql.replaceAll("(?i)#" + paramLabel + "#", finalValuesBuffer.toString()); //(?i) makes regex case insensitive. first parameter of replaceall is a regex expression.
                }
            }

            //replace any multi parameters that haven't been replaced yet. these are the ones where ALL_ITEMS was selected or all values are to be used
            it = htmlParams.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                String htmlName = (String) entry.getKey();
                if (htmlName.startsWith("M_")) {
                    ArtQueryParam param = (ArtQueryParam) entry.getValue();
                    paramLabel = param.getParamLabel();
                    String paramType = param.getFieldClass();

                    //check if parameter is yet to be replaced
                    foundPosition = querySql.toLowerCase().indexOf("#" + paramLabel.toLowerCase() + "#"); //use all lowercase to make find case insensitive
                    if (foundPosition != -1) {
                        //replace parameter with all possible values
                        finalValuesList = getAllParameterValues(paramLabel); //return all values from the parameter's lov query
                        if (finalValuesList != null && finalValuesList.size() > 0) {
                            StringBuilder finalValuesBuffer = new StringBuilder(512);
                            StringBuilder mdxValues = new StringBuilder(512);
                            String currentValue;
                            String escapedValue;
                            int i;
                            //add all except last item
                            for (i = 0; i < finalValuesList.size() - 1; i++) {
                                currentValue = finalValuesList.get(i);

                                //don't quote numbers. some databases won't do implicit conversion where column is numeric
                                //confirm that they are numbers to avoid sql injection                        
                                if ("NUMBER".equals(paramType) && NumberUtils.isNumber(currentValue)) {
                                    finalValuesBuffer.append(currentValue);
                                    finalValuesBuffer.append(",");
                                } else {
                                    //escape and quote non-numbers
                                    escapedValue = escapeSql(currentValue);
                                    finalValuesBuffer.append("'");
                                    finalValuesBuffer.append(escapedValue);
                                    finalValuesBuffer.append("',");
                                }

                                mdxValues.append(currentValue);
                                mdxValues.append(",");
                            }
                            //add last item
                            currentValue = String.valueOf(finalValuesList.get(i));
                            if ("NUMBER".equals(paramType) && NumberUtils.isNumber(currentValue)) {
                                finalValuesBuffer.append(currentValue);
                            } else {
                                //escape and quote non-numbers
                                escapedValue = escapeSql(currentValue);
                                finalValuesBuffer.append("'");
                                finalValuesBuffer.append(escapedValue);
                                finalValuesBuffer.append("'");
                            }
                            mdxValues.append(currentValue);

                            //populate jasper multi parameters hash map
                            paramValuesList.addAll(finalValuesList);
                            jasperMultiParams.put(paramLabel, paramValuesList);

                            //populate jxls multi parameters hash table
                            jxlsMultiParams.put(paramLabel, finalValuesBuffer.toString());

                            //replace all occurrences of labelled multi parameter with valid sql syntax
                            if (queryType == 112 || queryType == 113 || queryType == 114) {
                                querySql = querySql.replaceAll("(?i)#" + paramLabel + "#", mdxValues.toString()); //(?i) makes regex case insensitive. first parameter of replaceall is a regex expression.
                            } else {
                                querySql = querySql.replaceAll("(?i)#" + paramLabel + "#", finalValuesBuffer.toString()); //(?i) makes regex case insensitive. first parameter of replaceall is a regex expression.
                            }
                        }
                    }
                }
            }

            //update sb with new sql
            sb.replace(0, sb.length(), querySql);
        } else {
            //process non-labelled multi parameters
            it = multiParams.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                String paramId = (String) entry.getKey();
                String htmlName = "M_" + paramId;
                String[] paramValues = (String[]) entry.getValue();

                //get param label. for non-labelled params, this is the column name
                ArtQueryParam param = htmlParams.get(htmlName);
                paramLabel = param.getParamLabel();

                StringBuilder SqlAndParamIn = new StringBuilder(128);
                SqlAndParamIn.append(" AND " + paramLabel + " IN (");

                logger.debug("Number of parameters for {}/{} is {}", new Object[]{paramId, paramLabel, paramValues.length});

                String currentValue;
                String escapedValue;
                int parameterNumber;

                StringBuilder finalValuesBuffer = new StringBuilder(512);

                //multi parameters with all items never get passed
                for (parameterNumber = 0; parameterNumber < (paramValues.length - 1); parameterNumber++) {
                    currentValue = paramValues[parameterNumber];
                    escapedValue = escapeSql(paramValues[parameterNumber]);
                    SqlAndParamIn.append("'" + escapedValue + "' ,");
                    finalValuesBuffer.append("'" + escapedValue + "',");
                    paramValuesList.add(currentValue);
                }
                currentValue = paramValues[parameterNumber];
                escapedValue = escapeSql(paramValues[parameterNumber]);
                SqlAndParamIn.append("'" + escapedValue + "') ");
                finalValuesBuffer.append("'" + escapedValue + "'");
                paramValuesList.add(currentValue);

                //populate jasper multi parameters hash map
                jasperMultiParams.put(paramLabel, paramValuesList);

                //populate jxls multi parameters hash table
                jxlsMultiParams.put(paramLabel, finalValuesBuffer.toString());

                /* The line:
                 * AND PARAM IN ( 'value1', 'value2', ... , 'valueN')
                 * is completed (and stored in SqlAndParamIn); we can add it to
                 * the prepared statement (only if allItems is false)
                 * We had to handle the case
                 * where a GROUP BY or a ORDER BY expression is present (the case in which
                 * we have a HAVING without GROUP BY is not considered).
                 */
                /** NOTE: the 'GROUP BY' and 'ORDER BY' string on the (main query of the)
                 *       Prepared Statement must be in UPPERCASE and separated
                 *       with a single blank.  So nested queries should have
                 *       the words 'GROUP BY' or 'ORDER BY' in lower case.
                 *
                 *  NOTE2: the AND before the IN could be erroneous if we have
                 *         nothing after the WHERE => workaround set a dummy
                 *         condition WHERE 1 = 1
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

        StringBuilder queryBuffer = new StringBuilder(512);

        int databaseId = 0;
        Connection connLov = null;
        PreparedStatement psLovQuery = null;
        ResultSet rsLovQuery = null;
        PreparedStatement psLovValues = null;
        ResultSet rsLovValues = null;

        try {
            //get the lov query's sql
            String sqlLovQuery = "SELECT AAS.TEXT_INFO, AQ.DATABASE_ID, AQ.QUERY_TYPE"
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
            while (rsLovQuery.next()) {
                queryBuffer.append(rsLovQuery.getString("TEXT_INFO"));
                databaseId = rsLovQuery.getInt("DATABASE_ID");
                lovQueryType = rsLovQuery.getInt("QUERY_TYPE");
            }

            if (queryBuffer.length() > 0) {
                //lov found. run lov to get and build all possible parameter values

                if (lovQueryType == 120) {
                    //static lov. values coming from static values defined in sql source
                    String items = queryBuffer.toString();
                    String lines[] = items.split("\\r?\\n");
                    for (String line : lines) {
                        String[] values = line.trim().split("\\|");
                        finalValuesList.add(values[0]);
                    }
                } else {
                    //dynamic lov
                    connLov = ArtDBCP.getConnection(databaseId);
                    String lovSql = queryBuffer.toString();
                    lovSql = lovSql.replaceAll("(?i)#rules#", "1=1"); //replace rules if the label exists, with dummy condition. so that lov query executes without error
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

    private void replaceTag(StringBuffer sb, String fromText, String toText) {
        int startPos = sb.toString().indexOf(fromText);
        int fromTextLength = fromText.length();
        int maxCount = 255; // just to avoid infinite loops...

        while (startPos != -1 && maxCount-- > 0) {
            sb.replace(startPos, startPos + fromTextLength, " '" + toText + "' ");
            startPos = sb.toString().indexOf(fromText);

            logger.debug("Tag found. Sql query now is:\n{}", sb);
        }

    }

    /** Replace :TAGS
     */
    private void applyTags(StringBuffer sb) {
        /*  Update query :TAG
         */

        /* :USERNAME substitution with logged username */
        replaceTag(sb, ":USERNAME", username);

        /* :DATE substitution with current date in 'YYYY-MM-DD' format */
        java.util.Date today = new java.util.Date();

        String dateFormat = "yyyy-MM-dd";
        SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
        String date = dateFormatter.format(today);

        String timeFormat = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat timeFormatter = new SimpleDateFormat(timeFormat);
        String time = timeFormatter.format(today);

        replaceTag(sb, ":DATE", date);
        replaceTag(sb, ":TIME", time);


        /* :DRILL(label, query_id, group_id, mode, concat, param1, param2, ...) automatic substitution */

        /* Other ideas:
        DAY, MONTH, YEAR automatic substitution
        HOUR, MINUTE, YEAR automatic substitution
        ... automatic substitution con o senza apici etc */

    }

    /** Called by the prepareStatement() method.
     * The prepared statement is "fulfilled" with the parameters
     *  The query can use inline parameters OR old-style bind parameters
     *  if the query uses both it is rejected.
     */
    private void applyInlineParameters(PreparedStatement ps) throws SQLException, ArtException {

        logger.debug("applyInlineParameters");

        if (treeInline != null && !treeInline.isEmpty()) { // assume is inline

            Iterator paramNames = treeInline.values().iterator(); //contains parameters which were actually found and replaced in the sql source in the order they were found
            int i = 0; //parameter index/order of appearance
            String paramName, paramValue;

            java.util.Date dateValue;

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
                    paramDataType = aqp.getFieldClass();
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
                    dateValue = setDefaultValueDate(paramValue);
                    if (ps != null) {
                        ps.setDate(i, new java.sql.Date(dateValue.getTime()));
                    }
                    jasperInlineParams.put(paramName, dateValue);
                } else if (paramDataType.equals("DATETIME")) {
                    dateValue = setDefaultValueDate(paramValue);
                    if (ps != null) {
                        ps.setTimestamp(i, new java.sql.Timestamp(dateValue.getTime()));
                    }
                    jasperInlineParams.put(paramName, dateValue);
                } else {
                    //VARCHAR, TEXT
                    if (ps != null) {
                        ps.setString(i, paramValue);
                    }
                    jasperInlineParams.put(paramName, paramValue);
                }

            }

        } else if (bindParams != null && !bindParams.isEmpty() && treeInline != null && treeInline.isEmpty()) { // support for old bind parameters

            /**  Update the prepared statement.
             *  The name of a parameter is the number (in the same order)
             *  of the "?" in the prepared statement, prefixed with a 'P'.
             *  (i.e. P3 is the name of the 3rd '?' on the prepared statement).
             *  A date (splitted in year, month, day) is named with a 'PX_' followed by 'year' or 'month' or 'day',
             *  where X is the position number of the '?'
             *  escapeSql function handles special characters (as far, only "'")
             */
            String name;

            Iterator it = bindParams.entrySet().iterator();

            while (it.hasNext()) {
                // Get the parameter name (<-> name is Py or Py_year/month/day
                Map.Entry entry = (Map.Entry) it.next();
                name = (String) entry.getKey();

                if (name.length() > 4 && (name.indexOf('_') != -1)) { // Is a DATE field: PX_year or PX_month or PX_day

                    // Check if it has been already set (there are three params per each date PX_year & PX_month &r PX_day)
                    // the if below is not needed as the enumeration reflects the hashmap content
                    if (bindParams.get(name) != null) {
                        name = name.substring(0, (name.indexOf('_'))); // stores the PX prefix
						/*
                         * Get all the three field (Y, M, D) that are needed to create the current date parameter.
                         */
                        int year = Integer.parseInt(bindParams.get(name + "_year"));
                        int month = (Integer.parseInt(bindParams.get(name + "_month"))) - 1; // Java Months begin from 0!!!
                        int day = Integer.parseInt(bindParams.get(name + "_day"));
                        // Remove the three Date parameters from the hash table in
                        // order to do not count them twice
                        bindParams.remove(name + "_year");
                        bindParams.remove(name + "_month");
                        bindParams.remove(name + "_day");

                        // Create a JAVA Date... (this is a mess!)
                        // Maybe: java.sql.Date sqlDate = java.sql.Date.valueOf(year + "-" + month + "-" + day);

                        GregorianCalendar cal = new GregorianCalendar(year, month, day);
                        java.util.Date utilDate = cal.getTime();
                        java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

                        // Set the prepared statement's date value                                                
                        logger.debug("Setting date: {}", name);
                        ps.setDate(Integer.parseInt(name.substring(1)), sqlDate);
                        logger.debug("Date set. First name={}", name);
                    } else {
                        logger.debug("Skipped date already set: {}", name);
                    }
                } else { // VARCHAR/CHAR/INTEGER/NUMBER etc
                    // setString seems to work without problem with any
                    // sql type (NUMBER, INTEGER, VARCHAR, CHAR).
                    // BTW some further checks are needed,
                    // tested successfully works with mysql , pg, , oracle , db2 and MSSql
					/*
                     * Set the prepared statement's value
                     */

                    logger.debug("Setting {}", name);
                    // escapeSql pads ' for buggy drivers. but this is probably an error as the driver should do this
                    //ps.setString( Integer.parseInt(name.substring(1)), escapeSql((String) bindParams.get(name)) );
                    ps.setString(Integer.parseInt(name.substring(1)), (bindParams.get(name)).trim());
                }
            } // End For parameterNumber
        } else if (bindParams != null && !bindParams.isEmpty() && treeInline != null && !treeInline.isEmpty()) {
            /*
             * both new inline and old bind parameters are present: reject the query
             */
            throw new ArtException("<p>This query uses both inline parameters and old bind parameters. <br>"
                    + "Please remove the old-style bind parameters and replace them with inline parameters</p>");
        }

    }

    /** Parse a string value that is supposed to:
    1. be a valid date with format YYYY-MM-DD (returns it as it is)
    2. be null, SYSDATE or NOW (returns current date in YYYY-MM-DD format)
    3. follow the syntax "ADD DAYS|MONTHS|YEARS <integer>"
    (returns rolled date from current date in YYYY-MM-DD format)
     * 
     * @param defaultValue 
     * @return date object that corresponds to the given string
     */
    public static java.util.Date setDefaultValueDate(String defaultValue) {
        /*  if default value has syntax "ADD DAYS|MONTHS|YEARS <integer>"
        or "Add day|MoN|Year <integer>"
        set default value as sysdate plus an offset
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
     * Process inline parameters and generate a hash map with parameter name and values.
     * To be used for jasper reports
     * 
     * @param querySql
     * @return inline parameters to be used for jasper reports
     */
    public Map<String, Object> getJasperInlineParams(String querySql) {

        try {

            conn = ArtDBCP.getConnection();
            StringBuffer buffer = new StringBuffer(1024 * 2);
            buffer.append(querySql);

            prepareInlineParameters(buffer);
            applyInlineParameters(null);

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
