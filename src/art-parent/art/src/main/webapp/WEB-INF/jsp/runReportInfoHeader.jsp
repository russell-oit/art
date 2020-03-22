<%-- 
    Document   : runReportInfoHeader
    Created on : 29-May-2014, 17:39:27
    Author     : Timothy Anyona

Display report info about a report that is being run
e.g. report name and report generation progress
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<br>
<div class="reportBanner">
	<span id="reportProgress-${runId}" class="pull-right">
	</span>
</div>
<div class="reportBanner">
	<span id="reportInfo-${runId}">
		<encode:forHtmlContent value="${reportName}"/>
	</span>
</div>
