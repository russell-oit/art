<%-- 
    Document   : casLogout
    Created on : 07-Jul-2016, 09:20:27
    Author     : Timothy Anyona

Display logout page when cas authentication is used
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.logout" var="pageTitle" scope="page"/>

<t:headerlessPage title="${pageTitle}">
	<jsp:body>
		<div class="row">
			<div class="col-md-12 alert alert-info text-center spacer60">
				<p>
					<spring:message code="logout.message.loggedOutOfArt"/>. &nbsp;
					<a class="btn btn-default" href="${pageContext.request.contextPath}/">
						<spring:message code="logout.link.logInAgain"/>
					</a>
				</p>
				<c:if test="${not empty casLogoutUrl}">
					<br>
					<spring:message code="logout.message.toLogOutOfAllApps"/>. &nbsp;
					<a class="btn btn-default" href="${encode:forHtmlAttribute(casLogoutUrl)}">
						<spring:message code="header.link.logout"/>
					</a>
				</c:if>
			</div>
		</div>
	</jsp:body>
</t:headerlessPage>
