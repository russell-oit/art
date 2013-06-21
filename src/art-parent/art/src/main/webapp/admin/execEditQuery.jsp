<%@page import="org.apache.commons.lang.StringUtils"%>
<%@ page import="org.apache.commons.lang.math.NumberUtils"%>
<%@ page import="java.sql.*,art.utils.*,art.servlets.ArtDBCP, java.util.*, java.io.File" %>
<%@ page import="org.apache.commons.fileupload.*, org.apache.commons.fileupload.servlet.ServletFileUpload" %>
<%@ page import="org.apache.commons.fileupload.disk.DiskFileItemFactory, org.apache.commons.io.FilenameUtils" %>

<%@ page contentType="text/html; charset=UTF-8" %>
<%  request.setCharacterEncoding("UTF-8");%>


<%
	String fieldName;
	String fieldValue;

	boolean MODIFY = false;
	int queryId = -1;
	int groupId = -1;
	int databaseId = -1;
	int queryType = 0;
	String name = "";
	String shortDescription = "";
	String description = "";
	String contactPerson = "";
	String usesRules = "";
	String status = "";
	String xmlaUrl = "";
	String xmlaDatasource = "";
	String xmlaCatalog = "";
	String xmlaUsername = "";
	String xmlaPassword = "";
	String sql = "";
	String textSource = "";
	String xaxisLabel = "";
	String yaxisLabel = "";

	String graphWidth = "";
	String graphHeight = "";
	String graphBgColor = "";
	String showLegend = "";
	String showLabels = "";
	String showDataPoints = "";
	String showGraphData = "";
	String graphYMin = "";
	String graphYMax = "";
	String graphRotateAt = "0";
	String graphRemoveAt = "0";

	FileItem uploadItem = null;
	long uploadSize = 0;
	String fileName = "";
	String filePath = "";
	int MAX_UPLOAD_SIZE = 2000000; //approximately 2MB
	boolean fileUploaded = false;
	String templateFileName = ""; //allows typing of filename instead of re-uploading an existing template
	
	FileItem subreportUploadItem = null;
	long subreportUploadSize = 0;
	String subreportFileName = "";
	String subreportFilePath = "";
	boolean subreportFileUploaded = false;
    
    String showParameters="";
	int displayResultset=0;


	if (ServletFileUpload.isMultipartContent(request)) {
		ServletFileUpload servletFileUpload = new ServletFileUpload(new DiskFileItemFactory());
		List fileItemsList = servletFileUpload.parseRequest(request);

		Iterator it = fileItemsList.iterator();
		while (it.hasNext()) {
			FileItem fileItem = (FileItem) it.next();
			fieldName = fileItem.getFieldName();
			if (fileItem.isFormField()) {
				fieldValue = fileItem.getString("UTF-8");
				if (fieldName.equals("QUERYACTION")) {
					if (fieldValue.equals("MODIFY")) {
						MODIFY = true;
					} else {
						MODIFY = false;
					}
				} else if (fieldName.equals("QUERY_ID")) {
					queryId = Integer.parseInt(fieldValue);
				} else if (fieldName.equals("GROUP_ID")) {
					groupId = Integer.parseInt(fieldValue);
				} else if (fieldName.equals("DATABASE_ID")) {
					databaseId = Integer.parseInt(fieldValue);
				} else if (fieldName.equals("QUERY_TYPE")) {
					queryType = Integer.parseInt(fieldValue);
				} else if (fieldName.equals("NAME")) {
					name = fieldValue.replace('&', ' ');
				} else if (fieldName.equals("SHORT_DESCRIPTION")) {
					shortDescription = fieldValue;
				} else if (fieldName.equals("DESCRIPTION")) {
					description = fieldValue;
				} else if (fieldName.equals("CONTACT_PERSON")) {
					contactPerson = fieldValue;
				} else if (fieldName.equals("USES_RULES")) {
					usesRules = fieldValue;
				} else if (fieldName.equals("STATUS")) {
					status = fieldValue;
				} else if (fieldName.equals("xmla_url")) {
					xmlaUrl = fieldValue;
				} else if (fieldName.equals("xmla_datasource")) {
					xmlaDatasource = fieldValue;
				} else if (fieldName.equals("xmla_catalog")) {
					xmlaCatalog = fieldValue;
				} else if (fieldName.equals("SQL")) {
					sql = fieldValue;
				} else if (fieldName.equals("textSource")) {
					textSource = fieldValue;
				} else if (fieldName.equals("xaxisLabel")) {
					xaxisLabel = fieldValue;
				} else if (fieldName.equals("yaxisLabel")) {
					yaxisLabel = fieldValue;
				} else if (fieldName.equals("graph_width")) {
					graphWidth = fieldValue;
				} else if (fieldName.equals("graph_height")) {
					graphHeight = fieldValue;
				} else if (fieldName.equals("graph_bgcolor")) {
					graphBgColor = fieldValue;
				} else if (fieldName.equals("showLegend")) {
					showLegend = "showlegend";
				} else if (fieldName.equals("showLabels")) {
					showLabels = "showlabels";
				} else if (fieldName.equals("showDataPoints")) {
					showDataPoints = "showpoints";
				} else if (fieldName.equals("showGraphData")) {
					showGraphData = "showdata";
				} else if (fieldName.equals("graph_ymin")) {
					graphYMin = fieldValue;
				} else if (fieldName.equals("graph_ymax")) {
					graphYMax = fieldValue;
				} else if (fieldName.equals("template_filename")) {
					templateFileName = fieldValue;
				} else if (fieldName.equals("xmla_username")) {
					xmlaUsername = fieldValue;
				} else if (fieldName.equals("xmla_password")) {
					xmlaPassword = fieldValue;
				} else if (fieldName.equals("showParameters")) {
					showParameters = fieldValue;
				} else if (fieldName.equals("displayResultset")) {
					if(NumberUtils.isNumber(fieldValue)){
						displayResultset=Integer.parseInt(fieldValue);
					}
				} else if (fieldName.equals("graph_rotate_at")) {
					if(NumberUtils.isNumber(fieldValue)){
						graphRotateAt = fieldValue;
					}
				} else if (fieldName.equals("graph_remove_at")) {
					if(NumberUtils.isNumber(fieldValue)){
						graphRemoveAt = fieldValue;
					}
				} 
			} else {
				//file upload field
				if (fieldName.equals("template")) {
					uploadItem = fileItem;
					uploadSize = uploadItem.getSize();
					filePath = uploadItem.getName(); //may be only a file name or the full path of the file on the client machine (depending on the browser)
					fileName = FilenameUtils.getName(filePath);
				} else if (fieldName.equals("subreport")) {
					subreportUploadItem = fileItem;
					subreportUploadSize = subreportUploadItem.getSize();
					subreportFilePath = subreportUploadItem.getName(); 
					subreportFileName = FilenameUtils.getName(subreportFilePath);
				}
			}
		}

	} else {
%>
<jsp:forward page="error.jsp">
	<jsp:param name="MOD" value="Execute Update Query"/>
	<jsp:param name="ACT" value="Parsing field values"/>
	<jsp:param name="MSG" value="Multi part form expected"/>
	<jsp:param name="NUM" value="0"/>
</jsp:forward>
<%		}

	if (uploadSize > MAX_UPLOAD_SIZE || subreportUploadSize > MAX_UPLOAD_SIZE) {
%>
<jsp:forward page="error.jsp">
	<jsp:param name="MOD" value="Execute Update Query"/>
	<jsp:param name="ACT" value="Template size"/>
	<jsp:param name="MSG" value="Template file size greater than maximum allowed"/>
	<jsp:param name="NUM" value="0"/>
</jsp:forward>
<%		}
	
	//check upload file type
	List<String> validExtensions=new ArrayList<String>();
	validExtensions.add("xml");
	validExtensions.add("jrxml");
	validExtensions.add("xls");
	validExtensions.add("xlsx");
		
	String extension=FilenameUtils.getExtension(fileName).toLowerCase();
	String subreportExtension=FilenameUtils.getExtension(subreportFileName).toLowerCase();
		
	if ((fileName.length()>0 && !validExtensions.contains(extension))
	|| (subreportFileName.length()>0 && !validExtensions.contains(subreportExtension))) {
%>
<jsp:forward page="error.jsp">
	<jsp:param name="MOD" value="Execute Edit Query"/>
	<jsp:param name="ACT" value="Template type"/>
	<jsp:param name="MSG" value="Invalid template file type"/>
	<jsp:param name="NUM" value="0"/>
</jsp:forward>
<%		}

	Connection conn = (Connection) session.getAttribute("SessionConn");
	if (conn == null || conn.isClosed()) {
%>
<jsp:forward page="error.jsp">
	<jsp:param name="MOD" value="Execute Update Query"/>
	<jsp:param name="ACT" value="Get connection from session"/>
	<jsp:param name="MSG" value="Database connection not valid. Please log in again."/>
	<jsp:param name="NUM" value="100"/>
</jsp:forward>
<%		}


	ArtQuery aq = new ArtQuery();
	boolean queryExists = true;
	try {
		if (MODIFY) { // get existent
			queryExists = aq.create(conn, queryId);
		}
        
        //some query types don't use a datasource. explicitly set the datasource to 0 so that they don't prevent deletion of another datasource
        if(queryType==113 || queryType==120 || queryType==110 || queryType==111 || queryType==114){
            databaseId=0;
        }

		if (queryExists) {
			aq.setGroupId(groupId);
			aq.setDatabaseId(databaseId);
			aq.setQueryType(queryType);
			aq.setName(name);
			aq.setShortDescription(shortDescription);
			aq.setDescription(description);
			aq.setContactPerson(contactPerson);
			aq.setUsesRules(usesRules);
			aq.setStatus(status);
			aq.setXmlaUrl(xmlaUrl);
			aq.setXmlaDatasource(xmlaDatasource);
			aq.setXmlaCatalog(xmlaCatalog);
			aq.setXmlaUsername(xmlaUsername);
            aq.setShowParameters(showParameters);
			aq.setDisplayResultset(displayResultset);

			//encrypt xmla password
			if (!xmlaPassword.equals("")) {
				xmlaPassword = Encrypter.encrypt(xmlaPassword);
			}
			aq.setXmlaPassword(xmlaPassword);

			if (queryType < 0) {
				aq.setXaxisLabel(xaxisLabel);
				aq.setYaxisLabel(yaxisLabel);

				//build graph options string
				String graphSize = graphWidth + "x" + graphHeight;
				String graphRange = graphYMin + ":" + graphYMax;
				String graphOptions = graphSize + " " + graphRange + " " + graphBgColor
						+ " " + showLegend + " " + showLabels + " " + showDataPoints + " " + showGraphData
						+ " rotate_at:" + graphRotateAt + " remove_at:" + graphRemoveAt;
				aq.setGraphOptions(graphOptions);
			}

			if (queryType == 111) {
				aq.setText(textSource);
			} else {
				aq.setText(sql);
			}

			//upload template file if applicable
			File destinationFile;
			String destinationFileName;
			if (uploadSize > 0 && uploadSize < MAX_UPLOAD_SIZE) {
				destinationFileName = ArtDBCP.getTemplatesPath() + fileName;
				destinationFile = new File(destinationFileName);
				uploadItem.write(destinationFile);
				aq.setTemplate(fileName);
				fileUploaded = true;
			} else {
				//no upload. just set file name
				aq.setTemplate(templateFileName);
			}
			
			//upload subreport file if applicable
			File subreportDestinationFile;
			String subreportDestinationFileName;
			if (subreportUploadSize > 0 && subreportUploadSize < MAX_UPLOAD_SIZE) {
				subreportDestinationFileName = ArtDBCP.getTemplatesPath() + subreportFileName;
				subreportDestinationFile = new File(subreportDestinationFileName);
				subreportUploadItem.write(subreportDestinationFile);
				subreportFileUploaded = true;
			}

			//save
			if (MODIFY) {
				aq.update(conn);
			} else {
				aq.insert(conn);
				// Obtain the query Id in order to pass it to the next jsp
				queryId = aq.getQueryId();
			}

			conn.commit();

			//log template file upload
			if (fileUploaded) {
				String username = (String) session.getAttribute("username");
				String ip = request.getRemoteAddr();
				String msg = "size=" + uploadSize + ", file=" + fileName + ", path=" + filePath;				
				ArtDBCP.log(username, "upload", ip, queryId, 0, 0, msg);
			}
			//log subreport file upload
			if (subreportFileUploaded) {
				String username = (String) session.getAttribute("username");
				String ip = request.getRemoteAddr();
				String msg = "size=" + subreportUploadSize + ", file=" + subreportFileName + ", path=" + subreportFilePath;				
				ArtDBCP.log(username, "upload", ip, queryId, 0, 0, msg);
			}

%>
<jsp:forward page="manageQuery.jsp">
	<jsp:param name="QUERY_ID" value="<%= queryId%>"/>
	<jsp:param name="GROUP_ID" value="<%= groupId%>"/>
</jsp:forward>
<%
} else {
	// revert to page error
%>
<jsp:forward page="error.jsp">
	<jsp:param name="MOD" value="Execute Update Query"/>
	<jsp:param name="ACT" value="Modify Query"/>
	<jsp:param name="MSG" value="Record does not exist"/>
	<jsp:param name="NUM" value="130"/>
</jsp:forward>
<%	}
} catch (ArtException e) {
	System.err.println("execEditQuery.jsp ROLLBACK: ArtException:" + e);
	conn.rollback();
%>
<jsp:forward page="error.jsp">
	<jsp:param name="MOD" value="Execute Update Query"/>
	<jsp:param name="ACT" value="Modify Query Header and SQL"/>
	<jsp:param name="MSG" value="<%=e%>" />
	<jsp:param name="NUM" value="160"/>
</jsp:forward>
<%

} catch (Exception e) {
	// revert to page error
	System.err.println("execEditQuery.jsp ROLLBACK: Exception:" + e);
	conn.rollback();
%>
<jsp:forward page="error.jsp">
	<jsp:param name="MOD" value="Execute Update Query"/>
	<jsp:param name="ACT" value="Modify Query Header and SQL"/>
	<jsp:param name="MSG" value="<%=e%>"/>
	<jsp:param name="NUM" value="199"/>
</jsp:forward>
<%
	}


%>
