<%-- 
    Document   : editReportParameter
    Created on : 23-Mar-2016, 09:51:38
    Author     : Timothy Anyona
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
		<spring:message code="page.title.addReportParameter" var="pageTitle"/>
	</c:when>
	<c:when test="${action == 'edit'}">
		<spring:message code="page.title.editReportParameter" var="pageTitle"/>
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
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="reportsConfig"]').parent().addClass('active');

				//{container: 'body'} needed if tooltips shown on input-group element or button
				$("[data-toggle='tooltip']").tooltip({container: 'body'});

				//Enable Bootstrap-Select
				$('.selectpicker').selectpicker({
					liveSearch: true,
					noneResultsText: '${noResultsMatchText}'
				});

				//activate dropdown-hover. to make bootstrap-select open on hover
				//must come after bootstrap-select initialization
				$('button.dropdown-toggle').bootstrapDropdownHover({
					hideTimeout: 100
				});

			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<spring:url var="formUrl" value="/saveReportParameter"/>
		<form:form class="form-horizontal" method="POST" action="${formUrl}" modelAttribute="reportParameter">
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
				<input type="hidden" name="reportId" value="${reportId}">
				
				<form:hidden path="position"/>

				<div class="form-group">
					<div class="col-md-12 text-center">
						<b><spring:message code="page.text.report"/>:</b> ${reportName}
					</div>
				</div>

				<div class="form-group">
					<label class="control-label col-md-4">
						<spring:message code="page.label.id"/>
					</label>
					<div class="col-md-8">
						<c:if test="${action == 'edit'}">
							<form:input path="reportParameterId" readonly="true" class="form-control"/>
						</c:if>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="parameter.parameterId">
						<spring:message code="page.text.parameter"/>
					</label>
					<div class="col-md-8">
						<c:choose>
							<c:when test="${action == 'add'}">
								<form:select path="parameter.parameterId" class="form-control selectpicker">
									<c:forEach var="parameter" items="${parameters}">
										<form:option value="${parameter.parameterId}">${encode:forHtmlContent(parameter.name)} (${parameter.parameterId})</form:option>
									</c:forEach>
								</form:select>
								<form:errors path="parameter.parameterId" cssClass="error"/>
							</c:when>
							<c:when test="${action == 'edit'}">
								<form:hidden path="parameter.parameterId"/>
								${encode:forHtmlContent(reportParameter.parameter.name)} (${reportParameter.parameter.parameterId})
							</c:when>
						</c:choose>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="chainedParents">
						<spring:message code="parameters.label.chainedParents"/>
					</label>
					<div class="col-md-8">
						<div class="input-group">
							<form:input path="chainedParents" maxlength="200" class="form-control"/>
							<spring:message code="parameters.help.chainedParents" var="help"/>
							<span class="input-group-btn" >
								<button class="btn btn-default" type="button"
										data-toggle="tooltip" title="${help}">
									<i class="fa fa-info"></i>
								</button>
							</span>
						</div>
						<form:errors path="chainedParents" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="chainedDepends">
						<spring:message code="parameters.label.chainedDepends"/>
					</label>
					<div class="col-md-8">
						<div class="input-group">
							<form:input path="chainedDepends" maxlength="200" class="form-control"/>
							<spring:message code="parameters.help.chainedDepends" var="help"/>
							<span class="input-group-btn" >
								<button class="btn btn-default" type="button"
										data-toggle="tooltip" title="${help}">
									<i class="fa fa-info"></i>
								</button>
							</span>
						</div>
						<form:errors path="chainedDepends" cssClass="error"/>
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

