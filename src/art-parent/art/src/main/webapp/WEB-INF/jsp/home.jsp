<%-- 
    Document   : home
    Created on : 01-Oct-2013, 09:53:44
    Author     : Timothy Anyona

Home page
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<spring:htmlEscape defaultHtmlEscape="true"/>

<spring:message code="page.title.home" var="pageTitle" scope="page"/>

<t:mainPage title="ART - ${pageTitle}">
	Home
</t:mainPage>
