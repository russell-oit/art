<%-- 
    Document   : reportParameterConfig
    Created on : 23-Mar-2016, 10:37:10
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.reportParameters" var="pageTitle"/>

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText" javaScriptEscape="true"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText" javaScriptEscape="true"/>
<spring:message code="dialog.button.cancel" var="cancelText" javaScriptEscape="true"/>
<spring:message code="dialog.button.ok" var="okText" javaScriptEscape="true"/>
<spring:message code="dialog.message.deleteRecord" var="deleteRecordText" javaScriptEscape="true"/>
<spring:message code="page.message.recordDeleted" var="recordDeletedText" javaScriptEscape="true"/>
<spring:message code="page.message.recordMoved" var="recordMovedText" javaScriptEscape="true"/>
<spring:message code="page.help.dragToReorder" var="dragToReorderText"/>
<spring:message code="page.message.recordsDeleted" var="recordsDeletedText" javaScriptEscape="true"/>
<spring:message code="dialog.message.selectRecords" var="selectRecordsText" javaScriptEscape="true"/>


<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-10 col-md-offset-1" configPage="true">

	<jsp:attribute name="javascript">
		<script type="text/javascript">
			//enable use of bootstrap tooltips. both jquery ui and bootstrap define the tooltip function
			$.fn.bsTooltip = $.fn.tooltip.noConflict();
		</script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-ui-1.11.4-all-smoothness/jquery-ui.min.js"></script>

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/datatables-row-reordering-1.2.3/jquery.dataTables.rowReordering.min.js"></script>

		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="reportsConfig"]').parent().addClass('active');

				//{container: 'body'} needed if tooltips shown on input-group element or button
				$("[data-toggle='tooltip']").bsTooltip({container: 'body'});

				var tbl = $('#reportParameters');

				var pageLength = undefined; //pass undefined to use the default
				var showAllRowsText = "${showAllRowsText}";
				var contextPath = "${pageContext.request.contextPath}";
				var localeCode = "${pageContext.response.locale}";
				var addColumnFilters = undefined; //pass undefined to use the default
				var deleteRecordText = "${deleteRecordText}";
				var okText = "${okText}";
				var cancelText = "${cancelText}";
				var deleteRecordUrl = "${pageContext.request.contextPath}/deleteReportParameter";
				var deleteRecordsUrl = "${pageContext.request.contextPath}/deleteReportParameters";
				var recordDeletedText = "${recordDeletedText}";
				var recordsDeletedText = "${recordsDeletedText}";
				var errorOccurredText = "${errorOccurredText}";
				var showErrors = ${showErrors};
				var cannotDeleteRecordText = undefined;
				var linkedRecordsExistText = undefined;
				var selectRecordsText = "${selectRecordsText}";
				var someRecordsNotDeletedText = undefined;
				var columnDefs = undefined;

				//initialize datatable
				var oTable = initConfigTable(tbl, pageLength,
						showAllRowsText, contextPath, localeCode,
						addColumnFilters, columnDefs);

				var table = oTable.api();

				addDeleteRecordHandler(tbl, table, deleteRecordText, okText,
						cancelText, deleteRecordUrl, recordDeletedText,
						errorOccurredText, showErrors, cannotDeleteRecordText,
						linkedRecordsExistText);

				addDeleteRecordsHandler(table, deleteRecordText, okText,
						cancelText, deleteRecordsUrl, recordsDeletedText,
						errorOccurredText, showErrors, selectRecordsText,
						someRecordsNotDeletedText);

				//enable changing of report parameter position using drag and drop
				oTable.rowReordering({
					iIndexColumn: 1,
					sURL: "moveReportParameter",
					sRequestType: "POST",
					fnSuccess: function (response) {
						if (response.success) {
							notifyActionSuccessReusable("${recordMovedText}", escapeHtmlContent(response.data));
						} else {
							notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
						}
					},
					fnAlert: function (message) {
						bootbox.alert(message);
					}
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
			<a class="btn btn-default" href="${pageContext.request.contextPath}/addParameter?reportId=${reportId}">
				<i class="fa fa-plus"></i>
				<spring:message code="page.button.addNew"/>
			</a>
			<a class="btn btn-default" href="${pageContext.request.contextPath}/addReportParameter?reportId=${reportId}">
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

		<table id="reportParameters" class="table table-bordered table-striped table-condensed">
			<thead>
				<tr>
					<th class="noFilter selectCol"></th>
					<th><spring:message code="page.text.position"/></th>
					<th><spring:message code="page.text.id"/></th>
					<th><spring:message code="page.text.parameter"/></th>
					<th class="noFilter actionCol"><spring:message code="page.text.action"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="reportParameter" items="${reportParameters}">
					<tr data-id="${reportParameter.reportParameterId}"
						data-name="${encode:forHtmlAttribute(reportParameter.parameter.name)} (${reportParameter.parameter.parameterId})"
						id="${reportParameter.reportParameterId}">

						<td></td>
						<td>${reportParameter.position}</td>
						<td>${reportParameter.reportParameterId}</td>
						<td data-toggle="tooltip" title="${dragToReorderText}">
							<a href="${pageContext.request.contextPath}/editParameter?id=${reportParameter.parameter.parameterId}&returnReportId=${reportParameter.report.reportId}">
								${encode:forHtmlContent(reportParameter.parameter.name)} (${reportParameter.parameter.parameterId})
							</a>
							<c:if test="${reportParameter.parameter.shared}">
								&nbsp;
								<span class="label label-success">
									<spring:message code="parameters.label.shared"/>
								</span>
							</c:if>
						</td>							
						<td>
							<div class="btn-group">
								<a class="btn btn-default" 
								   href="${pageContext.request.contextPath}/editReportParameter?id=${reportParameter.reportParameterId}">
									<i class="fa fa-pencil-square-o"></i>
									<spring:message code="page.action.edit"/>
								</a>
								<button type="button" class="btn btn-default deleteRecord">
									<i class="fa fa-trash-o"></i>
									<spring:message code="page.action.delete"/>
								</button>
								<a class="btn btn-default" 
								   href="${pageContext.request.contextPath}/copyParameter?id=${reportParameter.parameter.parameterId}&reportId=${reportParameter.report.reportId}">
									<i class="fa fa-copy"></i>
									<spring:message code="page.action.copy"/>
								</a>
								<a class="btn btn-default" 
								   href="${pageContext.request.contextPath}/copyParameter?id=${reportParameter.parameter.parameterId}&reportId=${reportParameter.report.reportId}&reportParameterId=${reportParameter.reportParameterId}">
									<i class="fa fa-copy"></i>
									<spring:message code="parameters.action.replace"/>
								</a>
							</div>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</jsp:body>
</t:mainPageWithPanel>

