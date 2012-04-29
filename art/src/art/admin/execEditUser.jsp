<%@ page import="art.utils.*,art.servlets.*" %>


<%
String action = request.getParameter("ACTION");
String groupsAction=request.getParameter("GROUPS_ACTION");
String[] userGroups = request.getParameterValues("USER_GROUPS");

String password = request.getParameter("PASSWORD");
if(!password.trim().equals("")){
	password=Encrypter.HashPassword(password,ArtDBCP.getPasswordHashingAlgorithm());
}

UserEntity ue=new UserEntity();

ue.setUsername(request.getParameter("USERNAME").toLowerCase().trim());
ue.setPassword(password);
ue.setHashingAlgorithm(ArtDBCP.getPasswordHashingAlgorithm());
ue.setActiveStatus(request.getParameter("STATUS"));
ue.setFullName(request.getParameter("FULL_NAME").trim());
ue.setEmail(request.getParameter("EMAIL").trim());
ue.setAdminLevel(Integer.parseInt(request.getParameter("ADMIN_LEVEL")));
ue.setCanChangePasswordString(request.getParameter("CAN_CHANGE_PASSWORD"));
ue.setDefaultObjectGroup(Integer.parseInt(request.getParameter("DEFAULT_OBJECT_GROUP")));

if (action.equals("ADD")){
	ue.insert();
} else if (action.equals("MODIFY")){
	ue.update();
}

//update user group membership
ue.updateUserGroupMembership(groupsAction,userGroups);

//reload user entity in the session if details for the current user have changed
UserEntity ueSession=(UserEntity)session.getAttribute("ue");
if(ueSession!=null){
	if(ue.getUsername().equals(ueSession.getUsername())){
		ueSession.reload();
	}
}

response.sendRedirect("manageUsers.jsp");
%>
