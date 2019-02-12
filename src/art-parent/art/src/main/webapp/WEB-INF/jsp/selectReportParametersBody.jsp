<%-- 
    Document   : selectReportParametersBody
    Created on : 22-Jun-2016, 11:45:20
    Author     : Timothy Anyona

Display section to allow selecting of report parameters and initiate running of report
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="reports.message.fileSent" var="fileSentText"/>
<spring:message code="reports.message.parametersSaved" var="parametersSavedText"/>
<spring:message code="reports.message.parametersCleared" var="parametersClearedText"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="reports.message.accessUpdated" var="accessUpdatedText"/>
<spring:message code="select.text.nothingSelected" var="nothingSelectedText"/>
<spring:message code="select.text.noResultsMatch" var="noResultsMatchText"/>
<spring:message code="select.text.selectedCount" var="selectedCountText"/>
<spring:message code="select.text.selectAll" var="selectAllText"/>
<spring:message code="select.text.deselectAll" var="deselectAllText"/>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/notifyjs-0.4.2/notify.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootbox-4.4.0.min.js"></script>

<script type="text/javascript">
	$(document).ready(function () {
		$("#schedule").on("click", function (e) {
			e.preventDefault();
			var url = "${pageContext.request.contextPath}/addJob";
			$('#parametersForm').attr('action', url).submit();
		});

		$("#runInNewPage").on("click", function () {
			$("#showInline").val("false");
			//need to explicitly set. if click on schedule, then back then run in new page - goes to schedule again
			var url = "${pageContext.request.contextPath}/runReport";
			$('#parametersForm').attr('action', url).submit();
		});

		$("#runInline").on("click", function (e) {
			e.preventDefault();

			$("#showInline").val("true");

			//disable buttons
			$('.action').prop('disabled', true);

			$.ajax({
				type: "POST",
				url: "${pageContext.request.contextPath}/runReport",
				data: $('#parametersForm').serialize(),
				success: function (data) {
					$("#reportOutput").html(data);
				},
				error: function (xhr) {
					//https://stackoverflow.com/questions/6186770/ajax-request-returns-200-ok-but-an-error-event-is-fired-instead-of-success
					showUserAjaxError(xhr, '${errorOccurredText}');
				},
				complete: function () {
					$('.action').prop('disabled', false);
				}
			});
		});

		$("#saveParameterSelection").on("click", function (e) {
			e.preventDefault();

			$.ajax({
				type: 'POST',
				url: '${pageContext.request.contextPath}/saveParameterSelection',
				dataType: 'json',
				data: $('#parametersForm').serialize(),
				success: function (response) {
					if (response.success) {
						$.notify("${parametersSavedText}", "success");
					} else {
						notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
					}
				},
				error: function (xhr) {
					showUserAjaxError(xhr, '${errorOccurredText}');
				}
			});
		});

		$("#clearSavedParameterSelection").on("click", function (e) {
			e.preventDefault();

			var reportId = parseInt($('input[name="reportId"]').val(), 10);

			$.ajax({
				type: 'POST',
				url: '${pageContext.request.contextPath}/clearSavedParameterSelection',
				dataType: 'json',
				data: {reportId: reportId},
				success: function (response) {
					if (response.success) {
						$.notify("${parametersClearedText}", "success");
					} else {
						notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
					}
				},
				error: function (xhr) {
					showUserAjaxError(xhr, '${errorOccurredText}');
				}
			});
		});

		$('#errorsDiv').on("click", ".alert .close", function () {
			$(this).parent().hide();
		});


		$("#reportFormat").on("change", function () {
			toggleVisibleButtons();
		});

		toggleVisibleButtons(); //show/hide on page load

		//{container: 'body'} needed if tooltips shown on input-group element or button
		$("[data-toggle='tooltip']").tooltip({container: 'body'});

		if (${startSelectParametersHidden}) {
			$("#collapse1").collapse("hide");
		}

	}); //end document ready
