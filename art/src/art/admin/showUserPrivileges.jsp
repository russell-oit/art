<%@ page import="art.utils.*,java.util.*" %>
<%@ include file ="headerAdmin.jsp" %>


<%
UserEntity ue=new UserEntity();
Iterator it;
%>

    <table align="center">
        <tr>
			<td class="title" colspan="2">Current User/User Group Privileges</td>
        </tr>
	<tr>            
            <td colspan="2" class="data2">                
				<%				
				Map map;
				map=ue.getObjectGroupAssignment();
				if(map.size()>0){
					%>
					<b>Object Groups</b><br>
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
				map=ue.getObjectAssignment();
				if(map.size()>0){
					%>
					<b>Objects</b><br>
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
    
<%@ include file ="footer.html" %>