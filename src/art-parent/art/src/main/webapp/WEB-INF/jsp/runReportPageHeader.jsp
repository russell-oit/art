<%-- 
    Document   : runReportPageHeader
    Created on : 30-May-2014, 11:02:46
    Author     : Timothy Anyona

Html page header fragment when displaying report output in a new page
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">

		<title>ART - ${title}</title>

		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-3.0.0/css/bootstrap.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/font-awesome-4.0.3/css/font-awesome.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/art.css">
		
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables-1.10.0/bootstrap/3/dataTables.bootstrap.css">

		<link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico">

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
				<div  class="container">
