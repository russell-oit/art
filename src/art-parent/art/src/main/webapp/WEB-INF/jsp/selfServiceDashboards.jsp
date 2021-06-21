<%-- 
    Document   : selfServiceDashboard
    Created on : 11-May-2018, 15:23:10
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.selfServiceDashboards" var="pageTitle"/>

<spring:message code="select.text.nothingSelected" var="nothingSelectedText" javaScriptEscape="true"/>
<spring:message code="select.text.noResultsMatch" var="noResultsMatchText" javaScriptEscape="true"/>
<spring:message code="select.text.selectedCount" var="selectedCountText" javaScriptEscape="true"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText" javaScriptEscape="true"/>
<spring:message code="dialog.button.cancel" var="cancelText" javaScriptEscape="true"/>
<spring:message code="dialog.button.ok" var="okText" javaScriptEscape="true"/>
<spring:message code="reports.message.reportSaved" var="reportSavedText" javaScriptEscape="true"/>
<spring:message code="reports.message.reportDeleted" var="reportDeletedText" javaScriptEscape="true"/>
<spring:message code="dialog.title.saveReport" var="saveReportText" javaScriptEscape="true"/>
<spring:message code="dialog.message.deleteRecord" var="deleteRecordText" javaScriptEscape="true"/>
<spring:message code="reports.message.cannotDeleteReport" var="cannotDeleteReportText" javaScriptEscape="true"/>
<spring:message code="dialog.text.title" var="titleText" javaScriptEscape="true"/>

