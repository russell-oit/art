<%-- 
    Document   : selectReportParameters
    Created on : 20-May-2014, 15:01:32
    Author     : Timothy Anyona

Display report parameters and initiate running of report
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>

<t:mainPage title="${report.getLocalizedName(pageContext.response.locale)}">

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
