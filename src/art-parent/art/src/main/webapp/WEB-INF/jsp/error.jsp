<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<jsp:useBean id="date" class="java.util.Date" />
<!DOCTYPE html>
<html lang="en">
    <head>
        <title>Error</title>
        <!--<link rel="stylesheet" href="style.css">-->
    </head>
    <body>
        <h1>Error</h1>
        <p>Unfortunately an unexpected error has occurred. Below you can find the error details.</p>
        <h2>Details</h2>
        <ul>
            <li>Timestamp: <fmt:formatDate value="${date}" type="both" dateStyle="long" timeStyle="long" />
            <li>Action: <c:out value="${requestScope['javax.servlet.forward.request_uri']}" />
            <li>Status code: <c:out value="${requestScope['javax.servlet.error.status_code']}" />
            <li>User agent: <c:out value="${header['user-agent']}" />
        </ul>
		
		Message: 
		
		<pre>
		<c:out value="${requestScope['javax.servlet.error.message']}" />
</pre>
		
		<pre>
		
		<%
			java.io.PrintWriter pOut = new java.io.PrintWriter(out);
		// The Servlet spec guarantees this attribute will be available
         Throwable err = (Throwable) 
             request.getAttribute("javax.servlet.error.exception");

         if(err != null) {
            if(err instanceof ServletException) {
               // It's a ServletException: we should extract the root cause
               ServletException se = (ServletException) err;
               Throwable rootCause = se.getRootCause();
               if(rootCause == null) {
                  rootCause = se;
               }
			   err.printStackTrace(pOut);
               out.println("** Root cause is: ");
               rootCause.printStackTrace(pOut);
            }else {
               // It's not a ServletException, so we'll just show it
               err.printStackTrace(pOut);
            }
         }else {
            out.println("No error information available");
         }
		 %>
		 </pre>

    </body>
</html>