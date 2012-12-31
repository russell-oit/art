<%@ page import="art.utils.*" %>

<%@ page contentType="text/html; charset=UTF-8" %>
<% request.setCharacterEncoding("UTF-8"); %>


<%
String action=request.getParameter("ACTION");

String[] admins = request.getParameterValues("ADMINS");
String[] groups = request.getParameterValues("QUERY_GROUPS"); 
String[] datasources = request.getParameterValues("DATASOURCES");

UserEntity ue=new UserEntity();

ue.updateJuniorAdminPrivileges(action,admins,groups,datasources);

response.sendRedirect("manageAdminPrivileges.jsp");
%>