</script>

<script type="text/javascript">
	//https://stackoverflow.com/questions/2255291/print-the-contents-of-a-div
	function PrintElem(elem)
	{
		Popup($(elem).html());
	}

	function Popup(data)
	{
		var mywindow = window.open('', '${encode:forJavaScript(report.getLocalizedName(locale))}', 'height=400,width=600');
		mywindow.document.write('<html><head><title>${encode:forJavaScript(report.getLocalizedName(locale))}</title>');
		/*optional stylesheet*/ //mywindow.document.write('<link rel="stylesheet" href="main.css" type="text/css" />');
		mywindow.document.write('</head><body>');
		mywindow.document.write(data);
		mywindow.document.write('</body></html>');

		mywindow.document.close(); // necessary for IE >= 10
		mywindow.focus(); // necessary for IE >= 10

		mywindow.print();
		mywindow.close();

		return true;
	}

	function toggleVisibleButtons() {
		var reportFormat = $('#reportFormat option:selected').val();

		//show/hide print button
		if (${enablePrint}) {
			if (${enablePrintAlways}) {
				$('#printButton').show();
			} else {
				switch (reportFormat) {
					case 'htmlGrid':
					case 'htmlDataTable':
					case 'htmlFancy':
					case 'htmlPlain':
					case 'html':
						$('#printButton').show();
						break;
					default:
						$('#printButton').hide();
				}
			}
		}

		//show/hide email button
		if (${enableEmail}) {
			switch (reportFormat) {
				case 'htmlGrid':
				case 'htmlDataTable':
				case 'htmlFancy':
				case 'htmlPlain':
				case 'html':
					$('#emailButton').hide();
					break;
				default:
					$('#emailButton').show();
			}
		}
	}
</script>

<c:if test="${enableEmail}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/tinymce-4.8.5/tinymce.min.js"></script>
	<script type="text/javascript">
	tinymce.init(tinymceConfig);
	</script>

	<script>
		$(function () {
			$("#emailReportSubmit").on("click", function (e) {
				e.preventDefault();

				//https://stackoverflow.com/questions/2122085/jquery-and-tinymce-textarea-value-doesnt-submit
				tinyMCE.triggerSave();

				$.ajax({
					type: 'POST',
					url: '${pageContext.request.contextPath}/emailReport',
					dataType: 'json',
					data: $('#emailReportForm').serialize(),
					success: function (response) //on recieve of reply
					{
						$("#emailReportModal").modal('hide');
//					 $(':input','#emailReportForm').val("");
						if (response.success) {
							$.notify("${fileSentText}", "success");
						} else {
							notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
						}
					},
					error: function (xhr) {
						showUserAjaxError(xhr, '${errorOccurredText}');
					}
				});
			});
		});
	</script>

	<div id="emailReportModal" class="modal fade" role="dialog" 
		 aria-labelledby="emailReportLabel" aria-hidden="true" tabindex="-1">
		<div class="modal-dialog">
			<div class="modal-content">

				<form id="emailReportForm" class="form-horizontal" role="form" method="POST" action="${pageContext.request.contextPath}/emailReport">
					<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
					<!-- Modal Header -->
					<div class="modal-header">
						<button type="button" class="close" 
								data-dismiss="modal">
							<span aria-hidden="true">&times;</span>
							<span class="sr-only">Close</span>
						</button>
						<h4 class="modal-title" id="emailReportLabel">
							<spring:message code="reports.text.emailReport"/>
						</h4>
					</div>

					<!-- Modal Body -->
					<div class="modal-body">
						<div class="form-group">
							<label class="control-label col-md-4" for="mailFrom">
								<spring:message code="jobs.label.mailFrom"/>
							</label>
							<div class="col-md-8">
								<input type="text" id="mailFrom" name="mailFrom"
									   readonly class="form-control" value="${encode:forHtmlAttribute(sessionUser.email)}"/>
							</div>
						</div>
						<div class="form-group">
							<label class="control-label col-md-4" for="mailTo">
								<spring:message code="jobs.label.mailTo"/>
							</label>
							<div class="col-md-8">
								<input type="text" id="mailTo" name="mailTo" class="form-control"/>
							</div>
						</div>
						<div class="form-group">
							<label class="control-label col-md-4" for="mailCc">
								<spring:message code="jobs.label.mailCc"/>
							</label>
							<div class="col-md-8">
								<input type="text" id="mailCc" name="mailCc" class="form-control"/>
							</div>
						</div>
						<div class="form-group">
							<label class="control-label col-md-4" for="mailBcc">
								<spring:message code="jobs.label.mailBcc"/>
							</label>
							<div class="col-md-8">
								<input type="text" id="mailBcc" name="mailBcc" class="form-control"/>
							</div>
						</div>
						<div class="form-group">
							<label class="control-label col-md-4" for="mailSubject">
								<spring:message code="jobs.label.mailSubject"/>
							</label>
							<div class="col-md-8">
								<input type="text" id="mailSubject" name="mailSubject" class="form-control"/>
							</div>
						</div>
						<div>
							<label class="col-md-12 control-label" style="text-align: center">
								<spring:message code="jobs.label.mailMessage"/>
							</label>
							<div class="form-group">
								<div class="col-md-12">
									<textarea id="mailMessage" name="mailMessage" rows="5" cols="60" class="form-control editor"></textarea>
									<input name="image" type="file" id="upload" style="display:none;" onchange="">
								</div>
							</div>
						</div>
					</div>

					<!-- Modal Footer -->
					<div class="modal-footer">
						<button type="button" class="btn btn-default"
								data-dismiss="modal">
							<spring:message code="dialog.button.cancel"/>
						</button>
						<button type="submit" id="emailReportSubmit" class="btn btn-primary">
							<spring:message code="dialog.button.ok"/>
						</button>
					</div>
				</form>
			</div>
		</div>
	</div>
