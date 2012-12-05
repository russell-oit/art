<%@ page import="art.utils.*" %>


<%
String action=request.getParameter("ACTION");

String[] users = request.getParameterValues("USERS");
String[] userGroups = request.getParameterValues("USER_GROUPS"); 
String[] queries = request.getParameterValues("QUERIES");
String[] queryGroups = request.getParameterValues("QUERY_GROUPS"); 

UserEntity ue=new UserEntity();

ue.updateUserPrivileges(action,users,queries,queryGroups);
ue.updateUserGroupPrivileges(action,userGroups,queries,queryGroups);

response.sendRedirect("manageUserPrivileges.jsp?updated=true");
%>


