<%-- 
    Document   : genericPage
    Created on : 19-Sep-2013, 16:52:05
    Author     : Timothy Anyona

Template for a basic page
Includes bootstrap css, font awesome css, art css
--%>

<%@tag description="Generic Page Template" pageEncoding="UTF-8"%>
<%@tag trimDirectiveWhitespaces="true" %>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="title" required="true"%>
<%@attribute name="headContent" fragment="true" %>
<%@attribute name="javascript" fragment="true" %>
<%@attribute name="css" fragment="true" %>
<%@attribute name="metaContent" fragment="true" %>
<%@attribute name="header" fragment="true" %>
<%@attribute name="footer" fragment="true" %>

<%-- any content can be specified here e.g.: --%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">

		<title>${title}</title>
		
		<jsp:invoke fragment="metaContent"/>

		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-3.3.7/css/bootstrap.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/font-awesome-4.7.0/css/font-awesome.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/art.css">

		<jsp:invoke fragment="css"/>
		
		<link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico">
		
		<jsp:invoke fragment="headContent"/>

		<!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
		<!--[if lt IE 9]>
		  <script type='text/javascript' src="${pageContext.request.contextPath}/js/html5shiv-3.7.0.js"></script>
		  <script type='text/javascript' src="${pageContext.request.contextPath}/js/respond-1.4.2.min.js"></script>
		<![endif]-->
	</head>
	<body>
		<div id="wrap">
			<jsp:invoke fragment="header"/>

			<div id="pageContent">
				<div class="container-fluid">
					<jsp:doBody/>
				</div>
			</div>
			<div id="push"></div>
		</div>
		
		<jsp:invoke fragment="footer"/>

		<!-- javascript placed at the end of the document so that pages load faster -->
		<jsp:invoke fragment="javascript"/>
	</body>
</html>