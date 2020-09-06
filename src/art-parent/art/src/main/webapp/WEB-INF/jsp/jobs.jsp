<%-- 
    Document   : jobs
    Created on : 02-Oct-2013, 11:41:32
    Author     : Timothy Anyona

Display user jobs and jobs configuration
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

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

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText" javaScriptEscape="true"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText" javaScriptEscape="true"/>
<spring:message code="dialog.button.cancel" var="cancelText" javaScriptEscape="true"/>
<spring:message code="dialog.button.ok" var="okText" javaScriptEscape="true"/>
<spring:message code="dialog.message.deleteRecord" var="deleteRecordText" javaScriptEscape="true"/>
<spring:message code="page.message.recordDeleted" var="recordDeletedText" javaScriptEscape="true"/>
<spring:message code="jobs.message.running" var="runningText" javaScriptEscape="true"/>
<spring:message code="jobs.message.jobRefreshed" var="refreshedText" javaScriptEscape="true"/>
<spring:message code="page.message.recordsDeleted" var="recordsDeletedText" javaScriptEscape="true"/>
<spring:message code="dialog.message.selectRecords" var="selectRecordsText" javaScriptEscape="true"/>
<spring:message code="jobs.message.scheduled" var="scheduledText" javaScriptEscape="true"/>
<spring:message code="reports.text.selectValue" var="selectValueText" javaScriptEscape="true"/>
<spring:message code="jobs.message.running" var="runningText" javaScriptEscape="true"/>

