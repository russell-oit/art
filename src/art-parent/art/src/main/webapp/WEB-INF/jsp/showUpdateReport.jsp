<%-- 
    Document   : showUpdateReport
    Created on : 24-Mar-2016, 20:13:48
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<div style="text-align: center">
	<spring:message code="reports.text.rowsUpdated"/>: ${rowsUpdated}
</div>
