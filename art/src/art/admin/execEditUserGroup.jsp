<%@ page import="art.utils.*" %>


<%
String action=request.getParameter("ACTION");
String usersAction=request.getParameter("USERS_ACTION");
String[] users = request.getParameterValues("USERS");

UserGroup group=new UserGroup();

group.setGroupId(Integer.parseInt(request.getParameter("GROUP_ID")));
group.setName(request.getParameter("GROUP_NAME").trim());
group.setDescription(request.getParameter("GROUP_DESCRIPTION").trim());
group.setDefaultQueryGroup(Integer.parseInt(request.getParameter("DEFAULT_QUERY_GROUP")));

if (action.equals("ADD")){	
	group.insert();
} else if (action.equals("MODIFY")){
	group.update();
}

//add or remove users from the group
if(usersAction.equals("ADD")){
	group.addUsers(users);
} else if(usersAction.equals("REMOVE")){
	group.removeUsers(users);
}

response.sendRedirect("manageUserGroups.jsp");
%>


