<%-- 
    Document   : mainPage
    Created on : 17-Sep-2013, 10:08:05
    Author     : Timothy Anyona

Template for any main application page.
Includes bootstrap css, page header (navbar), page footer
bootstrap js, jquery js
--%>

<%@tag description="Main Page Template" pageEncoding="UTF-8"%>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="title" required="true"%>
<%@attribute name="headContent" fragment="true" %>
<%@attribute name="pageJavascript" fragment="true" %>
<%@attribute name="pageCss" fragment="true" %>
<%@attribute name="metaContent" fragment="true" %>

<%-- any content can be specified here e.g.: --%>
<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="content-type" content="text/html; charset=UTF-8">

		<title>${title}</title>

		<jsp:invoke fragment="metaContent"/>

		<jsp:invoke fragment="headContent"/>

		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/bootstrap-3.0.0.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/font-awesome-3.2.1.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/art-3.css">
		
		<jsp:invoke fragment="pageCss"/>
	</head>
	<body>

		<div id="wrap">
			<jsp:include page="/WEB-INF/jsp/header.jsp"/>

			<div id="pageContent">
				<div  class="container">
					<jsp:doBody/>
				</div>
			</div>
			<div id="push"></div>
		</div>

		<jsp:include page="/WEB-INF/jsp/footer.jsp"/>

		<!-- javascript placed at the end of the document so that pages load faster -->
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-1.10.2.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-3.0.0.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/twitter-bootstrap-hover-dropdown.min.js"></script>

		<jsp:invoke fragment="pageJavascript"/>
	</body>
</html>