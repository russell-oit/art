/*
 * Class to retrieve query details
 * combination of previous artqueryheader, artsource and new methods related to queries
 */
package art.utils;

import art.params.*;
import art.servlets.ArtDBCP;
import java.sql.*;
import java.text.Collator;
import java.util.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to retrieve query details
 *  
 */
public class ArtQuery {

    final static Logger logger = LoggerFactory.getLogger(ArtQuery.class);
    
    final int MAX_RECURSIVE_LOOKUP = 20;
    final int MAX_GRAPH_WIDTH = 1200;
    final int MAX_GRAPH_HEIGHT = 1200;
    final int SOURCE_CHUNK_LENGTH = 4000; //length of column that holds query source
    String name = "";
    String shortDescription = "";
    String description = "";
    String contactPerson = "";
    String usesRules = "N";
    String status = "A";
    java.util.Date updateDate;
    int databaseId;
    int groupId = -1;
    int queryId;
    int queryType;
    String template = "";
    String text = "";    
    String xmlaUrl = "";
    String xmlaDatasource = "";
    String xmlaCatalog = "";
    String xmlaUsername = "";
    String xmlaPassword = "";
    String xaxisLabel = "";
    String yaxisLabel = "";
    String graphOptions = "";
    //properties derived from graph options string
    boolean showLegend;
    boolean showLabels;
    boolean showPoints;
    boolean showGraphData;
    int graphWidth = 400;
    int graphHeight = 300;
    int graphYMin;
    int graphYMax;
    String graphBgColor = "#FFFFFF";
    String showParameters = "N"; //allow show parameters to be selected by default
    //properties not used in query creation
    List<ParamInterface> paramList; //parameter list used in showParams pages
    String username;
    String groupName;

    /**
     * 
     */
    public ArtQuery() {
    }

    /**
     * 
     * @param value 
     */
    public void setShowParameters(String value) {
        if (value == null) {
            value = "N"; //
        }
        showParameters = value;
    }

    /**
     * 
     * @return setting to determine whether show parameters option will be checked or not
     */
    public String getShowParameters() {
        return showParameters;
    }

    /**
     * 
     * @param value
     */
    public void setXmlaUsername(String value) {
        xmlaUsername = value;
    }

    /**
     * 
     * @return xmla username
     */
    public String getXmlaUsername() {
        return xmlaUsername;
    }

    /**
     * 
     * @param value
     */
    public void setXmlaPassword(String value) {
        xmlaPassword = value;
    }

    /**
     * 
     * @return xmla password
     */
    public String getXmlaPassword() {
        return xmlaPassword;
    }

    /**
     * 
     * @param value
     */
    public void setGroupName(String value) {
        groupName = value;
    }

    /**
     * 
     * @return object group name
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * 
     * @return query's html parameter objects
     */
    public List<ParamInterface> getParamList() {
        if (paramList == null) {
            paramList = new ArrayList<ParamInterface>();
        }

        return paramList;
    }

    /**
     * 
     * @param value
     */
    public void setUsername(String value) {
        username = value;
    }

    /**
     * 
     * @return graph width in pixels
     */
    public int getGraphWidth() {
        if (graphWidth > MAX_GRAPH_WIDTH) {
            graphWidth = MAX_GRAPH_WIDTH;
        }

        return graphWidth;
    }

    /**
     * 
     * @return height in pixels for graphs
     */
    public int getGraphHeight() {
        if (graphHeight > MAX_GRAPH_HEIGHT) {
            graphHeight = MAX_GRAPH_HEIGHT;
        }

        return graphHeight;
    }

    /**
     * 
     * @return min y-axis value for graph
     */
    public int getGraphYMin() {
        return graphYMin;
    }

    /**
     * 
     * @return max y-axis value for graph
     */
    public int getGraphYMax() {
        return graphYMax;
    }

    /**
     * 
     * @return background colour for graph
     */
    public String getGraphBgColor() {
        return graphBgColor;
    }

    /**
     * 
     * @return <code>true</code> if legend should be shown on graph
     */
    public boolean isShowLegend() {
        return showLegend;
    }

    /**
     * 
     * @return <code>true</code> if labels should be shown on graph
     */
    public boolean isShowLabels() {
        return showLabels;
    }

    /**
     * 
     * @return <code>true</code> if data points should be highlighted on graph
     */
    public boolean isShowPoints() {
        return showPoints;
    }

    /**
     * 
     * @return <code>true</code> if graph data should be shown below graph
     */
    public boolean isShowGraphData() {
        return showGraphData;
    }

    /**
     * 
     * @param value
     */
    public void setGraphOptions(String value) {
        graphOptions = value;
        setGraphDisplayOptions(graphOptions);
    }

    /**
     * 
     * @return graph options string
     */
    public String getGraphOptions() {
        return graphOptions;
    }

    /**
     * 
     * @param value
     */
    public void setYaxisLabel(String value) {
        yaxisLabel = value;
    }

    /**
     * 
     * @return y-axis label
     */
    public String getYaxisLabel() {
        return yaxisLabel;
    }

    /**
     * 
     * @param value
     */
    public void setXaxisLabel(String value) {
        xaxisLabel = value;
    }

    /**
     * 
     * @return x-axis label
     */
    public String getXaxisLabel() {
        return xaxisLabel;
    }

    /**
     * 
     * @param value
     */
    public void setXmlaUrl(String value) {
        xmlaUrl = value;
    }

    /**
     * 
     * @return xmla url
     */
    public String getXmlaUrl() {
        return xmlaUrl;
    }

    /**
     * 
     * @param value
     */
    public void setXmlaDatasource(String value) {
        xmlaDatasource = value;
    }

    /**
     * 
     * @return xmla datasource
     */
    public String getXmlaDatasource() {
        return xmlaDatasource;
    }

    /**
     * 
     * @param value
     */
    public void setXmlaCatalog(String value) {
        xmlaCatalog = value;
    }

    /**
     * 
     * @return xmla catalog name
     */
    public String getXmlaCatalog() {
        return xmlaCatalog;
    }
    
    /**
     * 
     * @param value
     */
    public void setTemplate(String value) {
        template = value;
    }

    /**
     * 
     * @return template file name
     */
    public String getTemplate() {
        return template;
    }

    /**
     * 
     * @param i
     */
    public void setDatabaseId(int i) {
        databaseId = i;
    }

    /**
     * 
     * @param i
     */
    public void setGroupId(int i) {
        groupId = i;
    }

    /**
     * 
     * @param i
     */
    public void setQueryId(int i) {
        queryId = i;
    }

    /**
     * 
     * @param i
     */
    public void setQueryType(int i) {
        queryType = i;
    }

    /**
     * 
     * @param s
     */
    public void setName(String s) {
        if (s.length() > 25) {
            s = s.substring(0, 25);
        }
        name = s;
    }

    /**
     * 
     * @param s
     */
    public void setShortDescription(String s) {
        shortDescription = s;
    }

    /**
     * 
     * @param s
     */
    public void setDescription(String s) {
        if (s != null && s.length() > 2000) {
            s = s.substring(0, 2000);
        }
        description = s;
    }

    /**
     * 
     * @param s
     */
    public void setContactPerson(String s) {
        contactPerson = s;
    }

    /**
     * 
     * @param s
     */
    public void setUsesRules(String s) {
        usesRules = s;
    }