</c:if>

<c:if test="${enableShare}">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/css/bootstrap-select.min.css">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/js/bootstrap-select.min.js"></script>

	<script>
		$(function () {
			$('.share').selectpicker({
				liveSearch: true,
				noneSelectedText: '${nothingSelectedText}',
				noneResultsText: '${noResultsMatchText}',
				countSelectedText: '${selectedCountText}',
				actionsBox: true,
				selectAllText: '${selectAllText}',
				deselectAllText: '${deselectAllText}'
			});

			$("#shareReportSubmit").on("click", function (e) {
				e.preventDefault();

				$.ajax({
					type: 'POST',
					url: '${pageContext.request.contextPath}/shareReport',
					dataType: 'json',
					data: $('#shareReportForm').serialize(),
					success: function (response) {
						$("#shareReportModal").modal('hide');
						$("#shareReportModal option:selected").prop("selected", false);
						$('.share').selectpicker('refresh');
						if (response.success) {
							$.notify("${accessUpdatedText}", "success");
						} else {
							notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
						}
					},
					error: function (xhr) {
						showUserAjaxError(xhr, '${errorOccurredText}');
					}
				});
			});
		});
	</script>

	<div id="shareReportModal" class="modal fade" role="dialog" 
		 aria-labelledby="shareReportLabel" aria-hidden="true" tabindex="-1">
		<div class="modal-dialog">
			<div class="modal-content">
				<form id="shareReportForm" class="form-horizontal" role="form" method="POST" action="${pageContext.request.contextPath}/shareReport">
					<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
					<input type="hidden" name="shareReportId" value="${report.reportId}">
					<!-- Modal Header -->
					<div class="modal-header">
						<button type="button" class="close" 
								data-dismiss="modal">
							<span aria-hidden="true">&times;</span>
							<span class="sr-only">Close</span>
						</button>
						<h4 class="modal-title" id="shareReportLabel">
							<spring:message code="dialog.title.shareReport"/>
						</h4>
					</div>

					<!-- Modal Body -->
					<div class="modal-body" style="overflow: visible;">
						<div class="form-group">
							<label class="control-label col-md-4" for="name">
								<spring:message code="page.text.name"/>
							</label>
							<div class="col-md-8">
								<input type="text" id="name" name="name" value="${encode:forHtmlAttribute(report.getLocalizedName(locale))}" readonly class="form-control"/>
							</div>
						</div>
						<div class="form-group">
							<label class="control-label col-md-4" for="users">
								<spring:message code="page.text.users"/>
							</label>
							<div class="col-md-8">
								<select id="users" name="users[]" class="form-control share" multiple>
									<c:forEach var="user" items="${users}">
										<option value="${user.userId}">${encode:forHtmlContent(user.fullName)}</option>
									</c:forEach>
								</select>
							</div>
						</div>
						<div class="form-group">
							<label class="control-label col-md-4" for="userGroups">
								<spring:message code="page.text.userGroups"/>
							</label>
							<div class="col-md-8">
								<select id="userGroups" name="userGroups[]" class="form-control share" multiple>
									<c:forEach var="userGroup" items="${userGroups}">
										<option value="${userGroup.userGroupId}">${encode:forHtmlContent(userGroup.name)}</option>
									</c:forEach>
								</select>
							</div>
						</div>
						<div class="form-group">
							<label class="control-label col-md-4" for="action">
								<spring:message code="page.text.action"/>
							</label>
							<div class="col-md-8">
								<select id="action" name="action" class="form-control">
									<option value="grant"><spring:message code="page.action.grant"/></option>
									<option value="revoke"><spring:message code="page.action.revoke"/></option>
								</select>
							</div>
						</div>
					</div>

					<!-- Modal Footer -->
					<div class="modal-footer">
						<button type="button" class="btn btn-default"
								data-dismiss="modal">
							<spring:message code="dialog.button.cancel"/>
						</button>
						<button type="submit" id="shareReportSubmit" class="btn btn-primary">
							<spring:message code="dialog.button.ok"/>
						</button>
					</div>
				</form>
			</div>
		</div>
	</div>
