<%-- 
    Document   : selfServiceReports
    Created on : 24-Dec-2018, 18:13:00
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.selfServiceReports" var="pageTitle"/>

<spring:message code="page.text.search" var="searchText" javaScriptEscape="true"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText" javaScriptEscape="true"/>
<spring:message code="select.text.nothingSelected" var="nothingSelectedText" javaScriptEscape="true"/>
<spring:message code="select.text.noResultsMatch" var="noResultsMatchText" javaScriptEscape="true"/>
<spring:message code="select.text.selectedCount" var="selectedCountText" javaScriptEscape="true"/>
<spring:message code="dialog.button.cancel" var="cancelText" javaScriptEscape="true"/>
<spring:message code="dialog.button.ok" var="okText" javaScriptEscape="true"/>
<spring:message code="reports.message.reportSaved" var="reportSavedText" javaScriptEscape="true"/>
<spring:message code="reports.message.reportDeleted" var="reportDeletedText" javaScriptEscape="true"/>
<spring:message code="dialog.title.saveReport" var="saveReportText" javaScriptEscape="true"/>
<spring:message code="dialog.message.deleteRecord" var="deleteRecordText" javaScriptEscape="true"/>
<spring:message code="reports.message.cannotDeleteReport" var="cannotDeleteReportText" javaScriptEscape="true"/>

