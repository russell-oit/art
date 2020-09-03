<%-- 
    Document   : pipelines
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.pipelines" var="pageTitle"/>

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText" javaScriptEscape="true"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText" javaScriptEscape="true"/>
<spring:message code="dialog.button.cancel" var="cancelText" javaScriptEscape="true"/>
<spring:message code="dialog.button.ok" var="okText" javaScriptEscape="true"/>
<spring:message code="dialog.message.deleteRecord" var="deleteRecordText" javaScriptEscape="true"/>
<spring:message code="page.message.recordDeleted" var="recordDeletedText" javaScriptEscape="true"/>
<spring:message code="page.message.recordsDeleted" var="recordsDeletedText" javaScriptEscape="true"/>
<spring:message code="dialog.message.selectRecords" var="selectRecordsText" javaScriptEscape="true"/>
<spring:message code="jobs.message.running" var="runningText" javaScriptEscape="true"/>
<spring:message code="pipelines.message.cancelled" var="cancelledText" javaScriptEscape="true"/>
<spring:message code="reports.text.status" var="statusText" javaScriptEscape="true"/>
<spring:message code="pipelines.message.refreshed" var="refreshedText" javaScriptEscape="true"/>

<t:mainPageWithPanel title="${pageTitle}" configPage="true">

	<jsp:attribute name="javascript">
		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="pipelines"]').parent().addClass('active');

				var tbl = $('#pipelines');

				var pageLength = undefined; //pass undefined to use the default
				var showAllRowsText = "${showAllRowsText}";
				var contextPath = "${pageContext.request.contextPath}";
				var localeCode = "${pageContext.response.locale}";
				var addColumnFilters = undefined; //pass undefined to use the default
				var deleteRecordText = "${deleteRecordText}";
				var okText = "${okText}";
				var cancelText = "${cancelText}";
				var deleteRecordUrl = "${pageContext.request.contextPath}/deletePipeline";
				var deleteRecordsUrl = "${pageContext.request.contextPath}/deletePipelines";
				var recordDeletedText = "${recordDeletedText}";
				var recordsDeletedText = "${recordsDeletedText}";
				var errorOccurredText = "${errorOccurredText}";
				var showErrors = ${showErrors};
				var cannotDeleteRecordText = undefined;
				var linkedRecordsExistText = undefined;
				var selectRecordsText = "${selectRecordsText}";
				var someRecordsNotDeletedText = undefined;
				var exportRecordsUrl = "${pageContext.request.contextPath}/exportRecords?type=Pipelines";
				var columnDefs = [
					{
						targets: "idCol",
						width: "50px"
					},
					{
						targets: "actionCol",
						width: "220px"
					}
				];

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
						
				addExportRecordsHandler(table, exportRecordsUrl, selectRecordsText);

				tbl.find('tbody').on('click', '.run', function () {
					var row = $(this).closest("tr"); //jquery object
					var recordName = escapeHtmlContent(row.attr("data-name"));
					var recordId = row.data("id");

					$.ajax({
						type: 'POST',
						url: '${pageContext.request.contextPath}/runPipeline',
						dataType: 'json',
						data: {id: recordId},
						success: function (response) {
							if (response.success) {
								notifyActionSuccessReusable("${runningText}", recordName);
							} else {
								notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
							}
						},
						error: ajaxErrorHandler
					});
				});

				tbl.find('tbody').on('click', '.cancel', function () {
					var row = $(this).closest("tr"); //jquery object
					var recordName = escapeHtmlContent(row.attr("data-name"));
					var recordId = row.data("id");

					$.ajax({
						type: 'POST',
						url: '${pageContext.request.contextPath}/cancelPipeline',
						dataType: 'json',
						data: {id: recordId},
						success: function (response) {
							if (response.success) {
								notifyActionSuccessReusable("${cancelledText}", recordName);
							} else {
								notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
							}
						},
						error: ajaxErrorHandler
					});
				});

				tbl.find('tbody').on('click', '.refresh', function () {
					var row = $(this).closest("tr"); //jquery object
					var recordName = escapeHtmlContent(row.attr("data-name"));
					var recordId = row.data("id");

					$.ajax({
						type: 'POST',
						url: '${pageContext.request.contextPath}/refreshPipeline',
						dataType: 'json',
						data: {id: recordId},
						success: function (response) {
							if (response.success) {
								var pipeline = response.data;

								var result = '';
								var runningJobsString = pipeline.runningJobsString;
								if (runningJobsString) {
									result = "${runningText}: " + runningJobsString;
								}

								table.cell(row, 4).data(result);

								notifyActionSuccessReusable("${refreshedText}", recordName);
							} else {
								notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
							}
						},
						error: ajaxErrorHandler
					});
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
					<p><encode:forHtmlContent value="${error}"/></p>
				</c:if>
			</div>
		</c:if>
		<c:if test="${not empty recordSavedMessage}">
			<div class="alert alert-success alert-dismissable">
				<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
				<spring:message code="${recordSavedMessage}"/>: <encode:forHtmlContent value="${recordName}"/>
			</div>
		</c:if>

		<div id="ajaxResponseContainer">
			<div id="ajaxResponse">
			</div>
		</div>

		<div style="margin-bottom: 10px;">
			<div class="btn-group">
				<a class="btn btn-default" href="${pageContext.request.contextPath}/addPipeline">
					<i class="fa fa-plus"></i>
					<spring:message code="page.action.add"/>
				</a>
				<button type="button" id="deleteRecords" class="btn btn-default">
					<i class="fa fa-trash-o"></i>
					<spring:message code="page.action.delete"/>
				</button>
			</div>
			<c:if test="${sessionUser.hasPermission('migrate_records')}">
				<div class="btn-group">
					<a class="btn btn-default" href="${pageContext.request.contextPath}/importRecords?type=Pipelines">
						<spring:message code="page.text.import"/>
					</a>
					<button type="button" id="exportRecords" class="btn btn-default">
						<spring:message code="page.text.export"/>
					</button>
				</div>
			</c:if>
		</div>

		<table id="pipelines" class="table table-bordered table-striped table-condensed">
			<thead>
				<tr>
					<th class="noFilter selectCol"></th>
					<th class="idCol"><spring:message code="page.text.id"/></th>
					<th><spring:message code="page.text.name"/></th>
					<th><spring:message code="page.text.description"/></th>
					<th><spring:message code="reports.text.status"/></th>
					<th class="noFilter actionCol"><spring:message code="page.text.action"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="pipeline" items="${pipelines}">
					<tr id="row_${pipeline.pipelineId}"
						data-id="${pipeline.pipelineId}"
						data-name="${encode:forHtmlAttribute(pipeline.name)}">

						<td></td>
						<td>${pipeline.pipelineId}</td>
						<td>${encode:forHtmlContent(pipeline.name)} &nbsp;
							<t:displayNewLabel creationDate="${pipeline.creationDate}"
											   updateDate="${pipeline.updateDate}"/>
						</td>
						<td>${encode:forHtmlContent(pipeline.description)}</td>
						<c:choose>
							<c:when test="${empty pipeline.runningJobs}">
								<td></td>
							</c:when>
							<c:otherwise>
								<td><spring:message code="jobs.message.running"/>: 
									${pipeline.runningJobsString}
								</td>
							</c:otherwise>
						</c:choose>
						<td>
							<div class="btn-group">
								<a class="btn btn-default" 
								   href="${pageContext.request.contextPath}/editPipeline?id=${pipeline.pipelineId}">
									<i class="fa fa-pencil-square-o"></i>
									<spring:message code="page.action.edit"/>
								</a>
								<button type="button" class="btn btn-default deleteRecord">
									<i class="fa fa-trash-o"></i>
									<spring:message code="page.action.delete"/>
								</button>
								<a class="btn btn-default" 
								   href="${pageContext.request.contextPath}/copyPipeline?id=${pipeline.pipelineId}">
									<i class="fa fa-copy"></i>
									<spring:message code="page.action.copy"/>
								</a>
								<button type="button" class="btn btn-default run">
									<i class="fa fa-bolt"></i>
									<spring:message code="jobs.action.run"/>
								</button>
								<button type="button" class="btn btn-default cancel">
									<spring:message code="dialog.button.cancel"/>
								</button>
								<button type="button" class="btn btn-default refresh">
									<i class="fa fa-refresh"></i>
									<spring:message code="page.action.refresh"/>
								</button>
							</div>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</jsp:body>
</t:mainPageWithPanel>
