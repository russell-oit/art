<%@ page import="java.util.ResourceBundle, art.servlets.ArtDBCP;" %>
<jsp:useBean id="ue" scope="session" class="art.utils.UserEntity" />

<%
ResourceBundle eMessages = ResourceBundle.getBundle("art.i18n.ArtMessages",request.getLocale());

 String msg       = (String) request.getAttribute("errorMessage");
 String headerOff = (String) request.getAttribute("headerOff");
 
 if(msg==null){
     msg=request.getParameter("MSG");
}
 
 boolean isInline=false;
if(request.getParameter("_isInline")!=null){
	isInline=true;
}

 if (headerOff == null && !isInline) {
%>
<%@ include file ="header.jsp" %>
<%}%>

<table class="centerTableAuto">
    <tr>
        <td colspan="2" class="data" align="center"> <span style="color:red"><b> <%=eMessages.getString("error")%> </b></span>
        </td>
    </tr>
    <tr>
        <td class="attr"> <%=eMessages.getString("message")%>
        </td>
        <td class="data"> <%=msg%>
        </td>
    </tr>
</table>

<%
if(!isInline){ 	%>	
	<%@ include file ="footer.jsp" %>
<% }
%>
