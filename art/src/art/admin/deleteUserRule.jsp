<%@ page import="art.utils.*" %>


<%
String ruleName=request.getParameter("RULE_NAME");
String username=request.getParameter("USERNAME");

Rule rule=new Rule();

rule.deleteUserRule(username,ruleName);

response.sendRedirect("manageUserRules.jsp");
%>
