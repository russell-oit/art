<%@ page import="java.util.ResourceBundle, art.servlets.ArtDBCP;" %>
<jsp:useBean id="ue" scope="session" class="art.utils.UserEntity" />
<%
 String msg       = (String) request.getAttribute("errorMessage");
 String headerOff = (String) request.getAttribute("headerOff");
 
 if(msg==null){
        msg=request.getParameter("MSG");
}

 if (headerOff == null) {
%>
<%@ include file ="header.jsp" %>
<%}%>

<table align="center">
    <tr>
        <td colspan="2" class="data" align="center"> <span style="color:red"><b>Error! </b></span>
        </td>
    </tr>
    <tr>
        <td class="attr"> Message:
        </td>
        <td class="data"> <%=msg%>
        </td>
    </tr>
</table>

<%
    java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("art.i18n.ArtMessages",request.getLocale());
%>
<%@ include file ="footer.jsp" %>
