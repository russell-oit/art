<%@ page import="art.utils.*,java.util.*,art.servlets.ArtDBCP" %>
<%@ include file ="headerAdmin.jsp" %>


<script type="text/javascript">
    
    function goToEdit() {
		if(document.manageUsers.ACTION.value=="ADD"){
			document.manageUsers.submit();
		} else {
			//modify or delete. an item must be selected
			if (document.manageUsers.USERNAME.selectedIndex>=0) {				
				document.manageUsers.submit();
			} else {
				alert("Please select a user");
			}
		}
    }
    
</script>


<form name="manageUsers" method="post" action="editUser.jsp">
    <table align="center">
        <tr>
			<td class="title" colspan="2"> Manage Users </td>
        </tr>
        <tr>
            <td class="data" colspan="2"> <b>Add/Modify/Delete Users </b> </td>
        </tr>
        <tr>
            <td class="data"> User </td>
            <td class="data">
                <select name="USERNAME" size="10">
                    <%
					UserEntity ue=new UserEntity();
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