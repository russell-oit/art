<%-- 
    Document   : error-405
    Created on : 28-Feb-2014, 16:54:54
    Author     : Timothy Anyona

Error page for 405 errors (method not allowed)
--%>

<%@page isErrorPage="true"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>ART - Method Not Allowed</title>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/bootstrap-3.0.0.min.css">
		<link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico">
    </head>
    <body>
        <h1>Method Not Allowed</h1>
		<table class="table table-bordered">
			<tr>
				<td><b>Page:</b></td>
				<td><c:out value="${pageContext.errorData.requestURI}"/></td>
			</tr>
			<tr>
				<td><b>Status Code:</b></td>
				<td>${pageContext.errorData.statusCode}</td>
			</tr>
			<tr>
				<td><b>Message:</b></td>
				<td>
					<%-- this doesn't have the same content as ${pageContext.exception.message} --%>
					<pre>
						<c:out value="${requestScope['javax.servlet.error.message']}"/>
					</pre>
				</td>
			</tr>
		</table>
    </body>
</html>