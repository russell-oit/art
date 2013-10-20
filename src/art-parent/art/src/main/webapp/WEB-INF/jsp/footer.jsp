<%-- 
    Document   : footer
    Created on : 15-Sep-2013, 09:15:02
    Author     : Timothy Anyona

Footer that appears on all pages
--%>

<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<spring:htmlEscape defaultHtmlEscape="true"/>

<c:set var="administratorEmail" 
	   value="<%= art.servlets.ArtConfig.getArtSetting("administrator_email")%>"/>

<div id="footer">
	<div class="container"> 
		<span class="navbar-text text-muted">
			<a href="http://art.sourceforge.net">ART</a>
			&nbsp; A Reporting Tool 
			&nbsp; version ${artVersion}
		</span>
		<span class="navbar-text pull-right">
			<a href="mailto:${administratorEmail}">
				<spring:message code="footer.link.artSupport"/>
			</a>
		</span>
	</div>
</div>
