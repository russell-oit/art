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

<spring:message code="page.title.jobs" var="pageTitle" scope="page"/>

<t:mainPage title="${pageTitle}">
	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/datatables-jowin.css">
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.dataTables-1.9.4.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/datatables-jowin.js"></script>
		<script type="text/javascript" charset="utf-8">
			$(document).ready(function() {
				$('.datatable').dataTable({
					"sPaginationType": "bs_full",
					"aaSorting": [],
					"aLengthMenu": [[5, 10, 25, -1], [5, 10, 25, "All"]],
					"iDisplayLength": 10,
					"oLanguage": {
						"sUrl": "${pageContext.request.contextPath}/dataTables/dataTables_${pageContext.response.locale}.txt"
					}
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

				$(function() {
					$("a[data-toggle='tooltip']").tooltip({container: 'body'});
				});
			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<div>
			<table class="datatable table table-bordered table-striped table-condensed">
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
							<td>
								<c:if test="${sessionUser.username eq job.username}">
									<a href="#" data-toggle="tooltip" title="<spring:message code="jobs.action.edit"/>">
										<i class="fa fa-pencil-square-o"></i>
									</a>
									<a href="#" data-toggle="tooltip" title="<spring:message code="jobs.action.delete"/>">
										<i class="fa fa-trash-o"></i>
									</a>
									<a href="#" data-toggle="tooltip" title="<spring:message code="jobs.action.run"/>">
										<i class="fa fa-bolt"></i>
									</a>
								</c:if>
							</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</div>
	</jsp:body>
</t:mainPage>

