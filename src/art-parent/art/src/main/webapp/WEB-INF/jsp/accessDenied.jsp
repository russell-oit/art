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
		<div class="row">
			<div class="col-md-6 col-md-offset-3 alert alert-danger text-center">
				<p><spring:message code="page.message.accessDenied"/></p>
				<c:if test="${not empty message}">
					<p><spring:message code="${message}"/></p>
				</c:if>
			</div>
		</div>
	</jsp:body>
</t:mainPage>
