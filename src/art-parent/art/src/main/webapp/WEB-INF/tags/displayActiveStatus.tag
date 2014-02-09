<%-- 
    Document   : displayActiveStatus
    Created on : 09-Feb-2014, 18:23:26
    Author     : Timothy Anyona

Display the active status of a record
--%>

<%@tag description="Display Active Status" pageEncoding="UTF-8"%>
<%@tag trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="active" type="java.lang.Boolean" required="true" %>
<%@attribute name="activeText" %>
<%@attribute name="disabledText" %>

<%-- any content can be specified here e.g.: --%>
<c:if test="${empty activeText}">
	<spring:message code="activeStatus.option.active" var="activeText"/>
</c:if>
<c:if test="${empty disabledText}">
	<spring:message code="activeStatus.option.disabled" var="disabledText"/>
</c:if>

<c:choose>
	<c:when test="${active}">
		<span class="label label-success">
			${activeText}
		</span>
	</c:when>
	<c:otherwise>
		<span class="label label-danger">
			${disabledText}
		</span>
	</c:otherwise>
</c:choose>