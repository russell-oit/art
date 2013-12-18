<%-- 
    Document   : displayDate
    Created on : 11-Dec-2013, 16:43:01
    Author     : Timothy Anyona

Display a formatted date. Also enables correct sorting in datatables
--%>

<%@tag description="Display Date" pageEncoding="UTF-8"%>
<%@tag trimDirectiveWhitespaces="true" %>

<%@tag body-content="empty" %> 

<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="date" type="java.util.Date"%>
<%@attribute name="sortable" type="java.lang.Boolean" %>
<%@attribute name="pattern" %>

<%-- any content can be specified here e.g.: --%>

<%-- set default values for sortable and pattern attributes --%>
<c:if test="${sortable == null}">
	<c:set var="sortable" value="${true}" />
</c:if>

<c:if test="${empty pattern}">
	<c:set var="pattern" value="${displayDatePattern}" />
</c:if>

<c:if test="${sortable}">
	<%-- hidden span to enable correct sorting of dates --%>
	<span class="hidden">
		<fmt:formatDate value="${date}" pattern="${sortDatePattern}"/>
	</span>
</c:if>
<fmt:formatDate value="${date}" pattern="${pattern}"/>