<%@ page import="art.utils.*,java.util.*" %>
<%@ include file ="headerAdmin.jsp" %>


<%
String action = request.getParameter("ACTION");
String ruleName = request.getParameter("RULE_NAME");

Rule rule=new Rule();

if (action.equals("DELETE")){
	//check if queries exist that are linked to this rule
	Map<Integer, ArtQuery> queries=rule.getLinkedQueries(ruleName);
	if(queries.size()>0){
		out.println("<pre>Error: There are queries that use the rule you want to delete");
		out.println("       Delete the following queries or change their rules");
		out.println("       in order to be able to delete this rule: ");
		out.println();
								
		for (Map.Entry<Integer, ArtQuery> entry : queries.entrySet()) {
			ArtQuery aq=entry.getValue();			
			out.println("Query ID: " + aq.getQueryId() + " , Name: " + aq.getName() + " , Group ID: " + aq.getGroupId());
		}
		out.println("</pre>");
		%>
		<%@ include file="/user/footer.jsp" %>
		<%
		return;
	} else {
		//no linked queries. delete rule
		rule.deleteDefinition(ruleName);
		response.sendRedirect("manageRuleDefinitions.jsp");
		return;
	}
}

boolean modify=false;
if (action.equals("MODIFY")){
	modify=true;
	rule.loadDefinition(ruleName);
}
%>


<form name="editRuleDefinition" method="post" action="execEditRuleDefinition.jsp">    	
	<input type="hidden" name="ACTION" value="<%=action%>">
	
	<table align="center">		
		<tr>
			<td class="title" colspan="2">Define Rule</td>
		</tr>
				
	    <tr>
			<td class="data"> Rule Name </td> 
			<%
			String edit="";
			if(modify){
				edit="readonly";
			}
			%>
			<td class="data"> <input type="text" name="RULE_NAME" value="<%=rule.getRuleName()%>" size="25" maxlength="40" <%=edit%>> </td>
			
		</tr>

		<tr><td class="data"> Rule Description </td>
			<td class="data"> <input type="text" name="RULE_DESCRIPTION" value="<%=rule.getDescription()%>" size="25" maxlength="40"> </td>
		</tr>

		<tr>
			<td class="data" colspan="2"> <input type="submit" value="Submit"> </td>
		</tr>
    </table>    
</form>


<%@ include file="/user/footer.jsp" %>