<%@ page import="art.utils.*,java.util.*" %>
<%@ include file ="headerAdmin.jsp" %>


<script type="text/javascript">
    
    function goToEdit() {		
		//ensure minimum number of items are selected
		if (document.manageUserRules.USERNAME.selectedIndex<0) {
			alert("Please select a user");	
		} else if (document.manageUserRules.RULE_NAME.selectedIndex<0) {
			alert("Please select a rule");
		} else {
			document.manageUserRules.submit();
		}		
    }
	
	function deleteUserRule() {		
		//ensure minimum number of items are selected
		if (document.manageUserRules.USERNAME.selectedIndex<0) {
			alert("Please select a user");	
		} else if (document.manageUserRules.RULE_NAME.selectedIndex<0) {
			alert("Please select a rule");
		} else {
			document.manageUserRules.action="deleteUserRule.jsp";
			document.manageUserRules.submit();
		}		
    }
    
</script>


<%
Iterator it;
String name;
%>

<form name="manageUserRules" method="post" action="manageUserRules2.jsp">
    <table align="center">
        <tr>
			<td class="title" colspan="2"> Manage Rule Values </td>
        </tr>
        <tr>
            <td class="data" colspan="2"> Select the user-rule combination to manage </td>
        </tr>
		
		<tr>
            <td class="data"> User </td>
            <td class="data">
                <select name="USERNAME" size="10">
                    <%		
					UserEntity ue=new UserEntity();
					List<String> usernames=ue.getAllUsernames();
					it=usernames.iterator();					
					while(it.hasNext()) {
						name=(String)it.next();	 
						%>
						<option value="<%=name%>" ><%=name%></option>
						<%
					} 
					%>
                </select>
            </td>
        </tr>
		
		 <tr>
            <td class="data"> Rule </td>
            <td class="data">
                <select name="RULE_NAME" size="10">
                    <%					
					Rule rule=new Rule();
					List<String> rules=rule.getAllRuleNames();
					it=rules.iterator();					         
					while(it.hasNext()) {
						name=(String)it.next();	 
						%>
						<option value="<%=name%>" ><%=name%></option>
						<%
					} 
					%>
                </select>
            </td>
        </tr>
		               		
		<tr>
            <td class="data" colspan="2"> 
				<input type="button" onclick="goToEdit()" value="Submit"> &nbsp;
				<input type="button" onclick="deleteUserRule()" value="Delete">
			</td>
        </tr>
		
		<tr><td colspan="2">&nbsp;</td></tr>
		<tr><td colspan="2">&nbsp;</td></tr>
		<tr><td colspan="2" class="data"> Current Assignment </td></tr>
		<tr>            
            <td colspan="2" class="data2">                
				<%				
				Map map=rule.getUserRuleAssignment();
				it = map.entrySet().iterator();					
				while(it.hasNext()) {
					Map.Entry entry = (Map.Entry)it.next();
					%>
					<%=entry.getValue()%> <br>
					<%
				} 
				%>                
            </td>
        </tr>
				        
    </table>    
</form>



<%@ include file ="footer.html" %>