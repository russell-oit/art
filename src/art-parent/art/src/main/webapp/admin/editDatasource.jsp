<%@ page import="art.utils.*,java.sql.*,java.util.Map,art.servlets.ArtDBCP" %>
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
	Map<Integer, ArtQuery> queries=ds.getLinkedQueries(datasourceId);
	if(queries.size()>0){
		out.println("<pre>Error: There are queries targeting the datasource you want to delete.");
		out.println("       Delete the following queries or change their datasources");
		out.println("       in order to be able to delete this datasource: ");
		out.println();
								
		for (Map.Entry<Integer, ArtQuery> entry : queries.entrySet()) {
			ArtQuery aq=entry.getValue();			
			out.println("Query ID: " + aq.getQueryId() + " , Name: " + aq.getName() + " , Group ID: " + aq.getGroupId());
		}
		out.println("</pre>");
		%>
		<%@ include file="/user/footer.jsp" %>
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

<script language="javascript" type="text/javascript">	
	function onTypeSelection() {		
		var dbType=document.getElementById("database_type").value;
		var driverElement=document.getElementById("DRIVER");
		var urlElement=document.getElementById("URL");		
		if(dbType == "oracle"){			
			driverElement.value="oracle.jdbc.OracleDriver";
			urlElement.value="jdbc:oracle:thin:@<server_name>:1521:<sid>";
		} else if(dbType == "mysql"){
			driverElement.value="com.mysql.jdbc.Driver";
			urlElement.value="jdbc:mysql://<server_name>/<database_name>";
		} else if(dbType == "postgresql"){
			driverElement.value="org.postgresql.Driver";
			urlElement.value="jdbc:postgresql://<server_name>/<database_name>";
		} else if(dbType == "hsqldb-standalone"){
			driverElement.value="org.hsqldb.jdbcDriver";
			urlElement.value="jdbc:hsqldb:file:<file_path>;shutdown=true;hsqldb.write_delay=false";
		} else if(dbType == "sqlserver-ms"){
			driverElement.value="com.microsoft.sqlserver.jdbc.SQLServerDriver";
			urlElement.value="jdbc:sqlserver://<server_name>;databaseName=<database_name>";
		} else if(dbType == "cubrid"){
			driverElement.value="cubrid.jdbc.driver.CUBRIDDriver";
			urlElement.value="jdbc:cubrid:<server_name>:33000:<database_name>";		
		} else if(dbType == "other"){
			driverElement.value="";
			urlElement.value="";
		} else if(dbType == "hsqldb-server"){
			driverElement.value="org.hsqldb.jdbcDriver";
			urlElement.value="jdbc:hsqldb:hsql://<server_name>:9001/<database_alias>";
		} else if(dbType == "sqlserver-jtds"){
			driverElement.value="net.sourceforge.jtds.jdbc.Driver";
			urlElement.value="jdbc:jtds:sqlserver://<server_name>/<database_name>";
		} else if(dbType == "log4jdbc"){
			driverElement.value="net.sf.log4jdbc.DriverSpy";
			urlElement.value="jdbc:log4" + urlElement.value;
		} else if(dbType == "jndi"){
			driverElement.value="";
			urlElement.value="";
		}
	}
</script>


<form name="editDatasource" method="post" action="execEditDatasource.jsp">    	
	<input type="hidden" name="ACTION" value="<%=action%>">
	
	<table align="center">		
		<tr>
			<td class="title" colspan="2">Define Datasource</td>
		</tr>
				
	    <tr>
			<td class="data"> ID </td> 
			<%			
			String inputType="hidden";			
			if(modify){				
				inputType="text";
			} 
			%>
			<td class="data"> 
				<input type="<%=inputType%>" name="DATASOURCE_ID" value="<%=ds.getDatasourceId()%>" size="25" readonly>
			</td>			
		</tr>

		<tr><td class="data"> Datasource Name </td>
			<td class="data">
				<input type="text" name="NAME" value="<%=ds.getName()%>" size="25" maxlength="25">
			</td>
		</tr>
		
		<tr><td class="data"> Database Type</td>
			<td class="data">
				<select name="database_type" id="database_type" size="1" onChange="javascript:onTypeSelection();">
					<option value="--">--</option>
					<option value="cubrid">CUBRID</option>
					<option value="oracle">Oracle</option>
					<option value="mysql">MySQL</option>
					<option value="postgresql">PostgreSQL</option>
					<option value="sqlserver-ms">SQL Server (Microsoft driver)</option>
					<option value="sqlserver-jtds">SQL Server (jTDS driver)</option>
					<option value="hsqldb-standalone">HSQLDB (Standalone mode)</option>
					<option value="hsqldb-server">HSQLDB (Server mode)</option>
					<option value="log4jdbc">SQL Logging</option>
					<option value="jndi">JNDI</option>
					<option value="other">Other</option>
				</select>
				<%msg = "Sets the jdbc driver and url fields with default values for the selected database type"; %>
				<input type="button" class="buttonup" onClick="alert('<%=msg%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="?">
			</td>
		</tr>
		
		<tr><td class="data"> JDBC Driver </td>
			<td class="data">
				<input type="text" name="DRIVER" id="DRIVER" value="<%=ds.getDriver()%>" size="50" maxlength="200"> 
				<%msg = "For a JNDI datasource, this must be blank"; %>
				<input type="button" class="buttonup" onClick="alert('<%=msg%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);" value="?">
			</td>
		</tr>
		
		<tr><td class="data"> JDBC URL </td>
			<td class="data">
				<input type="text" name="URL" id="URL" value="<%=ds.getUrl()%>" size="50" maxlength="2000">
				<%msg = "For a JNDI datasource, set this to the JNDI name e.g. jdbc/MyDatasource."
					+ "\\nYou can also use the full JNDI url e.g. java:comp/env/jdbc/MyDatasource"; %>
				<input type="button" class="buttonup" onClick="alert('<%=msg%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="?">
			</td>
		</tr>
		
		<tr><td class="data"> Username </td>
			<td class="data">
				<input type="text" name="USERNAME" value="<%=ds.getUsername()%>" size="25" maxlength="25">
			</td>
		</tr>
		
		<tr>
			<td class="data"> Password </td>			
			<td class="data">
				<%
				String password=ds.getPassword();
				if(password==null){
					password="";
				} else {
					if(password.startsWith("o:")){
						//password is encrypted. decrypt
						password=Encrypter.decrypt(password.substring(2));
					}					
				}
				%>	
				<input type="password" name="PASSWORD" value="<%=password%>" size="25" maxlength="40">
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
	
</form>

<%@ include file="/user/footer.jsp" %>