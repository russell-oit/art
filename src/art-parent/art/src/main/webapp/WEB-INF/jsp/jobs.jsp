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
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/datatables-jowin.css">
	</jsp:attribute>

	<jsp:attribute name="pageJavascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.dataTables-1.9.4.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/datatables-jowin.js"></script>
		<script type="text/javascript" charset="utf-8">
			$(document).ready(function() {
				$('.datatable').dataTable({
					"sPaginationType": "bs_full",
					"aaSorting": [],
					"aLengthMenu": [[5, 10, 25, -1], [5, 10, 25, "All"]],
					"iDisplayLength": -1
				});
				$('.datatable').each(function() {
					var datatable = $(this);
					// SEARCH - Add the placeholder for Search and Turn this into in-line form control
					var search_input = datatable.closest('.dataTables_wrapper').find('div[id$=_filter] input');
					search_input.attr('placeholder', 'Search');
					search_input.addClass('form-control input-sm');
					// LENGTH - Inline-Form control
					var length_sel = datatable.closest('.dataTables_wrapper').find('div[id$=_length] select');
					length_sel.addClass('form-control input-sm');
				});

				$(function() {
					$('a[href*="jobs.do"]').parent().addClass('active');
				});
			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<div class="row">
			<div class="col-md-12 text-center">
				<fmt:formatDate value="${now}" pattern="dd-MMM-yyyy HH:mm:ss"/>
				&nbsp;
				<a class="btn btn-default" href="">
					<i class="icon-refresh"></i> <spring:message code="jobs.button.refresh"/>
				</a>
			</div>
		</div>
		<div class="row">
			<div class="col-md-12">
				<table id="jobsTable" class="datatable table table-bordered table-striped">
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
									<span style="display: none">
										<fmt:formatDate value="${job.lastEndDate}" pattern="yyyy-MM-dd-HH:mm:ss.SSS"/>
									</span>
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
									<span style="display: none">
										<fmt:formatDate value="${job.nextRunDate}" pattern="yyyy-MM-dd-HH:mm:ss.SSS"/>
									</span>
									<fmt:formatDate value="${job.nextRunDate}" pattern="dd-MMM-yyyy HH:mm:ss"/>
								</td>
								<td  style="width: 220px">
									<c:if test="${sessionUser.username eq job.username}">
										<div class="btn-group">
											<a class="btn btn-small" href="#">
												<i class="icon-edit"></i> <spring:message code="jobs.button.edit"/>
											</a>
											<a class="btn btn-small" href="#">
												<i class="icon-trash"></i> <spring:message code="jobs.button.delete"/>
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
		</div>
	</jsp:body>
</t:mainPage>

