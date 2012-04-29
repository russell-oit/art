<%@ page import="art.utils.*" %>


<%
String action=request.getParameter("ACTION");

String[] admins = request.getParameterValues("ADMINS");
String[] groups = request.getParameterValues("OBJECT_GROUPS"); 
String[] datasources = request.getParameterValues("DATASOURCES");

UserEntity ue=new UserEntity();

ue.updateJuniorAdminPrivileges(action,admins,groups,datasources);

response.sendRedirect("manageAdminPrivileges.jsp");
%>


