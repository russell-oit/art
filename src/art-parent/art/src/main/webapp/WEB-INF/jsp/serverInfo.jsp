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

<spring:message code="page.title.serverInfo" var="pageTitle" scope="page"/>

<t:configurationPage title="${pageTitle}">
	<jsp:body>
		<div class="row">
			<div class="col-md-6 col-md-offset-3">
				<div class="panel panel-success">
					<div class="panel-heading">
						<h4 class="panel-title text-center">${fn:escapeXml(pageTitle)}</h4>
					</div>
					<div class="panel-body">
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
					</div>
				</div>
			</div>
		</div>
	</jsp:body>
</t:configurationPage>
