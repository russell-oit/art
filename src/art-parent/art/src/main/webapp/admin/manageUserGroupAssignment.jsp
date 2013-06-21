<%@ page import="art.utils.*,java.util.*" %>
<%@ include file ="headerAdmin.jsp" %>


<script type="text/javascript">

    function validateForm() {
		//ensure minimum number of items are selected
		if (document.manageUserGroupAssignment.USERS.selectedIndex<0) {
			alert("Please select at least one user");
			return false;
		} else if (document.manageUserGroupAssignment.USER_GROUPS.selectedIndex<0) {
			alert("Please select at least one user group");
			return false;
		}
    }

    function successForm() {
		alert("Update Successful");
        
        //clear selected items to enable change of selections
        document.manageUserGroupAssignment.USERS.selectedIndex=-1;
        document.manageUserGroupAssignment.USER_GROUPS.selectedIndex=-1;
    }


$jQuery(document).ready(function() {
    // bind form using ajaxForm
    $jQuery('#artForm').ajaxForm( { beforeSubmit: validateForm, success: successForm  });
});
</script>


<%
UserEntity ue=new UserEntity();
UserGroup ug=new UserGroup();
%>

<form id="artForm" name="manageUserGroupAssignment" method="post" action="execManageUserGroupAssignment.jsp">
    <table align="center">
        <tr>
			<td class="title" colspan="2"> Manage User Group Assignment </td>
        </tr>
        <tr>
            <td class="data" colspan="2"> Assign users to user groups </td>
        </tr>

		<tr>
            <td class="data"> Users </td>
            <td class="data">
                <select name="USERS" size="10" multiple>
                    <%
					List<String> usernames=ue.getAllUsernames();
					for(String name : usernames) {
						%>
						<option value="<%=name%>" ><%=name%></option>
						<%
					}
					%>
                </select>
            </td>
        </tr>

		<tr>
            <td class="data"> User Groups </td>
            <td class="data">
                <select name="USER_GROUPS" size="10" multiple>
                    <%
					Map<String, Integer> groups=ug.getAllUserGroupNames();
					for (Map.Entry<String, Integer> entry : groups.entrySet()) {
						%>
						<option value="<%=entry.getValue()%>" ><%=entry.getKey()%></option>
						<%
					}
					%>
                </select>
            </td>
        </tr>

        <tr><td colspan="2" class="data"> Action:
			<select name="ACTION">
				<option VALUE="GRANT">GRANT</option>
				<option VALUE="REVOKE">REVOKE</option>
				</select>
			</td>
		</tr>

		<tr>
            <td class="data">
	     <input type="submit" value="Submit">
	    </td>
	    <td class="data">
	     <a href="./showUserGroupAssignment.jsp">Show Current</a>
	    </td>
        </tr>


    </table>
</form>



<%@ include file ="/user/footer.jsp" %>