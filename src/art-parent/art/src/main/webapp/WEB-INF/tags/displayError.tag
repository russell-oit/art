<%-- 
    Document   : displayError
    Created on : 11-Nov-2014, 13:48:38
    Author     : Timothy Anyona
--%>

<%@tag description="Display error details" pageEncoding="UTF-8"%>
<%@tag trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="error" type="Throwable" required="true" %>
<%@attribute name="showErrors" type="Boolean" required="true" %>

<%-- any content can be specified here e.g.: --%>
<p>
	<spring:message code="page.message.errorOccurred"/>
</p>

<c:if test="${showErrors}">
	<t:displayStackTrace error="${error}"/>
</c:if>