<%-- 
    Document   : jobs
    Created on : 02-Oct-2013, 11:41:32
    Author     : Timothy Anyona

Display user jobs and jobs configuration
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:choose>
	<c:when test="${action == 'config'}">
		<spring:message code="page.title.jobsConfiguration" var="pageTitle"/>
	</c:when>
	<c:otherwise>
		<spring:message code="page.title.jobs" var="pageTitle"/>
	</c:otherwise>
</c:choose>

<spring:message code="datatables.text.showAllRows" var="showAllRowsText"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="dialog.button.cancel" var="cancelText"/>
<spring:message code="dialog.button.ok" var="okText"/>
<spring:message code="dialog.message.deleteRecord" var="deleteRecordText"/>
<spring:message code="page.message.recordDeleted" var="recordDeletedText"/>
<spring:message code="jobs.message.running" var="runningText"/>
<spring:message code="jobs.message.jobRefreshed" var="refreshedText"/>
<spring:message code="page.message.recordsDeleted" var="recordsDeletedText"/>
<spring:message code="dialog.message.selectRecords" var="selectRecordsText"/>
<spring:message code="page.message.someRecordsNotDeleted" var="someRecordsNotDeletedText"/>
<spring:message code="jobs.message.scheduled" var="scheduledText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-12">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/eonasdan-datepicker/css/bootstrap-datetimepicker.min.css">
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/notify-combined-0.3.1.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/eonasdan-datepicker/moment-with-locales.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/eonasdan-datepicker/js/bootstrap-datetimepicker.min.js"></script>

		<script type="text/javascript" charset="utf-8">
			$(document).ready(function () {
				$(function () {
					var actionValue = '${action}';
					if (actionValue === 'config') {
//						$('a[id="jobsConfigLink"]').parent().addClass('active');
						$('a[href*="jobsConfig.do"]').parent().addClass('active');
					} else {
						$('a[href*="jobs.do"]').parent().addClass('active');
					}
				});

				var tbl = $('#jobs');

				//initialize datatable and process delete action
				var oTable = initConfigPage(tbl,
						undefined, //pageLength. pass undefined to use the default
						"${showAllRowsText}",
						"${pageContext.request.contextPath}",
						"${pageContext.response.locale}",
						undefined, //addColumnFilters. pass undefined to use default
						".deleteRecord", //deleteButtonSelector
						true, //showConfirmDialog
						"${deleteRecordText}",
						"${okText}",
						"${cancelText}",
						"deleteJob.do", //deleteUrl
						"${recordDeletedText}",
						"${errorOccurredText}",
						true, //deleteRow
						undefined, //cannotDeleteRecordText
						undefined //linkedRecordsExistText
						);

				var table = oTable.api();

				tbl.find('tbody').on('click', '.run', function () {
					var row = $(this).closest("tr"); //jquery object
					var recordName = escapeHtmlContent(row.data("name"));
					var recordId = row.data("id");

					$.ajax({
						type: 'POST',
						url: '${pageContext.request.contextPath}/app/runJob.do',
						dataType: 'json',
						data: {id: recordId},
						success: function (response) //on recieve of reply
						{
							if (response.success) {
								notifyActionSuccess("${runningText}", recordName);
							} else {
								notifyActionError("${errorOccurredText}", escapeHtmlContent(response.errorMessage));
							}
						},
						error: ajaxErrorHandler
					});
				});

				tbl.find('tbody').on('click', '.runLater', function () {
					var row = $(this).closest("tr"); //jquery object
					var recordName = escapeHtmlContent(row.data("name"));
					var recordId = row.data("id");

					var currentTimeString = moment().format("YYYY-MM-DD HH:mm:ss")
					$('#runLaterDate').val(currentTimeString);
					$('#runLaterJobId').val(recordId);
					$('#runLaterJobName').val(recordName);
					$('#runLaterModal').modal('show');
				});

				$("#runLaterSubmit").click(function (e) {
					e.preventDefault();

					var recordName = $('#runLaterJobName').val();

					$.ajax({
						type: 'POST',
						url: '${pageContext.request.contextPath}/app/runLaterJob.do',
						dataType: 'json',
						data: $('#runLaterForm').serialize(),
						success: function (response) //on recieve of reply
						{
							if (response.success) {
								$("#runLaterModal").modal('hide');
								notifyActionSuccess("${scheduledText}", recordName);
							} else {
								notifyActionError("${errorOccurredText}", escapeHtmlContent(response.errorMessage));
							}
						},
						error: ajaxErrorHandler
					});
				});

				tbl.find('tbody').on('click', '.refresh', function () {
					var row = $(this).closest("tr"); //jquery object
					var recordName = escapeHtmlContent(row.data("name"));
					var recordId = row.data("id");

					$.ajax({
						type: 'POST',
						url: '${pageContext.request.contextPath}/app/refreshJob.do',
						dataType: 'json',
						data: {id: recordId},
						success: function (response)
						{
							if (response.success) {
								var job = response.data;

								var result = '';
								if (job.lastFileName) {
									result = '<a type="application/octet-stream" ';
									result = result + 'href="${pageContext.request.contextPath}/export/jobs/' + job.lastFileName + '">';
									result = result + job.lastFileName + '</a>';
									result = result + '<br>';
								}
								if (job.lastRunMessage) {
									result = result + job.lastRunMessage;
									result = result + '<br>';
								}
								if (job.lastRunDetails) {
									result = result + job.lastRunDetails;
								}

								table.cell(row, 3).data(job.lastEndDateString);
								table.cell(row, 4).data(result);
								table.cell(row, 5).data(job.nextRunDateString);

								notifyActionSuccess("${refreshedText}", recordName);
							} else {
								notifyActionError("${errorOccurredText}", escapeHtmlContent(response.errorMessage));
							}
						},
						error: ajaxErrorHandler
					});
				});

				$('#deleteRecords').click(function () {
					var selectedRows = table.rows({selected: true});
					var data = selectedRows.data();
					if (data.length > 0) {
						var ids = $.map(data, function (item) {
							return item[1];
						});

						bootbox.confirm({
							message: "${deleteRecordText}: <b>" + ids + "</b>",
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
										url: "${pageContext.request.contextPath}/app/deleteJobs.do",
										data: {ids: ids},
										success: function (response) {
											if (response.success) {
												selectedRows.remove().draw(false);
												notifyActionSuccess("${recordsDeletedText}", ids);
											} else {
												notifyActionError("${errorOccurredText}", escapeHtmlContent(response.errorMessage));
											}
										},
										error: ajaxErrorHandler
									});
								} //end if result
							} //end callback
						}); //end bootbox confirm
					} else {
						bootbox.alert("${selectRecordsText}");
					}
				});

				$('#editRecords').click(function () {
					var selectedRows = table.rows({selected: true});
					var data = selectedRows.data();
					if (data.length > 0) {
						var ids = $.map(data, function (item) {
							return item[1];
						});
						window.location.href = '${pageContext.request.contextPath}/app/editJobs.do?ids=' + ids;
					} else {
						bootbox.alert("${selectRecordsText}");
					}
				});

				$('.datetimepicker').datetimepicker({
					format: 'YYYY-MM-DD HH:mm:ss',
					locale: '${pageContext.response.locale}'
				});

				//display current time. updates every 1000 milliseconds
				setInterval('updateClock()', 1000);

			}); //end document ready
		</script>

		<script type="text/javascript">
			function updateClock()
			{
				var currentTimeString = moment().format("YYYY-MM-DD HH:mm:ss");
				$("#clock").val(currentTimeString);
			}
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

		<c:choose>
			<c:when test="${action == 'config'}">
				<div style="margin-bottom: 10px;">
					<button type="button" id="editRecords" class="btn btn-default">
						<i class="fa fa-pencil-square-o"></i>
						<spring:message code="page.action.edit"/>
					</button>
					<button type="button" id="deleteRecords" class="btn btn-default">
						<i class="fa fa-trash-o"></i>
						<spring:message code="page.action.delete"/>
					</button>
				</div>
			</c:when>
		</c:choose>

		<table id="jobs" class="table table-bordered table-striped table-condensed">
			<thead>
				<tr>
					<th class="noFilter"></th>
					<th><spring:message code="page.text.id"/></th>
					<th><spring:message code="page.text.name"/></th>
					<th><spring:message code="jobs.text.lastEndDate"/></th>
					<th><spring:message code="jobs.text.result"/></th>
					<th><spring:message code="jobs.text.nextRunDate"/></th>
					<th class="noFilter"><spring:message code="page.text.action"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="job" items="${jobs}">
					<tr data-id="${job.jobId}" 
						data-name="${encode:forHtmlAttribute(job.name)}">

						<td></td>
						<td>${job.jobId}</td>
						<td>${encode:forHtmlContent(job.name)}</td>
						<td data-sort="${job.lastEndDate.time}">
							<fmt:formatDate value="${job.lastEndDate}" pattern="${dateDisplayPattern}"/>
						</td>
						<td>
							<c:if test="${not empty job.lastFileName}">
								<a type="application/octet-stream" 
								   href="${pageContext.request.contextPath}/export/jobs/${job.lastFileName}">
									${job.lastFileName}
								</a>
								<br>
							</c:if>
							<c:if test="${not empty job.lastRunMessage}">
								<spring:message code="${job.lastRunMessage}"/>
								<br>
							</c:if>
							<c:if test="${not empty job.lastRunDetails}">
								${job.lastRunDetails}
							</c:if>
						</td>
						<td data-sort="${job.nextRunDate.time}">
							<fmt:formatDate value="${job.nextRunDate}" pattern="${dateDisplayPattern}"/>
						</td>
						<td>
							<c:if test="${sessionUser.userId == job.user.userId || action == 'config'}">
								<div class="btn-group">
									<a class="btn btn-default" 
									   href="${pageContext.request.contextPath}/app/editJob.do?id=${job.jobId}&nextPage=${nextPage}">
										<i class="fa fa-pencil-square-o"></i>
										<spring:message code="page.action.edit"/>
									</a>
									<button type="button" class="btn btn-default deleteRecord">
										<i class="fa fa-trash-o"></i>
										<spring:message code="page.action.delete"/>
									</button>
									<button type="button" class="btn btn-default run">
										<i class="fa fa-bolt"></i>
										<spring:message code="jobs.action.run"/>
									</button>
									<button type="button" class="btn btn-default runLater"
											data-toggle="modal">
										<i class="fa fa-clock-o"></i>
										<spring:message code="jobs.action.runLater"/>
									</button>
									<button type="button" class="btn btn-default refresh">
										<i class="fa fa-refresh"></i>
										<spring:message code="page.action.refresh"/>
									</button>
								</div>
							</c:if>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>

		<div id="runLaterModal" class="modal fade" role="dialog" 
			 aria-labelledby="runLater" aria-hidden="true" tabindex="-1">
			<div class="modal-dialog">
				<div class="modal-content">

					<form id="runLaterForm" role="form" method="POST" value="${pageContext.request.contextPath}/app/runLaterJob.do">
						<!-- Modal Header -->
						<div class="modal-header">
							<button type="button" class="close" 
									data-dismiss="modal">
								<span aria-hidden="true">&times;</span>
								<span class="sr-only">Close</span>
							</button>
							<h4 class="modal-title" id="runLater">
								<spring:message code="jobs.action.runLater"/>
							</h4>
						</div>

						<!-- Modal Body -->
						<div class="modal-body">
							<input type="hidden" id="runLaterJobId" name="runLaterJobId"/>
							<input type="text" id="runLaterJobName" name="runLaterJobName" readonly class="form-control"/>
							<input type="text" id="clock" readonly class="form-control"/>
							<div id="runLaterDatePicker" class='input-group date datetimepicker'>
								<input type="text" id="runLaterDate" name="runLaterDate" value="" class="form-control"/>
								<span class="input-group-addon">
									<span class="glyphicon glyphicon-calendar"></span>
								</span>
							</div>
						</div>

						<!-- Modal Footer -->
						<div class="modal-footer">
							<button type="button" class="btn btn-default"
									data-dismiss="modal">
								<spring:message code="dialog.button.cancel"/>
							</button>
							<button type="submit" id="runLaterSubmit" class="btn btn-primary">
								<spring:message code="dialog.button.ok"/>
							</button>
						</div>
					</form>
				</div>
			</div>
		</div>
	</jsp:body>
</t:mainPageWithPanel>

