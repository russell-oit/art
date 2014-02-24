<%-- 
    Document   : serverInfo
    Created on : 14-Dec-2013, 18:07:57
    Author     : Timothy Anyona

Display application server information
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<spring:message code="page.title.serverInfo" var="pageTitle"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-6 col-md-offset-3">
	<jsp:attribute name="javascript">
		<script type="text/javascript">
			$(document).ready(function() {
				$(function() {
					$('a[id="configure"]').parent().addClass('active');
					$('a[href*="serverInfo.do"]').parent().addClass('active');
				});
			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<c:if test="${showErrors}">
			<table class="table table-striped table-bordered">
				<tbody>
					<tr>
						<td><spring:message code="serverInfo.text.artHome"/></td>
						<td>${artHome}</td>
					</tr>
					<tr>
						<td><spring:message code="serverInfo.text.serverName"/></td>
						<td>${serverName}</td>
					</tr>
					<tr>
						<td><spring:message code="serverInfo.text.servletApiSupported"/></td>
						<td>${servletApiSupported}</td>
					</tr>
					<tr>
						<td><spring:message code="serverInfo.text.javaVendor"/></td>
						<td>${javaVendor}</td>
					</tr>
					<tr>
						<td><spring:message code="serverInfo.text.javaVersion"/></td>
						<td>${javaVersion}</td>
					</tr>
					<tr>
						<td><spring:message code="serverInfo.text.operatingSystem"/></td>
						<td>${operatingSystem}</td>
					</tr>
				</tbody>
			</table>
		</c:if>
	</jsp:body>
</t:mainPageWithPanel>
