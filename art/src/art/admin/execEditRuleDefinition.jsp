<%@ page import="art.utils.*,java.util.*" %>


<%
String action=request.getParameter("ACTION");

Rule rule=new Rule();

rule.setRuleName(request.getParameter("RULE_NAME").trim());
rule.setDescription(request.getParameter("RULE_DESCRIPTION").trim());

if (action.equals("ADD")){	
	rule.insertDefinition();
} else if (action.equals("MODIFY")){
	rule.updateDefinition();
}

response.sendRedirect("manageRuleDefinitions.jsp");
%>


