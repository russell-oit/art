<%-- 
    Document   : reportsConfig
    Created on : 25-Feb-2014, 10:46:51
    Author     : Timothy Anyona

Reports configuration page
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<spring:message code="page.title.reportsConfiguration" var="pageTitle"/>

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText" javaScriptEscape="true"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText" javaScriptEscape="true"/>
<spring:message code="dialog.button.cancel" var="cancelText" javaScriptEscape="true"/>
<spring:message code="dialog.button.ok" var="okText" javaScriptEscape="true"/>
<spring:message code="dialog.message.deleteRecord" var="deleteRecordText" javaScriptEscape="true"/>
<spring:message code="page.message.recordDeleted" var="recordDeletedText" javaScriptEscape="true"/>
<spring:message code="page.message.recordsDeleted" var="recordsDeletedText" javaScriptEscape="true"/>
<spring:message code="dialog.message.selectRecords" var="selectRecordsText" javaScriptEscape="true"/>
<spring:message code="page.message.someRecordsNotDeleted" var="someRecordsNotDeletedText" javaScriptEscape="true"/>
<spring:message code="page.message.cannotDeleteRecord" var="cannotDeleteRecordText" javaScriptEscape="true"/>
<spring:message code="reports.message.linkedJobsExist" var="linkedJobsExistText" javaScriptEscape="true"/>
<spring:message code="reports.text.selectValue" var="selectValueText" javaScriptEscape="true"/>

<t:mainPageWithPanel title="${pageTitle}" configPage="true">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/yadcf-0.9.3/jquery.dataTables.yadcf.css"/>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/yadcf.css"/>
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/yadcf-0.9.3/jquery.dataTables.yadcf.js"></script>

		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="reportsConfig"]').parent().addClass('active');

				var tbl = $('#reports');

				var pageLength = 10; //pass undefined to use the default
				var showAllRowsText = "${showAllRowsText}";
				var contextPath = "${pageContext.request.contextPath}";
				var localeCode = "${pageContext.response.locale}";
				var dataUrl = "${pageContext.request.contextPath}/getConfigReports";
				var deleteRecordText = "${deleteRecordText}";
				var okText = "${okText}";
				var cancelText = "${cancelText}";
				var deleteRecordUrl = "${pageContext.request.contextPath}/deleteReport";
				var deleteRecordsUrl = "${pageContext.request.contextPath}/deleteReports";
				var recordDeletedText = "${recordDeletedText}";
				var recordsDeletedText = "${recordsDeletedText}";
				var errorOccurredText = "${errorOccurredText}";
				var showErrors = ${showErrors};
				var cannotDeleteRecordText = "${cannotDeleteRecordText}";
				var linkedRecordsExistText = "${linkedJobsExistText}";
				var selectRecordsText = "${selectRecordsText}";
				var someRecordsNotDeletedText = "${someRecordsNotDeletedText}";
				var editRecordsUrl = "${pageContext.request.contextPath}/editReports";
				var exportRecordsUrl = "${pageContext.request.contextPath}/exportRecords?type=Reports";
				var columnDefs = undefined;

				var columns = [
					{"data": null, defaultContent: ""},
					{"data": "reportId"},
					{"data": "name2"},
					{"data": {
							_: "reportGroupNames2",
							filter: "reportGroupNamesFilter"
						}
					},
					{"data": "description2"},
					{"data": "dtActiveStatus"},
					{"data": "dtAction", width: '370px'}
				];

				//initialize datatable
				var oTable = initAjaxConfigTable(tbl, pageLength, showAllRowsText,
						contextPath, localeCode, dataUrl, errorOccurredText,
						showErrors, columnDefs, columns);

				var table = oTable.api();

				addDeleteRecordHandler(tbl, table, deleteRecordText, okText,
						cancelText, deleteRecordUrl, recordDeletedText,
						errorOccurredText, showErrors, cannotDeleteRecordText,
						linkedRecordsExistText);

				addDeleteRecordsHandler(table, deleteRecordText, okText,
						cancelText, deleteRecordsUrl, recordsDeletedText,
						errorOccurredText, showErrors, selectRecordsText,
						someRecordsNotDeletedText);

				addEditRecordsHandler(table, editRecordsUrl, selectRecordsText);
				
				addExportRecordsHandler(table, exportRecordsUrl, selectRecordsText);

				yadcf.init(table,
						[
							{
								column_number: 1,
								filter_type: 'text',
								filter_default_label: "",
								style_class: "yadcf-id-filter"
							},
							{
								column_number: 2,
								filter_type: 'text',
								filter_default_label: "",
								style_class: "yadcf-report-name-filter"
							},
							{
								column_number: 3,
								filter_default_label: '${selectValueText}',
								text_data_delimiter: ", ",
								case_insensitive: false,
								sort_as: "alphaNum",
								filter_type: 'custom_func',
								custom_func: filterReportGroups
							},
							{
								column_number: 4,
								filter_type: 'text',
								filter_default_label: "",
								style_class: "yadcf-report-description-filter"
							},
							{
								column_number: 5,
								filter_default_label: '${selectValueText}',
								column_data_type: "html",
								html_data_type: "text"
							}
						]
						);

				$("#refreshRecords").on("click", function () {
					table.ajax.reload();
				});

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

		<div style="margin-bottom: 10px;">
			<div class="btn-group">
				<a class="btn btn-default" href="${pageContext.request.contextPath}/addReport">
					<i class="fa fa-plus"></i>
					<spring:message code="page.action.add"/>
				</a>
				<button type="button" id="editRecords" class="btn btn-default">
					<i class="fa fa-pencil-square-o"></i>
					<spring:message code="page.action.edit"/>
				</button>
				<button type="button" id="deleteRecords" class="btn btn-default">
					<i class="fa fa-trash-o"></i>
					<spring:message code="page.action.delete"/>
				</button>
				<button type="button" id="refreshRecords" class="btn btn-default">
					<i class="fa fa-refresh"></i>
					<spring:message code="page.action.refresh"/>
				</button>
			</div>
			<c:if test="${sessionUser.hasPermission('migrate_records')}">
				<div class="btn-group">
					<a class="btn btn-default" href="${pageContext.request.contextPath}/importRecords?type=Reports">
						<spring:message code="page.text.import"/>
					</a>
					<button type="button" id="exportRecords" class="btn btn-default">
						<spring:message code="page.text.export"/>
					</button>
				</div>
			</c:if>
		</div>

		<div class="table-responsive">
			<table id="reports" class="table table-bordered table-striped table-condensed" style='width: 100%'>
				<thead>
					<tr>
						<th class="noFilter selectCol"></th>
						<th><spring:message code="page.text.id"/><p></p></th>
						<th><spring:message code="page.text.name"/><p></p></th>
						<th><spring:message code="reports.text.groupName"/><p></p></th>
						<th><spring:message code="page.text.description"/><p></p></th>
						<th><spring:message code="page.text.active"/><p></p></th>
						<th class="noFilter actionCol"><spring:message code="page.text.action"/><p></p></th>
					</tr>
				</thead>
			</table>
		</div>
	</jsp:body>
</t:mainPageWithPanel>
