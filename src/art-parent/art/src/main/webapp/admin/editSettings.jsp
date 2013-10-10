<%@ page import="java.sql.*,java.io.File,art.utils.*,art.servlets.ArtConfig" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ include file ="headerAdmin.jsp" %>

<%
  String isValidAdminSession = (String) session.getAttribute("AdminSession");
  if ( isValidAdminSession == null || !isValidAdminSession.equals("Y")) {
    %>
       <jsp:forward page="error.jsp">
		<jsp:param name="MOD" value="Authentication Error"/>
		<jsp:param name="ACT" value="Update Settings"/>
		<jsp:param name="MSG" value="No valid admin session"/>
		<jsp:param name="NUM" value="150"/>
       </jsp:forward>
    <%

  }
  
  //NOTE: the html element names correspond to art setting values
  //if they are changed, code that uses those settings needs to be changed

  String  administrator_email , smtp_server, smtp_username, smtp_password,
          art_username, art_password, art_jdbc_url, art_jdbc_driver, art_testsql, art_pooltimeout, msg;
  String  ldap_auth_server, ldap_auth_method  ,mswin_auth_server, mswin_domains,
          jdbc_auth_driver ,jdbc_auth_url ,authentication_method, bottom_logo, css_skin,header_with_public_user,
	  page_size, rss_link;


	 //new properties for use of secure smtp
	 String secure_smtp;
	 String smtp_port;

	 //new properties for ldap authentication
	 String ldap_users_parent_dn; //the parent DN of the user entries
	 String ldap_realm; //optional realm for digest-md5 authentication

	 String mondrian_cache_expiry;
	 
	 //new properties to enhance display of non-ascii characters in pdf output
	 String pdf_font_directory; //directory that contains fonts to be used
	 String pdf_font_name; //name of font
	 String pdf_font_file; //full path to a font file e.g. .ttf that contains the font to be used
	 String pdf_font_encoding; //font encoding to be used
	 String pdf_font_embedded; //whether font should be embedded in the generated pdf or not. embedding results in much larger files
	 
	 //new properties to enable custom date formats in query output
	 String date_format;
	 String time_format;
	 
	 String max_running_queries; //max number of running queries
	 String max_pool_connections;
	 String scheduling_enabled;
	 String view_modes; //user view modes
	 String default_max_rows;
	 String specific_max_rows;
	 String show_results_inline;
	 String null_value_enabled;


  if (ArtConfig.loadArtSettings()){
	  //settings defined
    art_username = ArtConfig.getArtSetting("art_username");
    art_password = ArtConfig.getArtSetting("art_password");
	// un-obfuscate password
	art_password = Encrypter.decrypt(art_password);
	art_jdbc_url                = ArtConfig.getArtSetting("art_jdbc_url");
    art_jdbc_driver        = ArtConfig.getArtSetting("art_jdbc_driver");
	art_testsql            = ArtConfig.getArtSetting("art_testsql");
	art_pooltimeout        = ArtConfig.getArtSetting("art_pooltimeout");
    administrator_email          = ArtConfig.getArtSetting("administrator_email");
    smtp_server            = ArtConfig.getArtSetting("smtp_server");
    smtp_username          = ArtConfig.getArtSetting("smtp_username");
    smtp_password          = ArtConfig.getArtSetting("smtp_password");
	smtp_password = Encrypter.decrypt(smtp_password);

    ldap_auth_server       = ArtConfig.getArtSetting("ldap_auth_server");
    ldap_auth_method       = ArtConfig.getArtSetting("ldap_auth_method");
    mswin_auth_server      = ArtConfig.getArtSetting("mswin_auth_server");
    mswin_domains          = ArtConfig.getArtSetting("mswin_domains");
    jdbc_auth_driver       = ArtConfig.getArtSetting("jdbc_auth_driver");
    jdbc_auth_url          = ArtConfig.getArtSetting("jdbc_auth_url");
    authentication_method     = ArtConfig.getArtSetting("authentication_method");

    bottom_logo            = ArtConfig.getArtSetting("bottom_logo");
    css_skin               = ArtConfig.getArtSetting("css_skin");
    
    //enable smooth upgrade from 2.1 to 2.2+
    if(StringUtils.equals(css_skin,"/art/css/art.css")){
        css_skin="/css/art.css";
    }
    if(StringUtils.equals(bottom_logo,"/art/images/artminiicon.png")){
        bottom_logo="/images/artminiicon.png";
    }
    
    header_with_public_user =  ArtConfig.getArtSetting("header_with_public_user");
    page_size = ArtConfig.getArtSetting("page_size");

	rss_link = ArtConfig.getArtSetting("rss_link");

	//new properties for use of secure smtp
	if (ArtConfig.getArtSetting("secure_smtp")==null){
		secure_smtp="no";
	}
	else {
		secure_smtp=ArtConfig.getArtSetting("secure_smtp");
	}
	if (ArtConfig.getArtSetting("smtp_port")==null){
		smtp_port="25";
	}
	else {
		smtp_port=ArtConfig.getArtSetting("smtp_port");
	}

	//new properties for ldap authentication
	if (ArtConfig.getArtSetting("ldap_users_parent_dn")==null){
		ldap_users_parent_dn="";
	}
	else {
		ldap_users_parent_dn=ArtConfig.getArtSetting("ldap_users_parent_dn");
	}
	if (ArtConfig.getArtSetting("ldap_realm")==null){
		ldap_realm="";
	}
	else {
		ldap_realm=ArtConfig.getArtSetting("ldap_realm");
	}

	if (ArtConfig.getArtSetting("mondrian_cache_expiry")==null){
		mondrian_cache_expiry="0";
	}
	else {
		mondrian_cache_expiry=ArtConfig.getArtSetting("mondrian_cache_expiry");
	}
	
	//new properties for pdf unicode support
	pdf_font_directory=ArtConfig.getArtSetting("pdf_font_directory");
	if(pdf_font_directory==null){
		pdf_font_directory="";
	}
	pdf_font_name=ArtConfig.getArtSetting("pdf_font_name");
	if(pdf_font_name==null){
		pdf_font_name="";
	}
	pdf_font_file=ArtConfig.getArtSetting("pdf_font_file");
	if(pdf_font_file==null){
		pdf_font_file="";
	}
	pdf_font_encoding=ArtConfig.getArtSetting("pdf_font_encoding");
	if(pdf_font_encoding==null){
		pdf_font_encoding="";
	}
	pdf_font_embedded=ArtConfig.getArtSetting("pdf_font_embedded");
	if(pdf_font_embedded==null){
		pdf_font_embedded="no";
	}
	
	//new properties for custom date formats
	date_format=ArtConfig.getArtSetting("date_format");
	if(date_format==null){
		date_format="dd-MMM-yyyy";
	}
	time_format=ArtConfig.getArtSetting("time_format");
	if(time_format==null){
		time_format="HH:mm:ss";
	}
	
	//other properties
	max_running_queries=ArtConfig.getArtSetting("max_running_queries");
	if(max_running_queries==null){
		max_running_queries="1000";
	}
	max_pool_connections=ArtConfig.getArtSetting("max_pool_connections");
	if(max_pool_connections==null){
		max_pool_connections="20";
	}
	scheduling_enabled=ArtConfig.getArtSetting("scheduling_enabled");
	if(scheduling_enabled==null){
		scheduling_enabled="true";
	}
	view_modes=ArtConfig.getArtSetting("view_modes");
	if(StringUtils.isBlank(view_modes)){
		view_modes="htmlDataTable,htmlGrid,xls,xlsx,pdf,htmlPlain,html,xlsZip,slk,slkZip,tsv,tsvZip";
	}
	default_max_rows=ArtConfig.getArtSetting("default_max_rows");
	if(StringUtils.isBlank(default_max_rows)){
		default_max_rows="10000";
	}
	specific_max_rows=ArtConfig.getArtSetting("specific_max_rows");
	if(specific_max_rows==null){
		specific_max_rows="xlsx:100000,slk:60000,slkZip:100000,tsv:60000,tsvZip:100000,tsvGz:100000";
	}
	show_results_inline=ArtConfig.getArtSetting("show_results_inline");
	if(StringUtils.isBlank(show_results_inline)){
		show_results_inline="yes";
	}
	null_value_enabled=ArtConfig.getArtSetting("null_value_enabled");
	if(null_value_enabled==null){
		null_value_enabled="no_numbers_as_blank";
	}

  } else {        
    art_username    = "ART";
    art_password    = "ART";
    art_jdbc_url         = "default";
    art_jdbc_driver = "org.hsqldb.jdbcDriver";
	art_testsql     = "";
	art_pooltimeout = "15";
    administrator_email   = "";
    smtp_server     = "";
    smtp_username   = "";
    smtp_password   = "";
    ldap_auth_server   = "";
    ldap_auth_method   = "simple";
    mswin_auth_server  ="";
    mswin_domains      ="Domain1,Domain2,Domain3";
    jdbc_auth_driver   = "";
    jdbc_auth_url      = "";
    authentication_method = "internal";

    bottom_logo	       = "/images/artminiicon.png";
    css_skin	       = "/css/art.css";

    header_with_public_user = "no";
    page_size               = "2"; // 1 A4, 2 A4 Landscape, 3 Letter, 4 Letter Landscape
	rss_link			= "http://art.sourceforge.net";

	//new properties for use of secure smtp
	secure_smtp="no";
	smtp_port="25";

	//new properties for ldap authentication
	ldap_users_parent_dn=""; //the parent DN of the user entries
	ldap_realm=""; //optional realm for digest-md5 authentication

	mondrian_cache_expiry="0";
	
	pdf_font_directory=""; 
	pdf_font_name=""; //default to blank so that no custom fonts are used
	pdf_font_file="";
	pdf_font_encoding="";
	pdf_font_embedded="no";

	date_format="dd-MMM-yyyy";
	time_format="HH:mm:ss";
	
	max_running_queries="1000";
	max_pool_connections="20";
	scheduling_enabled="true";
	view_modes="htmlDataTable,htmlGrid,xls,xlsx,pdf,htmlPlain,html,xlsZip,slk,slkZip,tsv,tsvZip";
	default_max_rows="10000";
	specific_max_rows="xlsx:100000,slk:60000,slkZip:100000,tsv:60000,tsvZip:100000,tsvGz:100000";
	
	show_results_inline="yes";
	null_value_enabled="no_numbers_as_blank";
	
    %>
	
    <table class="centerTableAuto">
		<tr>
			<td class="title"> Welcome! </td>
		</tr>
           <tr>
			   <td class="data"><span style="color:green">
	     This is the first time you are logging in.
		 You need to specify the settings below before being able to use ART.</span><br>
		 <span style="color:red"><small>
		 To use the embedded ART Repository and Demo, leave the default below for
		 ART Database Username/url/JDBC Driver but
		 set a new ART Database Password of your choice (default is "ART"). 
		 Once ART settings are defined, make sure to update
		 the password for the two art users in the repository
		 ("admin" and "auser", default passwords match the usernames).
		 Finally log off and log in using one of the two users.</small>
	   </span></td></tr>
    </table>
    <%
  }

