<%@ page import="java.util.ResourceBundle, art.servlets.ArtConfig;" %>
<jsp:useBean id="ue" scope="session" class="art.utils.UserEntity" />
<%@ include file ="header.jsp" %>

<%
//only allow password changing for those users who have been allowed
boolean canChangePassword=ue.isCanChangePassword();

if(!canChangePassword){
	//not allowed
	%>
	<p style="text-align:center">
	<%=messages.getString("passwordChangeNotAllowed")%>	
	</p>
	<%
} else {
%>

<div align="center">
    <form name="ChangePassword" method="post" action="execChangePassword.jsp">
        <table>
            <tr>
                <td colspan="2" class="title"> <%=messages.getString("changeArtPassword")%> </td>
            </tr>
            <tr>
                <td class="attr"><%=messages.getString("newPassword")%>:</td>
                <td class="data"><input type="password" name="newPassword1" size="25" maxlength="40"></td>
            </tr>
            <tr>
                <td class="attr"><%=messages.getString("retypeNewPassword")%>:</td>
                <td class="data"><input type="password" name="newPassword2" size="25" maxlength="40"></td>
            </tr>
            <tr>
                <td colspan="2" class="data"  align="center"> <input type="submit" value="<%=messages.getString("changePassword")%>"> </td>
            </tr>
        </table>
    </form>
</div>

<%
}
%>

<%@ include file ="footer.jsp" %>

