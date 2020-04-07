<%-- 
    Document   : runningQueries
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<spring:message code="page.title.runningQueries" var="pageTitle"/>

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText" javaScriptEscape="true"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText" javaScriptEscape="true"/>
<spring:message code="page.message.queryCancelled" var="queryCancelledText" javaScriptEscape="true"/>
<spring:message code="page.action.revoke" var="revokeText" javaScriptEscape="true"/>
<spring:message code="dialog.button.cancel" var="cancelText" javaScriptEscape="true"/>
<spring:message code="dialog.button.ok" var="okText" javaScriptEscape="true"/>

<t:mainPageWithPanel title="${pageTitle}" hasTable="true" hasNotify="true">

	<jsp:attribute name="javascript">

		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="runningQueries"]').parent().addClass('active');

				var tbl = $('#queries');

				var pageLength = undefined; //pass undefined to use the default
				var showAllRowsText = "${showAllRowsText}";
				var contextPath = "${pageContext.request.contextPath}";
				var localeCode = "${pageContext.response.locale}";
				var addColumnFilters = undefined; //pass undefined to use the default
				var deleteRecordText = "${cancelText}";
				var okText = "${okText}";
				var cancelText = "${cancelText}";
				var deleteRecordUrl = "${pageContext.request.contextPath}/cancelQuery";
				var recordDeletedText = "${queryCancelledText}";
				var errorOccurredText = "${errorOccurredText}";
				var showErrors = ${showErrors};
				var columnDefs = undefined; //pass undefined to use the default

				//initialize datatable
				var oTable = initBasicTable(tbl, pageLength, showAllRowsText,
						contextPath, localeCode, addColumnFilters, columnDefs);

				var table = oTable.api();

				tbl.find('tbody').on('click', ".deleteRecord", function () {
					var row = $(this).closest("tr"); //jquery object
					var recordName = escapeHtmlContent(row.data("name"));
					var recordId = row.attr("data-id");

					//display confirm dialog
					bootbox.confirm({
						message: deleteRecordText + ": <b>" + recordName + "</b>",
						buttons: {
							confirm: {
								label: okText
							},
							cancel: {
								label: cancelText
							}
						},
						callback: function (result) {
							if (result) {
								//user confirmed delete. make delete request
								$.ajax({
									type: "POST",
									dataType: "json",
									url: deleteRecordUrl,
									data: {runId: recordId},
									success: function (response) {
										if (response.success) {
											table.row(row).remove().draw(false); //draw(false) to prevent datatables from going back to page 1
											notifyActionSuccessReusable(recordDeletedText, recordName);
										} else if (response.errorMessage) {
											notifyActionErrorReusable(errorOccurredText, escapeHtmlContent(response.errorMessage), showErrors);
										}
									},
									error: ajaxErrorHandler
								});
							} //end if result
						} //end callback
					}); //end bootbox confirm
				}); //end on click

				$('#ajaxResponseContainer').on("click", ".alert .close", function () {
					$(this).parent().hide();
				});

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

		<div id="ajaxResponseContainer">
			<div id="ajaxResponse">
			</div>
		</div>

		<table id="queries" class="table table-striped table-bordered table-condensed">
			<thead>
				<tr>
					<th><spring:message code="page.text.report"/></th>
					<th><spring:message code="jobs.text.job"/></th>
					<th><spring:message code="page.text.user"/></th>
					<th><spring:message code="page.text.startTime"/></th>
					<th class="noFilter actionCol"><spring:message code="page.text.action"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="query" items="${queries}">
					<tr data-name="${encode:forHtmlAttribute(query.report.name)} - 
						${encode:forHtmlAttribute(query.job.name)} - 
						${encode:forHtmlAttribute(query.user.username)}"
						data-id="${query.runId}">

						<td>${encode:forHtmlContent(query.report.name)}</td>
						<td>${encode:forHtmlContent(query.job.name)}</td>
						<td>${encode:forHtmlContent(query.user.username)}</td>
						<td data-sort="${query.startTime.time}">
							<fmt:formatDate value="${query.startTime}" pattern="${dateDisplayPattern}"/>
						</td>
						<td>
							<button type="button" class="btn btn-default deleteRecord">
								<spring:message code="dialog.button.cancel"/>
							</button>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</jsp:body>
</t:mainPageWithPanel>
