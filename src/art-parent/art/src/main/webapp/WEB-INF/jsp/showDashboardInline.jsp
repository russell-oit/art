<%-- 
    Document   : showDashboardInline
    Created on : 17-Mar-2016, 10:23:24
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/public/css/dashboard.css" /> 

<div class="container-fluid">
	<div class="col-md-12">
		<div class="row">
			<h2>${encode:forHtmlContent(dashboard.title)}  
				<small>  ${encode:forHtmlContent(dashboard.description)}</small>
			</h2>
		</div>
		<div class="row">
			<c:choose>
				<%-- https://stackoverflow.com/questions/10738044/jstl-el-equivalent-of-testing-for-null-and-list-size --%>
				<c:when test="${dashboard.tabList == null}">
					<table class="plain">
						<tr>
							<c:forEach var="column" items="${dashboard.columns}">
								<td style="vertical-align: top">
									<c:forEach var="portlet" items="${column}">
										<div id="portlet_${portlet.index}">
											<div class="${portlet.classNamePrefix}Box">
												<div class="${portlet.classNamePrefix}Tools"
													 data-content-div-id="#portletContent_${portlet.index}"
													 data-url="${portlet.url}"
													 data-refresh-period-seconds="${portlet.refreshPeriodSeconds}">
													<img class="refresh" src="${pageContext.request.contextPath}/public/images/refresh.png"/>
													<img class="toggle" src="${pageContext.request.contextPath}/public/images/minimize.png"/>
												</div>
												<div class="${portlet.classNamePrefix}Title">
													${portlet.title}
												</div>
												<div id="portletContent_${portlet.index}" class="${portlet.classNamePrefix}Content">
												</div>
											</div>
										</div>
									</c:forEach>
								</td>
							</c:forEach>
						</tr>
					</table>
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
								<table class="plain">
									<tr>
										<c:forEach begin="1" end="${dashboard.columns.size()}" varStatus="loop2">
											<td style="vertical-align: top">
												<c:forEach var="portlet" items="${tab.items}">
													<c:if test="${loop2.count == portlet.columnIndex}">
														<div id="portlet_${portlet.index}">
															<div class="${portlet.classNamePrefix}Box">
																<div class="${portlet.classNamePrefix}Tools"
																	 data-content-div-id="#portletContent_${portlet.index}"
																	 data-url="${portlet.url}"
																	 data-refresh-period-seconds="${portlet.refreshPeriodSeconds}">
																	<img class="refresh" src="${pageContext.request.contextPath}/public/images/refresh.png"/>
																	<img class="toggle" src="${pageContext.request.contextPath}/public/images/minimize.png"/>
																</div>
																<div class="${portlet.classNamePrefix}Title">
																	${portlet.title}
																</div>
																<div id="portletContent_${portlet.index}" class="${portlet.classNamePrefix}Content">
																</div>
															</div>
														</div>
													</c:if>
												</c:forEach>
											</td>
										</c:forEach>
									</tr>
								</table>
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
			<c:set var="items" value="${dashboard.allPortlets}"/>
		</c:when>
		<c:otherwise>
			<c:set var="items" value="${dashboard.tabList.allItems}"/>
		</c:otherwise>
	</c:choose>

	<c:forEach var="portlet" items="${items}">
		var contentDivId = "#portletContent_${portlet.index}";
		var portletUrl = "${portlet.url}";

		//http://balusc.omnifaces.org/2009/05/javajspjsf-and-javascript.html
		if (${portlet.executeOnLoad}) {
			$(contentDivId).load(portletUrl);
		}

		var refreshPeriodSeconds = ${portlet.refreshPeriodSeconds};
		if (refreshPeriodSeconds !== -1) {
			var refreshPeriodMilliseconds = refreshPeriodSeconds * 1000;
			var intervalId = setInterval(function () {
				$("#portletContent_${portlet.index}").load("${portlet.url}");
				//https://stackoverflow.com/questions/2441197/javascript-setinterval-loop-not-holding-variable
				//using variables like below doesn't work properly. setinterval will be set on the last portlet (last variable contents)
//				$(contentDivId).load(portletUrl);
			}, refreshPeriodMilliseconds);

			intervalIds[contentDivId] = intervalId;
		}
	</c:forEach>

		$('body').on('click', '.toggle', function () {
			var parentDiv = $(this).parent('div');
			var contentDivId = parentDiv.data("content-div-id");
			var portletUrl = parentDiv.data("url");
			var src = $(this).attr('src'); //this.src gives full url i.e. http://... while $(this).attr('src') gives relative url i.e. contextpath/...
			var mimimizeUrl = "${pageContext.request.contextPath}/public/images/minimize.png";
			var maximizeUrl = "${pageContext.request.contextPath}/public/images/maximize.png";
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
			var refreshPeriodSeconds = parentDiv.data("refresh-period-seconds");

			$(contentDivId).load(portletUrl);

			//reset/restart refresh interval
			if (refreshPeriodSeconds !== -1) {
				clearInterval(intervalIds[contentDivId]);

				var refreshPeriodMilliseconds = refreshPeriodSeconds * 1000;

				var setIntervalId = setInterval(function () {
					$(contentDivId).load(portletUrl);
				}, refreshPeriodMilliseconds);

				intervalIds[contentDivId] = setIntervalId;
			}
		});
	});
</script>

<script type="text/javascript">
	//https://blogs.msdn.microsoft.com/ukadc/2010/02/12/handling-errors-with-jquery-load/
	$(document).ajaxError(function (event, xhr, options) {
		ajaxErrorHandler(xhr);
	});
</script>
