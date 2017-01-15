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
        <title>ART - ${reportName}</title>

		<link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-3.3.6/css/bootstrap.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/font-awesome-4.5.0/css/font-awesome.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/art.css">

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-1.12.4.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-3.3.6/js/bootstrap.min.js"></script>

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-hover-dropdown-2.0.3.min.js"></script>

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/art.js"></script>

		<!-- include tooltipster resources for chart tooltips -->
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/tooltipster/css/tooltipster.bundle.min.css">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/tooltipster/js/tooltipster.bundle.min.js"></script>

	</head>
	<body>
		<jsp:include page="/WEB-INF/jsp/header.jsp"/>

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

		<jsp:include page="/WEB-INF/jsp/showDashboardInline.jsp"/>

		<jsp:include page="/WEB-INF/jsp/footer.jsp"/>
	</body>
</html>
