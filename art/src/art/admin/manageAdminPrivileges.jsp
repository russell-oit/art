<%@ page import="art.utils.*,java.util.*" %>
<%@ include file ="headerAdmin.jsp" %>


<script type="text/javascript">
    
    function goToEdit() {		
		//ensure minimum number of items are selected
		if (document.manageAdminPrivileges.ADMINS.selectedIndex<0) {
			alert("Please select at least one admin");	
		} else if (document.manageAdminPrivileges.OBJECT_GROUPS.selectedIndex<0 && document.manageAdminPrivileges.DATASOURCES.selectedIndex<0) {
			alert("Please select at least one object group or datasource");
		} else {
			document.manageAdminPrivileges.submit();
		}		
    }
    
</script>


<%
UserEntity ue=new UserEntity();
Iterator it;
%>

<form name="manageAdminPrivileges" method="post" action="execManageAdminPrivileges.jsp">
    <table align="center">
        <tr>
			<td class="title" colspan="2"> Manage Privileges for Mid/Junior Admins </td>
        </tr>
        <tr>
            <td class="data" colspan="2"> Specify Object Groups and Datasources that Mid and Junior Admins can work with </td>
        </tr>
		
		<tr>
            <td class="data"> Admins </td>
            <td class="data">
                <select name="ADMINS" size="5" multiple>
                    <%										
					List<String> usernames=ue.getJuniorAdminUsernames();
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
            <td class="data"> Object Groups </td>
            <td class="data">
                <select name="OBJECT_GROUPS" size="5" multiple>
                    <%
					ObjectGroup og=new ObjectGroup();
					Map groups=og.getAllObjectGroupNames();
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
		
		<tr>
            <td class="data"> Datasources </td>
            <td class="data">
                <select name="DATASOURCES" size="5" multiple>
                    <%					
					Datasource ds=new Datasource();
					Map datasources=ds.getAllDatasourceNames();
					it = datasources.entrySet().iterator();					
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
				Map map;
				map=ue.getJuniorAdminGroupAssignment();
				if(map.size()>0){
					%>
					<b>Groups</b><br>
					<%
					it = map.entrySet().iterator();					
					while(it.hasNext()) {
						Map.Entry entry = (Map.Entry)it.next();
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
					it = map.entrySet().iterator();					
					while(it.hasNext()) {
						Map.Entry entry = (Map.Entry)it.next();
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



<%@ include file ="footer.html" %>