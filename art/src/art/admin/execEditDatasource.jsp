<%@ page import="art.utils.*,java.sql.*,art.servlets.ArtDBCP" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ include file ="headerAdmin.jsp" %>


<%
String action = request.getParameter("ACTION");
String password = request.getParameter("PASSWORD");
String driver=request.getParameter("DRIVER").trim();
String url=request.getParameter("URL").trim();
String username=request.getParameter("USERNAME").trim();

//test database connection
try{
	out.println("<p><table align=\"center\"><tr><td class=\"data\"><b>Testing Datasource</b><br>");

	if(StringUtils.isNotBlank(driver)){
		// Register the database driver for the database
		// that has just been specified (only if it is not the one already registered for the ART repository)
		if (!driver.equals(ArtDBCP.getArtSetting("art_jdbc_driver"))) {
			out.println("<br>Registering driver "+driver+" ...<br>");
			Class.forName(driver).newInstance();
			out.println("...OK<br>");
		}
		out.println("<br>Testing connection ...<br>");
		Connection testConn = DriverManager.getConnection(url, username, password);
		testConn.close();
		testConn=null;
		out.println("...OK<br>");
	} else {
		//jndi datasource
		out.println("<br>Testing connection ...<br>");
		Connection testConn = ArtDBCP.getJndiConnection(url);
		testConn.close();
		testConn=null;
		out.println("...OK<br>");
	}
}catch(SQLException ex){
	ex.printStackTrace();
	out.println("<p><b>Connection Exception:</b><br> The datasource parameters you specified are not correct " +
	"and the connection to the database has failed.<br>"+
	"Anyway, the datasource has been defined in ART - but you will probably "+
	"get an error when trying to execute queries that use it.");
	out.println("<br><br><b>Exception Description:</b> "+ex+"</p>");
}catch(ClassNotFoundException ex) {
	ex.printStackTrace();
	out.println("<p><b>Exception:</b><br> The database driver is not correct. "+
	"Maybe you mispelled it or it is not in the classpath.<br>"+
	"Anyway, the datasource has been defined in ART - but you will probably "+
	"get an error when trying to execute queries that use it.");
	out.println("<br><br><b>Exception Description:</b> "+ex+"</p>");
} catch(Exception ex) {
	ex.printStackTrace();
	out.println("<p><b>General Exception:</b><br> "+
	"Anyway, the datasource has been defined in ART - but you will probably "+
	"get an error when trying to execute queries that use it.");
	out.println("<br><br><b>Exception Description:</b> "+ex+"</p>");
} finally {
	out.println("</td></tr></table></p>");
}

//encrypt password
if (!password.equals(""))  {
	password = "o:" + Encrypter.encrypt(password);
}


Datasource ds=new Datasource();

ds.setDatasourceId(Integer.parseInt(request.getParameter("DATASOURCE_ID")));
ds.setName(request.getParameter("NAME").trim());
ds.setDriver(driver);
ds.setUrl(url);
ds.setUsername(username);
ds.setPassword(password);
ds.setPoolTimeout(Integer.parseInt(request.getParameter("POOL_TIMEOUT")));
ds.setTestSql(request.getParameter("TEST_SQL"));

if (action.equals("ADD")){
	ds.insert();
} else if (action.equals("MODIFY")){
	ds.update();
}

//Refresh the Art connection pools so that the new connection is ready to use (no manual refresh needed)
ArtDBCP.refreshConnections();

%>

<div style="text-align:center">
	<a href="manageDatasources.jsp">Finish</a>
</div>

<%@ include file ="/user/footer.jsp" %>


