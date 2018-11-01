<%-- 
    Document   : displayNewLabel
    Created on : 10-Feb-2014, 10:04:56
    Author     : Timothy Anyona

Display a label to indicate that a record is new or recently updated
--%>

<%@tag description="Display New Label" pageEncoding="UTF-8"%>
<%@tag trimDirectiveWhitespaces="true" %>

<%@taglib uri="/WEB-INF/tlds/functions.tld" prefix="f" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="creationDate" type="java.util.Date" %>
<%@attribute name="updateDate" type="java.util.Date" %>
<%@attribute name="limit" type="java.lang.Integer" %>
<%@attribute name="newText" %>
<%@attribute name="updatedText" %>

<%-- any content can be specified here e.g.: --%>
<c:if test="${empty limit}">
	<c:set var="limit" value="7"/>
</c:if>
<c:if test="${empty newText}">
	<spring:message code="page.text.new" var="newText"/>
</c:if>
<c:if test="${empty updatedText}">
	<spring:message code="page.text.updated" var="updatedText"/>
</c:if>

<c:if test="${f:daysUntilToday(creationDate) le limit}">
	<span class="label label-success">
		${newText}
	</span>
</c:if>
<c:if test="${f:daysUntilToday(updateDate) le limit}">
	<span class="label label-success">
		${updatedText}
	</span>
</c:if>