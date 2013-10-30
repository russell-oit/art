<%-- 
    Document   : logout
    Created on : 30-Oct-2013, 09:10:16
    Author     : Timothy Anyona

Display logout message. Only used with auto authentication
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<spring:htmlEscape defaultHtmlEscape="true"/>

<spring:message code="page.title.logout" var="pageTitle" scope="page"/>

<t:genericPage title="ART - ${pageTitle}">
	<jsp:attribute name="pageFooter">
		<jsp:include page="/WEB-INF/jsp/footer.jsp"/>
	</jsp:attribute>

	<jsp:body>
		<div class="row">
			<div class="col-lg-6 col-lg-offset-3 text-center">
				<a class="btn btn-default" href="${pageContext.request.contextPath}/login.do">
					<spring:message code="logout.link.login"/>
				</a>
			</div>
		</div>
	</jsp:body>
</t:genericPage>
