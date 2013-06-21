<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="java.sql.*,java.util.*,art.utils.*,art.servlets.ArtDBCP" %>
<%@ page import="org.quartz.*,org.quartz.impl.*,com.lowagie.text.FontFactory" %>
<%@ page import="javax.naming.InitialContext,javax.naming.NamingException,javax.sql.DataSource" %>

<%@ page contentType="text/html; charset=UTF-8" %>
<%  request.setCharacterEncoding("UTF-8"); %>
<%

  String isValidAdminSession = (String) session.getAttribute("AdminSession");

  if ( isValidAdminSession == null || !isValidAdminSession.equals("Y")) {
    %>
       <jsp:forward page="error.jsp">
		<jsp:param name="MOD" value="Authentication Error"/>
		<jsp:param name="ACT" value="Error on Update Settings"/>
		<jsp:param name="MSG" value="No Valid Admin Session"/>
		<jsp:param name="NUM" value="150"/>
       </jsp:forward>
    <%
  }

String baseDir = ArtDBCP.getAppPath();
String sep = java.io.File.separator;
String defaultArtUrl = "jdbc:hsqldb:file:"+baseDir+sep+"WEB-INF"+sep+"hsqldb"+sep+"ArtRepositoryDB;shutdown=true;create=false;hsqldb.write_delay=false";
String defaultArtDriver="org.hsqldb.jdbcDriver";
ArtSettings as = new ArtSettings();

String username=request.getParameter("art_username");
String password=request.getParameter("art_password");
String url=request.getParameter("art_jdbc_url");
String driver=request.getParameter("art_jdbc_driver");
boolean useDefaultDatabase=false;
if(StringUtils.equalsIgnoreCase(url,"default") || StringUtils.equalsIgnoreCase(url,"demo")){
	useDefaultDatabase=true;
	url=defaultArtUrl;
	driver=defaultArtDriver;
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
	}
    //encrypt smtp password
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
	
	//don't save utility fields (fields starting with _)
	if (!name.startsWith("_")){
		as.setSetting(name, value);
	}
}


 try {

    // try to get and properly close the old connection
    try {
       Connection connOld  = (Connection) session.getAttribute("SessionConn");
       if (connOld != null){
			connOld.close();
			connOld=null;
		}
    } catch (Exception e) {
       System.out.println("ART - execEditSettings.jsp: WARNING: Error closing old connection: " + e);
	   e.printStackTrace(System.out);
    }

	Connection conn = null;
	if(StringUtils.isNotBlank(driver)){
		Class.forName(driver).newInstance();
		if(useDefaultDatabase && !ArtDBCP.isArtSettingsLoaded()){
			conn = DriverManager.getConnection(url, username, "ART");
		} else {
			conn = DriverManager.getConnection(url, username, password);   
		}    
		conn.setAutoCommit(false);
	} else {
		//using jndi datasource
		conn = ArtDBCP.getJndiConnection(url);
		conn.setAutoCommit(false);
	}
    
    session.setAttribute("SessionConn",conn);

    /* Connection to art repository is successfull.

       If this is a connection to default database just after the initial setup (art props are not defined yet),
       update "SampleDB" and "Art Repository"  datasources in Art Repository  to point to the correct files
       and update the repository password with the one provided by the user

    */
    if (useDefaultDatabase) {
		System.out.println("ART - execEditSettings.jsp: Updating ART demo...");
		String defaultDB_url = "jdbc:hsqldb:file:"+baseDir+sep+"WEB-INF"+sep+"hsqldb"+sep+"ArtRepositoryDB;shutdown=true;create=false;hsqldb.write_delay=false";
		String sampleDB_url  = "jdbc:hsqldb:file:"+baseDir+sep+"WEB-INF"+sep+"hsqldb"+sep+"SampleDB;shutdown=true;create=false;hsqldb.write_delay=false";
		Statement st = conn.createStatement();
		st.executeUpdate("UPDATE ART_DATABASES SET URL='"+defaultDB_url+"' , PASSWORD='"+artRepositoryEncryptedPassword+"' WHERE DATABASE_ID=2");
		st.executeUpdate("UPDATE ART_DATABASES SET URL='"+sampleDB_url+"' WHERE DATABASE_ID=1");
		
		if(!ArtDBCP.isArtSettingsLoaded()){
			//allow changing of password on initial setup
			st.executeUpdate("ALTER USER ART SET PASSWORD \""+request.getParameter("art_password")+"\"");
		}
		st.close();
		st=null;
		conn.commit();
    }


 } catch(Exception ex){
  System.out.println("ART - execEditSettings.jsp: " +ex);
  ex.printStackTrace(System.out);
    %>
       <jsp:forward page="error.jsp">
		<jsp:param name="MOD" value="Exec Update Settings"/>
		<jsp:param name="ACT" value="Error when creating new connection to ART database. Not able to connect to ART repository. Verify the connection parameters."/>
		<jsp:param name="MSG" value="<%=ex%>"/>
		<jsp:param name="NUM" value="150"/>
       </jsp:forward>
    <%
 }

