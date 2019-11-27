<%-- 
    Document   : reportRules
    Created on : 25-Apr-2014, 17:17:20
    Author     : Timothy Anyona

Display report filters
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.reportRules" var="pageTitle"/>

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText" javaScriptEscape="true"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText" javaScriptEscape="true"/>
<spring:message code="dialog.button.cancel" var="cancelText" javaScriptEscape="true"/>
<spring:message code="dialog.button.ok" var="okText" javaScriptEscape="true"/>
<spring:message code="dialog.message.deleteRecord" var="deleteRecordText" javaScriptEscape="true"/>
<spring:message code="page.message.recordDeleted" var="recordDeletedText" javaScriptEscape="true"/>
<spring:message code="page.message.recordsDeleted" var="recordsDeletedText" javaScriptEscape="true"/>
<spring:message code="dialog.message.selectRecords" var="selectRecordsText" javaScriptEscape="true"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-8 col-md-offset-2"
					 hasTable="true" hasNotify="true">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/Select-1.2.6/css/select.bootstrap.min.css"/>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/Buttons-1.5.4/css/buttons.dataTables.min.css"/>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/Buttons-1.5.4/css/buttons.bootstrap.min.css"/>
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Select-1.2.6/js/dataTables.select.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Buttons-1.5.4/js/dataTables.buttons.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Buttons-1.5.4/js/buttons.bootstrap.min.js"></script>

		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="reportsConfig"]').parent().addClass('active');

				//{container: 'body'} needed if tooltips shown on input-group element or button
				$("[data-toggle='tooltip']").tooltip({container: 'body'});

				var tbl = $('#reportRules');
				
				var pageLength = 10; //pass undefined to use the default
				var showAllRowsText = "${showAllRowsText}";
				var contextPath = "${pageContext.request.contextPath}";
				var localeCode = "${pageContext.response.locale}";
				var addColumnFilters = false; //pass undefined to use the default
				var deleteRecordText = "${deleteRecordText}";
				var okText = "${okText}";
				var cancelText = "${cancelText}";
				var deleteRecordUrl = "${pageContext.request.contextPath}/deleteReportRule";
				var deleteRecordsUrl = "${pageContext.request.contextPath}/deleteReportRules";
				var recordDeletedText = "${recordDeletedText}";
				var recordsDeletedText = "${recordsDeletedText}";
				var errorOccurredText = "${errorOccurredText}";
				var showErrors = ${showErrors};
				var cannotDeleteRecordText = undefined;
				var linkedRecordsExistText = undefined;
				var selectRecordsText = "${selectRecordsText}";
				var someRecordsNotDeletedText = undefined;
				var columnDefs = undefined; //pass undefined to use the default

				//initialize datatable
				var oTable = initConfigTable(tbl, pageLength,
						showAllRowsText, contextPath, localeCode,
						addColumnFilters, columnDefs);

				var table = oTable.api();
				
				addDeleteRecordsHandler(table, deleteRecordText, okText,
						cancelText, deleteRecordsUrl, recordsDeletedText,
						errorOccurredText, showErrors, selectRecordsText,
						someRecordsNotDeletedText);

				tbl.find('tbody').on('click', '.deleteRecord', function () {
					var row = $(this).closest("tr"); //jquery object
					var recordName = escapeHtmlContent(row.attr("data-name"));
					var recordId = row.data("id");
					bootbox.confirm({
						message: "${deleteRecordText}: <b>" + recordName + "</b>",
						buttons: {
							cancel: {
								label: "${cancelText}"
							},
							confirm: {
								label: "${okText}"
							}
						},
						callback: function (result) {
							if (result) {
								//user confirmed delete. make delete request
								$.ajax({
									type: "POST",
									dataType: "json",
									url: "${pageContext.request.contextPath}/deleteReportRule",
									data: {id: recordId},
									success: function (response) {
										if (response.success) {
											table.row(row).remove().draw(false); //draw(false) to prevent datatables from going back to page 1
											notifyActionSuccessReusable("${recordDeletedText}", recordName);
										} else {
											notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
										}
									},
									error: ajaxErrorHandler
								});
							} //end if result
						} //end callback
					}); //end bootbox confirm
				});

				$('#ajaxResponseContainer').on("click", ".alert .close", function () {
					$(this).parent().hide();
				});

			}); //end document ready
		</script>
	</jsp:attribute>

	<jsp:body>
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

		<div id="ajaxResponseContainer">
			<div id="ajaxResponse">
			</div>
		</div>

		<div class="text-center">
			<p>
				<b><spring:message code="page.text.report"/>:</b> ${encode:forHtmlContent(reportName)}
			</p>
		</div>
		<div style="margin-bottom: 10px;">
			<a class="btn btn-default" href="${pageContext.request.contextPath}/addRule?reportId=${reportId}">
				<i class="fa fa-plus"></i>
				<spring:message code="page.button.addNew"/>
			</a>
			<a class="btn btn-default" href="${pageContext.request.contextPath}/addReportRule?reportId=${reportId}">
				<i class="fa fa-plus"></i>
				<spring:message code="page.button.addExisting"/>
			</a>
			<button type="button" id="deleteRecords" class="btn btn-default">
				<i class="fa fa-trash-o"></i>
				<spring:message code="page.action.delete"/>
			</button>
			<a class="btn btn-default" href="${pageContext.request.contextPath}/reportConfig?reportId=${reportId}">
				<spring:message code="page.text.report"/>
			</a>
		</div>

		<table id="reportRules" class="table table-bordered table-striped table-condensed">
			<thead>
				<tr>
					<th class="noFilter selectCol"></th>
					<th><spring:message code="page.text.id"/></th>
					<th><spring:message code="page.text.rule"/></th>
					<th><spring:message code="page.text.reportColumn"/></th>
					<th class="noFilter actionCol"><spring:message code="page.text.action"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="reportRule" items="${reportRules}">
					<tr data-id="${reportRule.reportRuleId}" 
						data-name="${encode:forHtmlAttribute(reportRule.rule.name)}">

						<td></td>
						<td>${reportRule.reportRuleId}</td>
						<td>
							<a href="${pageContext.request.contextPath}/editRule?id=${reportRule.rule.ruleId}&returnReportId=${reportRule.reportId}">
								${encode:forHtmlContent(reportRule.rule.name)}
							</a>
						</td>							
						<td>${encode:forHtmlContent(reportRule.reportColumn)}</td>
						<td>
							<div class="btn-group">
								<a class="btn btn-default" 
								   href="${pageContext.request.contextPath}/editReportRule?id=${reportRule.reportRuleId}">
									<i class="fa fa-pencil-square-o"></i>
									<spring:message code="page.action.edit"/>
								</a>
								<button type="button" class="btn btn-default deleteRecord">
									<i class="fa fa-trash-o"></i>
									<spring:message code="page.action.delete"/>
								</button>
							</div>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</jsp:body>
</t:mainPageWithPanel>
