<%-- 
    Document   : showDashboardInline
    Created on : 17-Mar-2016, 10:23:24
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="dialog.button.cancel" var="cancelText"/>
<spring:message code="dialog.button.ok" var="okText"/>
<spring:message code="reports.message.reportSaved" var="reportSavedText"/>
<spring:message code="reports.message.reportDeleted" var="reportDeletedText"/>
<spring:message code="dialog.title.saveReport" var="saveReportText"/>
<spring:message code="dialog.message.deleteRecord" var="deleteRecordText"/>
<spring:message code="reports.message.cannotDeleteReport" var="cannotDeleteReportText"/>


<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/dashboard.css" /> 

<%-- https://www.versioneye.com/javascript/troolee:gridstack/0.2.5-dev --%>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/gridstack-0.2.5/gridstack.min.css" /> 
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/gridstack-0.2.5/gridstack-extra.min.css" /> 

<c:if test="${!ajax}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-1.12.4.min.js"></script>
	<script type="text/javascript">
		$(document).ajaxStart(function () {
			$('#spinner').show();
		}).ajaxStop(function () {
			$('#spinner').hide();
		});
	</script>
</c:if>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-ui-1.11.4-all-smoothness/jquery-ui.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.ui.touch-punch-0.2.3.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lodash-3.5.0/lodash.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/gridstack-0.2.5/gridstack.min.js"></script>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-3.3.6/css/bootstrap.min.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-3.3.6/js/bootstrap.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootbox-4.4.0.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/notify-combined-0.3.1.min.js"></script>

<c:if test="${not empty cssFileName}">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js-templates/${encode:forHtmlAttribute(cssFileName)}">
</c:if>


<c:if test="${sessionUser.hasAnyPermission('save_reports', 'self_service_dashboards')}">
	<div class="row form-inline" style="margin-right: 1px;">
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
</c:if>

