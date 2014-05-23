<%-- 
    Document   : runReportInlineError
    Created on : 22-May-2014, 10:21:37
    Author     : Timothy Anyona

Display error when running a report inline (using ajax)
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:if test="${not empty message}">
	<div class="col-md-6 col-md-offset-3 alert alert-danger text-center">
		<spring:message code="${message}"/>
	</div>
</c:if>

<c:if test="${error != null}">
	<div class="alert alert-danger alert-dismissable">
		<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
		<p><spring:message code="page.message.errorOccurred"/></p>
		<c:if test="${showErrors}">
			<p><encode:forHtmlContent value="${error}"/></p>
		</c:if>
</div>
</c:if>
