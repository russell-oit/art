<%-- 
    Document   : accessDenied
    Created on : 01-Oct-2013, 09:30:56
    Author     : Timothy Anyona

Show access denied message when a user tries to access a page he is not authorized to
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<spring:message code="page.title.accessDenied" var="pageTitle"/>

<t:mainPage title="${pageTitle}">
	<jsp:body>
		<jsp:include page="/WEB-INF/jsp/accessDeniedInline.jsp"/>
	</jsp:body>
</t:mainPage>
