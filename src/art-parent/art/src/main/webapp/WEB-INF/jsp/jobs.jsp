<%-- 
    Document   : jobs
    Created on : 02-Oct-2013, 11:41:32
    Author     : Timothy Anyona

Display job results for the jobs a user has access to
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib  uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<spring:htmlEscape defaultHtmlEscape="true"/>

<spring:message code="page.title.jobs" var="pageTitle" scope="page"/>

<c:set var="now" value="<%=new java.util.Date()%>" />

<t:mainPage title="ART - ${pageTitle}">
	<jsp:attribute name="headContent">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/dataTables_demo_table.css">
	</jsp:attribute>

	<jsp:attribute name="pageJavascript">
		<script>
			var $jQuery = jQuery.noConflict();
		</script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.dataTables.min.js"></script>
		<script type="text/javascript" charset="utf-8">
			var $jQuery = jQuery.noConflict();
			$jQuery(document).ready(function() {
				$jQuery('#jobsTable').dataTable({
					"sPaginationType": "full_numbers",
					"aaSorting": [],
					"aLengthMenu": [[10, 25, -1], [10, 25, "All"]],
					"iDisplayLength": -1
				});
			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<div class="row">
			<div class="col-md-12 text-right">
				<fmt:formatDate value="${now}" pattern="dd-MMM-yyyy HH:mm:ss"/>
				&nbsp;
				<a class="btn btn-default" href="">
					<i class="icon-refresh"></i> <spring:message code="jobs.button.refresh"/>
				</a>
			</div>
		</div>
		<div class="row">
			<table id="jobsTable" class="display">
				<thead>
					<tr>
						<th><spring:message code="jobs.text.jobName"/></th>
						<th><spring:message code="jobs.text.lastEndDate"/></th>
						<th><spring:message code="jobs.text.result"/></th>
						<th><spring:message code="jobs.text.nextRunDate"/></th>
						<th><spring:message code="jobs.text.action"/></th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="job" items="${jobs}">
						<tr>
							<td>${job.jobName}</td>
							<td>
								<fmt:formatDate value="${job.lastEndDate}" pattern="dd-MMM-yyyy HH:mm:ss"/>
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
								<fmt:formatDate value="${job.nextRunDate}" pattern="dd-MMM-yyyy HH:mm:ss"/>
							</td>
							<td>
								<c:if test="${sessionUser.username eq job.username}">
									<div class="btn-group">
										<a class="btn btn-small" href="#">
											<i class="icon-trash"></i> <spring:message code="jobs.button.delete"/>
										</a>
										<a class="btn btn-small" href="#">
											<i class="icon-edit"></i> <spring:message code="jobs.button.edit"/>
										</a>
										<a class="btn btn-small" href="#">
											<i class="icon-bolt"></i> <spring:message code="jobs.button.run"/>
										</a>
									</div>
								</c:if>
							</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</div>
	</jsp:body>
</t:mainPage>

