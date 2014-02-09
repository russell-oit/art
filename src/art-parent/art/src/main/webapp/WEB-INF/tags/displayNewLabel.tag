<%-- 
    Document   : displayNewLabel
    Created on : 09-Feb-2014, 18:13:56
    Author     : Timothy Anyona

Display a label to indicate that a record is new
--%>

<%@tag description="Display New Label" pageEncoding="UTF-8"%>
<%@tag trimDirectiveWhitespaces="true" %>

<%@taglib uri="/WEB-INF/functions.tld" prefix="f" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="date" type="java.util.Date" required="true" %>
<%@attribute name="newLimit" type="java.lang.Integer" %>

<%-- any content can be specified here e.g.: --%>
<c:if test="${empty newLimit}">
	<c:set var="newLimit" value="14"/>
</c:if>

<c:if test="${f:daysUntilToday(date) le newLimit}">
	<span class="label label-success">
		<spring:message code="page.text.new"/>
	</span>
</c:if>