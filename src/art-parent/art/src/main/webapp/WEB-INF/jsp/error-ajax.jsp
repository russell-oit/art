<%-- 
    Document   : error-ajax
    Created on : 28-Feb-2014, 15:25:22
    Author     : Timothy Anyona

Unhandled exception response for ajax requests
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

Page: <c:out value="${pageContext.errorData.requestURI}"/>
Status Code: ${pageContext.errorData.statusCode}
Message: <c:out value="${requestScope['javax.servlet.error.message']}"/>
Error: <c:out value="${pageContext.exception}"/>
