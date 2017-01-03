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

<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/js/bootstrap-select.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/eonasdan-datepicker/moment-with-locales.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/eonasdan-datepicker/js/bootstrap-datetimepicker.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/notify-combined-0.3.1.min.js"></script>

<script type="text/javascript">
	$(document).ready(function () {
		$("#schedule").click(function (e) {
			e.preventDefault();
			var url = "${pageContext.request.contextPath}/app/addJob.do";
			$('#parametersForm').attr('action', url).submit();
		});

		$("#emailReportSubmit").click(function (e) {
			e.preventDefault();

			//https://stackoverflow.com/questions/2122085/jquery-and-tinymce-textarea-value-doesnt-submit
			tinyMCE.triggerSave();

			$.ajax({
				type: 'POST',
				url: '${pageContext.request.contextPath}/app/emailReport.do',
				dataType: 'json',
				data: $('#emailReportForm').serialize(),
				success: function (response) //on recieve of reply
				{
					$("#emailReportModal").modal('hide');
//					 $(':input','#emailReportForm').val("");
					if (response.success) {
						$.notify("${fileSentText}", "success");
					} else {
						$.notify(response.errorMessage, "error");
					}
				},
				error: ajaxErrorHandler
			});
		});

		$("#runInNewPage").click(function () {
			$("#showInline").val("false");
			$("#parametersForm").submit();
		});

		$("#runInline").click(function (e) {
			e.preventDefault();

			$("#showInline").val("true");

			var $form = $(this).closest('form');

			//disable buttons
			$('.action').prop('disabled', true);

			var url = "${pageContext.request.contextPath}/app/runReport.do";
			$("#reportOutput").load(url, $form.serialize(),
					function (responseText, statusText, xhr) {
						//callback funtion for when jquery load has finished

						if (statusText === "success") {
							//make htmlgrid output sortable
							sorttable.DATE_RE = /^(\d\d?)[\/\.-](\d\d?)[\/\.-]((\d\d)?\d\d)$/;
							$('.sortable').each(function (i, obj) {
								sorttable.makeSortable(obj);
							});
						} else if (statusText === "error") {
							bootbox.alert("<b>${errorOccurredText}</b><br>"
									+ xhr.status + "<br>" + responseText);
						}

						//enable buttons
						$('.action').prop('disabled', false);

					});

		});

		$("#reportFormat").change(function () {
			toggleVisibleButtons();
		});

		toggleVisibleButtons(); //show/hide on page load

		$('.datepicker').datetimepicker({
			format: 'YYYY-MM-DD',
			locale: '${pageContext.response.locale}',
			keepInvalid: true
		});

		$('.datetimepicker').datetimepicker({
			format: 'YYYY-MM-DD HH:mm:ss',
			locale: '${pageContext.response.locale}',
			keepInvalid: true
		});

		//Enable Bootstrap-Select
		$('.selectpicker').selectpicker();

		//activate dropdown-hover. to make bootstrap-select open on hover
		//must come after bootstrap-select initialization
		$('button.dropdown-toggle').dropdownHover({
			delay: 100
		});

		//{container: 'body'} needed if tooltips shown on input-group element or button
		$("[data-toggle='tooltip']").tooltip({container: 'body'});


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
		var mywindow = window.open('', '${report.name}', 'height=400,width=600');
		mywindow.document.write('<html><head><title>${report.name}</title>');
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
		var enablePrint = ${enablePrint};
		if (enablePrint) {
			switch (reportFormat) {
				case 'htmlGrid':
				case 'htmlDataTable':
				case 'htmlFancy':
				case 'htmlPlain':
					$('#printButton').show();
					break;
				default:
					$('#printButton').hide();
			}
		}

		//show/hide email button
		var enableEmail = ${enableEmail};
		if (enableEmail) {
			switch (reportFormat) {
				case 'htmlGrid':
				case 'htmlDataTable':
				case 'htmlFancy':
				case 'htmlPlain':
					$('#emailButton').hide();
					break;
				default:
					$('#emailButton').show();
			}
		}
	}

</script>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/tinymce-4.3.8/tinymce.min.js"></script>
<script type="text/javascript">
	tinymce.init({
		selector: "textarea.editor",
		plugins: [
			"advlist autolink lists link image charmap print preview hr anchor pagebreak",
			"searchreplace visualblocks visualchars code",
			"nonbreaking table contextmenu directionality",
			"paste textcolor"
		],
		toolbar1: "insertfile undo redo | styleselect | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent",
		toolbar2: "print preview | forecolor backcolor | link image | code",
		image_advtab: true
	});
</script>

<c:if test="${error != null}">
	<div class="alert alert-danger alert-dismissable">
		<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
		<p><spring:message code="page.message.errorOccurred"/></p>
		<c:if test="${showErrors}">
			<t:displayStackTrace error="${error}"/>
		</c:if>
	</div>
</c:if>

<div class="row">
	<div class="col-md-6 col-md-offset-3">
		<div class="panel panel-success">
			<div class="panel-body text-center">
				${encode:forHtmlContent(report.name)}
			</div>
		</div>
	</div>
	<div class="row">
		<div class="col-md-6 col-md-offset-3">
			<div class="panel-group">
				<div class="panel panel-default">
					<div class="panel-heading">
						<h4 class="panel-title text-center">
							<a data-toggle="collapse" href="#collapse1">
								<spring:message code="jobs.text.parameters"/>
							</a>
						</h4>
					</div>
					<div id="collapse1" class="panel-collapse collapse in">
						<div class="panel-body">
							<spring:url var="formUrl" value="/app/runReport.do"/>
							<form id="parametersForm" class="form-horizontal" method="POST" action="${formUrl}">
								<fieldset>
									<input type="hidden" name="reportId" value="${report.reportId}">
									<input type="hidden" name="showInline" id="showInline" value="true">
									<input type="hidden" name="nextPage" id="nextPage" value="jobs.do">

									<c:set var="labelColClass" value="col-md-5" scope="request"/>
									<c:set var="inputColClass" value="col-md-7" scope="request"/>
									<jsp:include page="reportParameters.jsp"/>

									<c:if test="${isChart}">
										<div id="chartOptions">
											<div class="form-group">
												<label class="control-label col-md-5">
													<spring:message code="reports.label.show"/>
												</label>
												<div class="col-md-7">
													<label class="checkbox-inline">
														<input type="checkbox" name="showLegend" value=""
															   ${report.chartOptions.showLegend ? "checked" : ""}>
														<spring:message code="reports.label.showLegend"/>
													</label>
													<label class="checkbox-inline">
														<input type="checkbox" name="showLabels" value=""
															   ${report.chartOptions.showLabels ? "checked" : ""}>
														<spring:message code="reports.label.showLabels"/>
													</label>
													<label class="checkbox-inline">
														<input type="checkbox" name="showPoints" value=""
															   ${report.chartOptions.showPoints ? "checked" : ""}>
														<spring:message code="reports.label.showPoints"/>
													</label>
													<label class="checkbox-inline">
														<input type="checkbox" name="showData" value=""
															   ${report.chartOptions.showData ? "checked" : ""}>
														<spring:message code="reports.label.showData"/>
													</label>
												</div>
											</div>
											<div class="form-group">
												<label class="control-label col-md-5" for="chartWidth">
													<spring:message code="reports.label.width"/>
												</label>
												<div class="col-md-7">
													<input type="text" name="chartWidth" 
														   maxlength="4" class="form-control"
														   value="${report.chartOptions.width}">
												</div>
											</div>
											<div class="form-group">
												<label class="control-label col-md-5" for="chartHeight">
													<spring:message code="reports.label.height"/>
												</label>
												<div class="col-md-7">
													<input type="text" name="chartHeight" 
														   maxlength="4" class="form-control"
														   value="${report.chartOptions.height}">
												</div>
											</div>
											<c:if test="${enableSwapAxes}">
												<div class="form-group">
													<label class="control-label col-md-5" for="swapAxes">
														<spring:message code="reports.label.swapAxes"/>
													</label>
													<div class="col-md-7">
														<label>
															<input type="checkbox" name="swapAxes" id="swapAxes" value="">
														</label>
													</div>
												</div>
											</c:if>
											<input type="hidden" name="yAxisMin" value="${report.chartOptions.yAxisMin}"> 
											<input type="hidden" name="yAxisMax" value="${report.chartOptions.yAxisMax}"> 
											<input type="hidden" name="backgroundColor" value="${report.chartOptions.backgroundColor}"> 
											<input type="hidden" name="labelFormat" value="${report.chartOptions.labelFormat}"> 
										</div>
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
																   <c:if test="${report.parametersInOutput}">checked="checked"</c:if> value="">
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
															<input type="checkbox" name="showSql" id="showSql" value="">
														</label>
													</div>
												</div>
											</div>
										</c:if>
									</div>

									<div class="form-group">
										<div class="col-md-8 col-md-offset-2">
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
	</div>
</div>

<div id="emailReportModal" class="modal fade" role="dialog" 
	 aria-labelledby="emailReportLabel" aria-hidden="true" tabindex="-1">
	<div class="modal-dialog">
		<div class="modal-content">

			<form id="emailReportForm" class="form-horizontal" role="form" method="POST" action="${pageContext.request.contextPath}/app/emailReport.do">
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
								   readonly class="form-control" value="${sessionUser.email}"/>
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
								<textarea id="mailMessage" name="mailMessage" rows="5" cols="60" class="form-control editor">
								</textarea>
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