    /**
     * 
     * @param s
     */
    public void setStatus(String s) {
        if (s == null || s.equals("")) {
            s = "A";
        }

        status = s;
    }

    /**
     * 
     * @param d
     */
    public void setUpdateDate(java.util.Date d) {
        updateDate = d;
    }

    /**
     * 
     * @return datasource id
     */
    public int getDatabaseId() {
        return databaseId;
    }

    /**
     * 
     * @return object group id
     */
    public int getGroupId() {
        return groupId;
    }

    /**
     * 
     * @return query id
     */
    public int getQueryId() {
        return queryId;
    }

    /**
     * 
     * @return query type
     */
    public int getQueryType() {
        return queryType;
    }

    /**
     * 
     * @return query name
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * @return short description
     */
    public String getShortDescription() {
        return shortDescription;
    }

    /**
     * 
     * @return query description
     */
    public String getDescription() {
        return description;
    }

    /**
     * 
     * @return contact person
     */
    public String getContactPerson() {
        return contactPerson;
    }

    /**
     * 
     * @return whether rules should be applied
     */
    public String getUsesRules() {
        return usesRules;
    }

    /**
     * 
     * @return active status
     */
    public String getStatus() {
        return status;
    }

    /**
     * 
     * @return update date
     */
    public java.util.Date getUpdateDate() {
        return updateDate;
    }

    /**
     * 
     * @return sql source
     */
    public String getText() {
        return text;
    }

    /**
     * 
     * @param s
     */
    public void setText(String s) {
        text = s;
    }

    /**
     * Create the object from an existing query header (qId)
     *
     * @param conn connection to art repository
     * @param qId query id
     * @return <code>true</code> if query found and object populated
     */
    public boolean create(Connection conn, int qId) {
        boolean success = false;

        try {
            String SQL = "SELECT QUERY_GROUP_ID, QUERY_ID, NAME, SHORT_DESCRIPTION "
                    + " , DESCRIPTION, CONTACT_PERSON, USES_RULES, DATABASE_ID, QUERY_TYPE, ACTIVE_STATUS "
                    + " ,UPDATE_DATE, TEMPLATE, XMLA_URL, XMLA_DATASOURCE, XMLA_CATALOG, XMLA_USERNAME, XMLA_PASSWORD "
                    + ", X_AXIS_LABEL, Y_AXIS_LABEL, GRAPH_OPTIONS, SHOW_PARAMETERS "
                    + " FROM ART_QUERIES "
                    + " WHERE QUERY_ID = ? ";
            PreparedStatement ps = conn.prepareStatement(SQL);
            ps.setInt(1, qId);
            ResultSet rs = ps.executeQuery();

            PreparedStatement ps2;
            ResultSet rs2;
            if (rs.next()) {
                setName(rs.getString("NAME"));
                setShortDescription(rs.getString("SHORT_DESCRIPTION"));
                setDescription(rs.getString("DESCRIPTION"));
                setContactPerson(rs.getString("CONTACT_PERSON"));
                setUsesRules(rs.getString("USES_RULES"));
                setDatabaseId(rs.getInt("DATABASE_ID"));
                setGroupId(rs.getInt("QUERY_GROUP_ID"));
                setQueryId(rs.getInt("QUERY_ID"));
                setQueryType(rs.getInt("QUERY_TYPE"));
                setStatus(rs.getString("ACTIVE_STATUS"));
                setUpdateDate(rs.getDate("UPDATE_DATE"));
                setTemplate(rs.getString("TEMPLATE"));
                setXmlaUrl(rs.getString("XMLA_URL"));
                setXmlaDatasource(rs.getString("XMLA_DATASOURCE"));
                setXmlaCatalog(rs.getString("XMLA_CATALOG"));
                setXmlaUsername(rs.getString("XMLA_USERNAME"));
                setXmlaPassword(rs.getString("XMLA_PASSWORD"));
                setXaxisLabel(rs.getString("X_AXIS_LABEL"));
                setYaxisLabel(rs.getString("Y_AXIS_LABEL"));
                setShowParameters(rs.getString("SHOW_PARAMETERS"));
                graphOptions = rs.getString("GRAPH_OPTIONS");
                if (graphOptions == null) {
                    setGraphDisplayOptions(shortDescription, true);
                } else {
                    setGraphDisplayOptions(graphOptions, false);
                }

                //get query source
                SQL = "SELECT TEXT_INFO FROM ART_ALL_SOURCES "
                        + " WHERE OBJECT_ID = ?"
                        + " ORDER BY LINE_NUMBER";
                ps2 = conn.prepareStatement(SQL);
                ps2.setInt(1, qId);
                rs2 = ps2.executeQuery();

                StringBuilder concatText = new StringBuilder(1024);
                while (rs2.next()) {
                    concatText.append(rs2.getString(1));                    
                }
                setText(concatText.toString());
                ps2.close();
                rs2.close();

                rs.close();
                ps.close();

                success = true;
            } else {
                rs.close();
                ps.close();
                logger.warn("The query id {} does not exist",qId);
            }
        } catch (SQLException e) {
            logger.error("Error. Query id={}",qId,e);
        }

        return success;
    }

    /**
     * Determine if query is only directly allocated to a single user
     * 
     * @param conn connection to art repository
     * @param username username of user to check
     * @return <code>true</code> if user has exclusive access to this query
     */
    public boolean exclusiveAccess(Connection conn, String username) {
        boolean exclusive = false;

        PreparedStatement ps;
        ResultSet rs;
        String sql;
        int userAccessCount = 0;
        boolean userHasAccess = false;
        boolean assignedToGroup = false;

        try {
            sql = "SELECT USER_GROUP_ID FROM ART_USER_GROUP_QUERIES "
                    + " WHERE QUERY_ID = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, queryId);
            rs = ps.executeQuery();
            if (rs.next()) {
                //query granted to a group. user doesn't have exclusive access
                assignedToGroup = true;
            }
            ps.close();
            rs.close();

            if (!assignedToGroup) {
                sql = "SELECT USERNAME FROM ART_USER_QUERIES "
                        + " WHERE QUERY_ID = ?";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, queryId);
                rs = ps.executeQuery();
                while (rs.next()) {
                    userAccessCount++;
                    if (userAccessCount >= 2) {
                        //more than one user has access
                        break;
                    }
                    if (rs.getString("USERNAME").equals(username)) {
                        userHasAccess = true;
                    }
                }
                ps.close();
                rs.close();
            }

            if (!assignedToGroup && userHasAccess && userAccessCount == 1) {
                //only one user has explicit access
                exclusive = true;
            }
        } catch (Exception e) {
           logger.error("Error",e);
        }

