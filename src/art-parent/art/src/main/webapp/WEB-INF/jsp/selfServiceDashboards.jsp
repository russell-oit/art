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

<spring:message code="select.text.nothingSelected" var="nothingSelectedText"/>
<spring:message code="select.text.noResultsMatch" var="noResultsMatchText"/>
<spring:message code="select.text.selectedCount" var="selectedCountText"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="dialog.button.cancel" var="cancelText"/>
<spring:message code="dialog.button.ok" var="okText"/>
<spring:message code="reports.message.reportSaved" var="reportSavedText"/>
<spring:message code="reports.message.reportDeleted" var="reportDeletedText"/>
<spring:message code="dialog.title.saveReport" var="saveReportText"/>
<spring:message code="dialog.message.deleteRecord" var="deleteRecordText"/>
<spring:message code="reports.message.cannotDeleteReport" var="cannotDeleteReportText"/>

<t:mainPage title="${pageTitle}">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/css/bootstrap-select.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/gridstack-0.2.5/gridstack.min.css" /> 
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/gridstack-0.2.5/gridstack-extra.min.css" />
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/dashboard.css" />
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/js/bootstrap-select.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-ui-1.11.4-all-smoothness/jquery-ui.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.ui.touch-punch-0.2.3.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/lodash-3.5.0/lodash.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/gridstack-0.2.5/gridstack.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootbox-4.4.0.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/notify-combined-0.3.1.min.js"></script>

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

					if (newValue) {
						var el = $(processWidgetTemplate(reportId, reportName));
						var x = 0;
						var y = 0;
						var width = 4;
						var height = 3;
						var autoPosition = true;
						grid.addWidget(el, x, y, width, height, autoPosition);

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
								bootbox.alert({
									title: '${errorOccurredText}',
									message: xhr.responseText
								});
							}
						});
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

				//https://stackoverflow.com/questions/31983495/gridstack-js-delete-widget-using-jquery
				$('.grid-stack').on('click', '.removeWidget', function () {
					var grid = $('.grid-stack').data('gridstack');
					var el = $(this).closest('.grid-stack-item');
					grid.removeWidget(el);
					var reportId = $(this).data("reportId");
					$('#reports').find('[value=' + reportId + ']').prop('selected', false);
					$('#reports').selectpicker('refresh');
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

				$("#newDashboard").click(function () {
					$("#newDashboardLink").hide();
					resetDashboard();
				});

				function loadCandidateReports() {
					$.ajax({
						type: 'GET',
						dataType: "json",
						url: '${pageContext.request.contextPath}/getDashboardCandidateReports',
						success: function (response) {
							if (response.success) {
								//https://github.com/silviomoreto/bootstrap-select/issues/1151
								var reports = response.data;
								var options = "";
								$.each(reports, function (index, report) {
									options += "<option value=" + report.reportId + ">" + report.name2 + "</option>";
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
							bootbox.alert({
								title: '${errorOccurredText}',
								message: xhr.responseText
							});
						}
					});
				}

				function resetDashboard() {
					var grid = $('.grid-stack').data('gridstack');
					grid.removeAll();
					//calling deselectAll causes problem with gridstack
					//$("#reports").selectpicker('deselectAll');
					$("#reports option").prop("selected", false);
					$("#reports").selectpicker('refresh');
					
					$("#dashboardReports option").prop("selected", false);
					$("#dashboardReports").selectpicker('refresh');
				}

				$('#errorsDiv').on("click", ".alert .close", function () {
					$(this).parent().hide();
				});

				$("#editDashboard").click(function () {
					$("#newDashboardLink").hide();

					resetDashboard();
					loadEditDashboard();
				});

				function loadEditDashboard() {
					$.ajax({
						type: 'GET',
						dataType: "json",
						url: '${pageContext.request.contextPath}/getEditDashboardReports',
						success: function (response) {
							if (response.success) {
								//https://github.com/silviomoreto/bootstrap-select/issues/1151
								var reports = response.data;
								var options = "<option value='0'>--</option>";
								$.each(reports, function (index, report) {
									options += "<option value=" + report.reportId + ">" + report.name2 + "</option>";
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
							bootbox.alert({
								title: '${errorOccurredText}',
								message: xhr.responseText
							});
						}
					});
				}

				$("#editAllDashboards").click(function () {
					$("#newDashboardLink").hide();

					resetDashboard();

					$.ajax({
						type: 'GET',
						dataType: "json",
						url: '${pageContext.request.contextPath}/getEditAllDashboardReports',
						success: function (response) {
							if (response.success) {
								//https://github.com/silviomoreto/bootstrap-select/issues/1151
								var reports = response.data;
								var options = "<option value='0'>--</option>";
								$.each(reports, function (index, report) {
									options += "<option value=" + report.reportId + ">" + report.name2 + "</option>";
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
							bootbox.alert({
								title: '${errorOccurredText}',
								message: xhr.responseText
							});
						}
					});
				});

				$("#dashboardReports").on('changed.bs.select', function (event, clickedIndex, newValue, oldValue) {
					//https://stackoverflow.com/questions/36944647/bootstrap-select-on-click-get-clicked-value
					var reportId = $(this).find('option').eq(clickedIndex).val();
					if (reportId > 0) {
						$.ajax({
							type: 'GET',
							url: '${pageContext.request.contextPath}/getDashboardDetails',
							data: {reportId: reportId},
							success: function (response) {
								if (response.success) {
									var grid = $('.grid-stack').data('gridstack');
									var dashboard = response.data;
									$.each(dashboard.items, function (index, item) {
										var itemReportId = item.reportId;
										if (itemReportId > 0) {
											var el = $(processWidgetTemplate(itemReportId, item.title));
											var autoPosition = true;
											grid.addWidget(el, item.xPosition, item.yPosition, item.width, item.height, autoPosition);

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
													bootbox.alert({
														title: '${errorOccurredText}',
														message: xhr.responseText
													});
												}
											});
										}
									});
								} else {
									notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
								}
							},
							error: function (xhr) {
								bootbox.alert({
									title: '${errorOccurredText}',
									message: xhr.responseText
								});
							}
						});
					}
				});

			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<script type="text/template" id="widgetTemplate">
			<div>
			<div class="grid-stack-item-content" style="border: 1px solid #ccc" id="itemContent_#reportId#" data-report-id="#reportId#">
			<div class="portletTitle">
			<span><b>#reportName#</b></span>
			<span class="fa fa-times removeWidget pull-right" style="cursor: pointer" data-report-id="#reportId#">					
			</span>
			</div>				
			<div id="content_#reportId#">
			</div>
			</div>		
			</div>
		</script>

		<div class='row' id="errorsDiv">
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

				<div id="ajaxResponse">
				</div>
			</div>
		</div>

		<div class="row" style="margin-bottom: 10px;">
			<div class="col-md-4">
				<button class="btn btn-default" id="newDashboard">
					<spring:message code="page.text.new"/>
				</button>
			</div>
			<div class="col-md-4">
				<button class="btn btn-default" id="editDashboard">
					<spring:message code="page.action.edit"/>
				</button>
				<c:if test="${sessionUser.hasPermission('configure_reports')}">
					<button class="btn btn-default" id="editAllDashboards">
						<spring:message code="selfService.button.editAll"/>
					</button>
				</c:if>
			</div>
			<div class="col-md-4">
				<span class="pull-right">
					<a class="btn btn-default" id="newDashboardLink" style="display: none"
					   href="">
						<spring:message code="reports.link.newReport"/>
					</a>
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
				<input type="hidden" name="reportId" value="">
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

					items.push({
						index: index + 1,
						reportId: reportId,
						x: parseInt($this.attr('data-gs-x'), 10),
						y: parseInt($this.attr('data-gs-y'), 10),
						width: parseInt($this.attr('data-gs-width'), 10),
						height: parseInt($this.attr('data-gs-height'), 10)
					});
				});

				$("#config").val(JSON.stringify(items));

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
										}
									} else {
										notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
									}
								},
								error: function (xhr) {
									bootbox.alert({
										title: '${errorOccurredText}',
										message: xhr.responseText
									});
								}
							});
						} //end if result
					} //end callback
				}); //end bootbox confirm

				//https://github.com/makeusabrew/bootbox/issues/411
				//https://blog.shinychang.net/2014/06/05/Input%20autofocus%20in%20the%20bootbox%20dialog%20with%20buttons/
				dialog.on("shown.bs.modal", function () {
					dialog.attr("id", "saveDashboardDialog");
					dialog.find('#name').focus();
				});
			});

			$(document).on("submit", "#saveDashboardForm", function (e) {
				e.preventDefault();
				$("#saveDashboardDialog .btn-primary").click();
			});

			var token = $("meta[name='_csrf']").attr("content");
			var header = $("meta[name='_csrf_header']").attr("content");
			$(document).ajaxSend(function (e, xhr, options) {
				xhr.setRequestHeader(header, token);
			});
		</script>
	</jsp:body>
</t:mainPage>
