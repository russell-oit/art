<%-- 
    Document   : selfServiceDashboard
    Created on : 11-May-2018, 15:23:10
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.selfServiceDashboards" var="pageTitle"/>

<spring:message code="select.text.nothingSelected" var="nothingSelectedText"/>
<spring:message code="select.text.noResultsMatch" var="noResultsMatchText"/>
<spring:message code="select.text.selectedCount" var="selectedCountText"/>

<t:mainPage title="${pageTitle}">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/css/bootstrap-select.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/gridstack-0.2.5/gridstack.min.css" /> 
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/gridstack-0.2.5/gridstack-extra.min.css" />
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/js/bootstrap-select.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-ui-1.11.4-all-smoothness/jquery-ui.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.ui.touch-punch-0.2.3.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/lodash-3.5.0/lodash.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/gridstack-0.2.5/gridstack.min.js"></script>

		<script>
			$(document).ready(function () {
				$('a[id="selfService"]').parent().addClass('active');
				$('a[href*="selfServiceDashboards"]').parent().addClass('active');

				//Enable Bootstrap-Select
				$('.selectpicker').selectpicker({
					liveSearch: true,
					noneSelectedText: '${nothingSelectedText}',
					noneResultsText: '${noResultsMatchText}',
					countSelectedText: '${selectedCountText}'
				});

				$('.grid-stack').gridstack();

				//https://stackoverflow.com/questions/35349239/bootstrap-select-event-parameters
				//https://github.com/gridstack/gridstack.js/tree/master/doc
				$("#reports").on('changed.bs.select', function (event, clickedIndex, newValue, oldValue) {
					var reportId = $(this).val();
					var grid = $('.grid-stack').data('gridstack');
					var el = $('<div><div class="grid-stack-item-content" style="border: 1px solid #ccc"><div id="content_'
							+ reportId + '"></div></div></div>');
					grid.addWidget(el, 0, 0, 3, 2, true);
				});
			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<c:if test="${error != null}">
			<div class="alert alert-danger alert-dismissable">
				<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
				<p><spring:message code="page.message.errorOccurred"/></p>
				<c:if test="${showErrors}">
					<p>${encode:forHtmlContent(error)}</p>
				</c:if>
			</div>
		</c:if>

		<div class="row">
			<div class="col-md-2">
				<select id="reports" class="form-control selectpicker" multiple>
					<c:forEach var="report" items="${reports}">
						<option value="${report.reportId}">${encode:forHtmlContent(report.getLocalizedName(pageContext.response.locale))}</option>
					</c:forEach>
				</select>
			</div>
			<div class="col-md-10">
				<div id="dashboard" class="grid-stack"></div>
			</div>
		</div>
	</jsp:body>
</t:mainPage>

