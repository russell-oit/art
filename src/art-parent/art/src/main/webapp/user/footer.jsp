<% if (request.getParameter("_isFragment")==null) { %>

<%	
 java.util.ResourceBundle fMessages = java.util.ResourceBundle.getBundle("art.i18n.ArtMessages",request.getLocale());
%>

<p>
<hr style="width:100%;height:1px">

<div style="float: left; text-align: left">
	<span style="font-size:75%"><a href="http://art.sourceforge.net">ART</a> &nbsp; A Reporting Tool</span>
</div>

<div style="float: right; text-align: right">
	<span style="font-size:75%">
	 version <%=art.servlets.ArtDBCP.getArtVersion()%> <img src="<%=request.getContextPath() + art.servlets.ArtDBCP.getArtSetting("bottom_logo")%>" alt="">
	  <a href="mailto:<%=art.servlets.ArtDBCP.getArtSetting("administrator")%>"><%=fMessages.getString("artSupport")%></a>
	</span>
</div>
</p>

 </body>
</html>

<% } %>
