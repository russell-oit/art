<%@ page import="art.utils.*,art.servlets.*,java.security.MessageDigest,java.util.ResourceBundle" %>
<jsp:useBean id="ue" scope="session" class="art.utils.UserEntity" />
<%@ include file ="header.jsp" %>

<%
//only allow password changing for those users who have been allowed
boolean canChangePassword=ue.isCanChangePassword();
%>


 <div align="center">
  <table>
   <tr>

<%
  // check if the two password matches
  String newPassword1 = request.getParameter("newPassword1");
  String newPassword2 = request.getParameter("newPassword2");
  if (newPassword1 == null || newPassword2 ==null ||
      newPassword1.equals("") || newPassword1.equals(ue.getUsername()) ||
      newPassword1.length() < 6) {
	   %>
	       <td class="title"> <%=messages.getString("passwordChangeFailed")%> </td>
	       </tr>
	       <tr>
	       <td class="attr"> <%=messages.getString("invalidPassword")%> </td>
	   <%
  } else if ( !newPassword1.equals(newPassword2)) {
	   %>
	       <td class="title"> <%=messages.getString("passwordChangeFailed")%> </td>
	       </tr>
	       <tr>
	       <td class="attr"> <%=messages.getString("passwordsDontMatch")%> </td>
	   <%
} else if (!canChangePassword) {
	   %>
	       <td class="title"> <%=messages.getString("passwordChangeFailed")%> </td>
	       </tr>
	       <tr>
	       <td class="attr"> <%=messages.getString("passwordChangeNotAllowed")%> </td>
	   <%
  } else { // change it
     // hash and update password     
    newPassword1 = Encrypter.HashPassword(newPassword1, ArtDBCP.getPasswordHashingAlgorithm());     
	boolean success=ue.updatePassword(newPassword1,ArtDBCP.getPasswordHashingAlgorithm());
	if (success) {	   
	   %>
	        <td class="attr"> <%=messages.getString("passwordChangeSuccess")%> </td>
	   <%
	} else {
	   %>
	       <td class="title"><%=messages.getString("passwordChangeFailed")%> </td>
	       </tr>
	       <tr>
	       <td class="attr"> <%=messages.getString("errorOccurred")%> </td>
	   <%
	}	
  } 
%>
  </tr>
 </table>	   
 </div>
 
<%@ include file ="footer.jsp" %>

