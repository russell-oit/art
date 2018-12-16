<%-- 
    Document   : reportConfig
    Created on : 13-Jun-2018, 14:01:14
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<spring:message code="page.title.reportConfiguration" var="panelTitle"/>
<c:set var="pageTitle">
	${panelTitle} - ${report.name}
</c:set>

<t:mainPage title="${pageTitle}">

	<jsp:attribute name="javascript">

		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="reportsConfig"]').parent().addClass('active');
			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<div class='row'>
			<div class="col-md-12">
				<div class="panel panel-success">
					<div class="panel-heading">
						<h4 class="panel-title text-center">
							${panelTitle}
						</h4>
					</div>
				</div>
			</div>
		</div>
		<div class='row'>
			<div class='col-md-12'>
				<c:if test="${error != null}">
					<div class="alert alert-danger alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
						<p><spring:message code="page.message.errorOccurred"/></p>
						<c:if test="${showErrors}">
							<p>${encode:forHtmlContent(error)}</p>
						</c:if>
					</div>
				</c:if>
				<div class="alert alert-success">
					<c:if test="${not empty recordSavedMessage}"><spring:message code="${recordSavedMessage}"/>: </c:if>
					${encode:forHtmlContent(report.name)} (${report.reportId})
					&nbsp;
					<div class="btn-group">
						<a class="btn btn-default" 
						   href="${pageContext.request.contextPath}/editReport?id=${report.reportId}">
							<spring:message code="page.action.edit"/>
						</a>
						<a class="btn btn-default" 
						   href="${pageContext.request.contextPath}/copyReport?id=${report.reportId}">
							<spring:message code="page.action.copy"/>
						</a>
					</div>
					<div class="btn-group">
						<a class="btn btn-default"
						   href="${pageContext.request.contextPath}/reportParameterConfig?reportId=${report.reportId}">
							<spring:message code="reports.action.parameters"/>
						</a>
						<a class="btn btn-default"
						   href="${pageContext.request.contextPath}/reportRules?reportId=${report.reportId}">
							<spring:message code="reports.action.rules"/>
						</a>
						<a class="btn btn-default"
						   href="${pageContext.request.contextPath}/drilldowns?reportId=${report.reportId}">
							<spring:message code="reports.action.drilldowns"/>
						</a>
					</div>
					<c:if test="${report.reportType.canSchedule() && sessionUser.hasPermission('schedule_jobs')}">
						<div class="btn-group">
							<a class="btn btn-default"
							   href="${pageContext.request.contextPath}/addJob?reportId=${report.reportId}&nextPage=jobsConfig">
								<spring:message code="reports.action.schedule"/>
							</a>
						</div>
					</c:if>
					<c:if test="${report.reportType != 'LovStatic'}">
						<div class="btn-group">
							<a class="btn btn-default"
							   href="${pageContext.request.contextPath}/selectReportParameters?reportId=${report.reportId}">
								<spring:message code="reports.action.preview"/>
							</a>
						</div>
					</c:if>
					<span class="pull-right">
						<a class="btn btn-default"
						   href="${pageContext.request.contextPath}/reportsConfig">
							<spring:message code="page.text.reports"/>
						</a>
						<a class="btn btn-default"
						   href="${pageContext.request.contextPath}/addReport">
							<spring:message code="page.action.add"/>
						</a>
					</span>
				</div>
			</div>
		</div>
	</jsp:body>
</t:mainPage>
