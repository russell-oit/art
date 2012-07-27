<%@ page import="java.util.ResourceBundle, art.servlets.ArtDBCP;" %>
<%	
 java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("art.i18n.ArtMessages",request.getLocale());
%>


<table width="90%" border="0" cellspacing="0" cellpadding="0">
 <tr> 
  <td style="text-align: left">
   <div class="small">
    <small> <%=messages.getString("numberOfRows")%> <%=(String) request.getAttribute("numberOfRows")%>
       <br> <%=messages.getString("timeElapsed")%> <%=(String) request.getAttribute("timeElapsed")%>
    </small>
   </div>
  </td>
 </tr>
</table>

<script language="javascript">
 writeStatus("&nbsp;");
</script>	  

</div>

<%@ include file ="footer.jsp" %>
