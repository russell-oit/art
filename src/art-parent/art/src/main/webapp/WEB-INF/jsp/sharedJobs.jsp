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

<spring:htmlEscape defaultHtmlEscape="true"/>

<t:mainPage title="ART - Shared Jobs">
	<jsp:attribute name="pageCss">
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
			<table id="sharedJobsTable" class="display">
				<thead>
					<tr>
						<th><spring:message code="jobs.header.jobId"/></th>
						<th><spring:message code="jobs.header.jobName"/></th>
						<th><spring:message code="jobs.header.lastEndDate"/></th>
						<th><spring:message code="jobs.header.result"/></th>
						<th><spring:message code="jobs.header.nextRunDate"/></th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="job" items="${jobs}">
						<tr>
							<td>${job.jobId}</td>
							<td>${job.jobName}</td>
							<td>${job.lastEndDate}</td>
							<td>${job.lastRunResult}</td>
							<td>${job.nextRunDate}</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</div>
	</jsp:body>
</t:mainPage>
