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

<t:mainPage title="${pageTitle}">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/css/bootstrap-select.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/gridstack-0.2.5/gridstack.min.css" /> 
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/gridstack-0.2.5/gridstack-extra.min.css" />
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

				$('.grid-stack').gridstack();

				//https://stackoverflow.com/questions/35349239/bootstrap-select-event-parameters
				//https://github.com/gridstack/gridstack.js/tree/master/doc
				//https://jonsuh.com/blog/javascript-templating-without-a-library/
				$("#reports").on('changed.bs.select', function (event, clickedIndex, newValue, oldValue) {
					//https://stackoverflow.com/questions/36944647/bootstrap-select-on-click-get-clicked-value
					var reportId = $(this).find('option').eq(clickedIndex).val();
					var grid = $('.grid-stack').data('gridstack');

					if (newValue) {
						var el = $(processWidgetTemplate(reportId));
						grid.addWidget(el, 0, 0, 4, 3, true);

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

				function processWidgetTemplate(reportId) {
					var processedTemplate = $("#widgetTemplate").html().replace(/#reportId#/g, reportId);
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

//						console.log("itemContentDiv", itemContentDiv);
//						console.log("itemDiv", itemDiv);

						var newHeightPixels;
						if (autoheight) {
							newHeightPixels = Math.ceil((itemContentDiv[0].scrollHeight + $('.grid-stack').data('gridstack').opts.verticalMargin) / ($('.grid-stack').data('gridstack').cellHeight() + $('.grid-stack').data('gridstack').opts.verticalMargin));
						} else {
							newHeightPixels = $(itemDiv).attr('data-gs-height');
						}
//						console.log("newHeightPixels", newHeightPixels);

						var newWidthPixels;
						if (autowidth) {
							var dashboardWidth = 14; //12 + 2 (2 because of col-md-10)
//							console.log("scrollWidth", itemContentDiv[0].scrollWidth);
//							console.log("gridstackWidth", $('.grid-stack').width());
//							console.log("calc", itemContentDiv[0].scrollWidth / $('.grid-stack').width() * dashboardWidth);
							newWidthPixels = Math.ceil(itemContentDiv[0].scrollWidth / $('.grid-stack').width() * dashboardWidth);
						} else {
							newWidthPixels = $(itemDiv).attr('data-gs-width');
						}
//						console.log("newWidthPixels", newWidthPixels);

						$('.grid-stack').data('gridstack').resize(
								itemDiv,
								newWidthPixels,
								newHeightPixels
								);
					}
				}

				$("#newDashboard").click(function () {
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
									options += "<option value=" + report.reportId + ">" + escapeDoubleQuotes(report.name2) + "</option>";
								});
								var select = $("#reports");
								select.empty();
								select.append(options);
								select.selectpicker('refresh');

								var grid = $('.grid-stack').data("gridstack");
								grid.removeAll();
							} else {
								$.notify(response.errorMessage, "error");
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
			});
		</script>

		<script type="text/template" id="widgetTemplate">
			<div>
			<div class="grid-stack-item-content" style="border: 1px solid #ccc" id="itemContent_#reportId#">
			<div style="text-align: right">
			<span class="fa fa-times removeWidget" style="cursor: pointer" data-report-id="#reportId#">
			</div>
			<div id="content_#reportId#">
			</div>
			</div>
			</div>
		</script>


	</jsp:attribute>

	<jsp:body>
		<c:if test="${error != null}">
			<div class="alert alert-danger alert-dismissable">
				<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
				<p><spring:message code="page.message.errorOccurred"/></p>
				<c:if test="${showErrors}">
					<p>${encode:forHtmlContent(error)}</p>
				</c:if>
			</div>
		</c:if>

		<div class="row" style="margin-right: 1px; margin-bottom: 10px;">
			<div class="col-md-12">
				<button class="btn btn-default" id="newDashboard">
					<spring:message code="page.text.new"/>
				</button>
				<button class="btn btn-default" id="editDashboard">
					<spring:message code="page.action.edit"/>
				</button>
				<span class="pull-right">
					<a class="btn btn-default" id="newDashboardLink" style="display: none"
					   href="">
						<spring:message code="reports.link.newReport"/>
					</a>
					<c:if test="${exclusiveAccess}">
						<button class="btn btn-default" id="deleteDashboard">
							<spring:message code="page.action.delete"/>
						</button>
					</c:if>
					<button class="btn btn-primary" id="saveDashboard">
						<spring:message code="page.button.save"/>
					</button>
				</span>
			</div>
		</div>
		<div class="row">
			<div class="col-md-2">
				<select id="reports" class="form-control selectpicker" multiple>
				</select>
			</div>
			<div class="col-md-10">
				<div id="dashboard" class="grid-stack"></div>
			</div>
		</div>
	</jsp:body>
</t:mainPage>

