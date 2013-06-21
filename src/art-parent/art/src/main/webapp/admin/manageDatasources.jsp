<%@ page import="art.utils.*,java.util.*,art.servlets.ArtDBCP" %>
<%@ include file ="headerAdmin.jsp" %>


<script type="text/javascript">
    
    function goToEdit() {
		if(document.manageDatasources.ACTION.value=="ADD"){
			document.manageDatasources.submit();
		} else {
			//modify or delete. an item must be selected
			if (document.manageDatasources.DATASOURCE_ID.selectedIndex>=0) {				
				document.manageDatasources.submit();
			} else {
				alert("Please select a datasource");
			}
		}
    }
    
</script>


<form name="manageDatasources" method="post" action="editDatasource.jsp">
    <table align="center">
        <tr>
			<td class="title" colspan="2"> Manage Datasources </td>
        </tr>
        <tr>
            <td class="data" colspan="2"> <b>Add/Modify/Delete Datasources </b> </td>
        </tr>
        <tr>
            <td class="data"> Datasource </td>
            <td class="data">
                <select name="DATASOURCE_ID" size="10">
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
		
	<p>
		<table align="center" width="90%">	
			<tr>
				<td colspan="3" class="attr">
				Refresh and View Database Connections
				</td>
			</tr>
	
			<tr>
				<td class="data">
					<a href="datasourceStatus.jsp?action=REFRESH">Refresh Connection Pools</a> | <a href="datasourceStatus.jsp?action=FORCEREFRESH">Force Refresh</a>
					<br> Refresh all the database connection pools 
				</td>
				<td class="data">
					<a href="datasourceStatus.jsp?action=STATUS">Show Datasource Status</a>
					<br> See how the pooled connections are being used					
				</td>
				
				<td class="data">
					<%
					if(ArtDBCP.isArtFullVersion()){
					%>
						<a href="clearMondrianCache.jsp">Clear Mondrian Cache</a>		
						<br>Clear the mondrian cache of any pivot table data
						<%
					}
					%>
				</td>
			</tr>
		</table>
	</p>		
           
</form>


<%@ include file ="/user/footer.jsp" %>