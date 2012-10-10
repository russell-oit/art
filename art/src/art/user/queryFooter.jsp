
<%	
java.util.ResourceBundle qfMessages = java.util.ResourceBundle.getBundle("art.i18n.ArtMessages",request.getLocale());
 
boolean isInline=false;
if(request.getParameter("_isInline")!=null){
	isInline=true;
}
%>


<table width="90%" border="0" cellspacing="0" cellpadding="0">
 <tr> 
  <td style="text-align: left">
   <div class="small">
    <small> <%=qfMessages.getString("numberOfRows")%> <%=(String) request.getAttribute("numberOfRows")%>
       <br> <%=qfMessages.getString("timeElapsed")%> <%=(String) request.getAttribute("timeElapsed")%>
    </small>
   </div>
  </td>
 </tr>
</table>

<script language="javascript">
 writeStatus("&nbsp;");
</script>	  

</div>

<%
if(!isInline){ 	%>	
	<%@ include file ="footer.jsp" %>
<% }
%>
