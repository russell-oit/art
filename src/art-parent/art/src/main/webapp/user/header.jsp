<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%  request.setCharacterEncoding("UTF-8"); %>

<%	
//note that variables will be defined in files that include this one, so duplicate variable names may result
 java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("art.i18n.ArtMessages",request.getLocale());
 
 art.utils.UserEntity ueHeader = (art.utils.UserEntity) session.getAttribute("ue");
 int accessLevelHeader=ueHeader.getAccessLevel();
 String usernameHeader=ueHeader.getUsername();
%>


<% if (request.getParameter("_isFragment")==null) { %>
<!DOCTYPE html>
<html>
<head>
  <title>ART</title>
      
<c:if test="${empty param._mobile}">  
 <script type="text/javascript" src="<%= request.getContextPath() %>/js/prototype.js"></script>
 <script type="text/javascript" src="<%= request.getContextPath() %>/js/scriptaculous/scriptaculous.js"></script>
 
  <script type="text/javascript" src="<%= request.getContextPath() %>/js/jquery-1.6.2.min.js"></script>
  <script type="text/javascript" src="<%= request.getContextPath() %>/js/jquery.form.js"></script>
 
 <script type="text/javascript">
	jQuery.noConflict();
</script>
  
 <script type="text/javascript" src="<%= request.getContextPath() %>/js/ajaxtags.js"></script>
 <link rel="stylesheet" href="<%= request.getContextPath() %>/js/dhtmlgoodies_calendar/dhtmlgoodies_calendar.css" media="screen" />
 
  <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/jpivot/table/mdxtable.css" />
  <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/jpivot/navi/mdxnavi.css" />
  <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/wcf/form/xform.css" />
  <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/wcf/table/xtable.css" />
  <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/wcf/tree/xtree.css" />
  <script type="text/javascript" src="<%=request.getContextPath()%>/wcf/scroller.js"></script>
  
  <script type="text/javascript" src="<%= request.getContextPath() %>/js/sorttable.js"></script>
</c:if> 
  
  <script type="text/javascript" src="<%= request.getContextPath() %>/js/art.js"></script>  
  <link rel="stylesheet" type="text/css" href="<%=request.getContextPath() + art.servlets.ArtDBCP.getArtSetting("css_skin")%>" />      
  <link rel="stylesheet" type="text/css" href="<%=request.getContextPath() %>/css/ajaxtags-art.css" /> 
  
  <script type="text/javascript" src="<%= request.getContextPath() %>/js/overlib.js"></script>
       
</head>
<body>
 <c:if test="${empty param._mobile}">  
 <% if ( !("public_user".equals(usernameHeader) && "no".equals(art.servlets.ArtDBCP.getArtSetting("header_with_public_user")))) { %>
 <table class="art centerTable zeroPadding" style="width: 100%" >
    <tr>
     <td class="attr" align="left" width="50%">
      <% if (accessLevelHeader >= 10) {%>
       &nbsp;<a href="<%= request.getContextPath() %>/admin/adminConsole.jsp" ><img src="<%= request.getContextPath() %>/images/admin.png" title="Admin Console" border="0" /></a> ::
      <% } %>

       <a href="<%= request.getContextPath() %>/user/showGroups.jsp"><img src="<%= request.getContextPath() %>/images/back-home.png" title="<%=messages.getString("startLink")%>" border="0" /></a> 

      <% if (accessLevelHeader >= 5) {%>
      :: <a href="<%= request.getContextPath() %>/user/myJobs.jsp" ><img src="<%= request.getContextPath() %>/images/jobs.png" title="<%=messages.getString("myJobs")%>" border="0" /></a>
      <% } %>
	  
	  :: <a href="<%= request.getContextPath() %>/user/sharedJobs.jsp"> <img src="<%= request.getContextPath() %>/images/shared-jobs.png" title="<%=messages.getString("sharedJobs")%>" border="0" /></a>
	  
	  :: <a href="<%= request.getContextPath() %>/user/jobArchives.jsp"> <img src="<%= request.getContextPath() %>/images/job-archives.png" title="<%=messages.getString("jobArchives")%>" border="0" /></a>
	  
	  <% if (accessLevelHeader >= 30) {%>
      :: <a href="<%= request.getContextPath() %>/logs" ><img src="<%= request.getContextPath() %>/images/logs.png" title="<%=messages.getString("logsLink")%>" border="0" /></a>
      <% } %>

      :: <a href="<%= request.getContextPath() %>/logOff.jsp"> <img src="<%= request.getContextPath() %>/images/exit.png" title="<%=messages.getString("logOffLink")%>" border="0" /></a>
      <img src="<%= request.getContextPath() %>/images/vertical_16px.gif" />
     </td>
     <td class="attr" align="right" width="50%">

      <img src="<%= request.getContextPath() %>/images/vertical_16px.gif" />

	<%= usernameHeader%>
	:: <%=messages.getString("loggedAt")%> <%=java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT,request.getLocale()).format(ueHeader.getLoginDate())%>
     </td>
    </tr>
   </table>
 <% } %>
  <div id="systemWorking" style="display: none;">
    <img src="<%= request.getContextPath() %>/images/spinner.gif" alt="Executing..." />
  </div>

  <br>
 
 </c:if>
 <c:if test="${!empty param._mobile}">  
   <img src="<%= request.getContextPath() %>/images/art-24px.jpg" alt="" />
 </c:if>

<% } %>

