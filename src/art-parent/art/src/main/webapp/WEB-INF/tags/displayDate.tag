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
<%@attribute name="span" type="java.lang.Boolean" %>

<%-- any content can be specified here e.g.: --%>

<%-- set default value for span --%>
<c:if test="${span == null}">
	<c:set var="span" value="${true}" />
</c:if>

<c:if test="${span}">
	<%-- hidden span to enable correct sorting of dates --%>
	<span class="hidden">
		<fmt:formatDate value="${date}" pattern="${sortDatePattern}"/>
	</span>
</c:if>
<fmt:formatDate value="${date}" pattern="${displayDatePattern}"/>