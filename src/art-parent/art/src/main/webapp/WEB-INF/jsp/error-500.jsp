<%-- 
    Document   : error-500
    Created on : 20-May-2014, 18:03:26
    Author     : Timothy Anyona

Display 500 error (internal server error)
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<!DOCTYPE html>
<html>
    <head>
        <meta charset='utf-8'>
        <title>ART - Internal Server Error</title>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-3.3.6/css/bootstrap.min.css">
		<link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico">
    </head>
    <body>
        <jsp:include page="/WEB-INF/jsp/error-500-inline.jsp"/>
    </body>
</html>