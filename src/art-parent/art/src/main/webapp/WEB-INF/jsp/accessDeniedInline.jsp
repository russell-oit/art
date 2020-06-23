<%-- 
    Document   : accessDeniedInline
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<div class="row">
	<div class="col-md-6 col-md-offset-3 alert alert-danger text-center">
		<p><spring:message code="page.message.accessDenied"/></p>
		<c:if test="${not empty message}">
			<p><spring:message code="${message}"/></p>
		</c:if>
	</div>
</div>
