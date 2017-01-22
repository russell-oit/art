<%-- 
    Document   : error-inline
    Created on : 22-Jan-2017, 17:21:13
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


<h1>Error</h1>

<p>An unexpected error has occurred</p>

<c:if test="${showErrors}">
	<table class="table table-bordered">
		<tr>
			<td><b>Error:</b></td>
			<td>
				<pre>
					<c:out value="${exception}"/>
				</pre>
			</td>
		</tr>
		<tr>
			<td><b>Page:</b></td>
			<td><c:out value="${requestUri}"/></td>
		</tr>
		<tr>
			<td><b>Status Code:</b></td>
			<td>${statusCode}</td>
		</tr>
		<tr>
			<td><b>Message:</b></td>
			<td>
				<pre>
					<c:out value="${errorMessage}"/>
				</pre>
			</td>
		</tr>
		<tr>
			<td><b>Exception:</b></td>
			<td>
				<pre>
					<c:out value="${errorDetails}"/>
				</pre>
			</td>
		</tr>
	</table>
</c:if>
