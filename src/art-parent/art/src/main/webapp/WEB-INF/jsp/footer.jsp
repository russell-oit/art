<%-- 
    Document   : footer
    Created on : 15-Sep-2013, 09:15:02
    Author     : Timothy Anyona
--%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div>
	<hr style="width:100%;height:1px">

	<div style="float: left; text-align: left">
		<span style="font-size:75%"><a href="http://art.sourceforge.net">ART</a> &nbsp; A Reporting Tool</span>
	</div>

	<div style="float: right; text-align: right">
		<span style="font-size:75%">
			version <%=art.servlets.ArtConfig.getArtVersion()%> <img src="<%=request.getContextPath() + art.servlets.ArtConfig.getArtSetting("bottom_logo")%>" alt="">
			<a href="mailto:<%=art.servlets.ArtConfig.getArtSetting("administrator")%>"><fmt:message key="artSupport"/></a>
		</span>
	</div>
</div>
