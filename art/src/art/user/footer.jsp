<% if (request.getParameter("_isFragment")==null) { %>

<%	
 java.util.ResourceBundle fMessages = java.util.ResourceBundle.getBundle("art.i18n.ArtMessages",request.getLocale());
%>

<p><hr style="width:100%;height:1px">
<div align="right">

<span style="font-size:75%">
 ART Reporting Tool ver. <%=art.servlets.ArtDBCP.getArtVersion()%> <img src="<%=request.getContextPath() + art.servlets.ArtDBCP.getArtSetting("bottom_logo")%>" />
  <i><a href="mailto:<%=art.servlets.ArtDBCP.getArtSetting("administrator")%>"><%=fMessages.getString("artSupport")%></a></i>
</span>
</div>
</p>

 </body>
</html>

<% } %>
