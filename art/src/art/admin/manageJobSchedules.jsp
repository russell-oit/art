<%@ page import="art.utils.*,java.util.*" %>
<%@ include file ="headerAdmin.jsp" %>


<script type="text/javascript">
    
    function goToEdit() {
		if(document.manageJobSchedules.ACTION.value=="ADD"){
			document.manageJobSchedules.submit();
		} else {
			//modify or delete. a schedule must be selected
			if (document.manageJobSchedules.SCHEDULE_NAME.selectedIndex>=0) {				
				document.manageJobSchedules.submit();
			} else {
				alert("Please select a schedule");
			}
		}
    }
    
</script>


<form name="manageJobSchedules" method="post" action="editJobSchedule.jsp">
    <table align="center">
        <tr>
			<td class="title" colspan="2">Manage Job Schedules</td>
        </tr>
        <tr>
            <td class="data" colspan="2"> <b>Add/Modify/Delete Job Schedules</b> </td>
        </tr>
        <tr>
            <td class="data"> Schedule </td>
            <td class="data">
                <select name="SCHEDULE_NAME" size="10">
                    <%					
					JobSchedule js=new JobSchedule();
					List<String> schedules=js.getAllScheduleNames();
					for(String name : schedules) {
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