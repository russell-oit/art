<%@ page import="art.utils.*" %>


<%
String ruleName=request.getParameter("RULE_NAME");
String username=request.getParameter("USERNAME");

int groupId=0;
String groupIdString=request.getParameter("USER_GROUP");
if(groupIdString!=null){
	groupId=Integer.parseInt(groupIdString);
}

Rule rule=new Rule();

if(username!=null){
	rule.deleteUserRule(username,ruleName);
}

if(groupId>0){
	rule.deleteUserGroupRule(groupId,ruleName);
}

response.sendRedirect("manageUserRules.jsp");
%>
