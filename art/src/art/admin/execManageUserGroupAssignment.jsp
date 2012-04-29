<%@ page import="art.utils.*" %>


<%
String action=request.getParameter("ACTION");

String[] users = request.getParameterValues("USERS");
String[] userGroups = request.getParameterValues("USER_GROUPS"); 

UserGroup ug=new UserGroup();

ug.updateUserGroupAssignment(action,users,userGroups);

response.sendRedirect("manageUserGroupAssignment.jsp?updated=true");
%>