<div class="container-fluid">
	<div class="row">
		<h2>${encode:forHtmlContent(dashboard.title)}  
			<small>  ${encode:forHtmlContent(dashboard.description)}</small>
		</h2>
	</div>
	<div class="row">
		<jsp:include page="/WEB-INF/jsp/showSelectedParameters.jsp"/>
	</div>
	<div class="row">
		<c:choose>
			<c:when test="${dashboard.tabList == null}">
				<div class="grid-stack grid-stack-${dashboard.width}" data-gs-height="${dashboard.height}">
					<c:forEach var="item" items="${dashboard.items}">
						<div class="grid-stack-item" 
							 data-gs-x="${item.xPosition}" data-gs-y="${item.yPosition}"
							 data-gs-width="${item.width}" data-gs-height="${item.height}"
							 data-gs-no-resize="${item.noResize}" data-gs-no-move="${item.noMove}"
							 data-gs-auto-position="${item.autoposition}" data-gs-locked="${item.locked}"
							 ${item.minWidth == 0 ? '' : ' data-gs-min-width="'.concat(item.minWidth).concat('"')}
							 ${item.minHeight == 0 ? '' : ' data-gs-min-height="'.concat(item.minHeight).concat('"')}
							 ${item.maxWidth == 0 ? '' : ' data-gs-max-width="'.concat(item.maxWidth).concat('"')}
							 ${item.maxHeight == 0 ? '' : ' data-gs-max-height="'.concat(item.maxHeight).concat('"')}
							 data-index='${item.index}'>
							<div class="grid-stack-item-content" style="border: 1px solid #ccc">
								<div id="item_${item.index}">
									<div class="portletAUTOBox">
										<div class="portletAUTOTools"
											 data-content-div-id="#itemContent_${item.index}"
											 data-url="${encode:forHtmlAttribute(item.url)}"
											 data-refresh-period-seconds="${item.refreshPeriodSeconds}"
											 data-base-url="${encode:forHtmlAttribute(item.baseUrl)}"
											 data-parameters-json="${encode:forHtmlAttribute(item.parametersJson)}"
											 data-index='${item.index}' data-autoheight='${item.autoheight}'
											 data-autowidth='${item.autowidth}'>
											<img class="refresh" src="${pageContext.request.contextPath}/images/refresh.png"/>
											<img class="toggle" src="${pageContext.request.contextPath}/images/minimize.png"/>
										</div>
										<div class="portletAUTOTitle">
											<%-- don't encode title because it may contain image source where onload is false --%>
											${item.title}
										</div>
										<div id="itemContent_${item.index}" class="portletAUTOContent">
										</div>
									</div>
								</div>
							</div>
						</div>
					</c:forEach>
				</div>
			</c:when>
			<c:otherwise>
				<c:set var="defaultTab" value="${dashboard.tabList.defaultTab}"/>
				<ul class="nav nav-tabs">
					<%-- https://stackoverflow.com/questions/6600738/use-jstl-foreach-loops-varstatus-as-an-id --%>
					<c:forEach var="tab" items="${dashboard.tabList.tabs}" varStatus="loop">
						<li ${loop.count == defaultTab ? 'class="active"' : ''}><a data-toggle="tab" href="#tab${loop.count}">${encode:forHtmlContent(tab.title)}</a></li>
						</c:forEach>
				</ul>

				<div class="tab-content">
					<c:forEach var="tab" items="${dashboard.tabList.tabs}" varStatus="loop">
						<div id="tab${loop.count}" class="tab-pane ${loop.count == defaultTab ? 'active' : ''}">
							<div class="grid-stack grid-stack-${dashboard.width}" data-gs-height="${dashboard.height}">
								<c:forEach var="item" items="${tab.items}">
									<div class="grid-stack-item" 
										 data-gs-x="${item.xPosition}" data-gs-y="${item.yPosition}"
										 data-gs-width="${item.width}" data-gs-height="${item.height}"
										 data-gs-no-resize="${item.noResize}" data-gs-no-move="${item.noMove}"
										 data-gs-auto-position="${item.autoposition}" data-gs-locked="${item.locked}"
										 ${item.minWidth == 0 ? '' : ' data-gs-min-width="'.concat(item.minWidth).concat('"')}
										 ${item.minHeight == 0 ? '' : ' data-gs-min-height="'.concat(item.minHeight).concat('"')}
										 ${item.maxWidth == 0 ? '' : ' data-gs-max-width="'.concat(item.maxWidth).concat('"')}
										 ${item.maxHeight == 0 ? '' : ' data-gs-max-height="'.concat(item.maxHeight).concat('"')}
										 data-index='${item.index}'>
										<div class="grid-stack-item-content" style="border: 1px solid #ccc">
											<div id="item_${item.index}">
												<div class="portletAUTOBox">
													<div class="portletAUTOTools"
														 data-content-div-id="#itemContent_${item.index}"
														 data-url="${encode:forHtmlAttribute(item.url)}"
														 data-refresh-period-seconds="${item.refreshPeriodSeconds}"
														 data-base-url="${encode:forHtmlAttribute(item.baseUrl)}"
														 data-parameters-json="${encode:forHtmlAttribute(item.parametersJson)}"
														 data-index='${item.index}' data-autoheight='${item.autoheight}'
														 data-autowidth='${item.autowidth}'>
														<img class="refresh" src="${pageContext.request.contextPath}/images/refresh.png"/>
														<img class="toggle" src="${pageContext.request.contextPath}/images/minimize.png"/>
													</div>
													<div class="portletAUTOTitle">
														${item.title}
													</div>
													<div id="itemContent_${item.index}" class="portletAUTOContent">
													</div>
												</div>
											</div>
										</div>
									</div>
								</c:forEach>
							</div>
						</div>
					</c:forEach>
				</div>
			</c:otherwise>
		</c:choose>
	</div>
</div>

