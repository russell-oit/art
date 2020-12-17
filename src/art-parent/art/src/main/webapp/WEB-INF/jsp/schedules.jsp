<%-- 
    Document   : schedules
    Created on : 01-Apr-2014, 10:47:16
    Author     : Timothy Anyona

Display schedules
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:choose>
	<c:when test="${action == 'unused'}">
		<spring:message code="page.title.unusedSchedules" var="pageTitle"/>
	</c:when>
	<c:otherwise>
		<spring:message code="page.title.schedules" var="pageTitle"/>
	</c:otherwise>
</c:choose>

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
<spring:message code="page.message.linkedRecordsExist" var="linkedRecordsExistText" javaScriptEscape="true"/>

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
				$('a[href*="schedules"]').parent().addClass('active');

				var tbl = $('#schedules');

				var pageLength = undefined; //pass undefined to use the default
				var showAllRowsText = "${showAllRowsText}";
				var contextPath = "${pageContext.request.contextPath}";
				var localeCode = "${pageContext.response.locale}";
				var dataUrl = "${pageContext.request.contextPath}/getSchedules?action=${action}";
						var deleteRecordText = "${deleteRecordText}";
						var okText = "${okText}";
						var cancelText = "${cancelText}";
						var deleteRecordUrl = "${pageContext.request.contextPath}/deleteSchedule";
						var deleteRecordsUrl = "${pageContext.request.contextPath}/deleteSchedules";
						var recordDeletedText = "${recordDeletedText}";
						var recordsDeletedText = "${recordsDeletedText}";
						var errorOccurredText = "${errorOccurredText}";
						var showErrors = ${showErrors};
						var cannotDeleteRecordText = "${cannotDeleteRecordText}";
						var linkedRecordsExistText = "${linkedRecordsExistText}";
						var selectRecordsText = "${selectRecordsText}";
						var someRecordsNotDeletedText = "${someRecordsNotDeletedText}";
						var exportRecordsUrl = "${pageContext.request.contextPath}/exportRecords?type=Schedules";
						var columnDefs = undefined

						var columns = [
							{"data": null, defaultContent: ""},
							{"data": "scheduleId"},
							{"data": "name2"},
							{"data": "description2"},
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
										filter_default_label: ""
									},
									{
										column_number: 3,
										filter_type: 'text',
										filter_default_label: ""
									}
								]
								);

						$("#refreshRecords").on("click", function () {
							table.ajax.reload();
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

		<div style="margin-bottom: 10px;">
			<div class="btn-group">
				<a class="btn btn-default" href="${pageContext.request.contextPath}/addSchedule">
					<i class="fa fa-plus"></i>
					<spring:message code="page.action.add"/>
				</a>
				<button type="button" id="deleteRecords" class="btn btn-default">
					<i class="fa fa-trash-o"></i>
					<spring:message code="page.action.delete"/>
				</button>
				<button type="button" id="refreshRecords" class="btn btn-default">
					<i class="fa fa-refresh"></i>
					<spring:message code="page.action.refresh"/>
				</button>
			</div>
			<c:if test="${action == 'all'}">
				<a class="btn btn-default" href="${pageContext.request.contextPath}/unusedSchedules">
					<spring:message code="page.text.unused"/>
				</a>
				<c:if test="${sessionUser.hasPermission('migrate_records')}">
					<div class="btn-group">
						<a class="btn btn-default" href="${pageContext.request.contextPath}/importRecords?type=Schedules">
							<spring:message code="page.text.import"/>
						</a>
						<button type="button" id="exportRecords" class="btn btn-default">
							<spring:message code="page.text.export"/>
						</button>
					</div>
				</c:if>
			</c:if>
		</div>

		<table id="schedules" class="table table-bordered table-striped table-condensed">
			<thead>
				<tr>
					<th class="noFilter selectCol"></th>
					<th><spring:message code="page.text.id"/><p></p></th>
					<th><spring:message code="page.text.name"/><p></p></th>
					<th><spring:message code="page.text.description"/><p></p></th>
					<th class="noFilter actionCol"><spring:message code="page.text.action"/><p></p></th>
				</tr>
			</thead>
		</table>
	</jsp:body>
</t:mainPageWithPanel>
