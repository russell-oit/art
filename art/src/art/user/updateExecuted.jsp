<%@ page contentType="text/html; charset=UTF-8" %>
<%  request.setCharacterEncoding("UTF-8"); %>

<%	
 java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("art.i18n.ArtMessages",request.getLocale());
%>
<script language="javascript">
 writeStatus(<%=messages.getString("updateExecuted")%>);
</script>

<br> <%=messages.getString("updatedRows")%> <%=(String) request.getAttribute("rowsUpdated")%>

<br><small><%=messages.getString("updateWarning")%></small><br>

<small>
 <div align="center">
  <input type="button" value="<%=messages.getString("backButton")%>" onClick="javascript:history.back()">
</div>
</small>
 
