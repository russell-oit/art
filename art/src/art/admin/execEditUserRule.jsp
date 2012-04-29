<%@ page import="art.utils.*,java.util.*" %>


<%
String action=request.getParameter("ACTION");

String username=request.getParameter("USERNAME");
String ruleName=request.getParameter("RULE_NAME");
String newRuleType=request.getParameter("RULE_TYPE");
String newRuleValue=request.getParameter("RULE_VALUE").trim();
String oldRuleType=request.getParameter("OLD_RULE_TYPE");
String oldRuleValue=request.getParameter("OLD_RULE_VALUE");

if(newRuleType.equals("LOOKUP")){
	newRuleValue=newRuleValue.toLowerCase(); //usernames are always in lowercase
}

Rule rule=new Rule();

rule.setUsername(username);
rule.setRuleName(ruleName);
rule.setRuleType(newRuleType);
rule.setRuleValue(newRuleValue);

if (action.equals("ADD")){	
	rule.insertUserRuleValue();
} else if (action.equals("MODIFY")){
	rule.updateUserRuleValue(oldRuleType,oldRuleValue);
}

response.sendRedirect("manageUserRules2.jsp?USERNAME="+username+"&RULE_NAME="+ruleName);
%>


