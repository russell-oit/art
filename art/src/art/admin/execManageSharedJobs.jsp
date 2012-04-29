<%@ page import="art.utils.*" %>


<%
String action=request.getParameter("ACTION");

String[] jobs = request.getParameterValues("JOBS");
String[] users = request.getParameterValues("USERS");
String[] userGroups = request.getParameterValues("USER_GROUPS"); 

ArtJob aj=new ArtJob();

aj.updateUserAccess(action,users,jobs);
aj.updateUserGroupAccess(action,userGroups,jobs);

response.sendRedirect("manageSharedJobs.jsp");
%>


