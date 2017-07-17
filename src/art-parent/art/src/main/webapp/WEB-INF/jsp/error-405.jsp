<%-- 
    Document   : error-405
    Created on : 28-Feb-2014, 16:54:54
    Author     : Timothy Anyona

Error page for 405 errors (method not allowed)
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<!DOCTYPE html>
<html>
    <head>
        <meta charset='utf-8'>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>ART - Method Not Allowed</title>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-3.3.6/css/bootstrap.min.css">
		<link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico">
    </head>
    <body>
        <jsp:include page="/WEB-INF/jsp/error-405-inline.jsp"/>
    </body>
</html>