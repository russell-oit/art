<%-- 
    Document   : success
    Created on : 23-Dec-2013, 11:29:05
    Author     : Timothy Anyona

Page to display success message
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<spring:message code="page.title.success" var="pageTitle"/>

<t:mainPage title="${pageTitle}">
	<c:if test="${not empty message}">
		<div class="alert alert-success alert-dismissable">
			<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
			<spring:message code="${message}"/>
		</div>
	</c:if>
</t:mainPage>
