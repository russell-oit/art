<% if (request.getParameter("_isFragment")==null) { %>

<%	
 java.util.ResourceBundle fMessages = java.util.ResourceBundle.getBundle("i18n.ArtMessages",request.getLocale());
%>

<p>
<hr style="width:100%;height:1px">

<div style="float: left; text-align: left">
	<span style="font-size:75%"><a href="http://art.sourceforge.net">ART</a> &nbsp; A Reporting Tool</span>
</div>

<div style="float: right; text-align: right">
	<span style="font-size:75%">
	 version ${applicationScope.artVersion} <img src="<%=request.getContextPath() + art.servlets.ArtConfig.getArtSetting("bottom_logo")%>" alt="">
	  <a href="mailto:<%=art.servlets.ArtConfig.getArtSetting("administrator_email")%>"><%=fMessages.getString("footer.link.artAdministrator")%></a>
	</span>
</div>
</p>

 </body>
</html>

<% } %>
