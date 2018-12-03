<%-- 
    Document   : error-403
    Created on : 21-Mar-2018, 20:20:20
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<!DOCTYPE html>
<html>
    <head>
        <meta charset='utf-8'>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>ART - Forbidden</title>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-3.3.7/css/bootstrap.min.css">
		<link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico">
    </head>
    <body>
        <jsp:include page="/WEB-INF/jsp/error-403-inline.jsp"/>
    </body>
</html>
