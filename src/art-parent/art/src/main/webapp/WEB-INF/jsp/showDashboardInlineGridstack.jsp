<%-- 
    Document   : showDashboardInline
    Created on : 17-Mar-2016, 10:23:24
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/dashboard.css" /> 

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
			<div class="grid-stack grid-stack-3">
				<c:forEach var="column" items="${dashboard.columns}">
					<c:forEach var="portlet" items="${column}">
						<div class="grid-stack-item">
							<div class="grid-stack-item-content">
								<div id="div_${portlet.source}">
									<div class="${portlet.classNamePrefix}Box">
										<div class="${portlet.classNamePrefix}Tools"
											 data-content-div-id="#portlet_${portlet.source}"
											 data-url="${portlet.baseUrl}"
											 data-refresh-period="${portlet.refreshPeriod}">
											<img class="refresh" src="${pageContext.request.contextPath}/images/refresh.png"/>
											<img class="toggle" src="${pageContext.request.contextPath}/images/minimize.png"/>
										</div>
										<div class="${portlet.classNamePrefix}Title">
											${portlet.title}
										</div>
										<div id="portlet_${portlet.source}" class="${portlet.classNamePrefix}Content">
										</div>
									</div>
								</div>
							</div>
						</div>
					</c:forEach>
				</c:forEach>
			</div>
		</div> 
    </div>
	<!-- Container-fluid -->
</div>

<script type="text/javascript">
	$(document).ready(function () {
		//https://stackoverflow.com/questions/109086/stop-setinterval-call-in-javascript
		//https://stackoverflow.com/questions/351495/dynamically-creating-keys-in-javascript-associative-array
		var intervalIds = {};

	<c:forEach var="column" items="${dashboard.columns}">
		<c:forEach var="portlet" items="${column}">
		var contentDivId = "#portlet_${portlet.source}";
		var portletUrl = "${portlet.baseUrl}";

		if (${portlet.executeOnLoad}) {
			$(contentDivId).load(portletUrl);
		}

		var refreshPeriod = '${portlet.refreshPeriod}';
		if (refreshPeriod !== '') {
			var refreshPeriodSeconds = parseInt(refreshPeriod, 10);
			var refreshPeriodMilliseconds = refreshPeriodSeconds * 1000;
			var intervalId = setInterval(function () {
				$("#portlet_${portlet.source}").load("${portlet.baseUrl}");
				//https://stackoverflow.com/questions/2441197/javascript-setinterval-loop-not-holding-variable
				//using variables like below doesn't work properly. setinterval will be set on the last portlet (last variable contents)
//				$(contentDivId).load(portletUrl);
			}, refreshPeriodMilliseconds);

			intervalIds[contentDivId] = intervalId;
		}
		</c:forEach>
	</c:forEach>

		$('body').on('click', '.toggle', function () {
			var parentDiv = $(this).parent('div');
			var contentDivId = parentDiv.data("content-div-id");
			var portletUrl = parentDiv.data("url");
			var src = $(this).attr('src'); //this.src gives full url i.e. http://... while $(this).attr('src') gives relative url i.e. contextpath/...
			var mimimizeUrl = "${pageContext.request.contextPath}/images/minimize.png";
			var maximizeUrl = "${pageContext.request.contextPath}/images/maximize.png";
			if (src === mimimizeUrl) {
				$(contentDivId).hide();
				$(this).attr('src', maximizeUrl);
			} else {
				$(contentDivId).show();
				$(this).attr('src', mimimizeUrl);
				//refresh portlet contents every time it's maximized/shown
				$(contentDivId).load(portletUrl);
			}
		});

		$('body').on('click', '.refresh', function () {
			var parentDiv = $(this).parent('div');
			var contentDivId = parentDiv.data("content-div-id");
			var portletUrl = parentDiv.data("url");
			var refreshPeriod = parentDiv.data("refresh-period");

			$(contentDivId).load(portletUrl);

			//reset/restart refresh interval
			if (refreshPeriod !== '') {
				clearInterval(intervalIds[contentDivId]);

				var refreshPeriodSeconds = parseInt(refreshPeriod, 10);
				var refreshPeriodMilliseconds = refreshPeriodSeconds * 1000;

				var setIntervalId = setInterval(function () {
					$(contentDivId).load(portletUrl);
				}, refreshPeriodMilliseconds);

				intervalIds[contentDivId] = setIntervalId;
			}
		});

		$('.grid-stack').gridstack({
			width: 3,
			alwaysShowResizeHandle: /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent),
			resizable: {
				handles: 'e, se, s, sw, w'
			},
			cellHeight: 100,
			float: true
		});
	});
</script>