<t:mainPage title="${pageTitle}">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jQuery-QueryBuilder-2.5.2/css/query-builder.default.min.css" /> 
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-datetimepicker-4.17.47/css/bootstrap-datetimepicker.min.css">
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jQuery-QueryBuilder-2.5.2/js/query-builder.standalone.min.js"></script>
		<c:if test="${not empty languageFileName}">
			<script type="text/javascript" src="${pageContext.request.contextPath}/js/jQuery-QueryBuilder-2.5.2/i18n/${encode:forHtmlAttribute(languageFileName)}"></script>
		</c:if>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/multiselect-2.5.5/js/multiselect.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootbox-4.4.0.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/notifyjs-0.4.2/notify.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/moment-2.22.2/moment-with-locales.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-datetimepicker-4.17.47/js/bootstrap-datetimepicker.min.js"></script>

		<script>
			$(function () {
				$('a[id="selfService"]').parent().addClass('active');
				$('a[href*="selfServiceReports"]').parent().addClass('active');

				$('.selectpicker').selectpicker({
					liveSearch: true,
					noneSelectedText: '${nothingSelectedText}',
					noneResultsText: '${noResultsMatchText}',
					countSelectedText: '${selectedCountText}'
				});

				loadViews();
				loadSelfServiceReports();

				function loadViews() {
					$.ajax({
						type: 'GET',
						dataType: "json",
						url: '${pageContext.request.contextPath}/getViews',
						success: function (response) {
							if (response.success) {
								//https://github.com/silviomoreto/bootstrap-select/issues/1151
								var reports = response.data;
								var options = "<option value='0'>--</option>";
								$.each(reports, function (index, report) {
									options += "<option value='" + report.reportId + "'>" + report.name2 + "</option>";
								});
								var select = $("#views");
								select.empty();
								select.append(options);
								select.selectpicker('refresh');
							} else {
								notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
							}
						},
						error: function (xhr) {
							showUserAjaxError(xhr, '${errorOccurredText}');
						}
					});
				}

				$("#views").on('changed.bs.select', function (event, clickedIndex, newValue, oldValue) {
					$("#selfServiceReports").val('0').selectpicker('refresh');
					//https://stackoverflow.com/questions/36944647/bootstrap-select-on-click-get-clicked-value
					var option = $(this).find('option').eq(clickedIndex);
					populateDetails(option);

					$("#newReportLink").hide();
					$("#deleteReport").hide();
					$("#reportId").val('');
				});

				$("#selfServiceReports").on('changed.bs.select', function (event, clickedIndex, newValue, oldValue) {
					$("#views").val('0').selectpicker('refresh');
					//https://stackoverflow.com/questions/36944647/bootstrap-select-on-click-get-clicked-value
					var option = $(this).find('option').eq(clickedIndex);
					populateDetails(option);

					var reportId = option.val();
					var reportName = option.text();

					showDeleteReport(reportName, reportId);
				});

				function populateDetails(option) {
					$("#reportOutput").empty();

					var reportId = option.val();
					$("#viewReportId").val(reportId);

					//https://stackoverflow.com/questions/27347004/jquery-val-integer-datatype-comparison
					if (reportId === '0') {
						reset();
					} else {
						$.ajax({
							type: 'GET',
							url: '${pageContext.request.contextPath}/getViewDetails',
							data: {reportId: reportId},
							success: function (response) {
								if (response.success) {
									var result = response.data;
									var conditionColumns = result.conditionColumns;
									var fromColumns = result.fromColumns;
									var toColumns = result.toColumns;
									var selfServiceOptionsString = result.selfServiceOptions;
									var reportOptionsString = result.reportOptions;

									var fromOptions = "";
									var toOptions = "";

									$.each(fromColumns, function (index, column) {
										fromOptions += createOptionForColumn(column);
									});

									var fromSelect = $("#multiselect");
									fromSelect.empty();
									fromSelect.append(fromOptions);

									$.each(toColumns, function (index, column) {
										toOptions += createOptionForColumn(column);
									});

									var toSelect = $("#multiselect_to");
									toSelect.empty();
									toSelect.append(toOptions);

									var ruleObject;
									if (selfServiceOptionsString) {
										var selfServiceOptions = JSON.parse(selfServiceOptionsString);
										ruleObject = selfServiceOptions.jqueryRule;
									}

									var viewOptions;
									if (reportOptionsString) {
										var reportOptions = JSON.parse(reportOptionsString);
										var viewOptions = reportOptions.view;
									}

									updateBuilder(conditionColumns, ruleObject, viewOptions);
								} else {
									notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
								}
							},
							error: function (xhr) {
								showUserAjaxError(xhr, '${errorOccurredText}');
							}
						});
					}
				}

				function createOptionForColumn(column) {
					var label = escapeHtmlContent(column.label);
					var description = escapeHtmlContent(column.description);
					var userLabel = escapeHtmlContent(column.userLabel);

					return "<option value='" + label
							+ "' data-type='" + column.type
							+ "' title='" + description + "'>"
							+ userLabel
							+ "</option>";
				}

				function loadSelfServiceReports() {
					$.ajax({
						type: 'GET',
						dataType: "json",
						url: '${pageContext.request.contextPath}/getEditSelfService',
						success: function (response) {
							if (response.success) {
								//https://github.com/silviomoreto/bootstrap-select/issues/1151
								var reports = response.data;
								var options = "<option value='0'>--</option>";
								$.each(reports, function (index, report) {
									options += "<option value='" + report.reportId
											+ "' data-view-report-id='" + report.viewReportId + "'>"
											+ report.name2
											+ "</option>";
								});
								var select = $("#selfServiceReports");
								select.empty();
								select.append(options);
								select.selectpicker('refresh');
							} else {
								notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
							}
						},
						error: function (xhr) {
							showUserAjaxError(xhr, '${errorOccurredText}');
						}
					});
				}

				$('#ajaxResponseContainer').on("click", ".alert .close", function () {
					$(this).parent().hide();
				});

				$('#multiselect').multiselect({
					search: {
						left: '<input type="text" class="form-control" placeholder="${searchText}" />',
						right: '<input type="text" class="form-control" placeholder="${searchText}" />'
					},
					fireSearch: function (value) {
						return value.length > 0;
					},
					sort: false
				});

				$('#preview').on('click', function () {
					$('#preview').prop('disabled', true);

					var viewId = $("#views").val();
					var selfServiceReportId = $("#selfServiceReports").val();
					var reportId;
					if (viewId === '0') {
						reportId = selfServiceReportId;
					} else {
						reportId = viewId;
					}

					var selfServiceOptionsString = getSelfServiceOptionsString();

					var limit = $("#limit").val();
					var runId = '${runId}';

					var data = {testRun: true, reportId: reportId,
						selfServicePreview: true, reportFormat: "htmlDataTable",
						selfServiceOptions: selfServiceOptionsString,
						showInline: true, limit: limit, runId: runId};

					if ($('#showSql').is(':checked')) {
						data.showSql = true;
					}

					$("#reportOutput").empty();
					
					$("#cancelQueryDiv-${runId}").show();

					//https://stackoverflow.com/questions/10398783/jquery-form-serialize-and-other-parameters
					$.ajax({
						type: "POST",
						url: "${pageContext.request.contextPath}/runReport",
						data: data,
						success: function (data) {
							$("#reportOutput").html(data);
						},
						error: function (xhr) {
							//https://stackoverflow.com/questions/6186770/ajax-request-returns-200-ok-but-an-error-event-is-fired-instead-of-success
							showUserAjaxError(xhr, '${errorOccurredText}');
						},
						complete: function () {
							$('#preview').prop('disabled', false);
							$("#cancelQueryDiv-${runId}").hide();
						}
					});
				});

				$("#deleteReport").on("click", function () {
					var reportName = $(this).attr("data-report-name");
					reportName = escapeHtmlContent(reportName);
					var reportId = $(this).attr("data-report-id");

					bootbox.confirm({
						message: "${deleteRecordText}: <b>" + reportName + "</b>",
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
									url: "${pageContext.request.contextPath}/deleteSelfService",
									data: {id: reportId},
									success: function (response) {
										var nonDeletedRecords = response.data;
										if (response.success) {
											$("#selfServiceReports option[value='" + reportId + "']").remove();
											resetAll();
											$.notify("${reportDeletedText}", "success");
										} else if (nonDeletedRecords !== null && nonDeletedRecords.length > 0) {
											$.notify("${cannotDeleteReportText}", "error");
										} else {
											notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
										}
									},
									error: function (xhr) {
										showUserAjaxError(xhr, '${errorOccurredText}');
									}
								});
							} //end if result
						} //end callback
					}); //end bootbox confirm
				});

				initializeBuilder();

			});

			function resetAll() {
				reset();
				$("#views").val('0').selectpicker('refresh');
				$("#selfServiceReports").val('0').selectpicker('refresh');
				$("#deleteReport").hide();
				$("#reportOutput").empty();
			}

			function reset() {
				$('#multiselect').empty();
				$('#multiselect_to').empty();
				$("#whereDiv").hide();
				$("#newReportLink").hide();
			}

			function getSelfServiceOptionsString() {
				var selectedColumns = $("#multiselect_to option").map(function () {
					return $(this).val();
				}).get();

				//https://stackoverflow.com/questions/24403732/check-if-array-is-empty-or-does-not-exist-js
				if (selectedColumns.length === 0) {
					selectedColumns = $("#multiselect option").map(function () {
						return $(this).val();
					}).get();
				}

				var selfServiceOptions = {};
				selfServiceOptions.columns = selectedColumns;

				var ruleObject = $('#builder').queryBuilder('getRules');
				selfServiceOptions.jqueryRule = ruleObject;

				var selfServiceOptionsString = JSON.stringify(selfServiceOptions);
				return selfServiceOptionsString;
			}

			var dummyFilter = [
				{
					id: 'placeholder',
					type: 'string'
				}
			];

			function initializeBuilder() {
				$('#builder').queryBuilder({
					plugins: ['bt-tooltip-errors', 'not-group'],
					filters: dummyFilter
				});
			}

			function updateBuilder(conditionColumns, ruleObject, viewOptions) {
				if (conditionColumns && conditionColumns.length > 0) {
					var filters = createFilters(conditionColumns, viewOptions);
					updateFilters(filters);
					if (ruleObject) {
						$('#builder').queryBuilder('setRules', ruleObject);
					}
					$("#builderDiv").show();
				} else {
					updateFilters(dummyFilter);
					$("#builderDiv").hide();
				}
				$("#whereDiv").show();
			}

			function updateFilters(filters) {
				var force = true;
				$('#builder').queryBuilder('setFilters', force, filters);
				$('#builder').queryBuilder('reset');
			}

			function createFilters(conditionColumns, viewOptions) {
				var filters = [];
				var ids = [];

				var valueSeparator;
				var filterOptions;
				var allColumnName;

				if (viewOptions) {
					valueSeparator = viewOptions.valueSeparator;
					filterOptions = viewOptions.filterOptions;
					allColumnName = viewOptions.allColumnName;
				}

				if (valueSeparator === undefined) {
					valueSeparator = ",";
				}

				if (allColumnName === undefined) {
					allColumnName = "all";
				}

				$.each(conditionColumns, function (index, column) {
					//use label as id rather than arbitrary value
					//filter id in rules must match that of the condition columns and condition columns sort order may change
					var id = column.label;

					if ($.inArray(id, ids) !== -1) {
						id += index;
					}

					ids.push(id);

					var filter = {
						id: id,
						field: column.label,
						label: column.userLabel,
						type: column.type
					};

					//https://github.com/mistic100/jQuery-QueryBuilder/issues/418
					if (valueSeparator) {
						filter.input = 'text';
						filter.value_separator = valueSeparator;
					} else {
						//explicitly specify value separator for itfsw query builder
						filter.value_separator = ',';
					}

					if (column.type === 'date' || column.type === 'datetime'
							|| column.type === 'time') {

						var datePickerConfig = {
							locale: '${pageContext.response.locale}',
							keepInvalid: true,
							useStrict: true
						};

						//https://github.com/itfsw/QueryBuilder/blob/master/src/main/java/com/itfsw/query/builder/support/filter/DatetimeConvertFilter.java
						if (column.type === 'date') {
							datePickerConfig.format = 'YYYY-MM-DD';
						} else if (column.type === 'datetime') {
							datePickerConfig.format = 'YYYY-MM-DD HH:mm:ss';
						} else if (column.type === 'time') {
							datePickerConfig.format = 'HH:mm:ss';
						}

						filter.plugin = 'datetimepicker';
						filter.input_event = 'dp.change';
						filter.plugin_config = datePickerConfig;
					}

					//https://stackoverflow.com/questions/7364150/find-object-by-id-in-an-array-of-javascript-objects
					if (filterOptions) {
						var result = $.grep(filterOptions, function (option) {
							return option.column === column.label || option.column === allColumnName;
						});

						if (result.length > 0) {
							$.extend(filter, result[0].options);
						}
					}

					filters.push(filter);
				});

				return filters;
			}

			function showDeleteReport(reportName, reportId) {
				$("#deleteReport").attr("data-report-name", reportName);
				$("#deleteReport").attr("data-report-id", reportId);
				$("#deleteReport").show();
				$("#reportId").val(reportId);
			}
		</script>
	</jsp:attribute>

	<jsp:body>
		<div class="row">
			<div class="col-md-12">
				<c:if test="${error != null}">
					<div class="alert alert-danger alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
						<p><spring:message code="page.message.errorOccurred"/></p>
						<c:if test="${showErrors}">
							<p>${encode:forHtmlContent(error)}</p>
						</c:if>
					</div>
				</c:if>

				<div id="ajaxResponseContainer">
					<div id="ajaxResponse">
					</div>
				</div>
			</div>
		</div>

		<div class="row" style="margin-bottom: 20px">
			<div class="col-md-4">
				<select id="views" class="form-control selectpicker">
					<option value="0">--</option>
				</select>
			</div>
			<div class="col-md-4">
				<select id="selfServiceReports" class="form-control selectpicker">
					<option value="0">--</option>
				</select>
			</div>
		</div>

		<div class="row">
			<div class="col-md-5">
				<select name="from" id="multiselect" class="form-control" size="11" multiple="multiple">
				</select>
			</div>

			<div class="col-md-2">
				<button type="button" id="multiselect_undo" class="btn btn-primary btn-block"><spring:message code="multiselect.button.undo"/></button>
				<button type="button" id="multiselect_rightAll" class="btn btn-block btn-default"><i class="glyphicon glyphicon-forward"></i></button>
				<button type="button" id="multiselect_rightSelected" class="btn btn-block btn-default"><i class="glyphicon glyphicon-chevron-right"></i></button>
				<button type="button" id="multiselect_leftSelected" class="btn btn-block btn-default"><i class="glyphicon glyphicon-chevron-left"></i></button>
				<button type="button" id="multiselect_leftAll" class="btn btn-block btn-default"><i class="glyphicon glyphicon-backward"></i></button>
				<button type="button" id="multiselect_redo" class="btn btn-warning btn-block"><spring:message code="multiselect.button.redo"/></button>
			</div>

			<div class="col-md-5">
				<select name="to" id="multiselect_to" class="form-control" size="11" multiple="multiple"></select>

				<div class="row">
					<div class="col-md-6">
						<button type="button" id="multiselect_move_up" class="btn btn-block btn-default"><i class="glyphicon glyphicon-arrow-up"></i></button>
					</div>
					<div class="col-md-6">
						<button type="button" id="multiselect_move_down" class="btn btn-block btn-default col-sm-6"><i class="glyphicon glyphicon-arrow-down"></i></button>
					</div>
				</div>
			</div>
		</div>

		<div class="row" id="whereDiv" style="margin-top: 20px; display: none">
			<div class="col-md-12">
				<div class="row">
					<div class="col-md-12" style="text-align: center">
						<label for="limit">
							<spring:message code="selfService.text.limit"/>
						</label>
						<input id="limit" type="number" value="10">
						<button id="preview" class="btn btn-default">
							<spring:message code="reports.action.preview"/>
						</button>
						<button id="saveReport" class="btn btn-primary">
							<spring:message code="page.button.save"/>
						</button>
						<button class="btn btn-default" id="deleteReport" style="display: none">
							<spring:message code="page.action.delete"/>
						</button>
						<a class="btn btn-default" id="newReportLink" style="display: none"
						   href="">
							<spring:message code="reports.link.newReport"/>
						</a>
					</div>
				</div>
				<c:if test="${sessionUser.hasConfigureReportsPermission()}">
					<div class="row" style="padding-top:15px">
						<div class="col-md-12" style="text-align: center">
							<label for="showSql">
								<spring:message code="reports.label.showSql"/>
							</label>
							<input type="checkbox" name="showSql" id="showSql" value="">
						</div>
					</div>
				</c:if>
				<div class="row" style="padding-top:5px" id="builderDiv">
					<div class="col-md-12">
						<div id="builder"></div>
					</div>
				</div>
			</div>
		</div>

		<div class="row">
			<div class="col-md-12">
				<jsp:include page="/WEB-INF/jsp/showCancelQuery.jsp"/>
			</div>
		</div>

		<div class="row">
			<div class="col-md-12">
				<div id="reportOutput"></div>
			</div>
		</div>

		<div id="saveReportDialogDiv" style="display:none;">
			<form id="saveReportForm" class="form-horizontal" role="form">
				<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
				<input type="hidden" name="reportId" id="reportId" value="">
				<input type="hidden" name="viewReportId" id="viewReportId" value="">
				<input type="hidden" id="config" name="config" value="">
				<div class="form-group">
					<label class="control-label col-md-4" for="name">
						<spring:message code="page.text.name"/>
					</label>
					<div class="col-md-8">
						<input type="text" id="name" name="name" maxlength="50" class="form-control"/>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="description">
						<spring:message code="page.text.description"/>
					</label>
					<div class="col-md-8">
						<textarea id="description" name="description" class="form-control" rows="2" maxlength="200"></textarea>
					</div>
				</div>
				<div class="form-group" id="overwriteDiv">
					<label class="control-label col-md-4" for="overwrite">
						<spring:message code="reports.text.overwrite"/>
					</label>
					<div class="col-md-8">
						<div class="checkbox">
							<label>
								<input type="checkbox" name="overwrite" id="overwrite">
							</label>
						</div>
					</div>
				</div>
			</form>
		</div>

		<script>
			$("#saveReport").on("click", function () {
				var config = getSelfServiceOptionsString();
				$("#config").val(config);

				var reportId = $("#reportId").val();
				if (reportId) {
					//setting checked property here doesn't work with bootbox dialog
					//$('#overwrite').prop('checked', true);
					$("#overwriteDiv").show();
				} else {
					$("#overwriteDiv").hide();
				}

				var dialog = bootbox.confirm({
					title: "${saveReportText}",
					message: $("#saveReportDialogDiv").html(),
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
							//https://github.com/makeusabrew/bootbox/issues/572
							var form = dialog.find('#saveReportForm');
							var data = form.serialize();
							var reportName = dialog.find('#name').val();
							reportName = escapeHtml(reportName);

							$.ajax({
								type: 'POST',
								url: '${pageContext.request.contextPath}/saveSelfService',
								dataType: 'json',
								data: data,
								success: function (response) {
									if (response.success) {
										$.notify("${reportSavedText}", "success");
										var newReportId = response.data;
										if (newReportId) {
											var newUrl = "${pageContext.request.contextPath}/selectReportParameters?reportId=" + newReportId;
											$("#newReportLink").attr("href", newUrl);
											$("#newReportLink").show();
											$('#selfServiceReports').append("<option value='" + newReportId + "'>" + reportName + "</option>");
											$('#selfServiceReports').find('[value=' + newReportId + ']').prop('selected', true);
											$("#selfServiceReports").selectpicker('refresh');
											showDeleteReport(reportName, newReportId);
											$("#views").val('0').selectpicker('refresh');
										} else if (reportName) {
											$('#selfServiceReports').find('[value=' + reportId + ']').text(reportName);
											$("#selfServiceReports").selectpicker('refresh');
											$("#deleteReport").attr("data-report-name", reportName);
										}
									} else {
										notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
									}
								},
								error: function (xhr) {
									showUserAjaxError(xhr, '${errorOccurredText}');
								}
							});
						} //end if result
					} //end callback
				}); //end bootbox confirm

				//https://github.com/makeusabrew/bootbox/issues/411
				//https://blog.shinychang.net/2014/06/05/Input%20autofocus%20in%20the%20bootbox%20dialog%20with%20buttons/
				dialog.on("shown.bs.modal", function () {
					dialog.attr("id", "saveReportDialog");
					var reportId = dialog.find("#reportId").val();
					if (reportId) {
						dialog.find('#overwrite').prop('checked', true);
					} else {
						dialog.find("#overwrite").prop('checked', false);
					}
					dialog.find('#name').trigger("focus");
				});
			});

			$(document).on("submit", "#saveReportForm", function (e) {
				e.preventDefault();
				$("#saveReportDialog .btn-primary").click();
			});

			var token = $("meta[name='_csrf']").attr("content");
			var header = $("meta[name='_csrf_header']").attr("content");
			$(document).ajaxSend(function (e, xhr, options) {
				if (header) {
					xhr.setRequestHeader(header, token);
				}
			});
		</script>
	</jsp:body>
</t:mainPage>
