<%@ page import="art.utils.*" %>


<%
String action=request.getParameter("ACTION");

String[] users = request.getParameterValues("USERS");
String[] userGroups = request.getParameterValues("USER_GROUPS"); 
String[] objects = request.getParameterValues("OBJECTS");
String[] objectGroups = request.getParameterValues("OBJECT_GROUPS"); 

UserEntity ue=new UserEntity();

ue.updateUserPrivileges(action,users,objects,objectGroups);
ue.updateUserGroupPrivileges(action,userGroups,objects,objectGroups);

response.sendRedirect("manageUserPrivileges.jsp?updated=true");
%>


