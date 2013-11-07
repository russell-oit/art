<%-- 
    Document   : headerlessError
    Created on : 07-Nov-2013, 09:41:59
    Author     : Timothy Anyona

Display an error without the main header
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<spring:htmlEscape defaultHtmlEscape="true"/>

<spring:message code="page.title.error" var="pageTitle" scope="page"/>

<t:headerlessPage title="${pageTitle}">
	<jsp:body>
		<div class="row">
			<div class="col-lg-6 col-lg-offset-3 alert alert-danger text-center">
				<spring:message code="${message}"/>
			</div>
		</div>
	</jsp:body>
</t:headerlessPage>
