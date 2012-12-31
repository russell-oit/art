<%@ page import="art.utils.*,java.util.*" %>

<%@ page contentType="text/html; charset=UTF-8" %>
<% request.setCharacterEncoding("UTF-8"); %>


<%
String action=request.getParameter("ACTION");

String username=request.getParameter("USERNAME");
String ruleName=request.getParameter("RULE_NAME");
String newRuleType=request.getParameter("RULE_TYPE");
String newRuleValue=request.getParameter("RULE_VALUE").trim();
String oldRuleType=request.getParameter("OLD_RULE_TYPE");
String oldRuleValue=request.getParameter("OLD_RULE_VALUE");

String userGroup=request.getParameter("USER_GROUP");
int groupId=0;
if(userGroup!=null){
	groupId=Integer.parseInt(userGroup);
}

if(newRuleType.equals("LOOKUP")){
	newRuleValue=newRuleValue.toLowerCase(); //usernames are always in lowercase
}

Rule rule=new Rule();

rule.setUserGroupId(groupId);
rule.setUsername(username);
rule.setRuleName(ruleName);
rule.setRuleType(newRuleType);
rule.setRuleValue(newRuleValue);

if (action.equals("ADD")){
	if(username!=null){
		rule.insertUserRuleValue();
	} else if(userGroup!=null){
		rule.insertUserGroupRuleValue();
	}
} else if (action.equals("MODIFY")){
	if(username!=null){
		rule.updateUserRuleValue(oldRuleType,oldRuleValue);
	} else if(userGroup!=null){
		rule.updateUserGroupRuleValue(oldRuleType,oldRuleValue);
	}
}
if(username!=null){
	response.sendRedirect("manageUserRules2.jsp?USERNAME="+username+"&RULE_NAME="+ruleName);
} else if(userGroup!=null){
	response.sendRedirect("manageUserRules2.jsp?USER_GROUP="+userGroup+"&RULE_NAME="+ruleName);
}
%>


