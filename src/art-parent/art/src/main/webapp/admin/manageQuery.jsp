<%@ page import="java.sql.*,art.utils.*" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ include file ="headerAdmin.jsp" %>

<%
 Connection conn = (Connection) session.getAttribute("SessionConn");
 int queryId = Integer.parseInt(request.getParameter("QUERY_ID"));
 String requestGroupId=request.getParameter("GROUP_ID");

 if ( conn == null || conn.isClosed()) {
%>
<jsp:forward page="error.jsp">
    <jsp:param name="MOD" value="Edit Query"/>
    <jsp:param name="ACT" value="Get connection from session"/>
    <jsp:param name="MSG" value="Database connection not valid. Please log in again"/>
    <jsp:param name="NUM" value="100"/>
</jsp:forward>
<%
}

//refresh connection in case new queries have been created/deleted
conn.commit();

ArtQuery aq = new ArtQuery();
boolean queryExists = aq.create(conn, queryId);
if (!queryExists) {
String act = "Get Query Header and SQL for query " + queryId;
%>
<jsp:forward page="error.jsp">
    <jsp:param name="MOD" value="Edit Query"/>
    <jsp:param name="ACT" value="<%=act%>"/>
    <jsp:param name="MSG" value="Cannot get the Query Header or SQL. Please check if the query exists."/>
    <jsp:param name="NUM" value="120"/>
</jsp:forward>
<%
}
int queryGroupId = aq.getGroupId();
int queryType=aq.getQueryType();
boolean usesRules=false;
if(StringUtils.equals(aq.getUsesRules(),"Y")){
	usesRules=true;
}

%>

<p>
<table align="center">
    <tr><td class="title" colspan="2" > Query Editor </td></tr>

    <tr><td class="attr" >
            <form method="post" action="editQuery.jsp" class="data">
                <input type="hidden" name="QUERY_ID" value="<%=queryId%>">
                <input type="hidden" name="GROUP_ID" value="<%=requestGroupId%>">
                <input type="hidden" name="QUERYACTION" value="MODIFY">
                <input type="submit" value="Header and Source">
            </form>
        </td>
        <td class="data"> Edit Header and Source code</td></tr>

    <%
     if (queryType<110 || (queryType>=112 && queryType<=119)) {
     //static lovs, dashboards, text ojbects do not have parameters
    %>
    <tr><td class="attr" >
            <form method="post" action="manageParameters.jsp" class="data">
                <input type="hidden" name="QUERY_ID" value="<%=queryId%>">
                <input type="hidden" name="GROUP_ID" value="<%=requestGroupId%>">
                <input type="submit" value="Parameters">
            </form>
        </td>
        <td class="data"> Edit Parameters</td></tr>

    <%
     }
     if (usesRules) { // show only if the query uses rules
    %>
    <tr><td class="attr" >
            <form method="post" action="manageQueryRules.jsp" class="data">
                <input type="hidden" name="QUERY_ID" value="<%=queryId%>">
                <input type="hidden" name="GROUP_ID" value="<%=requestGroupId%>">
                <input type="submit" value="Rules">
            </form>
        </td>
        <td class="data"> Edit Rules</td></tr>
        <%
         }
        %>

    <%
     if (queryGroupId!=0 && queryType!=-10 && (queryType<0 || queryType==0 || queryType==103)) {
     //only graphs and normal queries can have drill down queries
    %>
    <tr><td class="attr" >
            <form method="post" action="manageDrilldowns.jsp" class="data">
                <input type="hidden" name="QUERY_ID" value="<%=queryId%>">
                <input type="submit" value="Drill Down Queries">
            </form>
        </td>
        <td class="data">Edit Drill Down Queries</td></tr>

    <% }%>


</table>
</p>

<div align="center" valign="center">
    <table>
        <tr><td>
                <form name="backToartQueryConsole" method="get" action="manageQueries.jsp">
                    <input type="submit"  value=" << " name="backQuery"><span style="font-size:80%">&nbsp;Back to Query Management Console</span>
                </form>
            </td></tr>
    </table>
</div>
<p>
<table align="center"  width="80%">
    <tr><td class="attr" align="center"><b><%=aq.getName()%></b> (ID: <b><%=queryId%></b>) <br>&nbsp;</td></tr>
    <tr><td class="data" align="center">
            <textarea cols="70" rows="10" wrap="off" style="background-color: #D3D3D3" readonly><%=aq.getText().trim()%>
            </textarea>
        </td></tr>
        
    <tr>
        <td align="center">
            
            <% if(queryType!=120) { //don't provide link to execute static lov %> 
            <small>Direct URLs <br>
                <a href="<%=request.getContextPath()%><%=QueryUrl.getExecuteUrl(queryId, true)%>" target="_blank">Execute</a>
                (using default parameter values)<br>

                <% if(queryGroupId!=0 && queryType!=119 && queryType!=120 && queryType!= 111) { %>
                <a href="<%=request.getContextPath()%>/user/showParams.jsp?queryId=<%=queryId%>" target="_blank">Params page</a> <br>                
                <br>
                (if the query is granted to the <b>public_user</b> user,
                the links work without authentication by appending <span style="color:red">&_public_user=true</span>)
                <% } %>
            </small>
            <% } %>
        </td>
    </tr>
    
</table>
</p>


<p>
<table align="center"  width="80%">

    <tr><td type="attr">
            <%
             if(queryGroupId!= 0 && queryType!=119 && queryType!=120 && (queryType<110 || (queryType>=112 && queryType<=118))) {
				 //list of values, dashboards, text ojbects do not have parameters
            %>
            <small>
                <b>Hints:</b><br>
                <ol>
                    <li><b>Header and SQL</b> <br>
                        The header contains query properties like name, group, description, datasource etc. while
                        the SQL source is an SQL query that may contain
						parameters or xml-style tags to create dynamic queries.
                    </li>
                    <li><b>Parameters</b><br>
                        Before executing a query, users will be prompted to enter values for query parameters.
						The values entered
						could be of different data types e.g. <i> varchar</i>,  <i> integer</i>,  <i> number</i>,  <i> date</i>
						or values could be picked from a pop-up list. The latter can be a single value or a set of values.
						There are two types of parameters:
						<i>inline</i> are used to replace single values in the SQL code;
						<i>multi</i> are used to replace a set of values in the SQL code; (i.e. the string
						 '<i>table_column in (&lt;list of  selectedvalues&gt;)</i>' is added to the SQL just before executing
						 the query).
                    </li>
					<%
					if (queryGroupId!=0 && queryType!=119 && queryType!=120 && queryType!=-10 && (queryType<0 || queryType==0 || queryType==103)) {
					//only graphs and normal queries can have drill down queries
					%>
                    <li><b>Drill Down Queries</b> <br>
						A query can have drill down queries defined for it. When defined, a hyperlink will be displayed
						after the query's last data column allowing a user to run another query based on certain values
						of the main query. Drill down queries can be used with pie charts, bar graphs
						and normal queries when displayed in one of the html modes.
                    </li>
                    <%
					}
                     if(usesRules) {
                    %>
                    <li><b>Rules</b> <br>
                        Rules are used to filter the query results in order to return only the rows where a column
						value appears in a list of values associated with the user that is running the query.
                    </li>
                    <%
                     }
                    %>
                </ol>
            </small>
            <%
             } else if(queryGroupId==0 || queryType==119){
            %>
            <small>
                The query above is to retrieve a list of values in order to present to users a
                pop-up list of values instead of having a free text field to specify a query parameter value.
            </small>
            <%
             }
            %>
        </td></tr>

</table>
</p>
<%@ include file ="/user/footer.jsp" %>