</c:if>

<c:if test="${error != null}">
	<div class="alert alert-danger alert-dismissable">
		<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
		<p><spring:message code="page.message.errorOccurred"/></p>
		<c:if test="${showErrors}">
			<t:displayStackTrace error="${error}"/>
		</c:if>
	</div>
</c:if>

<div class="row reportName">
    <div class="col-md-9">
        <h3>${encode:forHtmlContent(report.getLocalizedName(locale))}</h3>
    </div>
	<div class="col-md-3">
		<h3 class="text-right">
			<small>
				<c:if test="${enableShare}">
					<button type="button" id="shareReport" class="btn btn-sm btn-default action"
							data-toggle="modal" data-target="#shareReportModal">
						<spring:message code="reports.button.share"/>
					</button>
				</c:if>
				<c:if test="${sessionUser.hasPermission('configure_reports')}">
					<a class="btn btn-sm btn-default" href="${pageContext.request.contextPath}/reportConfig?reportId=${report.reportId}">
						<spring:message code="page.text.report"/>
					</a>
				</c:if>
				<a data-toggle="collapse" href="#collapse1" class="btn btn-sm btn-default">
					<spring:message code="jobs.text.parameters"/> <i class="fa fa-angle-down" aria-hidden="true"></i>
				</a>
				<label for="runInline" class="btn btn-primary btn-sm">
					<spring:message code="page.action.run"/>
				</label>
			</small>
		</h3> 
	</div>
</div>

