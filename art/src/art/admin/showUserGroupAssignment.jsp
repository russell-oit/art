<%@ page import="art.utils.*,java.util.*" %>
<%@ include file ="headerAdmin.jsp" %>


<%
UserGroup ug=new UserGroup();
Iterator it;
%>

   <table align="center">
        <tr>
	   <td class="title" colspan="2"> Current User Group Assignments </td>
        </tr>
         <tr>            
            <td colspan="2" class="data2">                
				<%				
				Map map=ug.getUserGroupAssignment();
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

<%@ include file ="footer.html" %>