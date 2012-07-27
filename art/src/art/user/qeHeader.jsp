<%@ page import="java.util.ResourceBundle, art.servlets.ArtDBCP,art.params.*;" %>
<jsp:useBean id="ue" scope="session" class="art.utils.UserEntity" />
<%@ include file ="header.jsp" %>

<div align="center">

<table width="90%" border="0" cellspacing="0" cellpadding="0">
 <tr>
  <td style="text-align: left">
   <div class="small" id="infoDiv" style="border-right: 0px">
   <%=request.getAttribute("queryName")%>
   </div>
  </td>
  <td>
   <div class="small" id="statusDiv" style="border-left: 0px">
    <c:if test="${empty param._mobile}">
     <%=messages.getString("queryInProgress")%>
    </c:if>
   </div>
  </td>
 </tr>
</table>

