<%-- 
    Document   : recordsWithHoliday
    Created on : 10-Dec-2017, 18:14:37
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.holidayUsage" var="pageTitle"/>

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText" javaScriptEscape="true"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-8 col-md-offset-2"
					 hasTable="true">

	<jsp:attribute name="javascript">
		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="holidays"]').parent().addClass('active');

				var tbl = $('#records');
				
				var pageLength = 10; //pass undefined to use the default
				var showAllRowsText = "${showAllRowsText}";
				var contextPath = "${pageContext.request.contextPath}";
				var localeCode = "${pageContext.response.locale}";
				var addColumnFilters = undefined; //pass undefined to use the default
				var columnDefs = undefined; //pass undefined to use the default

				//initialize datatable
				initBasicTable(tbl, pageLength, showAllRowsText, contextPath,
						localeCode, addColumnFilters, columnDefs);

			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<c:if test="${error != null}">
			<div class="alert alert-danger alert-dismissable">
				<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
				<p><spring:message code="page.message.errorOccurred"/></p>
				<c:if test="${showErrors}">
					<p><encode:forHtmlContent value="${error}"/></p>
				</c:if>
			</div>
		</c:if>

		<div id="ajaxResponse">
		</div>

		<div class="text-center">
			<p>
				<b><spring:message code="page.text.holiday"/>:</b> ${encode:forHtmlContent(holiday.name)}
			</p>
		</div>

		<table id="records" class="table table-striped table-bordered">
			<thead>
				<tr>
					<th><spring:message code="jobs.text.schedule"/></th>
					<th><spring:message code="jobs.text.job"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="schedule" items="${schedules}">
					<tr>
						<td>${encode:forHtmlContent(schedule.name)}</td>
						<td></td>
					</tr>
				</c:forEach>
				<c:forEach var="job" items="${jobs}">
					<tr>
						<td></td>
						<td>${encode:forHtmlContent(job.name)} (${job.jobId})</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</jsp:body>
</t:mainPageWithPanel>