        return exclusive;
    }

    /**
     * Update the existing sql source
     *
     * @param conn connection to art repository
     * @return <code>true</code> if query updated
     */
    public boolean updateSource(Connection conn) {
        boolean success = false;

        try {
            // Delete Old SQL Source
            String SQLUpdate = ("DELETE FROM ART_ALL_SOURCES WHERE OBJECT_ID = ?");
            PreparedStatement ps = conn.prepareStatement(SQLUpdate);
            ps.setInt(1, queryId);
            ps.executeUpdate();

            // Write the query in small segments
            // This guarantees portability across databases with different max VARCHAR sizes

            SQLUpdate = ("INSERT INTO ART_ALL_SOURCES "
                    + "(OBJECT_ID, OBJECT_GROUP_ID, LINE_NUMBER, TEXT_INFO)"
                    + " values (?, ?, ?, ?)");
            ps = conn.prepareStatement(SQLUpdate);
			
			//if text is empty string, save space. for error free database migrations using tools like PDI
			if(StringUtils.isBlank(text)){
				text=" ";
			}
			
            int start = 0;
            int end = SOURCE_CHUNK_LENGTH;
            int step = 1;
            int textLength = text.length();

            ps.setInt(1, queryId);
            ps.setInt(2, groupId);
                       
            while (end < textLength) {
                ps.setInt(3, step++);
                ps.setString(4, text.substring(start, end));

                ps.addBatch();
                start = end;
                end = end + SOURCE_CHUNK_LENGTH;
            }
            ps.setInt(3, step);
            ps.setString(4, text.substring(start));

            ps.addBatch();
            ps.executeBatch();
            ps.close();

            success = true;
        } catch (SQLException e) {
            logger.error("Error. Query id={}",queryId,e);
        }

        return success;
    }

    /**
     * Insert a new query, both header and source 
     *
     * @param conn connection to art repository
     * @return <code>true</code> if insert successful
     * @throws ArtException 
     */
    public boolean insert(Connection conn) throws ArtException {
        boolean success = false;

        int newQueryId = allocateNewId(conn);
        if (newQueryId != -1) {
            setQueryId(newQueryId);
            if (updateHeader(conn) && updateSource(conn)) {
                success = true;
            } else {
                logger.warn("Problem with insert. Either updateHeader or updateSource failed.");
            }
        }

        return success;
    }

    private synchronized int allocateNewId(Connection conn) {
        int newQueryId = -1;

        try {
            // Get the id
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT MAX(QUERY_ID) FROM ART_QUERIES");

            if (rs.next()) {
                newQueryId = 1 + rs.getInt(1);
            } else {
                logger.warn("Query allocateNewId failed");
                return -1;
            }
            String SQL = "INSERT INTO ART_QUERIES ( "
                    + "   QUERY_GROUP_ID, QUERY_ID, NAME "
                    + " , SHORT_DESCRIPTION, DESCRIPTION, USES_RULES "
                    + " , DATABASE_ID, QUERY_TYPE "
                    + " ) VALUES ( "
                    + "  0,?,? "
                    + " ,'-','-','N' "
                    + " , -1,0)";
            PreparedStatement ps = conn.prepareStatement(SQL);
            ps.setInt(1, newQueryId);
            ps.setString(2, ":allocating:" + newQueryId);

            // insert the "dummy" row so the newQueryId cannot be used by others
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            logger.error("Error",e);
            newQueryId = -1;
        }

        return newQueryId;
    }

    /**
     * Update and existing header in the database
     *
     *
     * @param conn 
     * @return <code>true</code> if header updated
     * @throws ArtException  if sql exception generated while trying to update
     */
    public boolean updateHeader(Connection conn) throws ArtException {
        boolean success = false;

        try {
            String SQL = "UPDATE ART_QUERIES SET QUERY_GROUP_ID=?, NAME=?, SHORT_DESCRIPTION=? "
                    + " ,DESCRIPTION=?, CONTACT_PERSON=?, USES_RULES=?, DATABASE_ID=?, QUERY_TYPE=?, UPDATE_DATE=? "
                    + " ,ACTIVE_STATUS=?, TEMPLATE = ?, XMLA_URL = ?, XMLA_DATASOURCE = ?, XMLA_CATALOG = ? "
                    + " ,X_AXIS_LABEL=?, Y_AXIS_LABEL=?, GRAPH_OPTIONS=?, XMLA_USERNAME=?, XMLA_PASSWORD=?, SHOW_PARAMETERS=? "
                    + " WHERE QUERY_ID = ? ";
            PreparedStatement ps = conn.prepareStatement(SQL);

            ps.setInt(1, groupId);
            ps.setString(2, name);
            ps.setString(3, shortDescription);
            ps.setString(4, description);
            ps.setString(5, contactPerson);
            ps.setString(6, usesRules);
            ps.setInt(7, databaseId);
            ps.setInt(8, queryType);
            ps.setDate(9, new java.sql.Date(System.currentTimeMillis()));
            ps.setString(10, status);
            ps.setString(11, template);
            ps.setString(12, xmlaUrl);
            ps.setString(13, xmlaDatasource);
            ps.setString(14, xmlaCatalog);
            ps.setString(15, xaxisLabel);
            ps.setString(16, yaxisLabel);
            ps.setString(17, graphOptions);
            ps.setString(18, xmlaUsername);
            ps.setString(19, xmlaPassword);
            ps.setString(20, showParameters);
            ps.setInt(21, queryId);
            if (ps.executeUpdate() == 1) {
                ps.close();
                success = true;
            } else {
                ps.close();
                logger.warn("Zero or more than one row updated. Query ID - {}",queryId);                
            }
        } catch (SQLException e) {
            logger.error("Error. Query id {}",queryId,e);
            throw new ArtException("Not able to update query. Error: " + e);
        }

        return success;

    }

    /**
     * Update query header and source
     * 
     * @param conn
     * @return <code>true</code> if query updated
     */
    public boolean update(Connection conn) {
        boolean updated = false;

        try {
            if (updateHeader(conn) && updateSource(conn)) {
                updated = true;
            }
        } catch (Exception e) {
            logger.error("Error",e);
        }

        return updated;
    }

    /**
     * Grant query access to a given user. Returns false if an error occurs
     * or access already granted to the user
     * 
     * @param conn
     * @param username
     * @return <code>true</code> if access actually granted
     */
    public boolean grantAccess(Connection conn, String username) {
        boolean accessGranted = false;

        String sql;
        PreparedStatement ps;

        try {
            sql = "INSERT INTO ART_USER_QUERIES (USERNAME, QUERY_ID, UPDATE_DATE) VALUES(?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setInt(2, queryId);

            java.util.Date now = new java.util.Date();
            ps.setDate(3, new java.sql.Date(now.getTime()));

            int insertCount = 0;
            try {
                insertCount = ps.executeUpdate();
            } catch (SQLException e) {
                //most likely user already has access
                logger.warn("Error",e);
            }
            ps.close();

            if (insertCount > 0) {
                accessGranted = true;
            }
        } catch (Exception e) {
           logger.error("Error",e);
        }

        return accessGranted;
    }

    /**
     * Return mondrian roles.
     * Jpivot seems to have a problem when more than one role returned
     * 
     * @param conn
     * @param username
     * @return comma separated roles
     */
    public String getMondrianRoles(Connection conn, String username) {
        //roles will be the rule vales of the first rule for the given user

        String roles = "";

        try {
            Statement st;
            ResultSet rs;
            String sql;

            //get rules for the current query
            sql = "SELECT RULE_NAME, FIELD_NAME FROM ART_QUERY_RULES WHERE QUERY_ID=" + queryId;
            st = conn.createStatement();
            rs = st.executeQuery(sql);

            //build roles string from rule values
            if (rs.next()) {
                StringBuffer tmpSb = new StringBuffer(64);

                String currentRule;

                currentRule = rs.getString("RULE_NAME");
                PreparedQuery pq = new PreparedQuery();
                tmpSb = pq.getRuleValuesList(conn, username, currentRule, 1);
                if (tmpSb != null) {
                    String s = tmpSb.toString();
                    if (s != null && s.length() > 1) {
                        //user has some rule values
                        roles = s.substring(1); //actual values start from second character. first character is a comma (,)
                    }
                }
            }

            rs.close();
            st.close();
        } catch (Exception e) {
            logger.error("Error",e);
        }

        return roles;
    }

    /**
     * Set display options for graphs
     * 
     * @param optionsString
     */
    public void setGraphDisplayOptions(String optionsString) {
        setGraphDisplayOptions(optionsString, false);
    }

    /**
     * Set display options for graphs
     * 
     * @param optionsString
     * @param usingShortDescription
     */
    public void setGraphDisplayOptions(String optionsString, boolean usingShortDescription) {

        try {

            if (optionsString != null) {
                int index;
                index = optionsString.lastIndexOf("@");

                if (usingShortDescription || index > -1) {
                    showLegend = true;
                    //set default for showlabels. true for pie charts. false for all other graphs
                    if (queryType == -2) {
                        showLabels = true;
                    } else {
                        showLabels = false;
                    }
                }

                String options;
                if (index > -1) {
                    //options specified as part of short description. for backward compatibility with pre-2.0
                    options = optionsString.substring(index + 1); //+1 so that the @ is not included in the options string
                } else {
                    if (usingShortDescription) {
                        //no @ symbol so graph options not specified in short description
                        options = "";
                    } else {
                        options = optionsString;
                    }
                }

                StringTokenizer st = new StringTokenizer(options.trim(), " ");

                String token;
                while (st.hasMoreTokens()) {
                    token = st.nextToken();

                    if (token.toLowerCase().startsWith("noleg")) {
                        showLegend = false;
                    } else if (token.toLowerCase().startsWith("showlegend")) {
                        showLegend = true;
                    } else if (token.toLowerCase().startsWith("nolab")) {
                        showLabels = false;
                    } else if (token.toLowerCase().startsWith("showlabels")) {
                        showLabels = true;
                    } else if (token.toLowerCase().startsWith("showpoints")) {
                        showPoints = true;
                    } else if (token.toLowerCase().startsWith("showdata")) {
                        showGraphData = true;
                    } else if (token.indexOf("x") != -1) {
                        int idx = token.indexOf("x");
                        graphWidth = Integer.parseInt(token.substring(0, idx));
                        graphHeight = Integer.parseInt(token.substring(idx + 1));
                    } else if (token.indexOf(":") != -1) {
                        int idx = token.indexOf(":");
                        graphYMin = Integer.parseInt(token.substring(0, idx));
                        graphYMax = Integer.parseInt(token.substring(idx + 1));
                    } else if (token.startsWith("#")) {
                        graphBgColor = token;
                    }
                }

            }
        } catch (Exception e) {
            logger.error("Error",e);
        }
    }

    /**
     * Get the drill down queries for a given query
     * 
     * @param qId
     * @return drill down queries for a given query
     */
    public Map<Integer, DrilldownQuery> getDrilldownQueries(int qId) {
        return getDrilldownQueries(qId, true);
    }

    /**
     * Get the drill down queries for a given query
     * 
     * @param qId
     * @param fillParams <code>true</code> if parameters data structures for the drill down queries should be built
     * @return drill down queries for a given query
     */
    public Map<Integer, DrilldownQuery> getDrilldownQueries(int qId, boolean fillParams) {
        TreeMap<Integer, DrilldownQuery> map = new TreeMap<Integer, DrilldownQuery>();

        Connection conn = null;

        try {
            conn = ArtDBCP.getConnection();

            String sql;
            Statement st;
            ResultSet rs;
            int position;

            //drill down queries should have at least one inline parameter where drill down column > 0
            sql = "SELECT ADQ.QUERY_ID, ADQ.DRILLDOWN_QUERY_ID, ADQ.DRILLDOWN_QUERY_POSITION "
                    + "  FROM ART_DRILLDOWN_QUERIES ADQ "
                    + " WHERE ADQ.QUERY_ID = " + qId;

            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                position = rs.getInt("DRILLDOWN_QUERY_POSITION");
                DrilldownQuery drilldown = new DrilldownQuery();
                drilldown.create(conn, qId, position);
                if (fillParams) {
                    drilldown.buildDrilldownParams();
                }
                map.put(new Integer(position), drilldown);
            }
            st.close();
            rs.close();

        } catch (Exception e) {
            logger.error("Error",e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Error",e);
            }
        }

        return map;
    }

    /**
     * Get name and id for all drill down queries
     * 
     * @return name and id for all drill down queries
     */
    public Map getAllDrilldownQueries() {
        Collator stringCollator = Collator.getInstance();
        stringCollator.setStrength(Collator.TERTIARY); //order by case
        TreeMap<String, Integer> map = new TreeMap<String, Integer>(stringCollator);

        Connection conn = null;

        try {
            conn = ArtDBCP.getConnection();
            Statement st = conn.createStatement();

            //candidate drill down query is a query with at least one inline parameter where drill down column > 0
            String SqlQuery = "SELECT AQ.QUERY_ID, AQ.NAME "
                    + " FROM ART_QUERIES AQ "
                    + " WHERE EXISTS "
                    + " (SELECT * FROM ART_QUERY_FIELDS AQF WHERE AQ.QUERY_ID = AQF.QUERY_ID "
                    + " AND AQF.PARAM_TYPE = 'I' AND AQF.DRILLDOWN_COLUMN > 0)";

            ResultSet rs = st.executeQuery(SqlQuery);
            while (rs.next()) {
                map.put(rs.getString("NAME"), new Integer(rs.getInt("QUERY_ID")));
            }
            rs.close();
            st.close();
        } catch (Exception e) {
            logger.error("Error",e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                logger.error("Error",e);
            }
        }

        return map;
    }
	
	/**
     *  Connect to Art repository and retrieve info related to the query.
     *  Also get the query params and build the list with all values
     */
	public void create(){
		create(queryId, true);
	}

    /**
     *  Connect to Art repository and retrieve info related to the query.
     *  Also get the query params and build the list with all values
     */
    public void create(int qId, boolean buildParamList) {
        Connection conn = null;

        try {

            //get query details
            conn = ArtDBCP.getConnection();
            create(conn, qId);

            //build parameter list
			if(buildParamList){
				buildParamList(conn);
			}

        } catch (Exception e) {
            logger.error("Error",e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Error",e);
            }

        }
    }

    //build list of parameters to be used in showParams pages
    private void buildParamList(Connection conn) {
        paramList = new ArrayList<ParamInterface>();

        try {
            //Get parameter definitions
            Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs;

            String SqlQuery = "SELECT FIELD_POSITION, NAME, FIELD_CLASS, SHORT_DESCRIPTION, DESCRIPTION "
                    + ", DEFAULT_VALUE, PARAM_TYPE, PARAM_LABEL, USE_LOV, LOV_QUERY_ID "
                    + ", APPLY_RULES_TO_LOV, CHAINED_PARAM_POSITION, QUERY_ID, CHAINED_VALUE_POSITION "
                    + " FROM ART_QUERY_FIELDS ";

            // Build WHERE part based on the object Type since for portlets id we need
            // to get all the (distinct) parameters of its objects

            if (queryType != 110) {
                SqlQuery = SqlQuery
                        + " WHERE QUERY_ID = " + queryId
                        + " ORDER BY FIELD_POSITION ";
            } else {
                // Lookup all inline query parameters for the queries used by this dashboard
                int[] queryIds = PortletsContainer.getPortletsObjectsId(queryId);
                StringBuilder queryIdsSb = new StringBuilder();
                int i = 0;
                for (i = 0; i < queryIds.length - 1; i++) {
                    queryIdsSb.append("" + queryIds[i] + ", ");
                }
                queryIdsSb.append("" + queryIds[i]);

                // Get all distinct InlineLabels that will appear as parameters
                // (parameters  need to have matching labels to show up one)
                // (in case of chained params, be careful since only the "latest" query defined drives both values
                String sqlOrs = "SELECT MAX(QUERY_ID), PARAM_LABEL"
                        + " FROM ART_QUERY_FIELDS "
                        + " WHERE PARAM_TYPE = 'I' "
                        + " AND QUERY_ID in (" + queryIdsSb.toString() + ") "
                        + " GROUP BY PARAM_LABEL ";

                rs = st.executeQuery(sqlOrs);

                StringBuilder orChainSb = new StringBuilder();
                while (rs.next()) {
                    orChainSb.append("OR ( QUERY_ID = ");
                    orChainSb.append(rs.getInt(1));
                    orChainSb.append(" AND PARAM_LABEL = '");
                    orChainSb.append(rs.getString(2));
                    orChainSb.append("' )");
                }
                rs.close();

                SqlQuery = SqlQuery
                        + " WHERE 1 = 0 "
                        + orChainSb.toString()
                        + "  ORDER BY FIELD_POSITION ";
            }

            rs = st.executeQuery(SqlQuery);

            while (rs.next()) { // for each parameter of this query...

                // build the parameter object
                String paramName = rs.getString("NAME");
                String paramShortDescr = rs.getString("SHORT_DESCRIPTION");
                String paramDescr = rs.getString("DESCRIPTION");
                String paramPosition = rs.getString("FIELD_POSITION");
                String paramClass = rs.getString("FIELD_CLASS").toUpperCase();
                String defaultValue = rs.getString("DEFAULT_VALUE");

                boolean isLovParameter = !rs.getString("USE_LOV").equals("N");
                boolean isOldBind = false;


                // Build param id for html input/select box
                // syntax is [param_type]_[param position]
                // html names need to start with a letter, thus param_type is P or M or B

                String paramHtmlId, paramHtmlName;
                String paramType;

                paramHtmlId = "P_" + rs.getString("QUERY_ID") + "_" + paramPosition;
                if (rs.getString("PARAM_TYPE").equals("I")) { // inline
                    paramHtmlName = "P_" + rs.getString("PARAM_LABEL"); // use P_ to maintain compatibility with params passed via URL to QueryExecute
                    paramType = "INLINE";
                } else if (rs.getString("PARAM_TYPE").equals("M")) { // multi                    
                    paramHtmlName = "M_" + rs.getString("PARAM_LABEL"); //logic in parameterprocessor and preparedquery will prevent sql injection
                    paramType = "MULTI";
                } else { // obsolete bind param for backward compatibility
                    paramHtmlName = "P" + rs.getString("CHAINED_PARAM_POSITION");
                    isOldBind = true;
                    paramType = "BIND";
                }

                if (!isLovParameter) {
                    // Normal parameters where the user need to type in
                    if (paramType.equals("MULTI")) {
                        //multi parameter that doesn't use LOV
                        paramList.add(new HtmlTextArea(paramHtmlId, paramHtmlName, paramName, paramShortDescr, paramDescr, defaultValue));
                    } else {
                        if (paramClass.equals("VARCHAR") || paramClass.equals("INTEGER") || paramClass.equals("NUMBER")) {
                            // Simple input text
                            paramList.add(new HtmlTextInput(paramHtmlId, paramHtmlName, paramName, paramClass, paramShortDescr, paramDescr, defaultValue));

                        } else if (paramClass.equals("DATE") || paramClass.equals("DATETIME")) {
                            // Build date box  - bind date are not supported from 1.7beta1
                            paramList.add(new HtmlDateInput(paramHtmlId, paramHtmlName, paramName, paramShortDescr, paramDescr, defaultValue, paramClass));

                        } else if (paramClass.equals("TEXT")) {
                            // TextArea
                            paramList.add(new HtmlTextArea(paramHtmlId, paramHtmlName, paramName, paramShortDescr, paramDescr, defaultValue));
                        }
                    }
                } else {
                    // LOV parameters - values are retrieved from a query
                    int chainedId = rs.getInt("CHAINED_PARAM_POSITION");
                    String chainedParamId = null;
                    String chainedValueId = null;
                    if (chainedId > 0 && !isOldBind) {
                        chainedParamId = "P_" + rs.getString("QUERY_ID") + "_" + rs.getInt("CHAINED_PARAM_POSITION");

                        //allow filter value to come from any chained parameter, not necessarily the previous in the sequence
                        int valueId = rs.getInt("CHAINED_VALUE_POSITION");
                        if (valueId > 0) {
                            chainedValueId = "P_" + rs.getString("QUERY_ID") + "_" + valueId;
                        } else {
                            //chained parameter gets it's value from the chained parameter sequence
                            chainedValueId = chainedParamId;
                        }
                    }
                    int lovQueryId = rs.getInt("LOV_QUERY_ID");
                    boolean useSmartRules;
                    if ("Y".equals(rs.getString("APPLY_RULES_TO_LOV"))) {
                        useSmartRules = true;
                    } else {
                        useSmartRules = false;
                    }
                    paramList.add(new HtmlLovParam(paramHtmlId, paramHtmlName, paramName, paramShortDescr, paramDescr, defaultValue, lovQueryId, chainedParamId, useSmartRules, username, chainedValueId));
                }
            }
            rs.close();
            st.close();
        } catch (Exception e) {
            logger.error("Error",e);
        }
    }

    /**
     * Get rules for a given query
     * 
     * @param qId
     * @return rules for a given query
     */
    public Map getQueryRules(int qId) {
        Collator stringCollator = Collator.getInstance();
        stringCollator.setStrength(Collator.TERTIARY); //order by case
        TreeMap<String, Rule> map = new TreeMap<String, Rule>(stringCollator);

        Connection conn = null;

        try {
            conn = ArtDBCP.getConnection();

            String sql;
            PreparedStatement ps;
            ResultSet rs;
            String ruleName;
            String fieldName;

            sql = "SELECT AQR.RULE_NAME, AQR.FIELD_NAME, AR.SHORT_DESCRIPTION "
                    + " FROM ART_QUERY_RULES AQR, ART_RULES AR "
                    + " WHERE AQR.QUERY_ID = ? "
                    + " AND AQR.RULE_NAME = AR.RULE_NAME "
                    + " ORDER BY RULE_NAME, FIELD_NAME ";

            ps = conn.prepareStatement(sql);
            ps.setInt(1, qId);
            rs = ps.executeQuery();
            while (rs.next()) {
                Rule rule = new Rule();
                ruleName = rs.getString("RULE_NAME");
                fieldName = rs.getString("FIELD_NAME");
                rule.setRuleName(ruleName);
                rule.setFieldName(fieldName);
                rule.setDescription(rs.getString("SHORT_DESCRIPTION"));
                map.put(ruleName + fieldName, rule);
            }
            ps.close();
            rs.close();

        } catch (Exception e) {
            logger.error("Error",e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Error",e);
            }
        }

        return map;
    }

    /**
     * Get all rules in the database
     * 
     * @return all rules in the database
     */
    public Map getAllRules() {
        Collator stringCollator = Collator.getInstance();
        stringCollator.setStrength(Collator.TERTIARY); //order by case
        TreeMap<String, Rule> map = new TreeMap<String, Rule>(stringCollator);

        Connection conn = null;

        try {
            conn = ArtDBCP.getConnection();

            String sql;
            PreparedStatement ps;
            ResultSet rs;
            String ruleName;

            sql = "SELECT RULE_NAME, SHORT_DESCRIPTION FROM ART_RULES";

            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Rule rule = new Rule();
                ruleName = rs.getString("RULE_NAME");
                rule.setRuleName(ruleName);
                rule.setDescription(rs.getString("SHORT_DESCRIPTION"));
                map.put(ruleName, rule);
            }
            ps.close();
            rs.close();

        } catch (Exception e) {
            logger.error("Error",e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Error",e);
            }
        }

        return map;
    }

    /**
     * Get rules that have not already been added to a query
     * 
     * @param qId
     * @return rules that have not already been added to a query
     */
    public Map getAvailableRules(int qId) {
        Collator stringCollator = Collator.getInstance();
        stringCollator.setStrength(Collator.TERTIARY); //order by case
        TreeMap<String, Rule> map = new TreeMap<String, Rule>(stringCollator);

        Connection conn = null;

        try {
            conn = ArtDBCP.getConnection();

            String sql;
            PreparedStatement ps;
            ResultSet rs;
            String ruleName;

            sql = "SELECT AR.RULE_NAME, AR.SHORT_DESCRIPTION "
                    + " FROM ART_RULES AR "
                    + " WHERE NOT EXISTS "
                    + " (SELECT * FROM ART_QUERY_RULES AQR "
                    + " WHERE AQR.QUERY_ID=? AND AQR.RULE_NAME=AR.RULE_NAME)";

            ps = conn.prepareStatement(sql);

            ps.setInt(1, qId);
            rs = ps.executeQuery();
            while (rs.next()) {
                Rule rule = new Rule();
                ruleName = rs.getString("RULE_NAME");
                rule.setRuleName(ruleName);
                rule.setDescription(rs.getString("SHORT_DESCRIPTION"));
                map.put(ruleName, rule);
            }
            ps.close();
            rs.close();

        } catch (Exception e) {
            logger.error("Error",e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Error",e);
            }
        }

        return map;
    }

    /**
     * Get lov queries (group id=0 or query type=119,120)
     * 
     * @return lov queries
     */
    public Map getLovQueries() {
        TreeMap<Integer, String> map = new TreeMap<Integer, String>();

        Connection conn = null;

        try {
            conn = ArtDBCP.getConnection();
            Statement st = conn.createStatement();
            String SqlQuery = "SELECT QUERY_ID, NAME FROM ART_QUERIES "
                    + " WHERE QUERY_GROUP_ID=0 OR QUERY_TYPE=119 OR QUERY_TYPE=120";

            ResultSet rs = st.executeQuery(SqlQuery);
            while (rs.next()) {
                map.put(new Integer(rs.getInt("QUERY_ID")), rs.getString("NAME"));
            }
            st.close();
            rs.close();
        } catch (Exception e) {
            logger.error("Error",e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Error",e);
            }
        }

        return map;
    }

    /**
     * Get object groups that junior and senior admins can see
     * 
     * @param level
     * @param uname
     * @return object groups that junior and senior admins can see
     */
    public Map getAdminObjectGroups(int level, String uname) {
        Collator stringCollator = Collator.getInstance();
        stringCollator.setStrength(Collator.TERTIARY); //order by case
        TreeMap<String, ObjectGroup> map = new TreeMap<String, ObjectGroup>(stringCollator);

        Connection conn = null;

        try {
            conn = ArtDBCP.getConnection();

            String sql;
            PreparedStatement ps;
            ResultSet rs;
            String groupName;

            if (level > 30) {
                //get all object groups
                sql = "SELECT AG.QUERY_GROUP_ID, AG.NAME, AG.DESCRIPTION"
                        + " FROM ART_QUERY_GROUPS AG ";
                ps = conn.prepareStatement(sql);
            } else {
                // get only object groups matching the "junior" admin priviledges
                sql = "SELECT AG.QUERY_GROUP_ID, AG.NAME, AG.DESCRIPTION"
                        + " FROM ART_QUERY_GROUPS AG, ART_ADMIN_PRIVILEGES APG "
                        + " WHERE AG.QUERY_GROUP_ID = APG.VALUE_ID "
                        + " AND APG.PRIVILEGE = 'GRP' "
                        + " AND APG.USERNAME = ? ";
                ps = conn.prepareStatement(sql);
                ps.setString(1, uname);
            }

            rs = ps.executeQuery();
            while (rs.next()) {
                ObjectGroup og = new ObjectGroup();
                groupName = rs.getString("NAME");
                og.setName(groupName);
                og.setGroupId(rs.getInt("QUERY_GROUP_ID"));
                og.setDescription(rs.getString("DESCRIPTION"));
                map.put(groupName, og);
            }
            ps.close();
            rs.close();

        } catch (Exception e) {
            logger.error("Error",e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Error",e);
            }
        }

        return map;
    }

    /**
     * Get object groups that junior and senior admins can see
     * 
     * @param level
     * @param uname
     * @return object groups that junior and senior admins can see
     */
    public Map getAdminObjectGroupsList(int level, String uname) {
        Collator stringCollator = Collator.getInstance();
        stringCollator.setStrength(Collator.TERTIARY); //order by case
        TreeMap<String, Integer> map = new TreeMap<String, Integer>(stringCollator);

        Connection conn = null;

        try {
            conn = ArtDBCP.getConnection();

            String sql;
            PreparedStatement ps;
            ResultSet rs;

            if (level > 30) {
                //get all object groups
                sql = "SELECT QUERY_GROUP_ID, NAME FROM ART_QUERY_GROUPS";
                ps = conn.prepareStatement(sql);
            } else {
                // get only object groups matching the "junior" admin priviledges
                sql = "SELECT AG.QUERY_GROUP_ID, AG.NAME  "
                        + " FROM ART_QUERY_GROUPS AG, ART_ADMIN_PRIVILEGES AP "
                        + " WHERE AG.QUERY_GROUP_ID = AP.VALUE_ID "
                        + " AND AP.PRIVILEGE = 'GRP' "
                        + " AND AP.USERNAME = ? ";
                ps = conn.prepareStatement(sql);
                ps.setString(1, uname);
            }

            rs = ps.executeQuery();
            while (rs.next()) {
                map.put(rs.getString("NAME"), new Integer(rs.getInt("QUERY_GROUP_ID")));
            }
            rs.close();
            ps.close();

        } catch (Exception e) {
            logger.error("Error",e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Error",e);
            }
        }

        return map;
    }

    /**
     * Get datasources that junior and senior admins can see
     * 
     * @param level
     * @param uname
     * @return datasources that junior and senior admins can see
     */
    public Map getAdminDatasources(int level, String uname) {
        Collator stringCollator = Collator.getInstance();
        stringCollator.setStrength(Collator.TERTIARY); //order by case
        TreeMap<String, Integer> map = new TreeMap<String, Integer>(stringCollator);

        Connection conn = null;

        try {
            conn = ArtDBCP.getConnection();

            String sql;
            PreparedStatement ps;
            ResultSet rs;

            if (level > 30) {
                //get all datasources
                sql = "SELECT DATABASE_ID, NAME FROM ART_DATABASES";
                ps = conn.prepareStatement(sql);
            } else {
                // get only datasources matching the "junior" admin priviledges
                sql = "SELECT AD.DATABASE_ID, AD.NAME  "
                        + " FROM ART_DATABASES AD, ART_ADMIN_PRIVILEGES AP "
                        + " WHERE AD.DATABASE_ID = AP.VALUE_ID "
                        + " AND AP.PRIVILEGE = 'DB' "
                        + " AND AP.USERNAME = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, uname);
            }

            rs = ps.executeQuery();
            while (rs.next()) {
                map.put(rs.getString("NAME"), new Integer(rs.getInt("DATABASE_ID")));
            }
            rs.close();
            ps.close();

        } catch (Exception e) {
            logger.error("Error",e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Error",e);
            }
        }

        return map;
    }

      /**
     * Get all parameters for a query, ordered by field position
     * 
     * @param qId query id for the relevant query
     * @return all parameters for a query, ordered by field position
     */
    public Map getQueryParams(int qId) {
        TreeMap<Integer, ArtQueryParam> map = new TreeMap<Integer, ArtQueryParam>();

        Connection conn = null;

        try {
            conn = ArtDBCP.getConnection();

            String sql;
            PreparedStatement ps;
            ResultSet rs;
            int fieldPosition;

            sql = "SELECT FIELD_POSITION "
                    + " FROM ART_QUERY_FIELDS "
                    + " WHERE QUERY_ID = ?";

            ps = conn.prepareStatement(sql);

            ps.setInt(1, qId);
            rs = ps.executeQuery();
            while (rs.next()) {
                ArtQueryParam param = new ArtQueryParam();
                fieldPosition = rs.getInt("FIELD_POSITION");
                param.create(conn, qId, fieldPosition);
                map.put(fieldPosition, param);
            }
            rs.close();
            ps.close();

        } catch (Exception e) {
            logger.error("Error",e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Error",e);
            }
        }

        return map;
    }

    /**
     * Delete query
     * 
     * @return <code>true</code> if successful
     */
    public boolean delete() {
        boolean success = false;

        Connection conn = null;

        try {
            conn = ArtDBCP.getConnection();

            String sql;
            PreparedStatement ps;

            //delete query-user relationships
            sql = "DELETE FROM ART_USER_QUERIES WHERE QUERY_ID = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, queryId);
            ps.executeUpdate();

            //delete query parameters
            sql = "DELETE FROM ART_QUERY_FIELDS WHERE QUERY_ID = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, queryId);
            ps.executeUpdate();

            //delete sql source
            sql = "DELETE FROM ART_ALL_SOURCES  WHERE OBJECT_ID = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, queryId);
            ps.executeUpdate();

            //delete query-rule relationships
            sql = "DELETE FROM ART_QUERY_RULES WHERE QUERY_ID = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, queryId);
            ps.executeUpdate();

            //delete drilldown queries
            sql = "DELETE FROM ART_DRILLDOWN_QUERIES WHERE QUERY_ID = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, queryId);
            ps.executeUpdate();


            //lastly, delete query
            sql = "DELETE FROM ART_QUERIES WHERE QUERY_ID = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, queryId);
            ps.executeUpdate();

            ps.close();
            success = true;
        } catch (Exception e) {
            logger.error("Error",e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                logger.error("Error",e);
            }
        }

        return success;
    }

    /**
     * Copy query
     * 
     * @param newQueryName
     * @return <code>true</code> if successful
     */
    public boolean copy(String newQueryName) {
        boolean success = false;

        Connection conn = null;

        try {
            conn = ArtDBCP.getConnection();

            String sql;
            PreparedStatement ps;
            ResultSet rs;

            String sqlUpdate;
            PreparedStatement psUpdate;

            //create new query
            int newQueryId = allocateNewId(conn);
            if (newQueryId != -1) {
                sql = "SELECT QUERY_GROUP_ID, SHORT_DESCRIPTION, DESCRIPTION, USES_RULES "
                        + " ,DATABASE_ID, QUERY_TYPE, ACTIVE_STATUS "
                        + " ,CONTACT_PERSON, TEMPLATE, XMLA_URL, XMLA_DATASOURCE, XMLA_CATALOG, XMLA_USERNAME, XMLA_PASSWORD "
                        + " ,X_AXIS_LABEL, Y_AXIS_LABEL, GRAPH_OPTIONS "
                        + " FROM ART_QUERIES "
                        + " WHERE QUERY_ID = ? ";
                ps = conn.prepareStatement(sql);

                ps.setInt(1, queryId);
                rs = ps.executeQuery();

                //copy query definition
                if (rs.next()) {
                    sqlUpdate = "UPDATE ART_QUERIES SET QUERY_GROUP_ID= ?, NAME = ?, SHORT_DESCRIPTION = ? , DESCRIPTION = ? , USES_RULES = ? "
                            + ", DATABASE_ID = ? , QUERY_TYPE = ?, ACTIVE_STATUS = ?, CONTACT_PERSON = ?, TEMPLATE = ? "
                            + ", XMLA_URL = ?, XMLA_DATASOURCE = ?, XMLA_CATALOG = ?, X_AXIS_LABEL=?, Y_AXIS_LABEL=?, GRAPH_OPTIONS=? "
                            + ", XMLA_USERNAME=?, XMLA_PASSWORD=?, UPDATE_DATE=? "
                            + " WHERE QUERY_ID = ? ";
                    psUpdate = conn.prepareStatement(sqlUpdate);

                    psUpdate.setInt(1, rs.getInt("QUERY_GROUP_ID"));
                    psUpdate.setString(2, newQueryName);
                    psUpdate.setString(3, rs.getString("SHORT_DESCRIPTION"));
                    psUpdate.setString(4, rs.getString("DESCRIPTION"));
                    psUpdate.setString(5, rs.getString("USES_RULES"));
                    psUpdate.setInt(6, rs.getInt("DATABASE_ID"));
                    psUpdate.setInt(7, rs.getInt("QUERY_TYPE"));
                    psUpdate.setString(8, rs.getString("ACTIVE_STATUS"));
                    psUpdate.setString(9, rs.getString("CONTACT_PERSON"));
                    psUpdate.setString(10, rs.getString("TEMPLATE"));
                    psUpdate.setString(11, rs.getString("XMLA_URL"));
                    psUpdate.setString(12, rs.getString("XMLA_DATASOURCE"));
                    psUpdate.setString(13, rs.getString("XMLA_CATALOG"));
                    psUpdate.setString(14, rs.getString("X_AXIS_LABEL"));
                    psUpdate.setString(15, rs.getString("Y_AXIS_LABEL"));
                    psUpdate.setString(16, rs.getString("GRAPH_OPTIONS"));
                    psUpdate.setString(17, rs.getString("XMLA_USERNAME"));
                    psUpdate.setString(18, rs.getString("XMLA_PASSWORD"));
                    psUpdate.setDate(19, new java.sql.Date(System.currentTimeMillis()));

                    psUpdate.setInt(20, newQueryId);

                    psUpdate.executeUpdate();
                    psUpdate.close();

                    //copy query parameters
                    copyTableRow(conn, "ART_QUERY_FIELDS", "QUERY_ID", queryId, newQueryId);

                    //copy sql source
                    copyTableRow(conn, "ART_ALL_SOURCES", "OBJECT_ID", queryId, newQueryId);

                    //copy query rules
                    copyTableRow(conn, "ART_QUERY_RULES", "QUERY_ID", queryId, newQueryId);

                    //copy drilldown queries
                    copyTableRow(conn, "ART_DRILLDOWN_QUERIES", "QUERY_ID", queryId, newQueryId);
                }
                rs.close();
                ps.close();
                success = true;
            }
        } catch (Exception e) {
            logger.error("Error",e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                logger.error("Error",e);
            }
        }

        return success;
    }

    //copy some aspect of a query
    private int copyTableRow(Connection conn, String tableName, String keyColumnName, int keyId, int newKeyId) {
        try {
            PreparedStatement ps;
            PreparedStatement psInsert;

            String SQL = "SELECT * FROM " + tableName
                    + " WHERE " + keyColumnName + " = ?";

            StringBuilder tmp = new StringBuilder();
            ResultSet rs;
            ResultSetMetaData rsmd;
            int i = 0;
            int count = 0;

            ps = conn.prepareStatement(SQL);
            ps.setInt(1, keyId);
            rs = ps.executeQuery();
            rsmd = rs.getMetaData();

            SQL = "INSERT INTO " + tableName + " VALUES ( ";

            for (i = 0; i < rsmd.getColumnCount() - 1; i++) {
                tmp.append("?,");
            }
            tmp.append("? )");
            SQL = SQL + tmp.toString();
            psInsert = conn.prepareStatement(SQL);

            while (rs.next()) {
                // Build insert prepared statement variables
                for (i = 0; i < rsmd.getColumnCount(); i++) {
                    switch (rsmd.getColumnType(i + 1)) {
                        case Types.CHAR:
                        case Types.VARCHAR:
                        case Types.LONGVARCHAR:
                            psInsert.setString(i + 1, rs.getString(i + 1));
                            break;
                        case Types.NUMERIC:
                        case Types.INTEGER:
                        case Types.TINYINT:
                        case Types.SMALLINT:
                        case Types.BIGINT:
                            if (rsmd.getColumnName(i + 1).toUpperCase().equals(keyColumnName)) { // postgres 8 returns lower case...
                                psInsert.setInt(i + 1, newKeyId);
                            } else {
                                psInsert.setInt(i + 1, rs.getInt(i + 1));
                            }
                            break;
                        case Types.FLOAT:
                            psInsert.setFloat(i + 1, rs.getFloat(i + 1));
                            break;
                        case Types.DOUBLE:
                            psInsert.setDouble(i + 1, rs.getFloat(i + 1));
                            break;
                        case Types.DATE:
                            psInsert.setDate(i + 1, rs.getDate(i + 1));
                            break;
                        case Types.TIMESTAMP:
                            psInsert.setTimestamp(i + 1, rs.getTimestamp(i + 1));
                            break;
                        case Types.NULL:
                            psInsert.setNull(i + 1, Types.NULL);
                            break;
                        default:
                            psInsert.setString(i + 1, rs.getString(i + 1));
                    }
                }

                //insert
                psInsert.executeUpdate();
                count++;
            }
            rs.close();
            ps.close();
            psInsert.close();

            return count;
        } catch (SQLException e) {
            logger.error("Error",e);
            return -1;
        }
    }
    
    /**
     * Check if a parameter label validly exists in the sql source
     * 
     * @param qId
     * @param label
     * @return <code>true</code> if parameter label validly exists in the sql source
     */
    public boolean validParamLabel(int qId, String label) {
        boolean validLabel = false;

        Connection conn = null;

        try {
            conn = ArtDBCP.getConnection();

            String sql;
            PreparedStatement ps;
            ResultSet rs;

            //get query source
            sql = "SELECT TEXT_INFO FROM ART_ALL_SOURCES "
                    + " WHERE OBJECT_ID = ?"
                    + " ORDER BY LINE_NUMBER";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, qId);
            rs = ps.executeQuery();

            text = "";
            StringBuilder concatText = new StringBuilder(1024);
            while (rs.next()) {
                concatText.append(rs.getString(1));
            }
            text = concatText.toString();
            ps.close();
            rs.close();

            //check if parameter label exists in query source. don't check if query has chained params
            if (text.indexOf(label) == -1) {
                //label doesn't exist in sql source. check if this query uses chained params
                sql = "SELECT CHAINED_PARAM_POSITION "
                        + " FROM ART_QUERY_FIELDS "
                        + " WHERE QUERY_ID = ?";

                ps = conn.prepareStatement(sql);
                ps.setInt(1, qId);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (rs.getInt("CHAINED_PARAM_POSITION") > 0) {
                        //query uses chained params. label may not be needed in sql logic so don't enforce label presence
                        validLabel = true;
                        break;
                    }
                }
                rs.close();
                ps.close();
            } else {
                //label exists in sql source
                validLabel = true;
            }

        } catch (Exception e) {
            logger.error("Error",e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Error",e);
            }
        }

        return validLabel;
    }
    

    /**
     * Get all parameters for a query, with the parameter's html name as the key
     * 
     * @param qId query id for the relevant query     
     * @return all parameters for a query
     */
    public Map<String, ArtQueryParam> getHtmlParams(int qId) {
        Map<String, ArtQueryParam> params = new HashMap<String, ArtQueryParam>();

        Connection conn = null;

        try {
            conn = ArtDBCP.getConnection();

            String sql;
            PreparedStatement ps;
            ResultSet rs;

            sql = "SELECT FIELD_POSITION, NAME, PARAM_LABEL, PARAM_TYPE "
                    + " FROM ART_QUERY_FIELDS WHERE QUERY_ID =?";

            ps = conn.prepareStatement(sql);
            ps.setInt(1, qId);
            rs = ps.executeQuery();

            String htmlName;
            String label;
            int position;
            String paramType;
            while (rs.next()) {
                position = rs.getInt("FIELD_POSITION");
                label = rs.getString("PARAM_LABEL");
                paramType = rs.getString("PARAM_TYPE");

                if (paramType.equals("I")) {
                    //inline param                    
                    htmlName = "P_" + label;
                    ArtQueryParam param = new ArtQueryParam();
                    param.create(conn, qId, position);
                    params.put(htmlName, param);                    
                } else if (paramType.equals("M")) {
                    //multi param. can be either labelled (M_label) or non-labelled (M_1)
                    //add entry for labelled param
                    htmlName = "M_" + label;
                    ArtQueryParam param = new ArtQueryParam();
                    param.create(conn, qId, position);
                    params.put(htmlName, param);

                    //add entry for non-labelled param
                    htmlName = "M_" + position;
                    params.put(htmlName, param);
                }
            }

            ps.close();
            rs.close();

        } catch (Exception e) {
            logger.error("Error",e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Error",e);
            }
        }

        return params;
    }
   
}