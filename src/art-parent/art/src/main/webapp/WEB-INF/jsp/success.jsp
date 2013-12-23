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
		<div class="row">
			<div class="col-md-6 col-md-offset-3">
				<div class="alert alert-success text-center">
					<spring:message code="${message}"/>
				</div>
			</div>
		</div>
	</c:if>
</t:mainPage>
