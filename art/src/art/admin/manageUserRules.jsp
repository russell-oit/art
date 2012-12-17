<%@ page import="art.utils.*,java.util.*" %>
<%@ include file ="headerAdmin.jsp" %>


<script type="text/javascript">
	
	function countSelected(list){
        var count=0;
        for(var i=0; i<list.options.length; i++ ){
            if(list.options[i].selected){
                count++;
            }
        }
        
        return count;
    }
    
    function goToEdit() {		
		//ensure minimum number of items are selected
		if(countSelected(document.getElementById("username"))>1){
            alert("Please select a single user");
		} else if(countSelected(document.getElementById("userGroup"))>1){
            alert("Please select a single user group");
		} else if (countSelected(document.getElementById("username"))==1 && countSelected(document.getElementById("userGroup"))==1) {
			alert("Please select either a user or a user group");
			//clear selected items to enable change of selections
			document.manageUserRules.USERNAME.selectedIndex=-1;
			document.manageUserRules.USER_GROUP.selectedIndex=-1;
		} else if (document.manageUserRules.USERNAME.selectedIndex<0 && document.manageUserRules.USER_GROUP.selectedIndex<0) {
			alert("Please select a user or user group");	
		} else if (document.manageUserRules.RULE_NAME.selectedIndex<0) {
			alert("Please select a rule");
		} else {
			document.manageUserRules.submit();
		}		
    }
	
	function deleteUserRule() {		
		//ensure minimum number of items are selected
		if (document.manageUserRules.USERNAME.selectedIndex<0 && document.manageUserRules.USER_GROUP.selectedIndex<0) {
			alert("Please select a user or user group");	
		} else if (document.manageUserRules.RULE_NAME.selectedIndex<0) {
			alert("Please select a rule");
		} else {
			document.manageUserRules.action="deleteUserRule.jsp";
			document.manageUserRules.submit();
		}		
    }
    
</script>


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
                <select name="USERNAME" id="username" size="5">
                    <%		
					UserEntity ue=new UserEntity();
					List<String> usernames=ue.getAllUsernames();
					for(String username : usernames) {
						%>
						<option value="<%=username%>" ><%=username%></option>
						<%
					} 
					%>
                </select>
            </td>
        </tr>
		
		<tr>
            <td class="data"> User Group </td>
            <td class="data">
                <select name="USER_GROUP" id="userGroup" size="5">
                    <%
					UserGroup ug=new UserGroup();
					Map<String, Integer> userGroups=ug.getAllUserGroupNames();
					for (Map.Entry<String, Integer> entry : userGroups.entrySet()) {
						%>
						<option value="<%=entry.getValue()%>" ><%=entry.getKey()%></option>
						<%
					}
					%>
                </select>
            </td>
        </tr>

		
		 <tr>
            <td class="data"> Rule </td>
            <td class="data">
                <select name="RULE_NAME" id="ruleName" size="10">
                    <%					
					Rule rule=new Rule();
					List<String> rules=rule.getAllRuleNames();
					for(String ruleName : rules) {
						%>
						<option value="<%=ruleName%>" ><%=ruleName%></option>
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
				Map<Integer, String> map=rule.getUserRuleAssignment();
				if(map.size()>0){
					%>
					<b>Users</b><br>
					<%
					for (Map.Entry<Integer, String> entry : map.entrySet()) {
						%>
						<%=entry.getValue()%> <br>
						<%
					}
					%>
					<br>
					<%
				}
				%>  
				
				<%				
				map=rule.getUserGroupRuleAssignment();
				if(map.size()>0){
					%>
					<b>User Groups</b><br>
					<%
					for (Map.Entry<Integer, String> entry : map.entrySet()) {
						%>
						<%=entry.getValue()%> <br>
						<%
					}
				}
				%>     
            </td>
        </tr>
				        
    </table>    
</form>



<%@ include file ="/user/footer.jsp" %>