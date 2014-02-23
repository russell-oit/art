<%-- 
    Document   : error-404
    Created on : 23-Feb-2014, 11:37:03
    Author     : Timothy Anyona

Error page for 404 errors (page not found)
--%>

<%@page isErrorPage="true" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>ART - Page Not Found</title>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/bootstrap-3.0.0.min.css">
    </head>
    <body>
        <h1>Page Not Found</h1>
		${pageContext.errorData.requestURI}
    </body>
</html>
