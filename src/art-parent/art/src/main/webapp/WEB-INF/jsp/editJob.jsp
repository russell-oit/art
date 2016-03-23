<%-- 
    Document   : editJob
    Created on : 16-Mar-2016, 17:57:30
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<c:choose>
	<c:when test="${action == 'add'}">
		<spring:message code="page.title.addJob" var="pageTitle"/>
	</c:when>
	<c:when test="${action == 'edit'}">
		<spring:message code="page.title.editJob" var="pageTitle"/>
	</c:when>
</c:choose>

<spring:message code="reports.format.htmlPlain" var="htmlPlainText"/>
<spring:message code="reports.format.xlsZip" var="xlsZipText"/>
<spring:message code="reports.format.pdf" var="pdfText"/>
<spring:message code="reports.format.xls" var="xlsText"/>
<spring:message code="reports.format.xlsx" var="xlsxText"/>
<spring:message code="reports.format.tsvZip" var="tsvZipText"/>
<spring:message code="reports.format.png" var="pngText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-6 col-md-offset-3">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/eonasdan-datepicker/css/bootstrap-datetimepicker.min.css">
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/tinymce-4.0.19/tinymce.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-select-1.4.3/bootstrap-select-modified.min.js"></script>

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/eonasdan-datepicker/moment.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/eonasdan-datepicker/js/bootstrap-datetimepicker.min.js"></script>

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

		<script type="text/javascript">
			$(document).ready(function () {
				$(function () {
					$('a[id="configure"]').parent().addClass('active');
					$('a[href*="job.do"]').parent().addClass('active');
				});

				$(function () {
					//needed if tooltips shown on input-group element or button
					$("[data-toggle='tooltip']").tooltip({container: 'body'});
				});

				$('#startDatePicker').datetimepicker({
					format: 'YYYY-MM-DD HH:mm:ss'
				});
				$('#endDatePicker').datetimepicker({
					format: 'YYYY-MM-DD HH:mm:ss',
					useCurrent: false //Important! See issue #1075
				});
				$("#startDatePicker").on("dp.change", function (e) {
					$('#endDatePicker').data("DateTimePicker").minDate(e.date);
				});
				$("#endDatePicker").on("dp.change", function (e) {
					$('#startDatePicker').data("DateTimePicker").maxDate(e.date);
				});
				
				$('.datepicker').datetimepicker({
					format: 'YYYY-MM-DD'
				});

				$('.datetimepicker').datetimepicker({
					format: 'YYYY-MM-DD HH:mm:ss'
				});

				//Enable Bootstrap-Select
				$('.selectpicker').selectpicker({
					iconBase: 'fa',
					tickIcon: 'fa-check-square'
				});

				//activate dropdown-hover. to make bootstrap-select open on hover
				//must come after bootstrap-select initialization
				$('button.dropdown-toggle').dropdownHover({
					delay: 100
				});

				$("#jobType").change(function () {
					toggleVisibleFields();
					populateOutputFormatField();
				});

				toggleVisibleFields(); //show/hide on page load
				populateOutputFormatField();

				$('#name').focus();

			});
		</script>

		<script type="text/javascript">
			$(function () {
				$('#getSchedule').click(function () {
					var recordId = $('#schedules option:selected').val();

					$.ajax({
						type: 'POST',
						url: '${pageContext.request.contextPath}/app/getSchedule.do',
						dataType: 'json',
						data: {id: recordId},
						success: function (response) //on recieve of reply
						{
							var schedule = response.data;

							$('#scheduleMinute').val(schedule.minute);
							$('#scheduleHour').val(schedule.hour);
							$('#scheduleDay').val(schedule.day);
							$('#scheduleMonth').val(schedule.month);
							$('#scheduleWeekday').val(schedule.weekday);
						},
						error: ajaxErrorHandler
					});
				});
			});

			function populateOutputFormatField() {
				var list = $("#outputFormat");
				var jobType = $('#jobType option:selected').val();
				//https://stackoverflow.com/questions/11445970/accessing-hidden-field-value-in-jquery
				var reportTypeId = parseInt($('input[name="report.reportTypeId"]').val(), 10);

				list.empty();

				if (reportTypeId < 0) {
					//chart
					list.append(new Option('${pdfText}', 'pdf'));
					list.append(new Option('${pngText}', 'png'));
				} else if (reportTypeId === 115 || reportTypeId === 116) {
					//jasper report
					list.append(new Option('${pdfText}', 'pdf'));
					list.append(new Option('${xlsText}', 'xls'));
					list.append(new Option('${xlsxText}', 'xlsx'));
				} else if (reportTypeId === 115 || reportTypeId === 116) {
					//jxls
					list.append(new Option('${xlsText}', 'xls'));
				} else {
					//non-chart
					switch (jobType) {
						case 'Alert':
						case 'JustRun':
							list.append(new Option('', '--'));
							break;
						case 'EmailInline':
						case 'CondEmailInline':
							list.append(new Option('${htmlPlainText}', 'htmlPlain'));
							break;
						case 'EmailAttachment':
						case 'Publish':
						case 'CondEmailAttachment':
						case 'CondPublish':
							list.append(new Option('${htmlPlainText}', 'htmlPlain'));
							list.append(new Option('${xlsZipText}', 'xlsZip'));
							list.append(new Option('${pdfText}', 'pdf'));
							list.append(new Option('${xlsText}', 'xls'));
							list.append(new Option('${xlsxText}', 'xlsx'));
							list.append(new Option('${tsvZipText}', 'tsvZip'));
							break;
						default:
							list.append(new Option('', '--'));
					}
				}
			}

			function toggleVisibleFields() {
				var jobType = $('#jobType option:selected').val();

				//show/hide emailFields
				switch (jobType) {
					case 'CacheAppend':
					case 'CacheInsert':
					case 'JustRun':
						$("#emailFields").hide();
						break;
					default:
						$("#emailFields").show();
				}

				//show/hide cachedTableNameDiv
				toggleCachedTableNameVisibility(jobType);

			}

			function toggleCachedTableNameVisibility(jobType) {
				//show/hide cachedTableNameDiv
				switch (jobType) {
					case 'CacheAppend':
					case 'CacheInsert':
						$("#cachedTableNameDiv").show();
						break;
					default:
						$("#cachedTableNameDiv").hide();
				}

			}

			function toggleRunsToArchiveVisibility(jobType) {
				//show/hide runsToArchiveDiv
				switch (jobType) {
					case 'Publish':
					case 'CondPublish':
						$("#runsToArchiveDiv").show();
						break;
					default:
						$("#runsToArchiveDiv").hide();
				}

			}
		</script>
	</jsp:attribute>

	<jsp:attribute name="aboveMainPanel">
		<div class="text-right">
			<a href="${pageContext.request.contextPath}/docs/manual.htm#user-groups">
				<spring:message code="page.link.help"/>
			</a>
		</div>
	</jsp:attribute>

	<jsp:body>
		<spring:url var="formUrl" value="/app/saveJob.do"/>
		<form:form class="form-horizontal" method="POST" action="${formUrl}" modelAttribute="job">
			<fieldset>
				<c:if test="${formErrors != null}">
					<div class="alert alert-danger alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
						<spring:message code="page.message.formErrors"/>
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

				<input type="hidden" name="action" value="${action}">
				<input type="hidden" name="nextPage" value="${param.nextPage}">

				<fieldset>
					<legend><spring:message code="jobs.text.parameters"/></legend>
					<jsp:include page="reportParameters.jsp">
						<jsp:param name="reportParamsList" value="${reportParamsList}"/>
					</jsp:include>
				</fieldset>

				<fieldset>
					<legend><spring:message code="jobs.text.job"/></legend>
					<div class="form-group">
						<label class="control-label col-md-4">
							<spring:message code="page.label.id"/>
						</label>
						<div class="col-md-8">
							<c:if test="${action == 'edit'}">
								<form:input path="jobId" readonly="true" class="form-control"/>
							</c:if>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-md-4" for="name">
							<spring:message code="page.text.name"/>
						</label>
						<div class="col-md-8">
							<form:input path="name" maxlength="50" class="form-control"/>
							<form:errors path="name" cssClass="error"/>
						</div>
					</div>

					<div class="form-group">
						<label class="col-md-4 control-label " for="jobType">
							<spring:message code="jobs.label.jobType"/>
						</label>
						<div class="col-md-8">
							<form:select path="jobType" class="form-control selectpicker">
								<c:forEach var="jobType" items="${jobTypes}">
									<form:option value="${jobType}">
										<spring:message code="${jobType.localizedDescription}"/>
									</form:option>
								</c:forEach>
							</form:select>
							<form:errors path="jobType" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-md-4" for="active">
							<spring:message code="page.label.active"/>
						</label>
						<div class="col-md-8">
							<div class="checkbox">
								<form:checkbox path="active" id="active"/>
							</div>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="outputFormat">
							<spring:message code="jobs.label.outputFormat"/>
						</label>
						<div class="col-md-8">
							<form:select path="outputFormat" class="form-control">

							</form:select>
							<form:errors path="outputFormat" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-md-4" for="enableAudit">
							<spring:message code="jobs.label.enableAudit"/>
						</label>
						<div class="col-md-8">
							<div class="checkbox">
								<form:checkbox path="enableAudit" id="enableAudit"/>
							</div>
						</div>
					</div>
					<div id="runsToArchiveDiv" class="form-group">
						<label class="col-md-4 control-label " for="runsToArchive">
							<spring:message code="jobs.label.runsToArchive"/>
						</label>
						<div class="col-md-8">
							<div class="input-group">
								<form:input path="runsToArchive" maxlength="2" class="form-control"/>
							</div>
							<form:errors path="runsToArchive" cssClass="error"/>
						</div>
					</div>
					<div id="cachedTableNameDiv" class="form-group">
						<label class="col-md-4 control-label " for="cachedTableName">
							<spring:message code="jobs.label.cachedTableName"/>
						</label>
						<div class="col-md-8">
							<form:input path="cachedTableName" maxlength="200" class="form-control"/>
							<form:errors path="cachedTableName" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-md-4" for="allowSharing">
							<spring:message code="jobs.label.allowSharing"/>
						</label>
						<div class="col-md-8">
							<div class="checkbox">
								<form:checkbox path="allowSharing" id="allowSharing"/>
							</div>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-md-4" for="allowSplitting">
							<spring:message code="jobs.label.allowSplitting"/>
						</label>
						<div class="col-md-8">
							<div class="checkbox">
								<form:checkbox path="allowSplitting" id="allowSplitting"/>
							</div>
						</div>
					</div>

					<hr>
					<div class="form-group">
						<label class="col-sm-4 control-label">
							<spring:message code="jobs.label.owner"/>
						</label>
						<div class="col-sm-8">
							<p class="form-control-static">${job.user.username}</p>
						</div>
					</div>
					<form:hidden path="user.userId" />
					<form:hidden path="user.username" />
					<div class="form-group">
						<label class="col-sm-4 control-label">
							<spring:message code="page.text.report"/>
						</label>
						<div class="col-sm-8">
							<p class="form-control-static">${job.report.name}</p>
						</div>
					</div>
					<form:hidden path="report.reportId" />
					<form:hidden path="report.reportTypeId" />
					<form:hidden path="report.name" />

				</fieldset>

				<fieldset id="emailFields">
					<legend><spring:message code="jobs.text.email"/></legend>
					<div class="form-group">
						<label class="col-md-4 control-label " for="mailFrom">
							<spring:message code="jobs.label.mailFrom"/>
						</label>
						<div class="col-md-8">
							<form:input path="mailFrom" class="form-control"/>
							<form:errors path="mailFrom" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="mailTo">
							<spring:message code="jobs.label.mailTo"/>
						</label>
						<div class="col-md-8">
							<form:input path="mailTo" class="form-control"/>
							<form:errors path="mailTo" cssClass="error"/>
						</div>
					</div>
					<div id="datasourceDiv" class="form-group">
						<label class="col-md-4 control-label " for="recipientsQueryId">
							<spring:message code="jobs.label.mailRecipients"/>
						</label>
						<div class="col-md-8">
							<form:select path="recipientsQueryId" class="form-control selectpicker">
								<form:option value="0"><spring:message code="select.text.none"/></form:option>
									<option data-divider="true"></option>
								<c:forEach var="dynamicRecipientReport" items="${dynamicRecipientReports}">
									<form:option value="${dynamicRecipientReport.reportId}">
										${dynamicRecipientReport.name} 
									</form:option>
								</c:forEach>
							</form:select>
							<form:errors path="recipientsQueryId" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="mailCc">
							<spring:message code="jobs.label.mailCc"/>
						</label>
						<div class="col-md-8">
							<form:input path="mailCc" class="form-control"/>
							<form:errors path="mailCc" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="mailBcc">
							<spring:message code="jobs.label.mailBcc"/>
						</label>
						<div class="col-md-8">
							<form:input path="mailBcc" class="form-control"/>
							<form:errors path="mailBcc" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="mailSubject">
							<spring:message code="jobs.label.mailSubject"/>
						</label>
						<div class="col-md-8">
							<form:input path="mailSubject" class="form-control"/>
							<form:errors path="mailSubject" cssClass="error"/>
						</div>
					</div>
					<label class="col-md-12 control-label" style="text-align: center">
						<spring:message code="jobs.label.mailMessage"/>
					</label>
					<div class="form-group">
						<div class="col-md-12">
							<form:textarea path="mailMessage" rows="8" cols="60" class="form-control editor"/>
							<form:errors path="mailMessage" cssClass="error"/>
						</div>
					</div>
				</fieldset>

				<fieldset>
					<legend><spring:message code="jobs.text.schedule"/></legend>
					<div class="form-group">
						<label class="control-label col-md-4" for="schedules">
							<spring:message code="jobs.label.schedules"/>
						</label>
						<div class="col-md-8">
							<select name="schedules" id="schedules" class="form-control selectpicker">
								<option value="0">--</option>
								<c:forEach var="schedule" items="${schedules}">
									<option value="${schedule.scheduleId}">${schedule.name}</option>
								</c:forEach>
							</select>
							<button type="button" id="getSchedule" class="btn btn-default">
								<spring:message code="jobs.button.getSchedule"/>
							</button>
						</div>
					</div>

					<hr>
					<div class="form-group">
						<label class="col-md-4 control-label " for="scheduleMinute">
							<spring:message code="schedules.label.minute"/>
						</label>
						<div class="col-md-8">
							<form:input path="scheduleMinute" maxlength="100" class="form-control"/>
							<form:errors path="scheduleMinute" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="scheduleHour">
							<spring:message code="schedules.label.hour"/>
						</label>
						<div class="col-md-8">
							<form:input path="scheduleHour" maxlength="100" class="form-control"/>
							<form:errors path="scheduleHour" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="scheduleMonth">
							<spring:message code="schedules.label.month"/>
						</label>
						<div class="col-md-8">
							<form:input path="scheduleMonth" maxlength="100" class="form-control"/>
							<form:errors path="scheduleMonth" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="scheduleDay">
							<spring:message code="schedules.label.day"/>
						</label>
						<div class="col-md-8">
							<form:input path="scheduleDay" maxlength="100" class="form-control"/>
							<form:errors path="scheduleDay" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="scheduleWeekday">
							<spring:message code="schedules.label.weekday"/>
						</label>
						<div class="col-md-8">
							<form:input path="scheduleWeekday" maxlength="100" class="form-control"/>
							<form:errors path="scheduleWeekday" cssClass="error"/>
						</div>
					</div>

					<hr>
					<div class="form-group">
						<label class="col-md-4 control-label " for="startDateString">
							<spring:message code="jobs.label.startDate"/>
						</label>
						<div class="col-md-8">
							<div id="startDatePicker" class='input-group date datetimepicker'>
								<form:input path="startDateString" class="form-control"/>
								<span class="input-group-addon">
									<span class="glyphicon glyphicon-calendar"></span>
								</span>
							</div>
							<form:errors path="startDateString" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="endDate">
							<spring:message code="jobs.label.endDate"/>
						</label>
						<div class="col-md-8">
							<div id="endDatePicker" class='input-group date datetimepicker'>
								<form:input path="endDateString" class="form-control"/>
								<span class="input-group-addon">
									<span class="glyphicon glyphicon-calendar"></span>
								</span>
							</div>
							<form:errors path="endDateString" cssClass="error"/>
						</div>
					</div>
				</fieldset>

				<div class="form-group">
					<div class="col-md-12">
						<button type="submit" class="btn btn-primary pull-right">
							<spring:message code="page.button.save"/>
						</button>
					</div>
				</div>
			</fieldset>


		</form:form>
	</jsp:body>
</t:mainPageWithPanel>

