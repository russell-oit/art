<%-- 
    Document   : error-404
    Created on : 23-Feb-2014, 11:37:03
    Author     : Timothy Anyona

Error page for 404 errors (page not found)
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


<h1>Page Not Found</h1>

<c:if test="${showErrors}">
	<table class="table table-bordered">
		<tr>
			<td><b>Page:</b></td>
			<td><c:out value="${requestUri}"/></td>
		</tr>
		<tr>
			<td><b>Status Code:</b></td>
			<td>${statusCode}</td>
		</tr>
	</table>
</c:if>
