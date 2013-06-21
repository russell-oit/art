<%@ page import="java.util.*,art.utils.*" %>
<%@ include file ="headerAdmin.jsp" %>


<script type="text/javascript">
    
    function goToEdit() {
		if(document.manageQueryGroups.ACTION.value=="ADD"){
			document.manageQueryGroups.submit();
		} else {
			//modify or delete. a group must be selected
			if (document.manageQueryGroups.GROUP_ID.selectedIndex>=0) {				
				document.manageQueryGroups.submit();
			} else {
				alert("Please select a group");
			}
		}
    }
    
</script>


<form name="manageQueryGroups" method="post" action="editQueryGroup.jsp">
    <table align="center">
        <tr>
			<td class="title" colspan="2"> Manage Query Groups </td>
        </tr>
        <tr>
            <td class="data" colspan="2"> <b>Add/Modify/Delete Query Groups </b> </td>
        </tr>
        <tr>
            <td class="data"> Query Group </td>
            <td class="data">
                <select name="GROUP_ID" size="10">
                    <%
					QueryGroup qg=new QueryGroup();
					Map<String, Integer> groups=qg.getAllQueryGroupNames();
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