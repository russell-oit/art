<%@ page import="art.utils.*,java.util.*" %>
<%@ include file ="headerAdmin.jsp" %>


<script type="text/javascript">
    
    function goToEdit() {		
		//ensure minimum number of items are selected
		if (document.manageSharedJobs.JOBS.selectedIndex<0) {
			alert("Please select at least one job");	
		} else if (document.manageSharedJobs.USERS.selectedIndex<0 && document.manageSharedJobs.USER_GROUPS.selectedIndex<0) {
			alert("Please select at least one user or user group");
		} else {
			document.manageSharedJobs.submit();
		}		
    }
    
</script>


<%
UserEntity ue=new UserEntity();
Iterator it;
%>

<form name="manageSharedJobs" method="post" action="execManageSharedJobs.jsp">
    <table align="center">
        <tr>
			<td class="title" colspan="2"> Manage Shared Jobs </td>
        </tr>
        <tr>
            <td class="data" colspan="2"> Select jobs and the users/user groups to share them with </td>
        </tr>
		
        <tr>
            <td class="data"> Jobs<br><small>Job Owner - Job ID - Job/Query Name</small> </td>
            <td class="data">
                <select name="JOBS" size="5" multiple>
                    <%																									
					Map jobs=ue.getAllSharedJobs();
					it = jobs.entrySet().iterator();
					int jobId;
					
					while (it.hasNext()) {
						Map.Entry entry = (Map.Entry)it.next();
						ArtJob aj=(ArtJob)entry.getValue();
                        jobId=aj.getJobId();
						%>
						<option value="<%=jobId%>">
							<%=aj.getUsername() + " - Job " + jobId + " - " + aj.getJobName()%>
						</option>
						<%
					 }                 
                    %>
                </select>
            </td>
        </tr>
				
		<tr>
            <td class="data"> Users </td>
            <td class="data">
                <select name="USERS" size="5" multiple>
                    <%										
					List<String> usernames=ue.getAllUsernames();
					it=usernames.iterator();
					String name;         
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
            <td class="data"> User Groups </td>
            <td class="data">
                <select name="USER_GROUPS" size="5" multiple>
                    <%
					UserGroup ug=new UserGroup();
					Map groups=ug.getAllUserGroupNames();
					it = groups.entrySet().iterator();					
					while(it.hasNext()) {
						Map.Entry entry = (Map.Entry)it.next();
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
            <td class="data" colspan="2"> <input type="button" onclick="goToEdit()" value="Submit"> </td>
        </tr>
		
		<tr><td colspan="2">&nbsp;</td></tr>
		<tr><td colspan="2">&nbsp;</td></tr>
		<tr><td colspan="2" class="data"> Current Assignment </td></tr>
		<tr>            
            <td colspan="2" class="data2">                
				<%
				ArtJob aj=new ArtJob();
				Map<Integer,String> map=aj.getSharedJobAssignment();
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



<%@ include file ="/user/footer.jsp" %>