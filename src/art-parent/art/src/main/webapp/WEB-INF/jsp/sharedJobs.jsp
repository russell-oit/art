<%-- 
    Document   : sharedJobs
    Created on : 30-Sep-2013, 11:15:57
    Author     : Timothy Anyona

Display shared jobs
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib  uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<spring:htmlEscape defaultHtmlEscape="true"/>

<spring:message code="page.title.sharedJobs" var="pageTitle" scope="page"/>

<c:set var="now" value="<%=new java.util.Date()%>" />

<t:mainPage title="ART - ${pageTitle}">
	<jsp:attribute name="headContent">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/dataTables_demo_table.css">
	</jsp:attribute>

	<jsp:attribute name="pageJavascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.dataTables.min.js"></script>
		<script type="text/javascript" charset="utf-8">
			var $jQuery = jQuery.noConflict();
			$jQuery(document).ready(function() {
				$jQuery('#sharedJobsTable').dataTable({
					"sPaginationType": "full_numbers",
					"aaSorting": [],
					"aLengthMenu": [[10, 25, -1], [10, 25, "All"]],
					"iDisplayLength": 10
				});
			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<div class="col-lg-10 col-lg-offset-1">
			<div class="row">
				<span class="pull-left">
					<fmt:formatDate value="${now}" type="both" dateStyle="medium" timeStyle="medium"/>
				</span>
				<span class="pull-right">
					<a class="btn btn-default" href="">
						<i class="icon-refresh"></i> <spring:message code="jobs.button.refresh"/>
					</a>
				</span>
			</div>
			<div class="row">
				<table id="sharedJobsTable" class="display">
					<thead>
						<tr>
							<th><spring:message code="jobs.text.jobId"/></th>
							<th><spring:message code="jobs.text.jobName"/></th>
							<th><spring:message code="jobs.text.lastEndDate"/></th>
							<th><spring:message code="jobs.text.result"/></th>
							<th><spring:message code="jobs.text.nextRunDate"/></th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="job" items="${jobs}">
							<tr>
								<td>${job.jobId}</td>
								<td>${job.jobName}</td>
								<td>
									<fmt:formatDate value="${job.lastEndDate}" type="both" 
													dateStyle="medium" timeStyle="medium"/>
								</td>
								<td>
									<a type="application/octet-stream" 
									   href="${pageContext.request.contextPath}/export/jobs/${job.lastFileName}" 
									   target="_blank">
										${job.lastFileName}
									</a>
									<br>
									${job.lastRunDetails}
								</td>
								<td>
									<fmt:formatDate value="${job.nextRunDate}" type="both" 
													dateStyle="medium" timeStyle="medium"/>
								</td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
			</div>
		</div>
	</jsp:body>
</t:mainPage>
