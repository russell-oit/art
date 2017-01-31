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
        <title>ART - Page Not Found</title>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/public/js/bootstrap-3.3.6/css/bootstrap.min.css">
		<link rel="shortcut icon" href="${pageContext.request.contextPath}/public/images/favicon.ico">
    </head>
    <body>
        <jsp:include page="/WEB-INF/jsp/error-404-inline.jsp"/>
    </body>
</html>
