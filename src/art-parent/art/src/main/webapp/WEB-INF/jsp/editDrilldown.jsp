<%-- 
    Document   : editDrilldown
    Created on : 14-Apr-2014, 09:25:11
    Author     : Timothy Anyona

Edit a drilldown
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<c:choose>
	<c:when test="${action == 'add'}">
		<spring:message code="page.title.addDrilldown" var="pageTitle"/>
	</c:when>
	<c:when test="${action == 'edit'}">
		<spring:message code="page.title.editDrilldown" var="pageTitle"/>
	</c:when>
</c:choose>

<spring:message code="select.text.noResultsMatch" var="noResultsMatchText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-6 col-md-offset-3">
	
	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/css/bootstrap-select.min.css">
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/js/bootstrap-select.min.js"></script>
		
		<script type="text/javascript">
			$(document).ready(function() {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="reportsConfig"]').parent().addClass('active');

				//{container: 'body'} needed if tooltips shown on input-group element or button
				$("[data-toggle='tooltip']").tooltip({container: 'body'});

				//Enable Bootstrap-Select
				$('.selectpicker').selectpicker({
					liveSearch: true,
					noneResultsText: '${noResultsMatchText}',
				});

				//activate dropdown-hover. to make bootstrap-select open on hover
				//must come after bootstrap-select initialization
				$('button.dropdown-toggle').dropdownHover({
					delay: 100
				});

			});
		</script>
	</jsp:attribute>

	<jsp:attribute name="aboveMainPanel">
		<div class="text-right">
			<a href="${pageContext.request.contextPath}/docs/Manual.html#drill-down-reports">
				<spring:message code="page.link.help"/>
			</a>
		</div>
	</jsp:attribute>

	<jsp:body>
		<spring:url var="formUrl" value="/saveDrilldown"/>
		<form:form class="form-horizontal" method="POST" action="${formUrl}" modelAttribute="drilldown">
			<fieldset>
				<c:if test="${formErrors != null}">
					<div class="alert alert-danger alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
						<spring:message code="page.message.formErrors"/>
					</div>
				</c:if>
				<c:if test="${error != null}">
					<div class="alert alert-danger alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
						<p><spring:message code="page.message.errorOccurred"/></p>
						<c:if test="${showErrors}">
							<p>${encode:forHtmlContent(error)}</p>
						</c:if>
					</div>
				</c:if>

				<input type="hidden" name="action" value="${action}">
				<input type="hidden" name="parent" value="${parent}">
				<form:hidden path="parentReportId"/>
				<form:hidden path="position"/>

				<div class="form-group">
					<div class="col-md-12 text-center">
						<b><spring:message code="drilldowns.text.parentReport"/>:</b> ${parentReportName}
					</div>
				</div>

				<div class="form-group">
					<label class="control-label col-md-4">
						<spring:message code="page.label.id"/>
					</label>
					<div class="col-md-8">
						<c:if test="${action == 'edit'}">
							<form:input path="drilldownId" readonly="true" class="form-control"/>
						</c:if>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="drilldownReport.reportId">
						<spring:message code="drilldowns.text.drilldownReport"/>
					</label>
					<div class="col-md-8">
						<form:select path="drilldownReport.reportId" class="form-control selectpicker">
							<c:forEach var="drilldownReport" items="${drilldownReports}">
								<form:option value="${drilldownReport.reportId}">${drilldownReport.getLocalizedName(pageContext.response.locale)}</form:option>
							</c:forEach>
						</form:select>
						<form:errors path="drilldownReport.reportId" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="headerText">
						<spring:message code="drilldowns.label.headerText"/>
					</label>
					<div class="col-md-8">
						<div class="input-group">
							<form:input path="headerText" maxlength="50" class="form-control"/>
							<spring:message code="drilldowns.help.headerText" var="help"/>
							<span class="input-group-btn" >
								<button class="btn btn-default" type="button"
										data-toggle="tooltip" title="${help}">
									<i class="fa fa-info"></i>
								</button>
							</span>
						</div>
						<form:errors path="headerText" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="linkText">
						<spring:message code="drilldowns.label.linkText"/>
					</label>
					<div class="col-md-8">
						<div class="input-group">
							<form:input path="linkText" maxlength="50" class="form-control"/>
							<spring:message code="drilldowns.help.linkText" var="help"/>
							<span class="input-group-btn" >
								<button class="btn btn-default" type="button"
										data-toggle="tooltip" title="${help}">
									<i class="fa fa-info"></i>
								</button>
							</span>
						</div>
						<form:errors path="linkText" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="reportFormat">
						<spring:message code="drilldowns.label.reportFormat"/>
					</label>
					<div class="col-md-8">
						<form:select path="reportFormat" class="form-control selectpicker">
							<form:option value="default"><spring:message code="drilldowns.option.default"/></form:option>
								<option data-divider="true"></option>
							<c:forEach var="reportFormat" items="${reportFormats}">
								<form:option value="${reportFormat.value}">
									<spring:message code="${reportFormat.localizedDescription}"/>
								</form:option>
							</c:forEach>
						</form:select>
						<form:errors path="reportFormat" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="openInNewWindow">
						<spring:message code="drilldowns.label.openInNewWindow"/>
					</label>
					<div class="col-md-8">
						<div class="checkbox">
							<form:checkbox path="openInNewWindow" id="openInNewWindow"/>
						</div>
					</div>
				</div>
				<div class="form-group">
					<div class="col-md-12">
						<button type="submit" class="btn btn-primary pull-right">
							<spring:message code="page.button.save"/>
						</button>
					</div>
				</div>
			</fieldset>
		</form:form>
	</jsp:body>
</t:mainPageWithPanel>
