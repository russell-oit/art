<%@ page import="java.sql.*,java.util.*,art.utils.*,art.servlets.ArtDBCP" %>
<%@ page import="org.quartz.*,org.quartz.impl.*" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%  request.setCharacterEncoding("UTF-8"); %>
<%

  String isValidAdminSession = (String) session.getAttribute("AdminSession");

  if ( isValidAdminSession == null || !isValidAdminSession.equals("Y")) {
    %>
       <jsp:forward page="error.jsp">
		<jsp:param name="MOD" value="Authentication Error"/>
		<jsp:param name="ACT" value="Error on Update Properties"/>
		<jsp:param name="MSG" value="No Valid Admin Session"/>
		<jsp:param name="NUM" value="150"/>
       </jsp:forward>
    <%
  }

ServletContext ctx   = getServletConfig().getServletContext();
String baseDir = ctx.getRealPath("");
String sep = java.io.File.separator;
String propsFile = ctx.getRealPath("")+sep+"WEB-INF"+sep+"art.props";
String defaultArtUrl = "jdbc:hsqldb:file:"+baseDir+sep+"WEB-INF"+sep+"hsqldb"+sep+"ArtRepositoryDB;shutdown=true;hsqldb.write_delay=false";
String defaultArtDriver="org.hsqldb.jdbcDriver";
ArtProps ap = new ArtProps();

String username=request.getParameter("art_username");
String password=request.getParameter("art_password");
String url=request.getParameter("art_jdbc_url");
String driver=request.getParameter("art_jdbc_driver");
boolean useDefaultDatabase=false;
if(url!=null && url.toLowerCase().equals("default")){
	useDefaultDatabase=true;
	url=defaultArtUrl;
	driver=defaultArtDriver;
}

//for cubrid database, username and password are specified in the url and need to be blank for DriverManager.getConnection(url, username, password) to work
if(driver.equals("cubrid.jdbc.driver.CUBRIDDriver")){
    username="";
    password="";
}

String name, value;
/* used to properly set the new password in ART_DATABASES
 * see "if (useDefaultDatabase)"  below
 */
String artRepositoryEncryptedPassword="";