<div class="row">
	<div class="col-md-6 col-md-offset-3">
		<div class="panel panel-default">
			<div id="collapse1" class="panel-collapse collapse in">
				<div class="panel-body">
					<spring:url var="formUrl" value="/runReport"/>
					<form id="parametersForm" class="form-horizontal" method="POST" action="${formUrl}">
						<fieldset>
							<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
							<input type="hidden" name="reportId" value="${report.reportId}">
							<input type="hidden" name="showInline" id="showInline" value="true">
							<input type="hidden" name="nextPage" id="nextPage" value="jobs">

							<c:set var="labelColClass" value="col-md-5" scope="request"/>
							<c:set var="inputColClass" value="col-md-7" scope="request"/>
							<jsp:include page="reportParameters.jsp"/>

							<c:if test="${report.reportType.isChart()}">
								<jsp:include page="chartOptions.jsp"/>
							</c:if>

							<div id="reportOptions">
								<c:if test="${enableReportFormats}">
									<div class="form-group">
										<label class="control-label col-md-5" for="reportFormat">
											<spring:message code="reports.label.format"/>
										</label>
										<div class="col-md-7">
											<select name="reportFormat" id="reportFormat" class="form-control">
												<c:forEach var="format" items="${reportFormats}">
													<option value="${format}" ${reportFormat == format ? "selected" : ""}>
														<spring:message code="reports.format.${format}"/>
													</option>
												</c:forEach>
											</select>
										</div>
									</div>
								</c:if>

								<c:if test="${enableShowSelectedParameters}">
									<div class="form-group">
										<label class="control-label col-md-5" for="showSelectedParameters">
											<spring:message code="reports.label.showSelectedParameters"/>
										</label>
										<div class="col-md-7">
											<div class="checkbox">
												<label>
													<input type="checkbox" name="showSelectedParameters" id="showSelectedParameters"
														   <c:if test="${reportOptions.showSelectedParameters}">checked="checked"</c:if> value="">
													</label>
												</div>
											</div>
										</div>
								</c:if>

								<c:if test="${enableShowSql}">
									<div class="form-group">
										<label class="control-label col-md-5" for="showSql">
											<spring:message code="reports.label.showSql"/>
										</label>
										<div class="col-md-7">
											<div class="checkbox">
												<label>
													<input type="checkbox" name="showSql" id="showSql"
														   <c:if test="${reportOptions.showSql}">checked="checked"</c:if> value="">
													</label>
												</div>
											</div>
										</div>
								</c:if>
							</div>

							<c:if test="${showSaveParameterSelection}">
								<hr>
								<div class="form-group">
									<div class="col-md-12">
										<div style="text-align: center">
											<button type="button" id="saveParameterSelection" class="btn btn-default action">
												<spring:message code="reports.action.saveParameterSelection"/>
											</button>
											<button type="button" id="clearSavedParameterSelection" class="btn btn-default action">
												<spring:message code="reports.action.clearSavedParameterSelection"/>
											</button>
										</div>
									</div>
								</div>
								<hr>
							</c:if>

							<div class="form-group">
								<div class="col-md-12">
									<div id="actionsDiv" style="text-align: center">
										<c:if test="${enableEmail}">
											<button type="button" id="emailButton" class="btn btn-default action"
													data-toggle="modal" data-target="#emailReportModal">
												<spring:message code="reports.action.email"/>
											</button>
										</c:if>
										<c:if test="${enablePrint}">
											<button type="button" id="printButton" class="btn btn-default action" onclick="PrintElem('#reportOutput')" >
												<spring:message code="reports.action.print"/>
											</button>
										</c:if>
										<c:if test="${enableSchedule}">
											<button type="button" id="schedule" class="btn btn-default action">
												<spring:message code="reports.action.schedule"/>
											</button>
										</c:if>
										<button type="button" id="runInNewPage" class="btn btn-default action">
											<spring:message code="reports.action.runInNewPage"/>
										</button>
										<c:if test="${enableRunInline}">
											<button type="submit" id="runInline" class="btn btn-primary action">
												<spring:message code="page.action.run"/>
											</button>
										</c:if>
									</div>
								</div>
							</div>
						</fieldset>
					</form>
				</div>
			</div>
		</div>
	</div>
</div>
