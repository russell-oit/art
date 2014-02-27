<%-- 
    Document   : logout
    Created on : 30-Oct-2013, 09:10:16
    Author     : Timothy Anyona

Display logout page. Only used with auto login
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<spring:message code="page.title.logout" var="pageTitle" scope="page"/>

<t:headerlessPage title="${pageTitle}">
	<jsp:body>
		<div class="row">
			<div class="col-md-12 alert alert-info text-center spacer60">
				<spring:message code="logout.message.sessionEnded"/>
			</div>
		</div>
		<div class="row">
			<div class="col-md-6 col-md-offset-3 text-center">
				<a class="btn btn-default" href="${pageContext.request.contextPath}/login.do">
					<spring:message code="logout.link.login"/>
				</a>
			</div>
		</div>
	</jsp:body>
</t:headerlessPage>
