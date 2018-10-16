<%-- 
    Document   : selectReportParameters
    Created on : 20-May-2014, 15:01:32
    Author     : Timothy Anyona

Display report parameters and initiate running of report
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="select.text.nothingSelected" var="nothingSelectedText"/>
<spring:message code="select.text.noResultsMatch" var="noResultsMatchText"/>
<spring:message code="select.text.selectedCount" var="selectedCountText"/>

<t:mainPage title="${report.getLocalizedName(pageContext.response.locale)}">

	<jsp:attribute name="headContent">
		<c:if test="${hasChainedParam}">
			<script type="text/javascript" src="${pageContext.request.contextPath}/js/appelsiini-chained-selects-1.0.1/jquery.chained.remote.min.js"></script>
		</c:if>
	</jsp:attribute>

	<jsp:body>
		<div class="row" id="errorsDiv">
			<div class="col-md-12">
				<div id="ajaxResponse">
				</div>
			</div>
		</div>
		<jsp:include page="/WEB-INF/jsp/selectReportParametersBody.jsp"/>
		<div class="row">
			<div class="col-md-12">
				<div id="reportOutput">
				</div>
			</div>
		</div>
	</jsp:body>
</t:mainPage>
