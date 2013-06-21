<%@ page import="art.utils.*,java.util.*" %>
<%@ include file ="headerAdmin.jsp" %>


<script type="text/javascript">
    
    function goToEdit() {
		if(document.manageRuleDefinitions.ACTION.value=="ADD"){
			document.manageRuleDefinitions.submit();
		} else {
			//modify or delete. a rule must be selected
			if (document.manageRuleDefinitions.RULE_NAME.selectedIndex>=0) {				
				document.manageRuleDefinitions.submit();
			} else {
				alert("Please select a rule");
			}
		}
    }
    
</script>


<form name="manageRuleDefinitions" method="post" action="editRuleDefinition.jsp">
    <table align="center">
        <tr>
			<td class="title" colspan="2"> Manage Rule Definitions </td>
        </tr>
        <tr>
            <td class="data" colspan="2"> <b>Add/Modify/Delete Rule Definitions </b> </td>
        </tr>
        <tr>
            <td class="data"> Rule </td>
            <td class="data">
                <select name="RULE_NAME" size="10">
                    <%					
					Rule rule=new Rule();
					List<String> rules=rule.getAllRuleNames();
					for(String name : rules) {
						%>
						<option value="<%=name%>" ><%=name%></option>
						<%
					} 
					%>
                </select>
            </td>
        </tr>

        <tr>
            <td colspan="2" class="data"> Action:
                <select name="ACTION">
                    <option value="ADD">ADD</option>
                    <option value="MODIFY">MODIFY</option>
                    <option value="DELETE">DELETE</option>
                </select>
            </td>
        </tr>
        <tr>
            <td class="data" colspan="2"> <input type="button" onclick="goToEdit()" value="Submit"> </td>
        </tr>
    </table>    
</form>

<p>&nbsp;</p>
    <div class="notes">
        <b>Notes:</b>
<table class="notes">
    <tr>
        <td>
            <ul>
                <li>Rules are used to dynamically filter query results.<br>
                For example, if a query that contains the table <b>"employees"</b> has been linked with the rule named <b>"DEPT"</b>
				for the column <b>employees.department</b> and the user that is executing that query has been linked with the same
				rule for values "North" and "East", the query will extract only the rows where the department value is "North" or "East".
				You can create dummy users to group rule values (lookup rules).
            </li>
            </ul>
        </td>
    </tr>
</table>
    </div>

<%@ include file ="/user/footer.jsp" %>