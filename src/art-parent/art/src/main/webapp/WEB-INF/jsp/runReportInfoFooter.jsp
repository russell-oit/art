<%-- 
    Document   : runReportInfoFooter
    Created on : 30-May-2014, 16:32:13
    Author     : Timothy Anyona

Display info about a report that has been run
e.g number of records retrieved and time taken to run the report
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<div class="reportBanner">
	<spring:message code="reports.text.rowsRetrieved"/>: ${rowsRetrieved} <br>
	<spring:message code="reports.text.timeTaken"/>: ${timeTaken}
</div>
