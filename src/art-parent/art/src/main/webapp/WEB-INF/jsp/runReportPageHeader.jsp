<%-- 
    Document   : runReportPageHeader
    Created on : 30-May-2014, 11:02:46
    Author     : Timothy Anyona

Html page header fragment when displaying report output in a new page
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">

		<meta name="_csrf" content="${_csrf.token}"/>
		<meta name="_csrf_header" content="${_csrf.headerName}"/>

		<title>ART - ${title}</title>

		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-3.3.7/css/bootstrap.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/font-awesome-4.7.0/css/font-awesome.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/art.css">

		<link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico">

		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-1.12.4.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-3.3.7/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/art.js"></script>

		<c:if test="${allowSelectParameters}">
			<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-datetimepicker-4.17.47/css/bootstrap-datetimepicker.min.css">
			<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/css/bootstrap-select.min.css">
			<script type="text/javascript" src="${pageContext.request.contextPath}/js/appelsiini-chained-selects-1.0.1/jquery.chained.remote.min.js"></script>
		</c:if>

		<!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
		<!--[if lt IE 9]>
		  <script type='text/javascript' src="${pageContext.request.contextPath}/js/html5shiv-3.7.0.js"></script>
		  <script type='text/javascript' src="${pageContext.request.contextPath}/js/respond-1.4.2.min.js"></script>
		<![endif]-->
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

					<c:if test="${allowSelectParameters}">
						<jsp:include page="/WEB-INF/jsp/selectReportParametersBody.jsp"/>
						<div class="row">
							<div class="col-md-12">
								<div id="reportOutput">
								</c:if>
