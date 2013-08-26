<%@ page contentType="text/html; charset=UTF-8" %>
<%  request.setCharacterEncoding("UTF-8"); %>

<%	
 java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("art.i18n.ArtMessages",request.getLocale());
  art.utils.UserEntity ueHeader = (art.utils.UserEntity) session.getAttribute("ue");
 int accessLevelHeader=0;
 if(ueHeader!=null){
	accessLevelHeader=ueHeader.getAccessLevel(); 
 }
%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="expires" content="0">
        <meta http-equiv="pragma" content="no-cache">
        <meta http-equiv="cache-control" content="no-cache">
        <title>ART</title>
        <script type="text/javascript" src="<%= request.getContextPath() %>/js/prototype.js"></script>
        <script type="text/javascript" src="<%= request.getContextPath() %>/js/scriptaculous/scriptaculous.js"></script>
        <script type="text/javascript" src="<%= request.getContextPath() %>/js/ajaxtags.js"></script>
        <script type="text/javascript" src="<%= request.getContextPath() %>/js/art.js"></script>
        <script type="text/javascript" src="<%= request.getContextPath() %>/js/jquery-1.6.2.min.js"></script>
        <script type="text/javascript" src="<%= request.getContextPath() %>/js/jquery.form.js"></script>
	<script> var $jQuery = jQuery.noConflict();</script>

        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/css/art.css">
        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/css/ajaxtags-art.css" />
    </head>
    <body>
        <table style="width:100%" class="art zeroPadding">
            <tr>
                <td class="attr" align="left" width="50%">
                    &nbsp;<a href="<%= request.getContextPath() %>/admin/adminConsole.jsp" ><img src="<%= request.getContextPath() %>/images/admin.png" title="Admin Console" border="0" /></a>

                    <% if (session.getAttribute("username") != null) {%>

                    :: <a href="<%= request.getContextPath() %>/user/showGroups.jsp"><img src="<%= request.getContextPath() %>/images/back-home.png" title="<%=messages.getString("startLink")%>" border="0" /></a> 
					:: <a href="<%= request.getContextPath() %>/user/myJobs.jsp" ><img src="<%= request.getContextPath() %>/images/jobs.png" title="<%=messages.getString("myJobs")%>" border="0" /></a>
					:: <a href="<%= request.getContextPath() %>/user/sharedJobs.jsp"> <img src="<%= request.getContextPath() %>/images/shared-jobs.png" title="<%=messages.getString("sharedJobs")%>" border="0" /></a>
					:: <a href="<%= request.getContextPath() %>/user/jobArchives.jsp"> <img src="<%= request.getContextPath() %>/images/job-archives.png" title="<%=messages.getString("jobArchives")%>" border="0" /></a>
					
					<% if (accessLevelHeader == 100) {%>
					:: <a href="<%= request.getContextPath() %>/logs" ><img src="<%= request.getContextPath() %>/images/logs.png" title="<%=messages.getString("logsLink")%>" border="0" /></a>
					<% }
					}%>

                    :: <a href="<%= request.getContextPath() %>/logOff.jsp"> <img src="<%= request.getContextPath() %>/images/exit.png" title="<%=messages.getString("logOffLink")%>" border="0" /></a>
                    <img src="<%= request.getContextPath() %>/images/vertical_16px.gif">
                </td>
                <td class="attr" align="right" width="50%">

                    <span id="systemWorking" style="display: none">
                        <img src="<%= request.getContextPath() %>/images/spinner.gif">
                    </span>
                    <img src="<%= request.getContextPath() %>/images/vertical_16px.gif">

                    <% if (ueHeader != null) { %>
                    <%= ueHeader.getUsername()%>
                    :: Logged in at <%=java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT,request.getLocale()).format(ueHeader.getLoginDate())%>
                    <% } %>
                </td>
            </tr>
        </table>
				<br>

