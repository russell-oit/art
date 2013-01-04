<%@ page import="art.utils.*" %>

<%@ page contentType="text/html; charset=UTF-8" %>
<% request.setCharacterEncoding("UTF-8"); %>


<%
String action=request.getParameter("ACTION");
String usersAction=request.getParameter("USERS_ACTION");
String[] users = request.getParameterValues("USERS");

UserGroup ug=new UserGroup();

ug.setGroupId(Integer.parseInt(request.getParameter("GROUP_ID")));
ug.setName(request.getParameter("GROUP_NAME").trim());
ug.setDescription(request.getParameter("GROUP_DESCRIPTION").trim());
ug.setDefaultQueryGroup(Integer.parseInt(request.getParameter("DEFAULT_QUERY_GROUP")));
ug.setStartQuery(request.getParameter("START_QUERY").trim());

if (action.equals("ADD")){	
	ug.insert();
} else if (action.equals("MODIFY")){
	ug.update();
}

//add or remove users from the group
if(usersAction.equals("ADD")){
	ug.addUsers(users);
} else if(usersAction.equals("REMOVE")){
	ug.removeUsers(users);
}

response.sendRedirect("manageUserGroups.jsp");
%>


