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
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:choose>
	<c:when test="${action == 'add'}">
		<spring:message code="page.title.addJob" var="pageTitle"/>
		<c:set var="panelTitle" value="${pageTitle}"/>
	</c:when>
	<c:when test="${action == 'edit'}">
		<spring:message code="page.title.editJob" var="panelTitle"/>
		<c:set var="pageTitle">
			${panelTitle} - ${job.name}
		</c:set>
	</c:when>
</c:choose>

<spring:message code="switch.text.yes" var="yesText"/>
<spring:message code="switch.text.no" var="noText"/>
<spring:message code="reports.format.htmlPlain" var="htmlPlainText"/>
<spring:message code="reports.format.xlsZip" var="xlsZipText"/>
<spring:message code="reports.format.pdf" var="pdfText"/>
<spring:message code="reports.format.xls" var="xlsText"/>
<spring:message code="reports.format.xlsx" var="xlsxText"/>
<spring:message code="reports.format.tsvZip" var="tsvZipText"/>
<spring:message code="reports.format.png" var="pngText"/>
<spring:message code="reports.format.html" var="htmlText"/>
<spring:message code="reports.format.docx" var="docxText"/>
<spring:message code="reports.format.odt" var="odtText"/>
<spring:message code="reports.format.pptx" var="pptxText"/>
<spring:message code="reports.format.ods" var="odsText"/>
<spring:message code="reports.format.csv" var="csvText"/>
<spring:message code="reports.format.slk" var="slkText"/>
<spring:message code="reports.format.tsv" var="tsvText"/>
<spring:message code="reports.format.txt" var="txtText"/>
<spring:message code="reports.format.txtZip" var="txtZipText"/>
<spring:message code="reports.format.csv" var="csvText"/>
<spring:message code="reports.format.csvZip" var="csvZipText"/>
<spring:message code="reports.format.file" var="fileText"/>
<spring:message code="reports.format.fileZip" var="fileZipText"/>
<spring:message code="reports.text.selectFile" var="selectFileText"/>
<spring:message code="reports.text.change" var="changeText"/>
<spring:message code="select.text.nothingSelected" var="nothingSelectedText"/>
<spring:message code="select.text.noResultsMatch" var="noResultsMatchText"/>
<spring:message code="select.text.selectedCount" var="selectedCountText"/>
<spring:message code="select.text.selectAll" var="selectAllText"/>
<spring:message code="select.text.deselectAll" var="deselectAllText"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="jobs.text.nextRunDate" var="nextRunDateText"/>

