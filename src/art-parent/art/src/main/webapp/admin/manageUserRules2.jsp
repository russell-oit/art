<%@ page import="art.utils.*,java.util.*" %>
<%@ include file ="headerAdmin.jsp" %>


<script type="text/javascript">
    
    function goToEdit() {
		if(document.manageUserRules2.ACTION.value=="ADD"){
			document.manageUserRules2.submit();
		} else {
			//modify or delete. an item must be selected
			if (document.manageUserRules2.RULE_TYPE_VALUE.selectedIndex>=0) {				
				document.manageUserRules2.submit();
			} else {
				alert("Please select a rule value");
			}
		}
    }
    
</script>

<%
String ruleName=request.getParameter("RULE_NAME");
String username=request.getParameter("USERNAME");
String userGroup=request.getParameter("USER_GROUP");

String userGroupName="";
int groupId=0;
if(userGroup!=null){
	groupId=Integer.parseInt(userGroup);
	UserGroup ug=new UserGroup();
	userGroupName=ug.getUserGroupName(groupId);
}
%>


<form name="manageUserRules2" method="post" action="editUserRule.jsp">
	<%if(username!=null){%>
		<input type="hidden" name="USERNAME" value="<%=username%>">
	<%} else if(userGroup!=null){%>
		<input type="hidden" name="USER_GROUP" value="<%=userGroup%>">
		<input type="hidden" name="USER_GROUP_NAME" value="<%=userGroupName%>">
	<%}%>
	<input type="hidden" name="RULE_NAME" value="<%=ruleName%>">
	
    <table align="center">
        <tr>
			<td class="title" colspan="2"> Manage Rule Values </td>
        </tr>
        <tr>
            <td class="data" colspan="2"> <b>Rule:</b> <%=ruleName%> <br>
				<%if(username!=null){%>
				<b>User:</b> <%=username%> 
				<%} else if(userGroup!=null){%>
				<b>User Group:</b> <%=userGroupName%> 
				<%}%>
				<br><br>Select a value to modify or delete, or add a new value </td>
        </tr>
        <tr>
            <td class="data"> Select rule value </td>
            <td class="data">
                <select name="RULE_TYPE_VALUE" size="10">
                    <%					
					Rule ar=new Rule();
					Map<Integer, Rule> values=null;
					if(username!=null){
						values=ar.getRuleValues(username,ruleName);
					} else if(userGroup!=null){
						values=ar.getRuleValues(groupId,ruleName);
					}
					String ruleType;
					String ruleValue;
					
					for (Map.Entry<Integer, Rule> entry : values.entrySet()) {
						Rule rule=entry.getValue();
                        ruleType=rule.getRuleType();
						ruleValue=rule.getRuleValue();
						%>
						<option value="<%=ruleType%>-<%=ruleValue%>">
							<%=ruleType%>-<%=ruleValue%>
						</option>
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

<div style="text-align:center">
	<a href="manageUserRules.jsp">Manage User Rules</a>
</div>


<%@ include file ="/user/footer.jsp" %>