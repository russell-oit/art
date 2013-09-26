<%-- 
    Document   : footer
    Created on : 15-Sep-2013, 09:15:02
    Author     : Timothy Anyona

Footer that appears on all pages
--%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>

<spring:htmlEscape defaultHtmlEscape="true"/>

<div id="pageFooter">
	<div class="navbar navbar-fixed-bottom well" style="padding: 10px; bottom: -20px;">
		<div class="container"> 
			<span class="text-muted credit">
				<a href="http://art.sourceforge.net">ART</a> &nbsp; A Reporting Tool
			</span>
			<span class="text-muted credit pull-right">
				<spring:message code="footer.content.artVersion"/> ${artVersion} <img src="<%=request.getContextPath() + art.servlets.ArtConfig.getBottomLogoPath()%>" alt="">
				<a href="mailto:<%=art.servlets.ArtConfig.getArtSetting("administrator")%>"><spring:message code="artSupport"/></a>
			</span>
		</div>
	</div>
</div>