<script type="text/javascript">
		$(document).ready(function () {
			//https://stackoverflow.com/questions/109086/stop-setinterval-call-in-javascript
			//https://stackoverflow.com/questions/351495/dynamically-creating-keys-in-javascript-associative-array
			var intervalIds = {};

			function autosize(autoheight, autowidth, itemIndex) {
				//https://github.com/gridstack/gridstack.js/issues/404
				if (autoheight || autowidth) {
					var index = itemIndex - 1;

					var newHeightPixels;
					if (autoheight) {
						newHeightPixels = Math.ceil(($('.grid-stack-item-content')[index].scrollHeight + $('.grid-stack').data('gridstack').opts.verticalMargin) / ($('.grid-stack').data('gridstack').cellHeight() + $('.grid-stack').data('gridstack').opts.verticalMargin));
					} else {
						newHeightPixels = $($('.grid-stack-item')[index]).attr('data-gs-height');
					}

					var newWidthPixels;
					if (autowidth) {
						newWidthPixels = Math.ceil($('.grid-stack-item-content')[index].scrollWidth / $('.grid-stack').width() * ${dashboard.width});
					} else {
						newWidthPixels = $($('.grid-stack-item')[index]).attr('data-gs-width');
					}

					$('.grid-stack').data('gridstack').resize(
							$('.grid-stack-item')[index],
							newWidthPixels,
							newHeightPixels
							);
				}
			}

	<c:choose>
		<c:when test="${dashboard.tabList == null}">
			<c:set var="items" value="${dashboard.items}"/>
		</c:when>
		<c:otherwise>
			<c:set var="items" value="${dashboard.tabList.allItems}"/>
		</c:otherwise>
	</c:choose>

	<c:forEach var="item" items="${items}">
			var contentDivId = "#itemContent_${item.index}";
			var itemUrl = "${encode:forJavaScript(item.url)}";

			if (${item.executeOnLoad}) {
				var baseUrl = "${encode:forJavaScript(item.baseUrl)}";
				if (baseUrl) {
					//use post for art reports
					//https://api.jquery.com/load/
					var parametersJson = '${encode:forJavaScript(item.parametersJson)}';
					var parametersObject = JSON.parse(parametersJson);
					$(contentDivId).load(baseUrl, parametersObject, function () {
						autosize(${item.autoheight}, ${item.autowidth}, ${item.index});
					});
				} else {
					$(contentDivId).load(itemUrl);
				}
			}

			var refreshPeriodSeconds = ${item.refreshPeriodSeconds};
			if (refreshPeriodSeconds !== -1) {
				var refreshPeriodMilliseconds = refreshPeriodSeconds * 1000;
				var intervalId = setInterval(function () {
					if ("${item.baseUrl}") {
						//use post for art reports
						//use single quote as json string will have double quotes for attribute names and values
						var parametersJson = '${item.parametersJson}';
						var parametersObject = JSON.parse(parametersJson);
						$("#itemContent_${item.index}").load("${item.baseUrl}", parametersObject, function () {
							if (${item.autoheight} || ${item.autowidth}) {
								var newHeightPixels;
								if (${item.autoheight}) {
									newHeightPixels = Math.ceil(($('.grid-stack-item-content')[${item.index} - 1].scrollHeight + $('.grid-stack').data('gridstack').opts.verticalMargin) / ($('.grid-stack').data('gridstack').cellHeight() + $('.grid-stack').data('gridstack').opts.verticalMargin));
								} else {
									newHeightPixels = $($('.grid-stack-item')[${item.index} - 1]).attr('data-gs-height');
								}

								var newWidthPixels;
								if (${item.autowidth}) {
									newWidthPixels = Math.ceil($('.grid-stack-item-content')[${item.index} - 1].scrollWidth / $('.grid-stack').width() * ${dashboard.width});
								} else {
									newWidthPixels = $($('.grid-stack-item')[${item.index} - 1]).attr('data-gs-width');
								}

								$('.grid-stack').data('gridstack').resize(
										$('.grid-stack-item')[${item.index} - 1],
										newWidthPixels,
										newHeightPixels
										);
							}
						});
					} else {
						$("#itemContent_${item.index}").load("${item.url}");
					}
					//https://stackoverflow.com/questions/2441197/javascript-setinterval-loop-not-holding-variable
					//using variables like below doesn't work properly. setinterval will be set on the last portlet (last variable contents)
					//$(contentDivId).load(itemUrl);
				}, refreshPeriodMilliseconds);

				intervalIds[contentDivId] = intervalId;
			}
	</c:forEach>

			$('.toggle').on('click', function () {
				var parentDiv = $(this).parent('div');
				var contentDivId = parentDiv.data("content-div-id");
				var itemUrl = parentDiv.data("url");
				var src = $(this).attr('src'); //this.src gives full url i.e. http://... while $(this).attr('src') gives relative url i.e. contextpath/...
				var mimimizeUrl = "${pageContext.request.contextPath}/images/minimize.png";
				var maximizeUrl = "${pageContext.request.contextPath}/images/maximize.png";
				var baseUrl = parentDiv.data("base-url");
				var parametersObject = parentDiv.data("parameters-json"); //json string gets converted to object
				var index = parentDiv.data("index");
				var autoheight = parentDiv.data("autoheight");
				var autowidth = parentDiv.data("autowidth");

				if (src === mimimizeUrl) {
					$(contentDivId).hide();
					$(this).attr('src', maximizeUrl);
				} else {
					$(contentDivId).show();
					$(this).attr('src', mimimizeUrl);

					//refresh item contents every time it's maximized/shown
					if (baseUrl) {
						//use post for art reports
						$(contentDivId).load(baseUrl, parametersObject, function () {
							autosize(autoheight, autowidth, index);
						});
					} else {
						$(contentDivId).load(itemUrl);
					}
				}
			});

			$('.refresh').on('click', function () {
				var parentDiv = $(this).parent('div');
				var contentDivId = parentDiv.data("content-div-id");
				var itemUrl = parentDiv.data("url");
				var refreshPeriodSeconds = parentDiv.data("refresh-period-seconds");
				var baseUrl = parentDiv.data("base-url");
				var parametersObject = parentDiv.data("parameters-json"); //json string gets converted to object
				var index = parentDiv.data("index");
				var autoheight = parentDiv.data("autoheight");
				var autowidth = parentDiv.data("autowidth");

				if (baseUrl) {
					//use post for art reports
					$(contentDivId).load(baseUrl, parametersObject, function () {
						autosize(autoheight, autowidth, index);
					});
				} else {
					$(contentDivId).load(itemUrl);
				}

				//reset/restart refresh interval
				if (refreshPeriodSeconds !== -1) {
					clearInterval(intervalIds[contentDivId]);

					var refreshPeriodMilliseconds = refreshPeriodSeconds * 1000;

					var setIntervalId = setInterval(function () {
						if (baseUrl) {
							//use post for art reports
							$(contentDivId).load(baseUrl, parametersObject, function () {
								autosize(autoheight, autowidth, index);
							});
						} else {
							$(contentDivId).load(itemUrl);
						}
					}, refreshPeriodMilliseconds);

					intervalIds[contentDivId] = setIntervalId;
				}
			});

			//horizontal margin/padding not an api option. defaults to 20px (10px on the left + 10px on the right)
			//can be overriden in css
			//https://github.com/troolee/gridstack.js/issues/33
			$('.grid-stack').gridstack({
				width: ${dashboard.width},
				alwaysShowResizeHandle: ${dashboard.alwaysShowResizeHandle},
				resizable: {
					handles: 'e, se, s, sw, w, n'
				},
				cellHeight: '${dashboard.cellHeight}',
				float: ${dashboard.floatEnabled},
				animate: ${dashboard.animate},
				disableDrag: ${dashboard.disableDrag},
				disableResize: ${dashboard.disableResize},
				verticalMargin: '${dashboard.verticalMargin}'
			});
		});
