<%@ page import="art.utils.*,java.util.*" %>
<%@ include file ="headerAdmin.jsp" %>


<%
UserGroup ug=new UserGroup();
%>

   <table align="center">
        <tr>
	   <td class="title" colspan="2"> Current User Group Assignments </td>
        </tr>
         <tr>            
            <td colspan="2" class="data2">                
				<%				
				Map<Integer, String> map=ug.getUserGroupAssignment();
				for (Map.Entry<Integer, String> entry : map.entrySet()) {
					%>
					<%=entry.getValue()%> <br>
					<%
				} 
				%>                
            </td>
        </tr>
		        
    </table>    

<%@ include file ="/user/footer.jsp" %>