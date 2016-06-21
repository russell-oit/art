<%-- 
    Document   : archives
    Created on : 27-Mar-2016, 03:37:17
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<spring:message code="page.title.archives" var="pageTitle"/>

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-10 col-md-offset-1">

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/notify-combined-0.3.1.min.js"></script>
		<script type="text/javascript" charset="utf-8">
			$(document).ready(function () {
				$(function () {
					$('a[href*="archives.do"]').parent().addClass('active');
				});

				var tbl = $("#archives");

				var columnFilterRow = createColumnFilters(tbl);

				var oTable = tbl.dataTable({
					orderClasses: false,
					pagingType: "full_numbers",
					lengthMenu: [[5, 10, 25, -1], [5, 10, 25, "${showAllRowsText}"]],
					pageLength: 10,
					language: {
						url: "${pageContext.request.contextPath}/js/dataTables-1.10.11/i18n/dataTables_${pageContext.response.locale}.txt"
					},
					initComplete: datatablesInitComplete
				});

				//move column filter row after heading row
				columnFilterRow.insertAfter(columnFilterRow.next());

				//get datatables api object
				var table = oTable.api();

				// Apply the column filter
				applyColumnFilters(tbl, table);

			}); //end document ready
		</script>

	</jsp:attribute>

	<jsp:body>
		<c:if test="${not empty message}">
			<div class="alert alert-success alert-dismissable">
				<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
				<spring:message code="${message}"/>
			</div>
		</c:if>
		<c:if test="${error != null}">
			<div class="alert alert-danger alert-dismissable">
				<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
				<p><spring:message code="page.message.errorOccurred"/></p>
				<c:if test="${showErrors}">
					<p>${encode:forHtmlContent(error)}</p>
				</c:if>
			</div>
		</c:if>
		<c:if test="${not empty recordSavedMessage}">
			<div class="alert alert-success alert-dismissable">
				<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
				<spring:message code="${recordSavedMessage}"/>: ${encode:forHtmlContent(recordName)}
			</div>
		</c:if>

		<div id="ajaxResponse">
		</div>

		<table id="archives" class="table table-bordered table-striped table-condensed">
			<thead>
				<tr>
					<th class="jobIdCol"><spring:message code="jobs.text.jobId"/></th>
					<th class="jobNameCol"><spring:message code="jobs.text.jobName"/></th>
					<th><spring:message code="logs.text.date"/></th>
					<th><spring:message code="jobs.text.result"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="archive" items="${archives}">
					<tr data-id="${archive.archiveId}" 
						data-name="${encode:forHtmlAttribute(archive.archiveId)}">

						<td>${archive.job.jobId}</td>
						<td>${encode:forHtmlContent(archive.job.name)}</td>
						<td data-sort="${archive.endDate.time}">
							<fmt:formatDate value="${archive.endDate}" pattern="${dateDisplayPattern}"/>
						</td>
						<td>
							<c:if test="${not empty archive.fileName}">
								<a type="application/octet-stream" 
								   href="${pageContext.request.contextPath}/export/jobs/${archive.fileName}">
									${archive.fileName}
								</a>
								<br>
							</c:if>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</jsp:body>
</t:mainPageWithPanel>