</script>

<script type="text/javascript">
	//https://blogs.msdn.microsoft.com/ukadc/2010/02/12/handling-errors-with-jquery-load/
	$(document).ajaxError(function (event, xhr, options) {
		bootbox.alert({
			title: '${errorOccurredText}',
			message: xhr.responseText
		});
	});

	var token = $("meta[name='_csrf']").attr("content");
	var header = $("meta[name='_csrf_header']").attr("content");
	$(document).ajaxSend(function (e, xhr, options) {
		xhr.setRequestHeader(header, token);
	});
</script>

<div id="saveDashboardDialogDiv" style="display:none;">
	<form id="saveDashboardForm" class="form-horizontal" role="form">
		<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
		<input type="hidden" name="reportId" value="${report.reportId}">
		<input type="hidden" id="config" name="config" value="">
        <div class="form-group">
			<label class="control-label col-md-5" for="name">
				<spring:message code="page.text.name"/>
			</label>
			<div class="col-md-7">
				<input type="text" id="name" name="name" maxlength="50" class="form-control"/>
			</div>
		</div>
		<div class="form-group">
			<label class="control-label col-md-5" for="description">
				<spring:message code="page.text.description"/>
			</label>
			<div class="col-md-7">
				<textarea id="description" name="description" class="form-control" rows="2" maxlength="200"></textarea>
			</div>
		</div>
		<c:if test="${exclusiveAccess}">
			<div class="form-group">
				<label class="control-label col-md-5" for="overwrite">
					<spring:message code="reports.text.overwrite"/>
				</label>
				<div class="col-md-7">
					<div class="checkbox">
						<label>
							<input type="checkbox" name="overwrite" id="overwrite" checked>
						</label>
					</div>
				</div>
			</div>
		</c:if>
		<div class="form-group">
			<label class="control-label col-md-5" for="saveSelectedParameters">
				<spring:message code="dialog.label.saveSelectedParameters"/>
			</label>
			<div class="col-md-7">
				<div class="checkbox">
					<label>
						<input type="checkbox" name="saveSelectedParameters" id="saveSelectedParameters">
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

		$('.grid-stack-item.ui-draggable').each(function () {
			var $this = $(this);
			items.push({
				index: parseInt($this.attr('data-index'), 10),
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
					var reportParameters = '${requestParameters}';
					if (reportParameters) {
						data = data + '&' + reportParameters;
					}

					$.ajax({
						type: 'POST',
						url: '${pageContext.request.contextPath}/saveGridstack',
						dataType: 'json',
						data: data,
						success: function (response) {
							if (response.success) {
								if (!${exclusiveAccess} ||
										(${exclusiveAccess} && !dialog.find('#overwrite').is(':checked'))) {
									var newReportId = response.data;
									var newUrl = "${pageContext.request.contextPath}/selectReportParameters?reportId=" + newReportId;
									$("#newDashboardLink").attr("href", newUrl);
									$("#newDashboardLink").show();
								}
								$.notify("${reportSavedText}", "success");
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

	$("#deleteDashboard").on("click", function () {
		var reportName = '${encode:forJavaScript(report.name)}';
		var reportId = ${report.reportId};

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
								$.notify("${reportDeletedText}", "success");
							} else if (nonDeletedRecords !== null && nonDeletedRecords.length > 0) {
								$.notify("${cannotDeleteReportText}", "error");
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
	});

//	$('.grid-stack').on('added', function (event, items) {
//		for (var i = 0; i < items.length; i++) {
//			console.log('item added');
//			console.log(items[i]);
//		}
//	});
//	
//	$('.grid-stack').on('change', function (event, items) {
//		for (var i = 0; i < items.length; i++) {
//			console.log('item changed');
//			console.log(items[i]);
//		}
//	});
</script>
