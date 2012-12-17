<%@ page import="java.util.*,art.utils.*" %>
<%@ include file ="headerAdmin.jsp" %>


<script type="text/javascript">
    
    function goToEdit() {
		if(document.manageUserGroups.ACTION.value=="ADD"){
			document.manageUserGroups.submit();
		} else {
			//modify or delete. a group must be selected
			if (document.manageUserGroups.GROUP_ID.selectedIndex>=0) {				
				document.manageUserGroups.submit();
			} else {
				alert("Please select a group");
			}
		}
    }
    
</script>


<form name="manageUserGroups" method="post" action="editUserGroup.jsp">
    <table align="center">
        <tr>
			<td class="title" colspan="2"> Manage User Groups </td>
        </tr>
        <tr>
            <td class="data" colspan="2"> <b>Add/Modify/Delete User Groups </b> </td>
        </tr>
        <tr>
            <td class="data"> User Group </td>
            <td class="data">
                <select name="GROUP_ID" size="10">
                    <%
					UserGroup ug=new UserGroup();
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


<%@ include file ="/user/footer.jsp" %>