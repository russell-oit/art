<%-- 
    Document   : error-ajax
    Created on : 28-Feb-2014, 15:25:22
    Author     : Timothy Anyona

Unhandled exception response for ajax requests
--%>

<%@page isErrorPage="true"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<h1>Error</h1>
<p>An unexpected error has occurred</p>
<table class="table table-bordered">
	<tr>
		<td><b>Error:</b></td>
		<td><pre><c:out value="${pageContext.exception}"/></pre></td>
	</tr>
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
