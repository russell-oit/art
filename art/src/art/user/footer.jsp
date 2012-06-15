<% if (request.getParameter("_isFragment")==null) { %>

<%	
 java.util.ResourceBundle messages2 = java.util.ResourceBundle.getBundle("art.i18n.ArtMessages",request.getLocale());
%>

<p><hr width="100%" size="1">
<div align="right">

<span style="font-size:75%">
 ART Reporting Tool ver. <%=art.servlets.ArtDBCP.getArtVersion()%> <img src="<%=request.getContextPath() + art.servlets.ArtDBCP.getArtSetting("bottom_logo")%>" />
  <i><a href="mailto:<%=art.servlets.ArtDBCP.getArtSetting("administrator")%>"><%=messages2.getString("artSupport")%></a></i>
</span>
</div>
</p>

 </body>
</html>

<% } %>
