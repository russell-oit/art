<%-- 
    Document   : displayDate
    Created on : 11-Dec-2013, 16:43:01
    Author     : Timothy Anyona

Display a date. Formatted and in a way to enable sorting
--%>

<%@tag description="Display Date" pageEncoding="UTF-8"%>
<%@taglib  uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="date" type="java.util.Date"%>

<%-- any content can be specified here e.g.: --%>
<!-- hidden span to enable correct sorting of dates -->
<span style="display: none">
	<fmt:formatDate value="${date}" pattern="${sortDatePattern}"/>
</span>
<fmt:formatDate value="${date}" pattern="${displayDatePattern}"/>