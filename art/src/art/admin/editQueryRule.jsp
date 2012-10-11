<%@ page import="java.sql.*,art.utils.*" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<%@ include file ="headerAdmin.jsp" %>

<%
String action = request.getParameter("RULEACTION");

int queryId=Integer.parseInt(request.getParameter("QUERY_ID"));

String ruleName;
if(StringUtils.equals(action, "NEW")){
	ruleName=request.getParameter("RULE_NAME");	
} else {
	//MODIFY
	String[] rules = request.getParameterValues("QUERY_RULES");
	ruleName=rules[0];
}

Rule ar=new Rule();
ar.loadQueryRuleColumn(queryId, ruleName);
 
%>

<form action="execEditQueryRule.jsp" method="post">
    <input type="hidden" name="QUERY_ID" value="<%= queryId%>">
    <input type="hidden" name="RULEACTION" value="<%= action%>">
	
    <table align="center">
        
        <tr><td class="title" colspan="2" ><i>Define Rule Column</i></td></tr>

        <tr><td class="data"> Rule Name </td>
            <td class="data"> <input type="text" name="RULE_NAME" value="<%=ruleName%>" size="40" readonly>
            </td>
        </tr>
        <tr><td class="data"> Column Name </td>
            <td class="data"> <input type="text" name="FIELD_NAME" value="<%=ar.getFieldName()%>" size="40" maxlength="40">
            </td>
        </tr>

        <tr>
            <td><input type="submit" value="Submit"></td>
            <td></td>
        </tr>

    </table>
</form> 

<%@ include file ="footer.html" %>

