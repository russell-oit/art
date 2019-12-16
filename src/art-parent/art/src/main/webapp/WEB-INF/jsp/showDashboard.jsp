<%-- 
    Document   : showDashboard
    Created on : 16-Mar-2016, 06:53:01
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">

		<meta name="_csrf" content="${_csrf.token}"/>
		<meta name="_csrf_header" content="${_csrf.headerName}"/>

        <title>${reportName} - ART</title>

		<link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-3.3.7/css/bootstrap.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/font-awesome-4.7.0/css/font-awesome.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/art.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-select-1.12.4/css/bootstrap-select.min.css">

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-1.12.4.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-3.3.7/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/art.js"></script>

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-select-1.12.4/js/bootstrap-select.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-hover-dropdown-2.0.3.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootbox-4.4.0.min.js"></script>
	</head>
	<body>
		<div id="wrap">
			<jsp:include page="/WEB-INF/jsp/header.jsp"/>

			<div id="pageContent">
				<div class="container-fluid">

					<div class="row" id="errorsDiv">
						<div class="col-md-12">
							<div id="ajaxResponse">
							</div>
						</div>
					</div>

					<div id="spinner">
						<img src="${pageContext.request.contextPath}/images/spinner.gif" alt="Processing..." />
					</div>

					<script type="text/javascript">
						$(document).ajaxStart(function () {
							$('#spinner').show();
						}).ajaxStop(function () {
							$('#spinner').hide();
						});
					</script>

					<c:if test="${allowSelectParameters}">
						<jsp:include page="/WEB-INF/jsp/selectReportParametersBody.jsp"/>
						<div class="row">
							<div class="col-md-12">
								<div id="reportOutput">
								</c:if>

								<c:choose>
									<c:when test="${reportFormat == 'pdf'}">
										<jsp:include page="/WEB-INF/jsp/showDashboardFileLink.jsp"/>
									</c:when>
									<c:when test="${reportType == 'Dashboard'}">
										<jsp:include page="/WEB-INF/jsp/showDashboardInline.jsp"/>
									</c:when>
									<c:when test="${reportType == 'GridstackDashboard'}">
										<jsp:include page="/WEB-INF/jsp/showGridstackDashboardInline.jsp"/>
									</c:when>
								</c:choose>

								<c:choose>
									<c:when test="${allowSelectParameters}">
									</div>
								</div>
							</div>
						</c:when>
						<c:otherwise>
							<script>
								$(document).ready(function () {
									var httpMethod = "${httpMethod}";
									if (httpMethod === "GET") {
										var mainRefreshPeriodSeconds = ${refreshPeriodSeconds};
										if (mainRefreshPeriodSeconds >= 5) {
											var mainRefreshPeriodMilliseconds = mainRefreshPeriodSeconds * 1000;
											setInterval(function () {
												location.reload(true);
											}, mainRefreshPeriodMilliseconds);
										}
									}
								});
							</script>
						</c:otherwise>
					</c:choose>

				</div>
			</div>
			<div id="push"></div>
		</div>

		<jsp:include page="/WEB-INF/jsp/footer.jsp"/>
	</body>
</html>
