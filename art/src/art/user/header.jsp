<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%  request.setCharacterEncoding("UTF-8"); %>

<%	
 java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("art.i18n.ArtMessages",request.getLocale());
 art.utils.UserEntity ue2 = (art.utils.UserEntity) session.getAttribute("ue");
%>


<% if (request.getParameter("_isFragment")==null) { %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
  <title>ART</title>
      
<c:if test="${empty param._mobile}">  
 <script type="text/javascript" src="<%= request.getContextPath() %>/js/prototype.js"></script>
 <script type="text/javascript" src="<%= request.getContextPath() %>/js/scriptaculous/scriptaculous.js"></script>
 <script type="text/javascript" src="<%= request.getContextPath() %>/js/ajaxtags.js"></script>
 <link rel="stylesheet" href="<%= request.getContextPath() %>/js/dhtmlgoodies_calendar/dhtmlgoodies_calendar.css" media="screen" />
  <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/jpivot/table/mdxtable.css" />
  <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/jpivot/navi/mdxnavi.css" />
  <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/wcf/form/xform.css" />
  <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/wcf/table/xtable.css" />
  <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/wcf/tree/xtree.css" />
  <script type="text/javascript" src="<%=request.getContextPath()%>/wcf/scroller.js"></script>
</c:if> 
  
  <script type="text/javascript" src="<%= request.getContextPath() %>/js/art.js"></script>  
  <link rel="stylesheet" type="text/css" href="<%=request.getContextPath() + art.servlets.ArtDBCP.getArtProps("css_skin")%>" />      
  <link rel="stylesheet" type="text/css" href="<%=request.getContextPath() %>/css/ajaxtags-art.css" /> 
       
</head>
<body>
 <c:if test="${empty param._mobile}">  
 <% if ( !(ue2.getUsername().equals("public_user") && art.servlets.ArtDBCP.getArtProps("header_with_public_user").equals("no") )  ) { %>
   <table width="100%"  class="art" cellpadding="0" cellspacing="0">
    <tr>
     <td class="attr" align="left" width="50%">
      <% if (ue2.getAdminLevel() > 5) {%>
       &nbsp;<a href="<%= request.getContextPath() %>/admin/adminAccess.jsp" ><img src="<%= request.getContextPath() %>/images/admin.png" title="Admin Console" border="0" /></a> ::
      <% } %>

       <a href="<%= request.getContextPath() %>/user/showGroups.jsp"><img src="<%= request.getContextPath() %>/images/back-home.png" title="<%=messages.getString("startLink")%>" border="0" /></a> 

      <% if (ue2.getAdminLevel() >= 5) {%>
      :: <a href="<%= request.getContextPath() %>/user/myJobs.jsp" ><img src="<%= request.getContextPath() %>/images/jobs.png" title="<%=messages.getString("myJobsLink")%>" border="0" /></a>
      <% } %>
	  
	  :: <a href="<%= request.getContextPath() %>/user/sharedJobs.jsp"> <img src="<%= request.getContextPath() %>/images/shared-jobs.png" title="<%=messages.getString("sharedJobsLink")%>" border="0" /></a>

      :: <a href="<%= request.getContextPath() %>/logOff.jsp"> <img src="<%= request.getContextPath() %>/images/exit.png" title="<%=messages.getString("logOffLink")%>" border="0" /></a>
      <img src="<%= request.getContextPath() %>/images/vertical_16px.gif" />
     </td>
     <td class="attr" align="right" width="50%">

      <img src="<%= request.getContextPath() %>/images/vertical_16px.gif" />

	<%= ue2.getUsername()%>
	:: <%=messages.getString("loggedAt")%> <%=java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT,request.getLocale()).format(ue2.getLoginDate())%>
     </td>
    </tr>
   </table>
 <% } %>
  <div id="systemWorking" style="display: none;">
    <img src="<%= request.getContextPath() %>/images/spinner.gif" />
  </div>

 <hr width="100%" size="1">
 
 </c:if>
 <c:if test="${!empty param._mobile}">  
   <img src="<%= request.getContextPath() %>/images/art-24px.jpg" alt="" />
 </c:if>

<% } %>

