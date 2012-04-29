<%@ page import="art.utils.*,java.sql.*,art.servlets.ArtDBCP" %>
<%@ include file ="headerAdmin.jsp" %>


<%
String action = request.getParameter("ACTION");
String datasourceIdString=request.getParameter("DATASOURCE_ID");
int datasourceId=-1;
if(datasourceIdString!=null){
	datasourceId=Integer.parseInt(datasourceIdString);
}

Datasource ds=new Datasource();

if (action.equals("DELETE")){
	//check if queries exist that use this datasource
	java.util.Map queries=ds.getLinkedQueries(datasourceId);
	if(queries.size()>0){
		out.println("<pre>Error: There are queries targeting the datasource you want to delete.");
		out.println("       Delete the following queries or change their datasources");
		out.println("       in order to be able to delete this datasource: ");
		out.println();
								
		java.util.Iterator it=queries.entrySet().iterator();		
		while(it.hasNext()){
			java.util.Map.Entry entry = (java.util.Map.Entry)it.next();
			ArtQuery aq=(ArtQuery)entry.getValue();			
			out.println("Query ID: " + aq.getQueryId() + " , Name: " + aq.getName() + " , Group ID: " + aq.getGroupId());
		}
		out.println("</pre>");
		%>
		<%@ include file="footer.html" %>
		<%
		return;
	} else {
		//no linked queries. delete datasource
		ds.delete(datasourceId);
		ArtDBCP.refreshConnections();
		response.sendRedirect("manageDatasources.jsp");
		return;
	}
}

boolean modify=false;
if (action.equals("MODIFY")){
	modify=true;
	ds.load(datasourceId);
}

String msg;
%>


<form name="editDatasource" method="post" action="execEditDatasource.jsp">    	
	<input type="hidden" name="ACTION" value="<%=action%>">
	
	<table align="center">		
		<tr>
			<td class="title" colspan="2">Manage Datasources</td>
		</tr>
		<tr>
			<td class="data" colspan="2"><b>Datasource Definition</b></td>
		</tr>
		
	    <tr>
			<td class="data"> ID </td> 
			<%			
			String inputType="hidden";			
			if(modify){				
				inputType="text";
			} 
			%>
			<td class="data"> <input type="<%=inputType%>" name="DATASOURCE_ID" value="<%=ds.getDatasourceId()%>" size="25" readonly </td>			
		</tr>

		<tr><td class="data"> Datasource Name </td>
			<td class="data"> <input type="text" name="NAME" value="<%=ds.getName()%>" size="25" maxlength="25"> </td>
		</tr>
		
		<tr><td class="data"> Driver </td>
			<td class="data"> <input type="text" name="DRIVER" value="<%=ds.getDriver()%>" size="50" maxlength="200"> </td>
		</tr>
		
		<tr><td class="data"> Database URL </td>
			<td class="data"> <input type="text" name="URL" value="<%=ds.getUrl()%>" size="50" maxlength="2000"> </td>
		</tr>
		
		<tr><td class="data"> Username </td>
			<td class="data"> <input type="text" name="USERNAME" value="<%=ds.getUsername()%>" size="25" maxlength="25"> </td>
		</tr>
		
		<tr>
			<td class="data"> Password </td>			
			<td class="data">
				<input type="hidden" name="OLD_PASSWORD" value="<%=ds.getPassword()%>">
				<input type="password" name="PASSWORD" value="" size="25" maxlength="40">
			</td>
		</tr>
		
		<tr><td class="data"> Connection Pool Timeout (mins)</td>
			<td class="data">
				<select name="POOL_TIMEOUT">
					<%
					int timeout=ds.getPoolTimeout();
					%>
					<option value="1" <%=(timeout==1?"selected":"")%>>1</option>
					<option value="5" <%=(timeout==5?"selected":"")%>>5</option>
					<option value="10" <%=(timeout==10?"selected":"")%>>10</option>
					<option value="15" <%=(timeout==15?"selected":"")%>>15</option>
					<option value="20" <%=(timeout==20?"selected":"")%>>20</option>
					<option value="30" <%=(timeout==30?"selected":"")%>>30</option>
					<option value="45" <%=(timeout==45?"selected":"")%>>45</option>
					<option value="60" <%=(timeout==60?"selected":"")%>>60</option>		
				</select>
				<%
				msg = "Set how long an idle connection should stay in the pool before being closed.\\nART checks connections every Timeout(mins) and closes the ones that have been idle for more than this value.";
				%>
				<input type="button" class="buttonup" onClick="alert('<%=msg%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);" value="?">
			</td>
		</tr>

		<tr><td class="data"> Connection Test SQL</td>
			<td class="data">
				<input type="text" name="TEST_SQL" value="<%=ds.getTestSql()%>" size="25" maxlength="60">
				<%
				msg = "Specify a short and fast SQL query to execute every Timeout(mins) to validate an active connection. If the validation fails the connection is removed from the pool. Leave blank to disable this test.\\n\\nExample:\\n Select 1 from dual - Oracle\\n Select 1 - MySQL, SQL Server, PostgreSQL, CUBRID\\n Select 1 from information_schema.system_users - HSQLDB";
				%>
				<input type="button" class="buttonup" onClick="alert('<%=msg%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);" value="?">
			</td>
		</tr>

		<tr>
			<td class="data" colspan="2"> <input type="submit" value="Submit"> </td>
		</tr>
    </table> 

	<%
	if(modify){
		%>
		<p align="center">Leave the password field blank to mantain the previous password</p>
	<%
	}
	%>
	
</form>
        
<p>&nbsp;</p>
    <div class="notes">
        <b>Notes:</b>
<table class="notes">
    <tr>
        <td>Some JDBC Drivers and URLs
            <ul>
                <li><b>CUBRID</b><br>
Driver Name: cubrid.jdbc.driver.CUBRIDDriver<br>
JDBC URL: jdbc:cubrid:&lt;server_name&gt;:&lt;port&gt;:&lt;database_name&gt;:&lt;username&gt;:&lt;password&gt;  <i>(default port is 30000)</i>
            </li>
            
            <br>
                <li><b>Oracle</b><br>
Driver Name: oracle.jdbc.driver.OracleDriver<br>
JDBC URL: jdbc:oracle:thin:@&lt;server_name&gt;:&lt;port&gt;:&lt;sid&gt;   <i>(default port is 1521)</i>
            </li>
                  
            <br>
                <li><b>MySQL</b><br>
Driver Name: com.mysql.jdbc.Driver<br>
JDBC URL: jdbc:mysql://&lt;server_name&gt;/&lt;database_name&gt;
            </li>
            
            <br>
            <li><b>PostgreSQL</b><br>
Driver Name: org.postgresql.Driver<br>
JDBC URL: jdbc:postgresql://&lt;host&gt;/&lt;database_name&gt;
            </li>
            
            <br>            
            <li><b>HSQLDB</b><br>
Driver Name: org.hsqldb.jdbcDriver<br>
JDBC URL: jdbc:hsqldb:&lt;file_path&gt;
            </li>
            
            <br>
            <li><b>SQL Server (using Microsoft's JDBC driver)</b><br>
Driver Name: com.microsoft.sqlserver.jdbc.SQLServerDriver<br>
JDBC URL: jdbc:sqlserver://&lt;server_name&gt;;databaseName=&lt;db&gt;[;instanceName=&lt;inst&gt;]
            </li>
            
            </ul>
        </td>
    </tr>
</table>
    </div>


<%@ include file="footer.html" %>