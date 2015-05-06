<%-- 
    Document   : error-400
    Created on : 20-May-2014, 17:57:37
    Author     : Timothy Anyona

Display 400 error (bad request)
--%>

<%@page isErrorPage="true"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
    <head>
        <meta charset='utf-8'>
        <title>ART - Bad Request</title>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-3.0.0/css/bootstrap.min.css">
		<link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico">
    </head>
    <body>
        <h1>Bad Request</h1>

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
		</c:if>
    </body>
</html>