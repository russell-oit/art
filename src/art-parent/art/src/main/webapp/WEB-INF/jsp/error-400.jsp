<%-- 
    Document   : error-400
    Created on : 20-May-2014, 17:57:37
    Author     : Timothy Anyona

Display 400 error (bad request)
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<!DOCTYPE html>
<html>
    <head>
        <meta charset='utf-8'>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Bad Request - ART</title>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-3.3.7/css/bootstrap.min.css">
		<link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico">
    </head>
    <body>
		<jsp:include page="/WEB-INF/jsp/error-400-inline.jsp"/>
    </body>
</html>