<t:mainPageWithPanel title="${pageTitle}" panelTitle="${panelTitle}"
					 mainColumnClass="col-md-6 col-md-offset-3" hasNotify="true">

	<jsp:attribute name="belowPanel">
		<div class="row">
			<div class="col-md-6 col-md-offset-3">
				<div class="alert alert-info">
					<jsp:include page="/WEB-INF/jsp/scheduleNotes.jsp" />
				</div>
			</div>
		</div>
	</jsp:attribute>

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-datetimepicker-4.17.47/css/bootstrap-datetimepicker.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-switch/css/bootstrap3/bootstrap-switch.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jasny-bootstrap-3.1.3/css/jasny-bootstrap.min.css">
	</jsp:attribute>

	<jsp:attribute name="headContent">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/moment-2.22.2/moment-with-locales.min.js"></script>

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
			}
		</script>
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jasny-bootstrap-3.1.3/js/jasny-bootstrap.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/tinymce-4.8.5/tinymce.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-datetimepicker-4.17.47/js/bootstrap-datetimepicker.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-switch/js/bootstrap-switch.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/ace-min-noconflict-1.4.2/ace.js" charset="utf-8"></script>

		<script type="text/javascript">
			tinymce.init(tinymceConfig);
		</script>

		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="jobsConfig"]').parent().addClass('active');

				//display current time. updates every 1000 milliseconds
				setInterval('updateClock()', 1000);

				//{container: 'body'} needed if tooltips shown on input-group element or button
				$("[data-toggle='tooltip']").tooltip({container: 'body'});

				$('#startDatePicker').datetimepicker({
					format: 'YYYY-MM-DD HH:mm:ss',
					locale: '${pageContext.response.locale}'
				});
				$('#endDatePicker').datetimepicker({
					format: 'YYYY-MM-DD HH:mm:ss',
					locale: '${pageContext.response.locale}',
					useCurrent: false //Important! See issue #1075
				});
				$("#startDatePicker").on("dp.change", function (e) {
					$('#endDatePicker').data("DateTimePicker").minDate(e.date);
				});
				$("#endDatePicker").on("dp.change", function (e) {
					$('#startDatePicker').data("DateTimePicker").maxDate(e.date);
				});

				//Enable Bootstrap-Select
				$('.selectpicker').selectpicker({
					liveSearch: true,
					noneSelectedText: '${nothingSelectedText}',
					noneResultsText: '${noResultsMatchText}',
					countSelectedText: '${selectedCountText}',
					selectAllText: '${selectAllText}',
					deselectAllText: '${deselectAllText}'
				});

				//activate dropdown-hover. to make bootstrap-select open on hover
				//must come after bootstrap-select initialization
				initializeSelectHover();

				//enable bootstrap-switch
				$('.switch-yes-no').bootstrapSwitch({
					onText: '${yesText}',
					offText: '${noText}'
				});

				$("#jobType").on("change", function () {
					toggleVisibleFields();
					populateOutputFormatField();
				});

				toggleVisibleFields(); //show/hide on page load
				populateOutputFormatField();

				$('#name').trigger("focus");

				$('#describeSchedule').on("click", function () {
					var second = $('#scheduleSecond').val();
					var minute = $('#scheduleMinute').val();
					var hour = $('#scheduleHour').val();
					var day = $('#scheduleDay').val();
					var month = $('#scheduleMonth').val();
					var weekday = $('#scheduleWeekday').val();
					var year = $('#scheduleYear').val();

					$.ajax({
						type: 'POST',
						url: '${pageContext.request.contextPath}/describeSchedule',
						dataType: 'json',
						data: {second: second, minute: minute, hour: hour, day: day,
							month: month, weekday: weekday, year: year},
						success: function (response) {
							if (response.success) {
								var scheduleDescription = response.data;
								$("#mainDescription").html(escapeHtmlContent(scheduleDescription.description));
								$("#mainNextRunDate").html(escapeHtmlContent(scheduleDescription.nextRunDateString));
							} else {
								notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
							}
						},
						error: function (xhr) {
							ajaxErrorHandler(xhr);
						}
					});
				});

				$("#schedules").on('changed.bs.select', function (event, clickedIndex, newValue, oldValue) {
					var scheduleId = $(this).find('option').eq(clickedIndex).val();

					if (scheduleId === '0') {
						$('#scheduleSecond').val('');
						$('#scheduleMinute').val('');
						$('#scheduleHour').val('');
						$('#scheduleDay').val('');
						$('#scheduleMonth').val('');
						$('#scheduleWeekday').val('');
						$('#scheduleYear').val('');
						$('#extraSchedules').val('');
						$('#holidays').val('');

						$("#scheduleTimeZone option").prop("selected", false);
						$("#scheduleTimeZone").selectpicker('refresh');

						$("#sharedHolidays option").prop("selected", false);
						$("#sharedHolidays").selectpicker('refresh');

						$("#mainDescription").html("");
						$("#mainNextRunDate").html("");
					} else {
						$.ajax({
							type: 'GET',
							url: '${pageContext.request.contextPath}/getSchedule',
							dataType: 'json',
							data: {id: scheduleId},
							success: function (response) {
								var schedule = response.data;

								if (response.success) {
									if (schedule !== null) {
										$('#scheduleSecond').val(schedule.second);
										$('#scheduleMinute').val(schedule.minute);
										$('#scheduleHour').val(schedule.hour);
										$('#scheduleDay').val(schedule.day);
										$('#scheduleMonth').val(schedule.month);
										$('#scheduleWeekday').val(schedule.weekday);
										$('#scheduleYear').val(schedule.year);
										$('#extraSchedules').val(schedule.extraSchedules);
										$('#holidays').val(schedule.holidays);
										$('#scheduleTimeZone').selectpicker('val', schedule.timeZone);

										//https://silviomoreto.github.io/bootstrap-select/methods/
										//https://stackoverflow.com/questions/19543285/use-jquery-each-to-iterate-through-object
										var sharedHolidayIds = [];
										$.each(schedule.sharedHolidays, function (index, holiday) {
											sharedHolidayIds.push(holiday.holidayId);
										});
										$('#sharedHolidays').selectpicker('val', sharedHolidayIds);

										$("#mainDescription").html("");
										$("#mainNextRunDate").html("");
									}
								} else {
									notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
								}
							},
							error: ajaxErrorHandler
						});
					}
				});

				$('#ajaxResponseContainer').on("click", ".alert .close", function () {
					$(this).parent().hide();
				});

				var optionsEditor = ace.edit("optionsEditor");
				optionsEditor.$blockScrolling = Infinity;
				optionsEditor.getSession().setMode("ace/mode/json");
				optionsEditor.setHighlightActiveLine(false);
				optionsEditor.setShowPrintMargin(false);
				optionsEditor.setOption("showLineNumbers", false);
				optionsEditor.setOption("maxLines", 20);
				optionsEditor.setOption("minLines", 7);
				document.getElementById('optionsEditor').style.fontSize = '14px';

				var options = $('#options');
				optionsEditor.getSession().setValue(options.val());
				optionsEditor.getSession().on('change', function () {
					options.val(optionsEditor.getSession().getValue());
				});

			});
		</script>

		<script type="text/javascript">
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
					list.append(new Option('${docxText}', 'docx'));
					list.append(new Option('${odtText}', 'odt'));
					list.append(new Option('${xlsxText}', 'xlsx'));
					list.append(new Option('${odsText}', 'ods'));
				} else if (reportTypeId === 117 || reportTypeId === 118) {
					//jxls
					list.append(new Option('${xlsxText}', 'xlsx'));
				} else if (reportTypeId === 122) {
					//freemarker
					list.append(new Option('${htmlText}', 'html'));
				} else if (reportTypeId === 131) {
					//thymeleaf
					list.append(new Option('${htmlText}', 'html'));
				} else if (reportTypeId === 153) {
					//velocity
					list.append(new Option('${htmlText}', 'html'));
				} else if (reportTypeId === 123 || reportTypeId === 124) {
					//xdocreport docx
					list.append(new Option('${docxText}', 'docx'));
					list.append(new Option('${pdfText}', 'pdf'));
					list.append(new Option('${htmlText}', 'html'));
				} else if (reportTypeId === 125 || reportTypeId === 126) {
					//xdocreport odt
					list.append(new Option('${odtText}', 'odt'));
					list.append(new Option('${pdfText}', 'pdf'));
					list.append(new Option('${htmlText}', 'html'));
				} else if (reportTypeId === 127 || reportTypeId === 128) {
					//xdocreport pptx
					list.append(new Option('${pptxText}', 'pptx'));
				} else if (reportTypeId === 141) {
					//fixed width
					list.append(new Option('${txtText}', 'txt'));
					list.append(new Option('${txtZipText}', 'txtZip'));
					list.append(new Option('${htmlText}', 'html'));
				} else if (reportTypeId === 152) {
					//csv
					list.append(new Option('${csvText}', 'csv'));
					list.append(new Option('${csvZipText}', 'csvZip'));
					list.append(new Option('${htmlText}', 'html'));
				} else if (reportTypeId === 110 || reportTypeId === 129) {
					//dashboard
					list.append(new Option('${pdfText}', 'pdf'));
				} else if (reportTypeId === 1) {
					//group
					list.append(new Option('${xlsxText}', 'xlsx'));
				} else if (reportTypeId === 162) {
					//file
					list.append(new Option('${fileText}', 'file'));
					list.append(new Option('${fileZipText}', 'fileZip'));
					list.append(new Option('${htmlText}', 'html'));
				} else {
					//tabular
					switch (jobType) {
						case 'Alert':
						case 'JustRun':
							list.append(new Option('--', '--'));
							break;
						case 'EmailInline':
						case 'CondEmailInline':
							list.append(new Option('${htmlPlainText}', 'htmlPlain'));
							break;
						case 'EmailAttachment':
						case 'Publish':
						case 'CondEmailAttachment':
						case 'CondPublish':
						case 'Burst':
			<c:forEach var="reportFormat" items="${fileReportFormats}">
							list.append(new Option('${reportFormat.value}', '${reportFormat.key}'));
			</c:forEach>
							break;
						case 'Print':
							list.append(new Option('${htmlPlainText}', 'htmlPlain'));
							list.append(new Option('${xlsxText}', 'xlsx'));
							list.append(new Option('${odsText}', 'ods'));
							list.append(new Option('${pdfText}', 'pdf'));
							list.append(new Option('${docxText}', 'docx'));
							list.append(new Option('${odtText}', 'odt'));
							list.append(new Option('${csvText}', 'csv'));
							list.append(new Option('${xlsText}', 'xls'));
							list.append(new Option('${slkText}', 'slk'));
							list.append(new Option('${tsvText}', 'tsv'));
							break;
						default:
							list.append(new Option('--', '--'));
					}
				}

				//set the selected item according to the job output format
				//https://jsfiddle.net/taditdash/dFK3K/
				var jobOutputFormat = "${job.outputFormat}";
				//https://stackoverflow.com/questions/5515310/is-there-a-standard-function-to-check-for-null-undefined-or-blank-variables-in
				//using .val() to set the value may cause the select to have a blank value
				//https://stackoverflow.com/questions/13343566/set-select-option-selected-by-value
				//https://bugs.jquery.com/ticket/8813
				//https://stackoverflow.com/questions/22983511/jquery-1-10-1-setting-non-existing-value-on-select
				if (jobOutputFormat) {
					$("#outputFormat > [value=" + jobOutputFormat + "]").attr("selected", "true");
				}

				$("#outputFormat").selectpicker('refresh');
			}

			function toggleVisibleFields() {
				var jobType = $('#jobType option:selected').val();
				var reportTypeId = parseInt($('input[name="report.reportTypeId"]').val(), 10);

				toggleEmailFieldsVisibility(jobType, reportTypeId);
				toggleCachedFieldsVisibility(jobType);
				toggleRunsToArchiveVisibility(jobType);
				toggleOutputFormatVisibility(jobType, reportTypeId);
			}

			function toggleEmailFieldsVisibility(jobType, reportTypeId) {
				//show/hide emailFields
				switch (jobType) {
					case 'CacheAppend':
					case 'CacheInsert':
					case 'JustRun':
					case 'Print':
						$("#emailFields").hide();
						break;
					case 'EmailInline':
					case 'CondEmailInline':
						if (reportTypeId === 122 || reportTypeId === 131
								|| reportTypeId === 153) {
							//freemarker, thymeleaf, velocity
							$("#mailMessageDiv").hide();
						}
						break;
					default:
						$("#emailFields").show();
						$("#mailMessageDiv").show();
				}
			}

			function toggleCachedFieldsVisibility(jobType) {
				//show/hide cachedFields
				switch (jobType) {
					case 'CacheAppend':
					case 'CacheInsert':
						$("#cachedFields").show();
						break;
					default:
						$("#cachedFields").hide();
				}
			}

			function toggleOutputFormatVisibility(jobType, reportTypeId) {
				//show/hide outputFormatDiv
				if (reportTypeId === 122
						|| reportTypeId === 131 || reportTypeId === 153
						|| reportTypeId === 117 || reportTypeId === 118) {
					//freemarker, thymeleaf, velocity, jxls
					$("#outputFormatDiv").hide();
				} else {
					switch (jobType) {
						case 'Alert':
						case 'JustRun':
						case 'CacheAppend':
						case 'CacheInsert':
							$("#outputFormatDiv").hide();
							break;
						default:
							$("#outputFormatDiv").show();
					}
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

	<jsp:attribute name="abovePanel">
		<div class="text-right">
			<a href="${pageContext.request.contextPath}/docs/Manual.html#scheduling">
				<spring:message code="page.link.help"/>
			</a>
		</div>
	</jsp:attribute>

	<jsp:body>
		<spring:url var="formUrl" value="/saveJob"/>
		<form:form class="form-horizontal" method="POST" action="${formUrl}" modelAttribute="job" enctype="multipart/form-data">
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
				<c:if test="${not empty message}">
					<div class="alert alert-danger alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
						<spring:message code="${message}"/>
					</div>
				</c:if>
				<c:if test="${not empty plainMessage}">
					<div class="alert alert-danger alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
						${encode:forHtmlContent(plainMessage)}
					</div>
				</c:if>

				<div id="ajaxResponseContainer">
					<div id="ajaxResponse">
					</div>
				</div>

				<input type="hidden" name="action" value="${action}">
				<input type="hidden" name="nextPage" value="${encode:forHtmlAttribute(param.nextPage)}">

				<form:hidden path="quartzCalendarNames" />

				<c:set var="labelColClass" value="col-md-4" scope="request"/>
				<c:set var="inputColClass" value="col-md-8" scope="request"/>

				<c:if test="${job.report.reportType.isChart()}">
					<jsp:include page="chartOptions.jsp"/>
				</c:if>

				<c:if test="${not empty reportParams}">
					<fieldset>
						<legend><spring:message code="jobs.text.parameters"/></legend>
						<jsp:include page="reportParameters.jsp"/>
						<div class="form-group">
							<label class="control-label col-md-4" for="showSelectedParameters">
								<spring:message code="reports.label.showSelectedParameters"/>
							</label>
							<div class="col-md-8">
								<div class="checkbox">
									<label>
										<input type="checkbox" name="showSelectedParameters" id="showSelectedParameters"
											   <c:if test="${reportOptions.showSelectedParameters}">checked="checked"</c:if> value="">
										</label>
									</div>
								</div>
							</div>
						</fieldset>
				</c:if>

				<fieldset>
					<legend><spring:message code="jobs.text.job"/></legend>
					<div class="form-group">
						<label class="col-md-4 control-label">
							<spring:message code="jobs.label.owner"/>
						</label>
						<div class="col-md-8">
							<p class="form-control-static">
								${encode:forHtmlContent(job.user.username)}
							</p>
						</div>
					</div>
					<form:hidden path="user.userId" />
					<form:hidden path="user.username" />
					<div class="form-group">
						<label class="col-md-4 control-label">
							<spring:message code="page.text.report"/>
						</label>
						<div class="col-md-8">
							<p class="form-control-static">
								<a href="${pageContext.request.contextPath}/reportConfig?reportId=${job.report.reportId}">
									${encode:forHtmlContent(job.report.getLocalizedName(pageContext.response.locale))}
								</a>
							</p>
						</div>
					</div>
					<form:hidden path="report.reportId" />
					<form:hidden path="report.reportTypeId" />
					<form:hidden path="report.name" />
					<hr>
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
							<form:input path="name" maxlength="100" class="form-control"/>
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
					<div id="outputFormatDiv" class="form-group">
						<label class="col-md-4 control-label " for="outputFormat">
							<spring:message code="jobs.label.outputFormat"/>
						</label>
						<div class="col-md-8">
							<form:select path="outputFormat" class="form-control selectpicker"/>
							<form:errors path="outputFormat" cssClass="error"/>
						</div>
					</div>
					<fieldset id="cachedFields">
						<div id="datasourceDiv" class="form-group">
							<label class="col-md-4 control-label " for="cachedDatasourceId">
								<spring:message code="jobs.label.cachedDatasource"/>
							</label>
							<div class="col-md-8">
								<form:select path="cachedDatasourceId" class="form-control selectpicker">
									<form:option value="0">--</form:option>
										<option data-divider="true"></option>
									<c:forEach var="datasource" items="${datasources}">
										<c:set var="datasourceStatus">
											<t:displayActiveStatus active="${datasource.active}" hideActive="true"/>
										</c:set>
										<form:option value="${datasource.datasourceId}"
													 data-content="${datasource.name} ${datasourceStatus}">
											${datasource.name} 
										</form:option>
									</c:forEach>
								</form:select>
								<form:errors path="cachedDatasourceId" cssClass="error"/>
							</div>
						</div>
						<div class="form-group">
							<label class="col-md-4 control-label " for="cachedTableName">
								<spring:message code="jobs.label.cachedTableName"/>
							</label>
							<div class="col-md-8">
								<form:input path="cachedTableName" maxlength="30" class="form-control"/>
								<form:errors path="cachedTableName" cssClass="error"/>
							</div>
						</div>
					</fieldset>
					<div class="form-group">
						<label class="control-label col-md-4" for="active">
							<spring:message code="page.label.active"/>
						</label>
						<div class="col-md-8">
							<div class="checkbox">
								<form:checkbox path="active" id="active" class="switch-yes-no"/>
							</div>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-md-4" for="enableAudit">
							<spring:message code="jobs.label.enableAudit"/>
						</label>
						<div class="col-md-8">
							<div class="checkbox">
								<form:checkbox path="enableAudit" id="enableAudit" class="switch-yes-no"/>
							</div>
						</div>
					</div>
					<div id="runsToArchiveDiv" class="form-group">
						<label class="col-md-4 control-label " for="runsToArchive">
							<spring:message code="jobs.label.runsToArchive"/>
						</label>
						<div class="col-md-8">
							<form:input type="number" path="runsToArchive" maxlength="3" class="form-control"/>
							<form:errors path="runsToArchive" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-md-4" for="allowSharing">
							<spring:message code="jobs.label.allowSharing"/>
						</label>
						<div class="col-md-8">
							<div class="checkbox">
								<form:checkbox path="allowSharing" id="allowSharing" class="switch-yes-no"/>
							</div>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-md-4" for="allowSplitting">
							<spring:message code="jobs.label.allowSplitting"/>
						</label>
						<div class="col-md-8">
							<div class="checkbox">
								<form:checkbox path="allowSplitting" id="allowSplitting" class="switch-yes-no"/>
							</div>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="fixedFileName">
							<spring:message code="jobs.label.fixedFileName"/>
						</label>
						<div class="col-md-8">
							<form:textarea path="fixedFileName" rows="3" cols="40" class="form-control" maxlength="1000"/>
							<form:errors path="fixedFileName" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="destinations">
							<spring:message code="jobs.label.destinations"/>
						</label>
						<div class="col-md-8">
							<form:select path="destinations" class="form-control selectpicker"
										 multiple="true" data-actions-box="true">
								<c:forEach var="destination" items="${destinations}">
									<c:set var="destinationStatus">
										<t:displayActiveStatus active="${destination.active}" hideActive="true"/>
									</c:set>
									<c:if test="${not empty job.destinations}">
										<c:set var="selected">
											${job.destinations.contains(destination) ? "selected" : ""}
										</c:set>
									</c:if>
									<option value="${destination.destinationId}" ${selected}
											data-content="${encode:forHtmlAttribute(destination.name)}&nbsp;${encode:forHtmlAttribute(destinationStatus)}">
										${encode:forHtmlContent(destination.name)}
									</option>
								</c:forEach>
							</form:select>
							<form:errors path="destinations" cssClass="error"/>
						</div>
					</div>
					<div id="subDirectoryDiv" class="form-group">
						<label class="col-md-4 control-label " for="subDirectory">
							<spring:message code="destinations.label.subDirectory"/>
						</label>
						<div class="col-md-8">
							<form:input path="subDirectory" maxlength="100" class="form-control"/>
							<form:errors path="subDirectory" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="batchFile">
							<spring:message code="jobs.label.batchFile"/>
						</label>
						<div class="col-md-8">
							<form:input path="batchFile" maxlength="50" class="form-control"/>
							<form:errors path="batchFile" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="preRunReport">
							<spring:message code="jobs.label.preRunReport"/>
						</label>
						<div class="col-md-8">
							<form:input path="preRunReport" maxlength="50" class="form-control"/>
							<form:errors path="preRunReport" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="postRunReport">
							<spring:message code="jobs.label.postRunReport"/>
						</label>
						<div class="col-md-8">
							<form:input path="postRunReport" maxlength="50" class="form-control"/>
							<form:errors path="postRunReport" cssClass="error"/>
						</div>
					</div>
				</fieldset>

				<fieldset id="emailFields">
					<legend><spring:message code="jobs.text.email"/></legend>
					<div class="form-group">
						<label class="col-md-4 control-label " for="mailFrom">
							<spring:message code="jobs.label.mailFrom"/>
						</label>
						<div class="col-md-8">
							<form:input path="mailFrom" readonly="${sessionUser.hasPermission('configure_jobs') ? 'false' : 'true'}" class="form-control"/>
							<form:errors path="mailFrom" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="mailTo">
							<spring:message code="jobs.label.mailTo"/>
						</label>
						<div class="col-md-8">
							<form:input path="mailTo" maxlength="254" class="form-control"/>
							<form:errors path="mailTo" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="recipientsReportId">
							<spring:message code="jobs.label.mailRecipients"/>
						</label>
						<div class="col-md-8">
							<form:select path="recipientsReportId" class="form-control selectpicker">
								<form:option value="0">--</form:option>
									<option data-divider="true"></option>
								<c:forEach var="dynamicRecipientReport" items="${dynamicRecipientReports}">
									<form:option value="${dynamicRecipientReport.reportId}">
										${dynamicRecipientReport.getLocalizedName(pageContext.response.locale)} 
									</form:option>
								</c:forEach>
							</form:select>
							<form:errors path="recipientsReportId" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="mailCc">
							<spring:message code="jobs.label.mailCc"/>
						</label>
						<div class="col-md-8">
							<form:input path="mailCc" maxlength="254" class="form-control"/>
							<form:errors path="mailCc" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="mailBcc">
							<spring:message code="jobs.label.mailBcc"/>
						</label>
						<div class="col-md-8">
							<form:input path="mailBcc" maxlength="254" class="form-control"/>
							<form:errors path="mailBcc" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="mailSubject">
							<spring:message code="jobs.label.mailSubject"/>
						</label>
						<div class="col-md-8">
							<form:input path="mailSubject" maxlength="1000" class="form-control"/>
							<form:errors path="mailSubject" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-md-4" for="emailTemplate">
							<spring:message code="reports.label.template"/>
						</label>
						<div class="col-md-8">
							<div>
								<form:input path="emailTemplate" maxlength="100" class="form-control"/>
								<form:errors path="emailTemplate" cssClass="error"/>
							</div>
							<div class="fileinput fileinput-new" data-provides="fileinput">
								<span class="btn btn-default btn-file">
									<span class="fileinput-new">${selectFileText}</span>
									<span class="fileinput-exists">${changeText}</span>
									<input type="file" name="emailTemplateFile">
								</span>
								<span class="fileinput-filename"></span>
								<a href="#" class="close fileinput-exists" data-dismiss="fileinput" style="float: none">&times;</a>
							</div>
							<div class="checkbox">
								<label>
									<form:checkbox path="overwriteFiles"/>
									<spring:message code="page.checkbox.overwriteFiles"/>
								</label>
							</div>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="smtpServer">
							<spring:message code="settings.label.smtpServer"/>
						</label>
						<div class="col-md-8">
							<form:select path="smtpServer" class="form-control selectpicker">
								<form:option value="0">--</form:option>
									<option data-divider="true"></option>
								<c:forEach var="smtpServer" items="${smtpServers}">
									<c:set var="smtpServerStatus">
										<t:displayActiveStatus active="${smtpServer.active}" hideActive="true"/>
									</c:set>
									<form:option value="${smtpServer.smtpServerId}"
												 data-content="${smtpServer.name} ${smtpServerStatus}">
										${smtpServer.name} 
									</form:option>
								</c:forEach>
							</form:select>
							<form:errors path="smtpServer" cssClass="error"/>
						</div>
					</div>
					<div id="mailMessageDiv">
						<label class="col-md-12 control-label" style="text-align: center">
							<spring:message code="jobs.label.mailMessage"/>
						</label>
						<div class="form-group">
							<div class="col-md-12">
								<form:textarea path="mailMessage" rows="8" cols="60" class="form-control editor"/>
								<input name="image" type="file" id="upload" style="display:none;" onchange="">
								<form:errors path="mailMessage" cssClass="error"/>
							</div>
						</div>
					</div>
				</fieldset>

				<fieldset>
					<legend><spring:message code="jobs.text.schedule"/></legend>
					<div class="form-group">
						<label class="col-md-4 control-label " for="schedule">
							<spring:message code="jobs.text.fixedSchedule"/>
						</label>
						<div class="col-md-8">
							<form:select path="schedule" class="form-control selectpicker">
								<form:option value="0">--</form:option>
									<option data-divider="true"></option>
								<form:options items="${schedules}" itemLabel="name" itemValue="scheduleId"/>
							</form:select>
							<form:errors path="schedule" cssClass="error"/>
						</div>
					</div>

					<hr>
					<div class="form-group">
						<label class="control-label col-md-4" for="schedules">
							<spring:message code="jobs.label.schedules"/>
						</label>
						<div class="col-md-8">
							<p>
								<select name="schedules" id="schedules" class="form-control selectpicker">
									<option value="0">--</option>
									<option data-divider="true"></option>
									<c:forEach var="schedule" items="${schedules}">
										<option value="${schedule.scheduleId}">${encode:forHtmlContent(schedule.name)}</option>
									</c:forEach>
								</select>
							</p>
							<input type="text" id="clock" readonly class="form-control"/>
						</div>
					</div>

					<div class="form-group">
						<label class="col-md-4 control-label " for="scheduleSecond">
							<spring:message code="schedules.label.second"/>
						</label>
						<div class="col-md-8">
							<form:input path="scheduleSecond" maxlength="100" class="form-control"/>
							<form:errors path="scheduleSecond" cssClass="error"/>
						</div>
					</div>
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
						<label class="col-md-4 control-label " for="scheduleDay">
							<spring:message code="schedules.label.day"/>
						</label>
						<div class="col-md-8">
							<form:input path="scheduleDay" maxlength="100" class="form-control"/>
							<form:errors path="scheduleDay" cssClass="error"/>
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
						<label class="col-md-4 control-label " for="scheduleWeekday">
							<spring:message code="schedules.label.weekday"/>
						</label>
						<div class="col-md-8">
							<form:input path="scheduleWeekday" maxlength="100" class="form-control"/>
							<form:errors path="scheduleWeekday" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="scheduleYear">
							<spring:message code="schedules.label.year"/>
						</label>
						<div class="col-md-8">
							<form:input path="scheduleYear" maxlength="100" class="form-control"/>
							<form:errors path="scheduleYear" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="scheduleTimeZone">
							<spring:message code="page.label.timeZone"/>
						</label>
						<div class="col-md-8">
							<form:select path="scheduleTimeZone" class="form-control selectpicker">
								<form:option value="${serverTimeZone}">${serverTimeZoneDescription}</form:option>
									<option data-divider="true"></option>
								<form:options items="${timeZones}"/>
							</form:select>
							<form:errors path="scheduleTimeZone" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<div class="col-md-8 col-md-offset-4">
							<button type="button" id="describeSchedule" class="btn btn-default">
								<spring:message code="schedules.button.describe"/>
							</button>
							<div id="mainScheduleDescriptionDiv">
								<p>
								<pre id="mainDescription">${encode:forHtmlContent(mainScheduleDescription)}</pre>
								<b><spring:message code="jobs.text.nextRunDate"/>:</b> 
								<pre id="mainNextRunDate"><fmt:formatDate value="${nextRunDate}" pattern="${dateDisplayPattern}"/></pre>
								</p>
							</div>
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

					<hr>
					<div class="form-group">
						<label class="col-md-4 control-label " for="extraSchedules">
							<spring:message code="jobs.label.extraSchedules"/>
						</label>
						<div class="col-md-8">
							<form:textarea path="extraSchedules" rows="3" cols="40" class="form-control"/>
							<form:errors path="extraSchedules" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="holidays">
							<spring:message code="schedules.label.holidays"/>
						</label>
						<div class="col-md-8">
							<form:textarea path="holidays" rows="3" cols="40" class="form-control"/>
							<form:errors path="holidays" cssClass="error"/>
						</div>
					</div>

					<hr>
					<div class="form-group">
						<label class="col-md-4 control-label " for="sharedHolidays">
							<spring:message code="schedules.label.sharedHolidays"/>
						</label>
						<div class="col-md-8">
							<form:select path="sharedHolidays" items="${holidays}" multiple="true" 
										 itemLabel="name" itemValue="holidayId" 
										 class="form-control selectpicker"
										 data-actions-box="true"
										 />
							<form:errors path="sharedHolidays" cssClass="error"/>
						</div>
					</div>
				</fieldset>

				<hr>
				<div class="form-group">
					<label class="col-md-4 control-label " for="errorNotificationTo">
						<spring:message code="jobs.label.errorNotificationEmail"/>
					</label>
					<div class="col-md-8">
						<form:input path="errorNotificationTo" maxlength="500" class="form-control"/>
						<form:errors path="errorNotificationTo" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-12" style="text-align: center" for="options">
						<spring:message code="page.label.options"/>
					</label>
					<div class="col-md-12">
						<form:hidden path="options"/>
						<div id="optionsEditor" style="height: 200px; width: 100%; border: 1px solid black"></div>
					</div>
				</div>

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

