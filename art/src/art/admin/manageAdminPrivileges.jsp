<%@ page import="art.utils.*,java.util.*" %>
<%@ include file ="headerAdmin.jsp" %>


<script type="text/javascript">
    
    function goToEdit() {		
		//ensure minimum number of items are selected
		if (document.manageAdminPrivileges.ADMINS.selectedIndex<0) {
			alert("Please select at least one admin");	
		} else if (document.manageAdminPrivileges.QUERY_GROUPS.selectedIndex<0 && document.manageAdminPrivileges.DATASOURCES.selectedIndex<0) {
			alert("Please select at least one query group or datasource");
		} else {
			document.manageAdminPrivileges.submit();
		}		
    }
    
</script>


<%
UserEntity ue=new UserEntity();
%>

<form name="manageAdminPrivileges" method="post" action="execManageAdminPrivileges.jsp">
    <table align="center">
        <tr>
			<td class="title" colspan="2"> Manage Privileges for Mid/Junior Admins </td>
        </tr>
        <tr>
            <td class="data" colspan="2"> Specify Query Groups and Datasources that Mid and Junior Admins can work with </td>
        </tr>
		
		<tr>
            <td class="data"> Admins </td>
            <td class="data">
                <select name="ADMINS" size="5" multiple>
                    <%										
					List<String> usernames=ue.getJuniorAdminUsernames();
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
            <td class="data"> Query Groups </td>
            <td class="data">
                <select name="QUERY_GROUPS" size="5" multiple>
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
            <td class="data"> Datasources </td>
            <td class="data">
                <select name="DATASOURCES" size="5" multiple>
                    <%					
					Datasource ds=new Datasource();
					Map<String, Integer> datasources=ds.getAllDatasourceNames();
					for (Map.Entry<String, Integer> entry : datasources.entrySet()) {
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
				Map<Integer, String> map;
				map=ue.getJuniorAdminGroupAssignment();
				if(map.size()>0){
					%>
					<b>Query Groups</b><br>
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
				map=ue.getJuniorAdminDatasourceAssignment();
				if(map.size()>0){
					%>
					<b>Datasources</b><br>
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