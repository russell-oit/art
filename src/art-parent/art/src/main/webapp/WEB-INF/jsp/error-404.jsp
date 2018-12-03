<%-- 
    Document   : error-404
    Created on : 23-Feb-2014, 11:37:03
    Author     : Timothy Anyona

Error page for 404 errors (page not found)
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<!DOCTYPE html>
<html>
    <head>
        <meta charset='utf-8'>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>ART - Page Not Found</title>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-3.3.7/css/bootstrap.min.css">
		<link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico">
    </head>
    <body>
        <jsp:include page="/WEB-INF/jsp/error-404-inline.jsp"/>
    </body>
</html>
