<%-- 
    Document   : error-404
    Created on : 23-Feb-2014, 11:37:03
    Author     : Timothy Anyona

Error page for 404 errors (page not found)
--%>

<%@page isErrorPage="true"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>ART - Page Not Found</title>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/bootstrap-3.0.0.min.css">
		<link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico">
    </head>
    <body>
        <h1>Page Not Found</h1>

		<c:if test="${showErrors}">
			<table class="table table-bordered">
				<tr>
					<td><b>Page:</b></td>
					<td><c:out value="${pageContext.errorData.requestURI}"/></td>
				</tr>
				<tr>
					<td><b>Status Code:</b></td>
					<td>${pageContext.errorData.statusCode}</td>
				</tr>
			</table>
		</c:if>
    </body>
</html>
