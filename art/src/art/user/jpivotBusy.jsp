<%@ page import="java.util.ResourceBundle, art.servlets.ArtDBCP;" %>
<jsp:useBean id="ue" scope="session" class="art.utils.UserEntity" />

<!-- start header. similar to default user header but has auto refresh to check if query has finished and display results -->
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%  request.setCharacterEncoding("UTF-8"); %>

<%	
 java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("art.i18n.ArtMessages",request.getLocale());
%>


<% if (request.getParameter("_isFragment")==null) { %>
<html>
<head>
  <title>ART</title>
  <meta http-equiv="refresh" content="2; URL=<c:out value="${requestSynchronizer.resultURI}"/>">
  <c:if test="${empty param._mobile}">  
    <script type="text/javascript" src="<%= request.getContextPath() %>/js/prototype-1.3.1.js"></script>
    <script type="text/javascript" src="<%= request.getContextPath() %>/js/ajaxtags-1.1.5.js"></script>
  </c:if>
  <script type="text/javascript" src="<%= request.getContextPath() %>/js/art.js"></script>
  <link rel="stylesheet" type="text/css" href="<%=request.getContextPath() + ArtDBCP.getArtSetting("css_skin")%>" />      
  <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/css/ajaxtags-art.css" />
  <c:if test="${empty param._mobile}">  
    <link rel="stylesheet" href="<%= request.getContextPath() %>/js/dhtmlgoodies_calendar/dhtmlgoodies_calendar.css" media="screen">
  </c:if>
  
</head>
<body>
 <c:if test="${empty param._mobile}">  
 <% if ( !(ue.getUsername().equals("public_user") && ArtDBCP.getArtSetting("header_with_public_user").equals("no") )  ) { %>
   <table width="100%"  class="art" cellpadding="0" cellspacing="0">
    <tr>
     <td class="attr" align="left" width="50%">
      <% if (ue.getAdminLevel() > 5) {%>
       &nbsp;<a href="<%= request.getContextPath() %>/admin/adminConsole.jsp" ><img src="<%= request.getContextPath() %>/images/admin.png" title="Admin Console" border="0"></a> ::
      <% } %>

       <a href="<%= request.getContextPath() %>/user/showGroups.jsp"><img src="<%= request.getContextPath() %>/images/back-home.png" title="<%=messages.getString("startLink")%>" border="0"></a> 

      <% if (ue.getAdminLevel() >= 5) {%>
      :: <a href="<%= request.getContextPath() %>/user/myJobs.jsp" ><img src="<%= request.getContextPath() %>/images/jobs.png" title="<%=messages.getString("myJobsLink")%>" border="0"></a>
      <% } %>
	  
	  :: <a href="<%= request.getContextPath() %>/user/sharedJobs.jsp"> <img src="<%= request.getContextPath() %>/images/shared-jobs.png" title="<%=messages.getString("sharedJobsLink")%>" border="0"></a>
	  
	  <% if (ue.getAdminLevel() == 100) {%>
      :: <a href="<%= request.getContextPath() %>/logs" ><img src="<%= request.getContextPath() %>/images/logs.png" title="<%=messages.getString("logsLink")%>" border="0" /></a>
      <% } %>

      :: <a href="<%= request.getContextPath() %>/logOff.jsp"> <img src="<%= request.getContextPath() %>/images/exit.png" title="<%=messages.getString("logOffLink")%>" border="0"></a>
      <img src="<%= request.getContextPath() %>/images/vertical_16px.gif">
     </td>
     <td class="attr" align="right" width="50%">

     <span id="systemWorking" style="display: none">
      <img src="<%= request.getContextPath() %>/images/spinner.gif">
     </span>
      <img src="<%= request.getContextPath() %>/images/vertical_16px.gif">

	<%= ue.getUsername()%>
	:: <%=messages.getString("loggedAt")%> <%=java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT,request.getLocale()).format(ue.getLoginDate())%>
     </td>
    </tr>
   </table>
 <% } else { %>
     <span id="systemWorking" style="display: none; position: absolute; top:0%; left:50%;">
      <img src="<%= request.getContextPath() %>/images/spinner.gif">
     </span>
 <% } %>

 <hr style="width:100%;height:1px">
 
 </c:if>
 <c:if test="${!empty param._mobile}">  
   <img src="<%= request.getContextPath() %>/images/art-24px.jpg">
 </c:if>

<% } %>
<!-- end header -->


 <table align=center>
  <tr>
    <td colspan=2 class=data align=center> <b><span style="color:red">Query executing... Please wait.</span></b>
	<img src="<%= request.getContextPath() %>/images/spinner.gif">
    </td>
  </tr>
  <tr>
    <td class=attr> Message:
    </td>
    <td class=data>Please wait until your results are computed.
    </td>
  </tr>
 </table>


<%@ include file ="footer.jsp" %>