<t:mainPage title="${pageTitle}">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/gridstack-0.5.0/gridstack.min.css" /> 
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/gridstack-0.5.0/gridstack-extra.min.css" />
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/dashboard.css" />
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-ui-1.11.4-all-smoothness/jquery-ui.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.ui.touch-punch-0.2.3.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/gridstack-0.5.0/gridstack.all.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootbox-4.4.0.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/notifyjs-0.4.2/notify.js"></script>

		<script>
			$(document).ready(function () {
				$('a[id="selfService"]').parent().addClass('active');
				$('a[href*="selfServiceDashboards"]').parent().addClass('active');

				//Enable Bootstrap-Select
				$('.selectpicker').selectpicker({
					liveSearch: true,
					noneSelectedText: '${nothingSelectedText}',
					noneResultsText: '${noResultsMatchText}',
					countSelectedText: '${selectedCountText}'
				});

				$('.grid-stack').gridstack({
					resizable: {
						handles: 'e, se, s, sw, w, n'
					}
				});

				loadCandidateReports();
				loadEditDashboard();

				//https://stackoverflow.com/questions/35349239/bootstrap-select-event-parameters
				//https://github.com/gridstack/gridstack.js/tree/master/doc
				//https://jonsuh.com/blog/javascript-templating-without-a-library/
				$("#reports").on('changed.bs.select', function (event, clickedIndex, newValue, oldValue) {
					//https://stackoverflow.com/questions/36944647/bootstrap-select-on-click-get-clicked-value
					var grid = $('.grid-stack').data('gridstack');
					var reportId = $(this).find('option').eq(clickedIndex).val();
					var reportName = $(this).find('option').eq(clickedIndex).text();
					reportName = escapeHtml(reportName);

					if (newValue) {
						var el = $(processWidgetTemplate(reportId, reportName));
						var x = 0;
						var y = 0;
						var width = 4;
						var height = 3;
						var autoPosition = true;
						grid.addWidget(el, x, y, width, height, autoPosition);

						var runImmediately = $("#runImmediately").is(":checked");
						if (runImmediately) {
							runReport(reportId);
						}
					} else {
						var contentDiv = $("#content_" + reportId);
						var item = contentDiv.closest('.grid-stack-item');
						grid.removeWidget(item);
					}
				});

				function processWidgetTemplate(reportId, reportName) {
					var processedTemplate = $("#widgetTemplate").html().replace(/#reportId#/g, reportId).replace(/#reportName#/g, reportName);
					return processedTemplate;
				}

				function runReport(reportId) {
					$("#content_" + reportId).empty();
					
					$.ajax({
						type: 'POST',
						url: '${pageContext.request.contextPath}/runReport',
						data: {reportId: reportId, isFragment: true},
						success: function (data) {
							$("#content_" + reportId).html(data);
							var autoheight = false;
							var autowidth = true;
							autosize(autoheight, autowidth, reportId);
						},
						error: function (xhr) {
							showUserAjaxError(xhr, '${errorOccurredText}');
						}
					});
				}

				//https://stackoverflow.com/questions/31983495/gridstack-js-delete-widget-using-jquery
				$('.grid-stack').on('click', '.removeWidget', function () {
					var grid = $('.grid-stack').data('gridstack');
					var el = $(this).closest('.grid-stack-item');
					grid.removeWidget(el);
					var reportId = $(this).data("reportId");
					$('#reports').find('[value=' + reportId + ']').prop('selected', false);
					$('#reports').selectpicker('refresh');
				});

				$('.grid-stack').on('click', '.refreshWidget', function () {
					var reportId = $(this).data("reportId");
					runReport(reportId);
				});

				$('.grid-stack').on('click', '.editWidgetTitle', function () {
					var titleDiv = $(this).closest('.portletTitle');
					bootbox.prompt({
						title: "${titleText}",
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
								titleDiv.find('.reportTitle').text(result);
							}
						}
					});
				});

				function autosize(autoheight, autowidth, reportId) {
					//https://github.com/gridstack/gridstack.js/issues/404
					if (autoheight || autowidth) {
						var itemContentDiv = $("#itemContent_" + reportId);
						var itemContentDiv = $("#itemContent_" + reportId);
						var itemDiv = itemContentDiv.closest('.grid-stack-item');

						var newHeightPixels;
						if (autoheight) {
							newHeightPixels = Math.ceil((itemContentDiv[0].scrollHeight + $('.grid-stack').data('gridstack').opts.verticalMargin) / ($('.grid-stack').data('gridstack').cellHeight() + $('.grid-stack').data('gridstack').opts.verticalMargin));
						} else {
							newHeightPixels = $(itemDiv).attr('data-gs-height');
						}

						var newWidthPixels;
						if (autowidth) {
							var dashboardWidth = 12;
							newWidthPixels = Math.ceil(itemContentDiv[0].scrollWidth / $('.grid-stack').width() * dashboardWidth);
						} else {
							newWidthPixels = $(itemDiv).attr('data-gs-width');
						}

						$('.grid-stack').data('gridstack').resize(
								itemDiv,
								newWidthPixels,
								newHeightPixels
								);
					}
				}

				$("#newDashboard").on("click", function () {
					resetAll();
				});

				function loadCandidateReports() {
					$.ajax({
						type: 'GET',
						dataType: "json",
						url: '${pageContext.request.contextPath}/getDashboardCandidates',
						success: function (response) {
							if (response.success) {
								//https://github.com/silviomoreto/bootstrap-select/issues/1151
								var reports = response.data;
								var options = "";
								$.each(reports, function (index, report) {
									options += "<option value='" + report.reportId + "'>" + report.name2 + "</option>";
								});
								var select = $("#reports");
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

				function loadEditDashboard() {
					$.ajax({
						type: 'GET',
						dataType: "json",
						url: '${pageContext.request.contextPath}/getEditDashboards',
						success: function (response) {
							if (response.success) {
								//https://github.com/silviomoreto/bootstrap-select/issues/1151
								var reports = response.data;
								var options = "<option value='0'>--</option>";
								$.each(reports, function (index, report) {
									options += "<option value='" + report.reportId + "'>" + report.name2 + "</option>";
								});
								var select = $("#dashboardReports");
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

				$("#dashboardReports").on('changed.bs.select', function (event, clickedIndex, newValue, oldValue) {
					//https://stackoverflow.com/questions/36944647/bootstrap-select-on-click-get-clicked-value
					var reportId = $(this).find('option').eq(clickedIndex).val();
					var reportName = $(this).find('option').eq(clickedIndex).text();

					$("#newDashboardLink").hide();

					//https://stackoverflow.com/questions/27347004/jquery-val-integer-datatype-comparison
					if (reportId === '0') {
						$("#deleteDashboard").hide();
						resetDashboard();
					} else {
						resetDashboard();
						showDeleteDashboard(reportName, reportId);

						$.ajax({
							type: 'GET',
							url: '${pageContext.request.contextPath}/getDashboardDetails',
							data: {reportId: reportId},
							success: function (response) {
								if (response.success) {
									var grid = $('.grid-stack').data('gridstack');
									var dashboard = response.data;
									
									var runImmediately = $("#runImmediately").is(":checked");
									$.each(dashboard.items, function (index, item) {
										var itemReportId = item.reportId;
										if (itemReportId > 0) {
											var el = $(processWidgetTemplate(itemReportId, item.title));
											var autoPosition = false;
											grid.addWidget(el, item.xPosition, item.yPosition, item.width, item.height, autoPosition);

											if (runImmediately) {
												$.ajax({
													type: 'POST',
													url: '${pageContext.request.contextPath}/runReport',
													data: {reportId: itemReportId, isFragment: true},
													success: function (data) {
														$("#content_" + itemReportId).html(data);
														$('#reports').find('[value=' + itemReportId + ']').prop('selected', true);
														$('#reports').selectpicker('refresh');
													},
													error: function (xhr) {
														showUserAjaxError(xhr, '${errorOccurredText}');
													}
												});
											}
										}
									});
								} else {
									notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
								}
							},
							error: function (xhr) {
								showUserAjaxError(xhr, '${errorOccurredText}');
							}
						});
					}
				});

				$("#deleteDashboard").on("click", function () {
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
									url: "${pageContext.request.contextPath}/deleteGridstack",
									data: {id: reportId},
									success: function (response) {
										var nonDeletedRecords = response.data;
										if (response.success) {
											$("#dashboardReports option[value='" + reportId + "']").remove();
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

			});

			function resetAll() {
				resetDashboard();
				$("#dashboardReports option").prop("selected", false);
				$("#dashboardReports").selectpicker('refresh');
				$("#newDashboardLink").hide();
				$("#deleteDashboard").hide();
			}

			function resetDashboard() {
				clearGrid();
				//calling deselectAll causes problem with gridstack
				//$("#reports").selectpicker('deselectAll');
				$("#reports option").prop("selected", false);
				$("#reports").selectpicker('refresh');
				$("#reportId").val('');
			}

			function clearGrid() {
				var grid = $('.grid-stack').data('gridstack');
				grid.removeAll();
			}

			function showDeleteDashboard(reportName, reportId) {
				$("#deleteDashboard").attr("data-report-name", reportName);
				$("#deleteDashboard").attr("data-report-id", reportId);
				$("#deleteDashboard").show();
				$("#reportId").val(reportId);
			}
		</script>
	</jsp:attribute>

	<jsp:body>
		<script type="text/template" id="widgetTemplate">
			<div>
			<div class="grid-stack-item-content" style="border: 1px solid #ccc" id="itemContent_#reportId#" data-report-id="#reportId#">
			<div class="portletTitle">
			<span><b class="reportTitle">#reportName#</b></span>
			<span class="fa fa-times removeWidget pull-right self-service-item-icon" data-report-id="#reportId#"></span>
			<span class="fa fa-refresh refreshWidget pull-right self-service-item-icon" data-report-id="#reportId#"></span>
			<span class="fa fa-pencil editWidgetTitle pull-right self-service-item-icon"></span>
			</div>				
			<div id="content_#reportId#">
			</div>
			</div>		
			</div>
		</script>

		<div class='row'>
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

		<div class="row" style="margin-bottom: 10px;">
			<div class="col-md-4">
				<button class="btn btn-default" id="newDashboard">
					<spring:message code="page.text.new"/>
				</button>
				&nbsp;
				<label class="checkbox-inline">
					<input type="checkbox" name="runImmediately" id="runImmediately" checked>
					<spring:message code="selfService.checkbox.runImmediately"/>
				</label>
			</div>
			<div class="col-md-8">
				<span class="pull-right">
					<a class="btn btn-default" id="newDashboardLink" style="display: none"
					   href="">
						<spring:message code="reports.link.newReport"/>
					</a>
					<button class="btn btn-default" id="deleteDashboard" style="display: none">
						<spring:message code="page.action.delete"/>
					</button>
					<button class="btn btn-primary" id="saveDashboard">
						<spring:message code="page.button.save"/>
					</button>
				</span>
			</div>
		</div>
		<div class="row" style="margin-bottom: 20px">
			<div class="col-md-4">
				<select id="reports" class="form-control selectpicker" multiple>
				</select>
			</div>
			<div class="col-md-4">
				<select id="dashboardReports" class="form-control selectpicker">
					<option value="0">--</option>
				</select>
			</div>
		</div>
		<div class="row">
			<div class="col-md-12">
				<div id="dashboard" class="grid-stack"></div>
			</div>
		</div>

		<div id="saveDashboardDialogDiv" style="display:none;">
			<form id="saveDashboardForm" class="form-horizontal" role="form">
				<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
				<input type="hidden" name="reportId" id="reportId" value="">
				<input type="hidden" id="config" name="config" value="">
				<input type="hidden" name="selfServiceDashboard" value="true">
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
			//https://github.com/gridstack/gridstack.js/issues/50
			//https://github.com/gridstack/gridstack.js/issues/575
			$("#saveDashboard").on("click", function () {
				var items = [];

				$('.grid-stack-item.ui-draggable').each(function (index) {
					var $this = $(this);
					var content = $(this).find('.grid-stack-item-content');
					//https://stackoverflow.com/questions/10296985/data-attribute-becomes-integer
					var reportId = content.data("reportId");
					var titleDiv = content.find('.portletTitle');
					var title = titleDiv.find('.reportTitle').text();

					items.push({
						index: index + 1,
						reportId: reportId,
						title: title,
						x: parseInt($this.attr('data-gs-x'), 10),
						y: parseInt($this.attr('data-gs-y'), 10),
						width: parseInt($this.attr('data-gs-width'), 10),
						height: parseInt($this.attr('data-gs-height'), 10)
					});
				});

				$("#config").val(JSON.stringify(items));

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
					message: $("#saveDashboardDialogDiv").html(),
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
							var form = dialog.find('#saveDashboardForm');
							var data = form.serialize();
							var reportName = dialog.find('#name').val();
							reportName = escapeHtml(reportName);

							$.ajax({
								type: 'POST',
								url: '${pageContext.request.contextPath}/saveGridstack',
								dataType: 'json',
								data: data,
								success: function (response) {
									if (response.success) {
										$.notify("${reportSavedText}", "success");
										var newReportId = response.data;
										if (newReportId) {
											var newUrl = "${pageContext.request.contextPath}/selectReportParameters?reportId=" + newReportId;
											$("#newDashboardLink").attr("href", newUrl);
											$("#newDashboardLink").show();
											$('#dashboardReports').append("<option value='" + newReportId + "'>" + reportName + "</option>");
											$('#dashboardReports').find('[value=' + newReportId + ']').prop('selected', true);
											$("#dashboardReports").selectpicker('refresh');
											showDeleteDashboard(reportName, newReportId);
										} else if (reportName) {
											$('#dashboardReports').find('[value=' + reportId + ']').text(reportName);
											$("#dashboardReports").selectpicker('refresh');
											$("#deleteDashboard").attr("data-report-name", reportName);
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
					dialog.attr("id", "saveDashboardDialog");
					var reportId = dialog.find("#reportId").val();
					if (reportId) {
						dialog.find('#overwrite').prop('checked', true);
					} else {
						dialog.find("#overwrite").prop('checked', false);
					}
					dialog.find('#name').trigger("focus");
				});
			});

			$(document).on("submit", "#saveDashboardForm", function (e) {
				e.preventDefault();
				$("#saveDashboardDialog .btn-primary").click();
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