Enumeration names = request.getParameterNames();
while (names.hasMoreElements()) {
	name = (String) names.nextElement();
	value = request.getParameter(name);
	// if the property is a password, let's obfuscate it
	if (name.equals("art_password") ) {
		value = Encrypter.encrypt(value);

		artRepositoryEncryptedPassword= "o:" + value;
        //for cubrid database, username and password are specified in the url and need to be blank for DriverManager.getConnection(url, username, newPassword) to work
        if(driver.equals("cubrid.jdbc.driver.CUBRIDDriver")){
            value ="";
        }
	}
    if (name.equals("art_username") ) {		
        //for cubrid database, username and password are specified in the url and need to be blank for DriverManager.getConnection(url, username, newPassword) to work
        if(driver.equals("cubrid.jdbc.driver.CUBRIDDriver")){
            value ="";
        }
	}
	if (name.equals("smtp_password") && value.length()>0 ) {
		value = Encrypter.encrypt(value);
	}
	// if the property is the art_jdbc_url and value is default one, let's set it properly
	if (name.equals("art_jdbc_url") && useDefaultDatabase ) {
		value = defaultArtUrl;
	}
	if (name.equals("art_jdbc_driver") && useDefaultDatabase ) {
		value = defaultArtDriver;
	}
	ap.setProp(name, value);
}


 try {

    // try to get and properly close the old connection
    try {
       Connection connOld  = (Connection) session.getAttribute("SessionConn");
       if (connOld != null){
			connOld.close();
		}
    } catch (Exception exOnClose) {
       System.err.println("ART - Update Props: WARNING: Error closing old connection: " +exOnClose);
    }

	Class.forName(driver).newInstance();
    Connection c;
	if(useDefaultDatabase && !ArtDBCP.getArtPropsStatus()){
		c = DriverManager.getConnection(url, username, "ART");
	} else {
		c = DriverManager.getConnection(url, username, password);        
	}

    c.setAutoCommit(false);
    session.setAttribute("SessionConn",c);

    /* Connection to art repository is successfull.

       If this is a connection to default database just after the initial setup (art props are not defined yet),
       update "SampleDB" and "Art Repository"  datasources in Art Repository  to point to the correct files
       and update the repository password with the one provided by the user

    */
    if (useDefaultDatabase) {
		System.out.println("ART - execUpdProps.jsp: Updating Art demo...");
		String defaultDB_url = "jdbc:hsqldb:"+baseDir+sep+"WEB-INF"+sep+"hsqldb"+sep+"ArtRepositoryDB;shutdown=true;hsqldb.write_delay=false";
		String sampleDB_url  = "jdbc:hsqldb:"+baseDir+sep+"WEB-INF"+sep+"hsqldb"+sep+"SampleDB;shutdown=true;hsqldb.write_delay=false";
		Statement st = c.createStatement();
		int i = st.executeUpdate("UPDATE ART_DATABASES SET URL='"+defaultDB_url+"' , PASSWORD='"+artRepositoryEncryptedPassword+"' WHERE DATABASE_ID=2");
		i = i+ st.executeUpdate("UPDATE ART_DATABASES SET URL='"+sampleDB_url+"' WHERE DATABASE_ID=1");
		
		if(!ArtDBCP.getArtPropsStatus()){
			//allow changing of password on initial setup
			st.executeUpdate("ALTER USER ART SET PASSWORD \""+request.getParameter("art_password")+"\"");
		}
		st.close();
		c.commit();
    }


 } catch(Exception ex){
  System.err.println("ART - Update Props: " +ex);
  ex.printStackTrace();
    %>
       <jsp:forward page="error.jsp">
		<jsp:param name="MOD" value="Exec Update Properties"/>
		<jsp:param name="ACT" value="Error when creating new connection to ART database. Not able to connect to ART repository. Verify the connection parameters."/>
		<jsp:param name="MSG" value="<%=ex%>"/>
		<jsp:param name="NUM" value="150"/>
       </jsp:forward>
    <%
 }


 if(ap.store(propsFile)) {
    ArtDBCP.refreshConnections();

	//recreate scheduler in case repository has changed

	//get current scheduler instance
	org.quartz.Scheduler scheduler=ArtDBCP.getScheduler();
	if (scheduler!=null){
		scheduler.shutdown();
		scheduler=null;
	}

	//create new scheduler instance
		QuartzProperties qp=new QuartzProperties();
		Properties props=qp.GetProperties();

		//start quartz scheduler
		SchedulerFactory schedulerFactory = new StdSchedulerFactory(props);
		scheduler = schedulerFactory.getScheduler();

		boolean enableJobScheduling=true;

		if ("false".equals(ctx.getInitParameter("enableJobScheduling"))) {
			enableJobScheduling    = false;
		}

		if (enableJobScheduling){
			scheduler.start();
		}
		else {
			scheduler.standby();
		}

		//save scheduler, to make it accessible throughout the application
		ArtDBCP.setScheduler(scheduler);

		//migrate jobs to quartz, if any exist from pre-1.11 art versions
		try{
			Connection conn = DriverManager.getConnection(url, username, password);            
			ArtJob aj=new ArtJob();
			aj.migrateJobsToQuartz(conn,scheduler);
			conn.close();
		} catch (Exception e) {
			System.err.println("ART - Update Props: Error migrating jobs to quartz: " + e);
		}

		//use client side redirect instead of jsp:forward to avoid job being resubmitted if browser refresh is done immediately after saving the job
		response.sendRedirect("adminAccess.jsp");
		return;

 } else {
    String msg = "Not able to write to file: " + propsFile;
    %>
       <jsp:forward page="error.jsp">
		<jsp:param name="MOD" value="Exec Update Properties"/>
		<jsp:param name="ACT" value="Error storing ART properties"/>
		<jsp:param name="MSG" value="<%=msg%>"/>
		<jsp:param name="NUM" value="140"/>
       </jsp:forward>
    <%
 }


%>