String settingsFilePath = ArtDBCP.getSettingsFilePath();
 if(as.save(settingsFilePath)) {
	 //refresh settings and connections
	 ArtDBCP.loadArtSettings();
    ArtDBCP.refreshConnections();

	//recreate scheduler in case repository has changed	or scheduling enabled has changed
	String oldUsername=request.getParameter("_old_art_username");
	String oldPassword=request.getParameter("_old_art_password");
	String oldUrl=request.getParameter("_old_art_jdbc_url");
	String oldDriver=request.getParameter("_old_art_jdbc_driver");
	boolean repositoryHasChanged=true;
	if(username.equals(oldUsername) && password.equals(oldPassword) && url.equals(oldUrl) && driver.equals(oldDriver)){
		repositoryHasChanged=false;
	}
	if(!ArtDBCP.isArtSettingsLoaded()){
		//first time repository is being defined. ensure scheduler is created/started
		repositoryHasChanged=true;
	}
	
	String schedulingEnabled=request.getParameter("scheduling_enabled");
	String oldSchedulingEnabled=request.getParameter("_old_scheduling_enabled");
	boolean schedulingEnabledHasChanged=false;
	if(!schedulingEnabled.equals(oldSchedulingEnabled)){
		schedulingEnabledHasChanged=true;
	}

	if(repositoryHasChanged || schedulingEnabledHasChanged){
		//get current scheduler instance
		org.quartz.Scheduler scheduler=ArtDBCP.getScheduler();
		if (scheduler!=null){
			scheduler.shutdown();
			scheduler=null;
		}

		//create new scheduler instance
		QuartzProperties qp=new QuartzProperties();
		Properties props=qp.getProperties();

		if(props!=null){
			//start quartz scheduler
			SchedulerFactory schedulerFactory = new StdSchedulerFactory(props);
			scheduler = schedulerFactory.getScheduler();

			if (ArtDBCP.isSchedulingEnabled()){
				scheduler.start();
			}
			else {
				scheduler.standby();
			}

			//save scheduler, to make it accessible throughout the application
			ArtDBCP.setScheduler(scheduler);

			//migrate jobs to quartz, if any exist that require migrating
			ArtJob aj=new ArtJob();
			aj.migrateJobsToQuartz();
		}
	}
	
	//register pdf font if not already registered
	ArtDBCP.registerPdfFonts();
				
	//use client side redirect instead of jsp:forward to avoid job being resubmitted if browser refresh is done immediately after saving the job
	response.sendRedirect("adminConsole.jsp");
	return;

 } else {
    String msg = "Not able to write to file: " + settingsFilePath;
    %>
       <jsp:forward page="error.jsp">
		<jsp:param name="MOD" value="Exec Update Settings"/>
		<jsp:param name="ACT" value="Error storing ART settings"/>
		<jsp:param name="MSG" value="<%=msg%>"/>
		<jsp:param name="NUM" value="140"/>
       </jsp:forward>
    <%
 }


%>