%>

<script type="text/javascript">	
	function onTypeSelection() {		
		var dbType=document.getElementById("art_database_type").value;
		var driverElement=document.getElementById("art_jdbc_driver");
		var urlElement=document.getElementById("art_jdbc_url");		
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
		} else if(dbType == "demo"){
			driverElement.value="org.hsqldb.jdbcDriver";
			urlElement.value="demo";
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
		} else if(dbType == "db2"){
			driverElement.value="com.ibm.db2.jcc.DB2Driver";
			urlElement.value="jdbc:db2://<server_name>/<database_name>";
		} else if(dbType == "odbc"){
			driverElement.value="sun.jdbc.odbc.JdbcOdbcDriver";
			urlElement.value="jdbc:odbc:<dsn_name>";
		}
	}
</script>

<form action="execEditSettings.jsp" method="post">
 <table class="centerTable" style="width:60%">
  <tr><td class="title" colspan="2" >Settings </td></tr>
  <tr><td class="data" colspan="2" > Specify <i>ART Repository</i> connection parameters and other settings </td></tr>

   <tr><td class="attr"> ART Database Username</td>
       <td class="data">
		   <input type="hidden" name="_old_art_username" value="<%=art_username%>">
		<input type="text" name="art_username" size="20" maxlength="25" value="<%=art_username%>">
       </td>
   </tr>

   <tr><td class="attr"> ART Database Password</td>
       <td class="data">
		   <input type="hidden" name="_old_art_password" value="<%=art_password%>">
		<input type="password" name="art_password" size="20" maxlength="40" value="<%=art_password%>">
       </td>
   </tr>
   
   <tr><td class="attr"> Database Type</td>
       <td class="data">
		   <select name="_art_database_type" id="art_database_type" size="1" onChange="javascript:onTypeSelection();">
			   <option value="--" selected="selected">--</option>
			   <option value="demo">Default/Demo</option>
			   <option value="cubrid">CUBRID</option>
				<option value="oracle">Oracle</option>
				<option value="mysql">MySQL</option>
				<option value="postgresql">PostgreSQL</option>
				<option value="sqlserver-ms">SQL Server (Microsoft driver)</option>
				<option value="sqlserver-jtds">SQL Server (jTDS driver)</option>
				<option value="hsqldb-standalone">HSQLDB (Standalone mode)</option>
				<option value="hsqldb-server">HSQLDB (Server mode)</option>
				<option value="db2">DB2</option>
				<option value="odbc">Generic ODBC</option>
				<option value="jndi">JNDI</option>
				<option value="log4jdbc">SQL Logging</option>					
				<option value="other">Other</option>
		   </select>
		   <%msg = "Sets the jdbc driver and url fields with default values for the selected database type"; %>
		<input type="button" class="buttonup" onClick="alert('<%=msg%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="?">
       </td>
   </tr>
   
   <tr><td class="attr"> ART Database JDBC Driver</td>
       <td class="data">
		   <input type="hidden" name="_old_art_jdbc_driver" value="<%=art_jdbc_driver%>">
         <input type="text" name="art_jdbc_driver" id="art_jdbc_driver" size="60" maxlength="200" value="<%=art_jdbc_driver%>">
       </td>
   </tr>

   <tr><td class="attr"> ART Database JDBC URL</td>
       <td class="data">
		   <input type="hidden" name="_old_art_jdbc_url" value="<%=art_jdbc_url%>">
         <input type="text" name="art_jdbc_url" id="art_jdbc_url" size="60" maxlength="2000" value="<%=art_jdbc_url%>">
        <%
		String baseDir = ArtConfig.getAppPath();
		String sep = java.io.File.separator;
		String baseDirEscaped=baseDir.replaceAll("\\\\","\\\\\\\\"); //escape backslash for correct display in windows environments
		String sepEscaped=sep.replaceAll("\\\\","\\\\\\\\"); //escape backslash for correct display in windows environments
		msg = "Note: If you use the embedded HSQLDB database as the ART repository pay attention." +
		         " The database files are stored by default within the ART application and will be deleted when undeploying ART. "+
				 " To retain your repository, copy the\\n"+baseDirEscaped+sepEscaped+"WEB-INF"+sepEscaped+"hsqldb"+sepEscaped+"*\\n" +
				 " files to a secure place and update the JDBC URL to point to the new path."; %>
        <input type="button" class="buttonup" onClick="alert('<%=msg%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="?">
       </td>
   </tr>
   
   <tr><td class="attr"> Connection Pool Timeout (mins)</td>
	    <td class="data"><select name="art_pooltimeout">
	     <option value="1"  <%=(art_pooltimeout.equals("1")?"SELECTED":"")%> >1</option>
	     <option value="5"  <%=(art_pooltimeout.equals("5")?"SELECTED":"")%> >5</option>
	     <option value="10" <%=(art_pooltimeout.equals("10")?"SELECTED":"")%>>10</option>
	     <option value="15" <%=(art_pooltimeout.equals("15")?"SELECTED":"")%>>15</option>
	     <option value="20" <%=(art_pooltimeout.equals("20")?"SELECTED":"")%>>20</option>
	     <option value="30" <%=(art_pooltimeout.equals("30")?"SELECTED":"")%>>30</option>
	     <option value="45" <%=(art_pooltimeout.equals("45")?"SELECTED":"")%>>45</option>
	     <option value="60" <%=(art_pooltimeout.equals("60")?"SELECTED":"")%>>60</option>
	    </select>
        <%msg = "Set how long an idle connection should stay in the pool before being closed.\\nART checks connections every Timeout(mins) and closes the ones that have been idle for more than this value."; %>
        <input type="button" class="buttonup" onClick="alert('<%=msg%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="?">
        </td>
	</tr>
   <tr><td class="attr"> Connection Test SQL</td>
       <td class="data">
         <input type="text" name="art_testsql" size="30" maxlength="60" value="<%=art_testsql%>">
		 <%
         msg = "Specify a short and fast SQL query to execute every Timeout(mins) to validate an active connection. If the validation fails the connection is removed from the pool. Leave blank to disable this test.\\n\\nExample:\\n Select 1 from dual - Oracle\\n Select 1 - MySQL, SQL Server, PostgreSQL, CUBRID\\n Select 1 from information_schema.system_users - HSQLDB";
         %>
        <input type="button" class="buttonup" onClick="alert('<%=msg%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="?">
       </td>
   </tr>
    
   <tr><td class="attr"> ART Administrator Email</td>
       <td class="data">
         <input type="text" name="administrator_email" size="60" maxlength="120" value="<%=administrator_email%>">
		 <%msg = "Appears in the ART Administrator link in the footer of ART pages"; %>
		<input type="button" class="buttonup" onClick="alert('<%=msg%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="?">
       </td>
   </tr>

   <tr><td class="attr"> SMTP Server</td>
       <td class="data">
         <input type="text" name="smtp_server" size="60" maxlength="120" value="<%=smtp_server%>">
       </td>
   </tr>
   <tr>
    <td colspan="2" align="center" class="data">

    <a href="javascript:showHide(document.getElementById('optional_div'));">Optional Settings</a>
	
     <div id="optional_div" class="collapse">
      <table>
		   
	    <tr>
			   <td colspan="2" class="data2">SMTP</td>
		   </tr>
		   
       <tr>
        <td class="attr">SMTP Username</td>
        <td class="data"><input type="text" name="smtp_username" size="50" maxlength="120" value="<%=smtp_username%>">
			<%
         msg = "Leave empty if your SMTP server does not require authentication";
         %>
        <input type="button" class="buttonup" onClick="alert('<%=msg%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="?">
		</td>
       </tr>
       <tr>
        <td class="attr">SMTP Password</td>
        <td class="data"><input type="password" name="smtp_password" size="50" maxlength="120" value="<%=smtp_password%>">
			<%
         msg = "Leave empty if your SMTP server does not require authentication";
         %>
        <input type="button" class="buttonup" onClick="alert('<%=msg%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="?">
		</td>
       </tr>

	   <tr>
        <td class="attr">Use Secure SMTP</td>
        <td class="data">
		<select name="secure_smtp">
	     <option value="no" <%= ("no".equals(secure_smtp)?"SELECTED":"") %> > No</option>
	     <option value="starttls" <%= ("starttls".equals(secure_smtp)?"SELECTED":"") %> > STARTTLS</option>
	   </select>
		</td>
       </tr>

	   <tr>
        <td class="attr">SMTP Port</td>
        <td class="data"><input type="text" name="smtp_port" size="6" maxlength="5" value="<%=smtp_port%>"></td>
       </tr>

       <tr>
			   <td colspan="2" class="data2">PDF</td>
		   </tr>
	   
       <tr>
        <td class="attr">PDF Document Page Size</td>
		<td class="data">
	   <select name="page_size">
	     <option value="1"   <%= (page_size.equals("1")?"SELECTED":"") %>  >A4 </option>
	     <option value="2"   <%= (page_size.equals("2")?"SELECTED":"") %>  >A4 Landscape </option>
	     <option value="3"   <%= (page_size.equals("3")?"SELECTED":"") %>  >Letter  </option>
	     <option value="4"   <%= (page_size.equals("4")?"SELECTED":"") %>  >Letter Landscape </option>
	   </select>
	</td>
       </tr>
	   
	   <tr>
        <td class="attr">PDF Font Name</td>
        <td class="data">			
			<input type="text" name="pdf_font_name" size="50" maxlength="200" value="<%=pdf_font_name%>">
		<%
		msg = "Name of custom font to be used for pdf output. Leave blank to use the default font." +
				"\\n\\nExample:\\nArial Unicode MS";
		%>
			<input type="button" class="buttonup" onClick="alert('<%=msg%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="?">
			</td>
       </tr>
	   
	   <tr>
        <td class="attr">PDF Font File</td>
        <td class="data">			
			<input type="text" name="pdf_font_file" size="50" maxlength="200" value="<%=pdf_font_file%>">
		<%
		msg = "File that contains custom font to be used for pdf output" +
				"\\n\\nExample:\\nc:"+sepEscaped+"windows"+sepEscaped+"fonts"+sepEscaped+"arialuni.ttf";
		%>
			<input type="button" class="buttonup" onClick="alert('<%=msg%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="?">
			</td>
       </tr>
	   	   	   	    	   
	   <tr>
        <td class="attr">PDF Font Directory</td>
        <td class="data">			
			<input type="text" name="pdf_font_directory" size="50" maxlength="200" value="<%=pdf_font_directory%>">
			<%
			msg = "Directory that contains custom fonts to be used for pdf output" +
					"\\n\\nExample:\\nc:"+sepEscaped+"windows"+sepEscaped+"fonts";
			%>
			<input type="button" class="buttonup" onClick="alert('<%=msg%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="?">
			</td>
       </tr>
	   
	    <tr>
        <td class="attr">PDF Font Encoding</td>
        <td class="data">
			<input type="text" name="pdf_font_encoding" size="50" maxlength="200" value="<%=pdf_font_encoding%>">
			<%
			msg = "Encoding for custom font to be used for pdf output" +
					"\\n\\nExamples:\\nCp1252 (Western European)\\nCp1250 (Central and Eastern European)" +
					"\\nCp1251 (Russian)\\nCp1253 (Greek)\\nIdentity-H (Full Unicode)" +
					"\\n\\nEncodings are case sensitive";
			%>
			<input type="button" class="buttonup" onClick="alert('<%=msg%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="?">
		</td>
       </tr>
	   
	    <tr>
        <td class="attr">PDF Font Embedded</td>
        <td class="data">
		<select name="pdf_font_embedded">
	     <option value="no" <%= ("no".equals(pdf_font_embedded)?"SELECTED":"") %> > No</option>
	     <option value="yes" <%= ("yes".equals(pdf_font_embedded)?"SELECTED":"") %> > Yes</option>
	   </select>
	   <%msg = "Whether to embed custom font in the pdf output. Embedding the font results in much larger pdf files."; %>
			<input type="button" class="buttonup" onClick="alert('<%=msg%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="?">
		</td>
       </tr>
	   
	   <tr>
			   <td colspan="2" class="data2">Max Rows</td>
		   </tr>
		   
       <tr>
        <td class="attr">Default Max Rows</td>
        <td class="data"><input type="text" name="default_max_rows" size="6" maxlength="8" value="<%=default_max_rows%>">
			<%
         msg = "Set the default maximum number of rows to output for a query";
         %>
        <input type="button" class="buttonup" onClick="alert('<%=msg%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="?">
		</td>
       </tr>
       <tr>
        <td class="attr">Specific Max Rows</td>
        <td class="data"><input type="text" name="specific_max_rows" size="50" maxlength="1000" value="<%=specific_max_rows%>">
			<%
         msg = "Set the maximum number of rows to output for specific view modes."
				 + "\\nComma separated list of settings in the format viewmode:value";
         %>
        <input type="button" class="buttonup" onClick="alert('<%=msg%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="?">
		</td>
       </tr>
	   
	   <tr>
			   <td colspan="2" class="data2">General</td>
		   </tr>
		   
		   <tr>
        <td class="attr">ART CSS (skin)</td>
        <td class="data"><input type="text" name="css_skin" size="50" maxlength="120" value="<%=css_skin%>"></td>
       </tr>
       <tr>
        <td class="attr">Page Footer Logo</td>
        <td class="data"><input type="text" name="bottom_logo" size="50" maxlength="120" value="<%=bottom_logo%>"></td>
       </tr>
	   
	   <tr>
        <td colspan="2" class="attr">Show standard header and footer in public_user sessions
	   <select name="header_with_public_user">
	     <option value="no"   <%= (header_with_public_user.equals("no")?"SELECTED":"") %>  >No </option>
	     <option value="yes"  <%= (header_with_public_user.equals("yes")?"SELECTED":"") %> >Yes</option>
	   </select>
	</td>
       </tr>
	   
	   <tr>
        <td class="attr">RSS Link</td>
        <td class="data"><input type="text" name="rss_link" size="50" maxlength="120" value="<%=rss_link%>">
			<%
			msg ="If you plan to use ART to generate RSS feeds you should specify the proper RSS link URL, i.e. the URL to the website corresponding to the channel.";
			%>
			<input type="button" class="buttonup" onClick="alert('<%=msg%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="?">
		</td>
       </tr>

	   <tr>
        <td class="attr">Mondrian Cache Expiry (hours)</td>
        <td class="data"><input type="text" name="mondrian_cache_expiry" size="6" maxlength="5" value="<%=mondrian_cache_expiry%>">
			<%msg = "The number of hours after which the mondrian cache will be automatically cleared. Set to 0 to disable automatic clearing."; %>
			<input type="button" class="buttonup" onClick="alert('<%=msg%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="?">
		</td>
       </tr>
	   
	    <tr>
        <td class="attr">Date Format</td>
        <td class="data">
			<input type="text" name="date_format" size="50" maxlength="30" value="<%=date_format%>">
			<%
			msg = "In query output, the format of the date portion of date fields" +
					"\\n\\nExamples: \\ndd-MMM-yyyy (02-Aug-2012)\\ndd/MM/yyyy (02/08/2012)" +
					"\\n\\nFormat strings are case sensitive";
			%>
			<input type="button" class="buttonup" onClick="alert('<%=msg%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="?">
		</td>
       </tr>
	   
	   <tr>
        <td class="attr">Time Format</td>
        <td class="data">
			<input type="text" name="time_format" size="50" maxlength="20" value="<%=time_format%>">
			<%
			msg = "In query output, the format of the time portion of date fields" +
					"\\n\\nExamples:\\nHH:mm:ss (21:59:59)\\nh:mm:ss.SSS a (9:59:59.786 PM)" +
					"\\n\\nFormat strings are case sensitive";
			%>
			<input type="button" class="buttonup" onClick="alert('<%=msg%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="?">
		</td>
       </tr>
	   
	    <tr>
        <td class="attr">Scheduling Enabled</td>
        <td class="data">
			<input type="hidden" name="_old_scheduling_enabled" value="<%=scheduling_enabled%>">
			<select name="scheduling_enabled">
	     <option value="yes"   <%= (scheduling_enabled.equals("yes")?"selected":"") %>  >Yes </option>
	     <option value="no"  <%= (scheduling_enabled.equals("no")?"selected":"") %> >No</option>
	   </select>
		</td>
       </tr>
	   
	   <tr>
        <td class="attr">Available View Modes</td>
        <td class="data">
			<input type="text" name="view_modes" size="50" maxlength="1000" value="<%=view_modes%>">
			<%
			msg = "Comma separated list of view modes (case sensitive) available to users when they run a query." +
					"\\nThe order will be respected in the list shown to users. First one is the default.";
			%>
			<input type="button" class="buttonup" onClick="alert('<%=msg%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="?">
		</td>
       </tr>
	   
	   <tr>
        <td class="attr">Maximum Running Queries</td>
        <td class="data"><input type="text" name="max_running_queries" size="6" maxlength="5" value="<%=max_running_queries%>">
			<%
			msg ="Set the maximum number of concurrently running queries";
			%>
			<input type="button" class="buttonup" onClick="alert('<%=msg%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="?">
		</td>
       </tr>
	   
	   <tr>
        <td class="attr">Maximum Pool Connections</td>
        <td class="data"><input type="text" name="max_pool_connections" size="6" maxlength="3" value="<%=max_pool_connections%>">
			<%
			msg ="Set the maximum number of connections a connection pool can open to the same datasource."
					+ " Further requests are queued.";
			%>
			<input type="button" class="buttonup" onClick="alert('<%=msg%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="?">
		</td>
       </tr>
	   
	   <tr>
        <td class="attr">Show Results Inline</td>
        <td class="data">
		<select name="show_results_inline">
	     <option value="yes" <%= ("yes".equals(show_results_inline)?"SELECTED":"") %> > Yes</option>
	     <option value="no" <%= ("no".equals(show_results_inline)?"SELECTED":"") %> > No</option>
	   </select>
	   <%msg = "Set to Yes to show query results below the parameter selection box" +
			   " or No to show query results in a new page.";
	   %>
			<input type="button" class="buttonup" onClick="alert('<%=msg%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="?">
		</td>
       </tr>
	   
	   <tr>
        <td class="attr">Display Null Value</td>
        <td class="data">
			<select name="null_value_enabled">
				<option value="yes"   <%= (null_value_enabled.equals("yes")?"selected":"") %>  >Yes </option>
				<option value="no_numbers_as_blank"  <%= (null_value_enabled.equals("no_numbers_as_blank")?"selected":"") %> >No (Numbers as blank)</option>
				<option value="no_numbers_as_zero"  <%= (null_value_enabled.equals("no_numbers_as_zero")?"selected":"") %> >No (Numbers as zero)</option>
			</select>
			<%msg = "When set to Yes, null is displayed for report fields that are null. When set to No, a blank space is displayed instead of null."; %>
			<input type="button" class="buttonup" onClick="alert('<%=msg%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="?">
		</td>
       </tr>
	   
       

   </table>
	<br><br>
		<div style="text-align:center"><b>External Authentication</b></div>
      <p align="justify">
       Configure the fields below if you would like to use an external authentication source to authenticate ART users.<br><br>

		<b>Note:</b> If you are unable to log in while using an external authentication source e.g. due
		to a configuration problem, you can always log in using the <i>Internal Login</i> link provided on the
		login page for the given external authentication method (the link points to the url <i>http://server:port/art/<b>login.jsp</b></i>). 
        If you don't have any internal users defined,
		or are otherwise unable to log in using an internal user, you can always log in using the ART database username and password.
      </p>
      <table>

       <tr>
        <td class="attr" rowspan="4">LDAP
	 
	</td>
        <td class="data"> Server: </td>
	 <td class="data"> <input type="text" name="ldap_auth_server" size="50" maxlength="120" value="<%=ldap_auth_server%>">
	 </td>
		 </tr>
		 <tr>
		 <td class="data">Authentication:</td>
	 <td class="data">
		<select name="ldap_auth_method">
	     <option value="simple" <%= ("simple".equals(ldap_auth_method)?"SELECTED":"") %> > Simple</option>
	     <option value="digestmd5" <%= ("digestmd5".equals(ldap_auth_method)?"SELECTED":"") %> > Digest-MD5</option>
			<option value="ad-simple" <%= ("ad-simple".equals(ldap_auth_method)?"SELECTED":"") %> > Active Directory: Simple</option>
	     <option value="ad-digestmd5" <%= ("ad-digestmd5".equals(ldap_auth_method)?"SELECTED":"") %> > Active Directory: Digest-MD5</option>
	   </select>
		</td>
	</tr>
	<tr>
	 <td class="data">Users Parent DN:</td>
	 <td class="data"> <input type="text" name="ldap_users_parent_dn" size="50" maxlength="400" value="<%=ldap_users_parent_dn%>">
	 <%msg = "The parent DN of the user entries in the LDAP directory. Not required for Active Directory authentication."; %>
        <input type="button" class="buttonup" onClick="alert('<%=msg%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="?">
	 </td>
       </tr>
	  <tr>
	 <td class="data">Realm:</td>
	 <td class="data"> <input type="text" name="ldap_realm" size="50" maxlength="400" value="<%=ldap_realm%>">
	 <%msg = "The LDAP realm when using a Digest-MD5 authentication method. If blank, the default realm will be used."; %>
        <input type="button" class="buttonup" onClick="alert('<%=msg%>')" onMouseOver="javascript:btndn(this);" onMouseOut="javascript:btnup(this);"  value="?">
	 </td>
       </tr>

	<tr>
        <td class="attr" rowspan="2">Windows Domain
	 
	</td>
        <td class="data">Domain Controller:</td>
	 <td class="data"><input type="text" name="mswin_auth_server" size="50" maxlength="120" value="<%=mswin_auth_server%>">	</td>
	 </tr>
	 <tr>
	 <td class="data">Allowed Domains:</td>
	 <td class="data"> <input type="text" name="mswin_domains" size="50" maxlength="400" value="<%=mswin_domains%>"> </td>
       </tr>

	   <tr>
        <td  class="attr" rowspan="2">Database
	 
	</td>
        <td class="data">JDBC Driver:</td>
	  <td class="data"><input type="text" name="jdbc_auth_driver" size="50" maxlength="120" value="<%=jdbc_auth_driver%>"> </td>
	 </tr>
	 <tr>
	 <td class="data">JDBC URL:</td>
	  <td class="data"><input type="text" name="jdbc_auth_url" size="50" maxlength="120" value="<%=jdbc_auth_url%>"> </td>
       </tr>

<tr>
        <td colspan="3" class="attr">Authentication Method:
	   <select name="authentication_method">
	     <option value="internal"     <%= (authentication_method.equals("internal")?"SELECTED":"") %> >Internal</option>
	     <option value="ldap" <%= (authentication_method.equals("ldap")?"SELECTED":"") %>>LDAP</option>
	     <option value="windowsDomain"   <%= (authentication_method.equals("windowsDomain")?"SELECTED":"") %>  >Windows Domain</option>
	     <option value="database"   <%= (authentication_method.equals("database")?"SELECTED":"") %>  >Database</option>
	     <option value="auto" <%= (authentication_method.equals("auto")?"SELECTED":"") %>>Auto</option>
	   </select>
	</td>
</tr>
      </table>
     </div>

     <!-- - ---------------------------------------- - -->
    </td>
   </tr>

   <tr>
    <td colspan="2"><input type="submit" value="Submit"></td>
   </tr>

 </table>
 </form>

<%
ServletContext ctx   = getServletConfig().getServletContext();
%>

 <p>
  <table class="centerTableAuto">
   <tr><td colspan="2" class="title"> Application Server Properties </td></tr>
   <tr><td class="data">ART Home</td><td><code> <%= ctx.getRealPath("")%></code></td></tr>   
   <tr><td class="data">Server Info</td><td><code> <%=ctx.getServerInfo()%></code></td></tr>
   <tr><td class="data">Servlet API Supported</td><td><code> <%=ctx.getMajorVersion()%>.<%= ctx.getMinorVersion()%></code> </td></tr>
   <tr><td class="data">Java Vendor</td><td><code> <%=System.getProperty("java.vendor")%></code> </td></tr>
   <tr><td class="data">Java Version</td><td><code> <%=System.getProperty("java.version")%></code> </td></tr>
   <tr><td class="data">OS</td><td><code><%=System.getProperty("os.arch")%> / <%=System.getProperty("os.name")%> / <%=System.getProperty("os.version")%></code> </td></tr>
  </table>
 </p>

<%@ include file ="/user/footer.jsp" %>

