<%-- 
    Document   : error
    Created on : 23-Feb-2014, 08:43:05
    Author     : Timothy Anyona

Error page for uncaught exceptions.
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<!DOCTYPE html>
<html>
    <head>
        <meta charset='utf-8'>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Error - ART</title>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-3.3.7/css/bootstrap.min.css">
		<link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico">
    </head>
    <body>
		<jsp:include page="/WEB-INF/jsp/error-inline.jsp"/>
    </body>
</html>
