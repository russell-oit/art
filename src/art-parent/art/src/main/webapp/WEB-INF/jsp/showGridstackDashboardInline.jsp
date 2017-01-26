<%-- 
    Document   : showDashboardInline
    Created on : 17-Mar-2016, 10:23:24
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/dashboard.css" /> 

<%-- https://www.versioneye.com/javascript/troolee:gridstack/0.2.5-dev --%>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/gridstack-0.2.5/gridstack.min.css" /> 
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/gridstack-0.2.5/gridstack-extra.min.css" /> 

<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-ui-1.11.4-all-smoothness/jquery-ui.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.ui.touch-punch-0.2.3.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lodash-3.5.0/lodash.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/gridstack-0.2.5/gridstack.min.js"></script>


<div class="container-fluid">
	<div class="col-md-12">
		<div class="row">
			<h2>${dashboard.title}  
				<small>  ${dashboard.description}</small>
			</h2>
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
								 ${item.maxHeight == 0 ? '' : ' data-gs-max-height="'.concat(item.maxHeight).concat('"')}>
								<div class="grid-stack-item-content" style="border: 1px solid #ccc">
									<div id="item_${item.index}">
										<div class="portletAUTOBox">
											<div class="portletAUTOTools"
												 data-content-div-id="#itemContent_${item.index}"
												 data-url="${item.url}"
												 data-refresh-period-seconds="${item.refreshPeriodSeconds}">
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
											 ${item.maxHeight == 0 ? '' : ' data-gs-max-height="'.concat(item.maxHeight).concat('"')}>
											<div class="grid-stack-item-content" style="border: 1px solid #ccc">
												<div id="item_${item.index}">
													<div class="portletAUTOBox">
														<div class="portletAUTOTools"
															 data-content-div-id="#itemContent_${item.index}"
															 data-url="${item.url}"
															 data-refresh-period-seconds="${item.refreshPeriodSeconds}">
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
	<!-- Container-fluid -->
</div>

<script type="text/javascript">
	$(document).ready(function () {
		//https://stackoverflow.com/questions/109086/stop-setinterval-call-in-javascript
		//https://stackoverflow.com/questions/351495/dynamically-creating-keys-in-javascript-associative-array
		var intervalIds = {};

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
		var itemUrl = "${item.url}";

		if (${item.executeOnLoad}) {
			$(contentDivId).load(itemUrl);
		}

		var refreshPeriodSeconds = ${item.refreshPeriodSeconds};
		if (refreshPeriodSeconds !== -1) {
			var refreshPeriodMilliseconds = refreshPeriodSeconds * 1000;
			var intervalId = setInterval(function () {
				$("#itemContent_${item.index}").load("${item.url}");
				//https://stackoverflow.com/questions/2441197/javascript-setinterval-loop-not-holding-variable
				//using variables like below doesn't work properly. setinterval will be set on the last item (last variable contents)
//				$(contentDivId).load(itemUrl);
			}, refreshPeriodMilliseconds);

			intervalIds[contentDivId] = intervalId;
		}
	</c:forEach>

		$('body').on('click', '.toggle', function () {
			var parentDiv = $(this).parent('div');
			var contentDivId = parentDiv.data("content-div-id");
			var itemUrl = parentDiv.data("url");
			var src = $(this).attr('src'); //this.src gives full url i.e. http://... while $(this).attr('src') gives relative url i.e. contextpath/...
			var mimimizeUrl = "${pageContext.request.contextPath}/images/minimize.png";
			var maximizeUrl = "${pageContext.request.contextPath}/images/maximize.png";
			if (src === mimimizeUrl) {
				$(contentDivId).hide();
				$(this).attr('src', maximizeUrl);
			} else {
				$(contentDivId).show();
				$(this).attr('src', mimimizeUrl);
				//refresh item contents every time it's maximized/shown
				$(contentDivId).load(itemUrl);
			}
		});

		$('body').on('click', '.refresh', function () {
			var parentDiv = $(this).parent('div');
			var contentDivId = parentDiv.data("content-div-id");
			var itemUrl = parentDiv.data("url");
			var refreshPeriodSeconds = parentDiv.data("refresh-period-seconds");

			$(contentDivId).load(itemUrl);

			//reset/restart refresh interval
			if (refreshPeriodSeconds !== -1) {
				clearInterval(intervalIds[contentDivId]);

				var refreshPeriodMilliseconds = refreshPeriodSeconds * 1000;

				var setIntervalId = setInterval(function () {
					$(contentDivId).load(itemUrl);
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
				handles: 'e, se, s, sw, w'
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
