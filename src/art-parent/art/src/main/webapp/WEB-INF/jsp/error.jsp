<%-- 
    Document   : error
    Created on : 23-Feb-2014, 08:43:05
    Author     : Timothy Anyona

Error page for uncaught exceptions.

Based on
https://stackoverflow.com/questions/3553294/ideal-error-page-for-java-ee-app
http://www.tutorialspoint.com/jsp/jsp_exception_handling.htm
--%>

<%@page isErrorPage="true" import="java.io.*"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>ART - Error</title>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-3.0.0/css/bootstrap.min.css">
		<link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico">
    </head>
    <body>
        <h1>Error</h1>
        <p>An unexpected error has occurred</p>
		
		<c:if test="${showErrors}">
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
				<tr>
					<td><b>Exception:</b></td>
					<td>
						<pre>
							<%
								java.io.PrintWriter pOut = new java.io.PrintWriter(out);
								try {
									// The Servlet spec guarantees this attribute will be available
									Throwable err = (Throwable) request.getAttribute("javax.servlet.error.exception");

									if (err != null) {
										if (err instanceof ServletException) {
											// It's a ServletException: we should extract the root cause
											ServletException se = (ServletException) err;
											Throwable rootCause = se.getRootCause();
											if (rootCause == null) {
												rootCause = se;
											}
											out.println("<b>** Root cause is:</b> " + rootCause.getMessage());
											rootCause.printStackTrace(pOut);
										} else {
											// It's not a ServletException, so we'll just show it
											err.printStackTrace(pOut);
										}
									} else {
										out.println("No error information available");
									}
								} catch (Exception ex) {
									ex.printStackTrace(pOut);
								}
							%>
						</pre>
					</td>
				</tr>
			</table>
		</c:if>
    </body>
</html>
