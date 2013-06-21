<%@ page import="art.utils.*,java.util.*" %>
<%@ include file ="headerAdmin.jsp" %>


<script type="text/javascript">

    function goToExec() {
		if(document.editUserRule.RULE_VALUE.value==""){
			alert("Please select a value");
		} else {
			document.editUserRule.submit();
		}
    }

</script>

<%
String action = request.getParameter("ACTION");
String ruleName=request.getParameter("RULE_NAME");
String username=request.getParameter("USERNAME");
String userGroup=request.getParameter("USER_GROUP");
String userGroupName=request.getParameter("USER_GROUP_NAME");
String ruleTypeValue=request.getParameter("RULE_TYPE_VALUE");
String ruleType="";
String ruleValue="";

int groupId=0;
if(userGroup!=null){
	groupId=Integer.parseInt(userGroup);
}

if(!action.equals("ADD")){
	//either modify or delete
	int index = ruleTypeValue.indexOf("-");
	ruleType = ruleTypeValue.substring(0,index);
	ruleValue = ruleTypeValue.substring(index+1);
}

Rule rule=new Rule();

if (action.equals("DELETE")){
	if(username!=null){
		rule.deleteUserRuleValue(username,ruleName,ruleType,ruleValue);
		response.sendRedirect("manageUserRules2.jsp?USERNAME="+username+"&RULE_NAME="+ruleName);
	} else if(userGroup!=null){
		rule.deleteUserGroupRuleValue(groupId,ruleName,ruleType,ruleValue);
		response.sendRedirect("manageUserRules2.jsp?USER_GROUP="+groupId+"&RULE_NAME="+ruleName);
	}
	return;
}

%>


<form name="editUserRule" method="post" action="execEditUserRule.jsp">
	<input type="hidden" name="ACTION" value="<%=action%>">
	
	<%if(username!=null){%>
		<input type="hidden" name="USERNAME" value="<%=username%>">
	<%} else if(userGroup!=null){%>
		<input type="hidden" name="USER_GROUP" value="<%=userGroup%>">
		<input type="hidden" name="USER_GROUP_NAME" value="<%=userGroupName%>">
	<%}%>
	<input type="hidden" name="RULE_NAME" value="<%=ruleName%>">
	<input type="hidden" name="OLD_RULE_TYPE" value="<%=ruleType%>">
	<input type="hidden" name="OLD_RULE_VALUE" value="<%=ruleValue%>">

	<table align="center">
		<tr>
			<td class="title" colspan="2">Define Rule Value</td>
		</tr>
		<tr>
			<td class="data" colspan="2"><b>Rule:</b> <%=ruleName%> <br>
				<%if(username!=null){%>
				<b>User:</b> <%=username%> 
				<%} else if(userGroup!=null){%>
				<b>User Group:</b> <%=userGroupName%> 
				<%}%>
				<br><br>Set the rule value</td>
		</tr>

	    <tr>
			<td class="data"> Rule Type </td>
			<td class="data">
				<select name="RULE_TYPE">
					<option value="EXACT" <%=(ruleType.equals("EXACT")?"selected":"")%>>Exact</option>
					<option value="LOOKUP" <%=(ruleType.equals("LOOKUP")?"selected":"")%>>Lookup</option>
				</select>
			</td>
		</tr>

		<tr>
			<td class="data"> Rule Value </td>
			<td class="data"> <input type="text" size="20" maxlength="25" name="RULE_VALUE" value="<%=ruleValue%>"> </td>
		</tr>
		 <tr>
            <td class="data" colspan="2"> <input type="button" onclick="goToExec()" value="Submit"> </td>
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
                <li>If you use the type <b>Lookup</b> you must specify the rule value as the username of the user
				you want to reference for this rule, or the id of the user group you want to reference (i.e. the selected user will use the same rule values
				as the looked up user or user group).
                </li>
                <li>
				Use the rule type <b>Exact</b> and value <b>ALL_ITEMS</b> if you want the user to be able to retrieve
				all the rows.
            </li>
            </ul>
        </td>
    </tr>
</table>
    </div>



<%@ include file="/user/footer.jsp" %>