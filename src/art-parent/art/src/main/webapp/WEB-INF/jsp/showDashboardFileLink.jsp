<%-- 
    Document   : showDashboardFileLink
    Created on : 26-Apr-2017, 20:37:55
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<br>
<div class="reportBanner">
	<div id="reportInfo">
		<h4>
			<encode:forHtmlContent value="${reportName}"/>
			<small>
				${encode:forHtmlContent(description)} :: ${encode:forHtmlContent(startTimeString)}
			</small>
		</h4>
	</div>
</div>

<jsp:include page="/WEB-INF/jsp/showSelectedParameters.jsp"/>

<jsp:include page="/WEB-INF/jsp/showFileLink.jsp"/>

<div class="reportBanner">
	<spring:message code="reports.text.timeTaken"/>: <spring:message code="reports.text.timeTakenInSeconds" arguments="${timeTakenSeconds}"/>
</div>
