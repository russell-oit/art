<%-- 
    Document   : genericPage
    Created on : 17-Sep-2013, 10:08:05
    Author     : Timothy Anyona

Base template for any application page.
Has elements common to all pages
--%>

<%@tag description="Generic Page Template" pageEncoding="UTF-8"%>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="title"%>
<%@attribute name="headContent" fragment="true" %>
<%@attribute name="pageHeader" fragment="true" %>
<%@attribute name="pageJavascript" fragment="true" %>
<%@attribute name="pageCss" fragment="true" %>
<%@attribute name="metaContent" fragment="true" %>

<%-- any content can be specified here e.g.: --%>
<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="content-type" content="text/html; charset=UTF-8">
		<jsp:invoke fragment="metaContent"/>

		<title>${title}</title>

		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/bootstrap.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/font-awesome.min.css">

		<jsp:invoke fragment="pageCss"/>
		<jsp:invoke fragment="headContent"/>
	</head>
	<body>
		
		<jsp:invoke fragment="pageHeader"/>

		<div id="pageContent" class="container">
			<jsp:doBody/>
		</div>

		<jsp:include page="/WEB-INF/jsp/footer.jsp"/>

		<!-- javascript placed at the end of the document so that pages load faster -->
		<jsp:invoke fragment="pageJavascript"/>
	</body>
</html>