<t:mainPageWithPanel title="${pageTitle}" configPage="true">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-datetimepicker-4.17.47/css/bootstrap-datetimepicker.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/yadcf-0.9.3/jquery.dataTables.yadcf.css"/>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/yadcf.css"/>
	</jsp:attribute>

	<jsp:attribute name="headContent">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/moment-2.22.2/moment-with-locales.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/yadcf-0.9.3/jquery.dataTables.yadcf.js"></script>

		<script>
			//put obtaining of server offset in head to reduce difference between server and client time
			//https://stackoverflow.com/questions/19629561/moment-js-set-the-base-time-from-the-server
			var serverDate = '${serverDateString}';
			var serverOffset = moment(serverDate, 'YYYY-MM-DD HH:mm:ss.SSS').diff(new Date());

			function currentServerDate()
			{
				return moment().add(serverOffset, 'milliseconds');
			}

			function updateClock()
			{
				var currentTimeString = currentServerDate().format("YYYY-MM-DD HH:mm:ss");
				currentTimeString += '   ${encode:forJavaScript(serverTimeZoneDescription)}';
				$("#clock").val(currentTimeString);
				$("#clock2").val(currentTimeString);
			}
		</script>
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-datetimepicker-4.17.47/js/bootstrap-datetimepicker.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Buttons-1.5.4/js/buttons.colVis.min.js"></script>

		<script type="text/javascript">
			$(document).ready(function () {
				var actionValue = '${action}';
				if (actionValue === 'config') {
					$('a[id="configure"]').parent().addClass('active');
					$('a[href*="jobsConfig"]').parent().addClass('active');
				} else if (actionValue === 'jobs') {
					$('a[href*="jobs"]').parent().addClass('active');
				}

				//display current time. updates every 1000 milliseconds
				setInterval('updateClock()', 1000);

				var tbl = $('#jobs');

				var deleteRecordText = "${deleteRecordText}";
				var okText = "${okText}";
				var cancelText = "${cancelText}";
				var deleteRecordUrl = "${pageContext.request.contextPath}/deleteJob";
				var deleteRecordsUrl = "${pageContext.request.contextPath}/deleteJobs";
				var recordDeletedText = "${recordDeletedText}";
				var recordsDeletedText = "${recordsDeletedText}";
				var errorOccurredText = "${errorOccurredText}";
				var showErrors = ${showErrors};
				var cannotDeleteRecordText = undefined;
				var linkedRecordsExistText = undefined;
				var selectRecordsText = "${selectRecordsText}";
				var someRecordsNotDeletedText = undefined;
				var editRecordsUrl = "${pageContext.request.contextPath}/editJobs";
				var columnDefs = [
					{
						targets: "idCol",
						width: "50px"
					},
					{
						targets: "nameCol",
						width: "220px"
					},
					{
						targets: "dateCol",
						width: "130px"
					},
					{
						targets: "resultCol",
						width: "400px"
					},
					{
						targets: "selectCol",
						orderable: false,
						className: 'select-checkbox'
					},
					{
						targets: "dtHidden", //target name matches class name of th.
						visible: false
					}
				];

				var oTable = tbl.dataTable({
					orderClasses: false,
					order: [1, "asc"],
					pagingType: "full_numbers",
					lengthMenu: [[10, 20, 50, -1], [10, 20, 50, "${showAllRowsText}"]],
					pageLength: 20,
					columnDefs: columnDefs,
					dom: 'lBfrtip',
					buttons: [
						'selectAll',
						'selectNone',
						{
							extend: 'colvis',
							postfixButtons: ['colvisRestore']
						}
					],
					select: {
						style: 'multi',
						selector: 'td:first-child'
					},
					language: {
						url: "${pageContext.request.contextPath}/js/dataTables/i18n/dataTables_${pageContext.response.locale}.json"
					},
					initComplete: function () {
						datatablesInitComplete();
					}
				});

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
								style_class: "yadcf-job-name-filter"
							},
							{
								column_number: 3,
								filter_default_label: '${selectValueText}',
								column_data_type: "html",
								html_data_type: "text"
							},
							{
								column_number: 4,
								filter_type: 'text',
								filter_default_label: "",
								html5_data: "data-filter",
								style_class: "yadcf-job-date-filter"
							},
							{
								column_number: 5,
								filter_type: 'text',
								filter_default_label: ""
							},
							{
								column_number: 6,
								filter_type: 'text',
								filter_default_label: "",
								html5_data: "data-filter",
								style_class: "yadcf-job-date-filter"
							}
						]
						);

				tbl.find('tbody').on('click', '.run', function () {
					var row = $(this).closest("tr"); //jquery object
					var recordName = escapeHtmlContent(row.attr("data-name"));
					var recordId = row.data("id");

					$.ajax({
						type: 'POST',
						url: '${pageContext.request.contextPath}/runJob',
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

				tbl.find('tbody').on('click', '.runLater', function () {
					var row = $(this).closest("tr"); //jquery object
					var recordName = escapeHtmlContent(row.attr("data-name"));
					var recordId = row.data("id");

					var currentTimeString = moment().format("YYYY-MM-DD HH:mm:ss");
					$('#runLaterDate').val(currentTimeString);
					$('#runLaterJobId').val(recordId);
					$('#runLaterJobName').val(recordName);
					$('#runLaterModal').modal('show');
				});

				$("#runLaterSubmit").on("click", function (e) {
					e.preventDefault();

					var recordName = $('#runLaterJobName').val();

					$.ajax({
						type: 'POST',
						url: '${pageContext.request.contextPath}/runLaterJob',
						dataType: 'json',
						data: $('#runLaterForm').serialize(),
						success: function (response) {
							$("#runLaterModal").modal('hide');
							if (response.success) {
								notifyActionSuccessReusable("${scheduledText}", recordName);
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
						url: '${pageContext.request.contextPath}/refreshJob',
						dataType: 'json',
						data: {id: recordId},
						success: function (response) {
							if (response.success) {
								var job = response.data;

								var result = '';
								if (job.lastFileName) {
									result = '<p><a type="application/octet-stream" ';
									result = result + 'href="${pageContext.request.contextPath}/export/jobs/' + job.lastFileName + '">';
									result = result + job.lastFileName + '</a></p>';
								}
								if (job.lastRunMessage) {
									result = result + '<p>' + job.lastRunMessage;
									result = result + '</p>';
								}
								if (job.lastRunDetails) {
									result = result + '<p>' + job.lastRunDetails;
									result = result + '</p>';
								}
								if (job.running) {
									result = result + '<p>' + "${runningText}";
									result = result + '</p>';
								}

								var hasConfigureJobsPermission = ${sessionUser.hasPermission('configure_jobs')};
								if (hasConfigureJobsPermission) {
									result = result + '<p><br><a type="application/octet-stream" ';
									result = result + 'href="${pageContext.request.contextPath}/export/jobLogs/' + job.jobId + '.log">';
									result = result + 'log</a></p>';
								}

								table.cell(row, 4).data(job.lastEndDateString);
								table.cell(row, 5).data(result);
								table.cell(row, 6).data(job.nextRunDateString);

								notifyActionSuccessReusable("${refreshedText}", recordName);
							} else {
								notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
							}
						},
						error: ajaxErrorHandler
					});
				});

				$('.datetimepicker').datetimepicker({
					format: 'YYYY-MM-DD HH:mm:ss',
					locale: '${pageContext.response.locale}'
				});

				$('#ajaxResponseContainer').on("click", ".alert .close", function () {
					$(this).parent().hide();
				});

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

		<div id="ajaxResponseContainer">
			<div id="ajaxResponse">
			</div>
		</div>

		<div class="row">
			<div class="col-md-4 col-md-offset-8">
				<form>
					<input type="text" id="clock2" readonly class="form-control"/>
				</form>
			</div>
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
					<a class="btn btn-default" href="${pageContext.request.contextPath}/runningJobs">
						<spring:message code="page.title.runningJobs"/>
					</a>
				</div>
			</c:when>
		</c:choose>

		<table id="jobs" class="table table-bordered table-striped table-condensed">
			<thead>
				<tr>
					<th class="noFilter selectCol"></th>
					<th class="idCol"><spring:message code="page.text.id"/><p></p></th>
					<th class="nameCol"><spring:message code="page.text.name"/><p></p></th>
					<th class="dtHidden"><spring:message code="page.text.active"/><p></p></th>
					<th class="dateCol"><spring:message code="jobs.text.lastEndDate"/><p></p></th>
					<th class="resultCol"><spring:message code="jobs.text.result"/><p></p></th>
					<th class="dateCol"><spring:message code="jobs.text.nextRunDate"/><p></p></th>
					<th class="noFilter actionCol"><spring:message code="page.text.action"/><p></p></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="job" items="${jobs}">
					<tr id="row_${job.jobId}"
						data-id="${job.jobId}"
						data-name="${encode:forHtmlAttribute(job.name)} (${job.jobId})">

						<td></td>
						<td>${job.jobId}</td>
						<td>${encode:forHtmlContent(job.name)} &nbsp;
							<t:displayNewLabel creationDate="${job.creationDate}"
											   updateDate="${job.updateDate}"/>
						</td>
						<td><t:displayActiveStatus active="${job.active}"/></td>
						<td data-sort="${job.lastEndDate.time}">
							<fmt:formatDate value="${job.lastEndDate}" pattern="${dateDisplayPattern}"/>
						</td>
						<td>
							<c:if test="${not empty job.lastFileName}">
								<p>
									<a type="application/octet-stream" 
									   href="${pageContext.request.contextPath}/export/jobs/${job.lastFileName}">
										${job.lastFileName}
									</a>
								</p>
							</c:if>
							<c:if test="${not empty job.lastRunMessage}">
								<p>
									<spring:message code="${job.lastRunMessage}"/>
								</p>
							</c:if>
							<c:if test="${not empty job.lastRunDetails}">
								<p>
									${job.lastRunDetails}
								</p>
							</c:if>
							<c:if test="${job.running}">
								<p>
									<spring:message code="jobs.message.running"/>
								</p>
							</c:if>
							<c:if test="${sessionUser.hasPermission('configure_jobs')}">
								<p><br>
									<a type="application/octet-stream" 
									   href="${pageContext.request.contextPath}/export/jobLogs/${job.jobId}.log">
										log
									</a>
								</p>
							</c:if>
						</td>
						<td data-sort="${job.nextRunDate.time}">
							<fmt:formatDate value="${job.nextRunDate}" pattern="${dateDisplayPattern}"/>
						</td>
						<td>
							<c:if test="${sessionUser.userId == job.user.userId || action == 'config'}">
								<div class="btn-group">
									<a class="btn btn-default" 
									   href="${pageContext.request.contextPath}/editJob?id=${job.jobId}&nextPage=${nextPage}">
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
							<c:if test="${action == 'config'}">
								<div class="btn-group">
									<button type="button" class="btn btn-default dropdown-toggle"
											data-toggle="dropdown" data-hover="dropdown"
											data-delay="100">
										<spring:message code="reports.action.more"/>
										<span class="caret"></span>
									</button>
									<ul class="dropdown-menu">
										<li>
											<a 
												href="${pageContext.request.contextPath}/jobAccessRights?jobId=${job.jobId}">
												<spring:message code="page.action.accessRights"/>
											</a>
										</li>
									</ul>
								</div>
							</c:if>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>

		<div id="runLaterModal" class="modal fade" role="dialog" 
			 aria-labelledby="runLaterLabel" aria-hidden="true" tabindex="-1">
			<div class="modal-dialog">
				<div class="modal-content">

					<!-- Modal Header -->
					<div class="modal-header">
						<button type="button" class="close" 
								data-dismiss="modal">
							<span aria-hidden="true">&times;</span>
							<span class="sr-only">Close</span>
						</button>
						<h4 class="modal-title" id="runLaterLabel">
							<spring:message code="jobs.action.runLater"/>
						</h4>
					</div>

					<!-- Modal Body -->
					<div class="modal-body">
						<form id="runLaterForm" class="form-horizontal" role="form" method="POST" action="${pageContext.request.contextPath}/runLaterJob">
							<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
							<input type="hidden" id="runLaterJobId" name="runLaterJobId"/>
							<div class="form-group">
								<label class="control-label col-md-4" for="runLaterJobName">
									<spring:message code="jobs.text.jobName"/>
								</label>
								<div class="col-md-8">
									<input type="text" id="runLaterJobName" name="runLaterJobName" readonly class="form-control"/>
								</div>
							</div>
							<div class="form-group">
								<label class="control-label col-md-4" for="clock">
									<spring:message code="jobs.label.currentTime"/>
								</label>
								<div class="col-md-8">
									<input type="text" id="clock" readonly class="form-control"/>
								</div>
							</div>
							<div class="form-group">
								<label class="control-label col-md-4" for="runLaterDate">
									<spring:message code="jobs.label.runDate"/>
								</label>
								<div class="col-md-8">
									<div id="runLaterDatePicker" class='input-group date datetimepicker'>
										<input type="text" id="runLaterDate" name="runLaterDate" value="" class="form-control"/>
										<span class="input-group-addon">
											<span class="glyphicon glyphicon-calendar"></span>
										</span>
									</div>
								</div>
							</div>
						</form>
					</div>
					<!-- Modal Footer -->
					<div class="modal-footer">
						<button type="button" class="btn btn-default"
								data-dismiss="modal">
							<spring:message code="dialog.button.cancel"/>
						</button>
						<button type="button" id="runLaterSubmit" class="btn btn-primary">
							<spring:message code="dialog.button.ok"/>
						</button>
					</div>		
				</div>
			</div>
		</div>
	</jsp:body>
</t:mainPageWithPanel>

