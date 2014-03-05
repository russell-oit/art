<%-- 
    Document   : editReport
    Created on : 25-Feb-2014, 16:10:21
    Author     : Timothy Anyona

Edit report page
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
		<spring:message code="page.title.addReport" var="pageTitle"/>
		<spring:url var="formUrl" value="/app/addReport.do"/>
	</c:when>
	<c:when test="${action == 'copy'}">
		<spring:message code="page.title.copyReport" var="pageTitle"/>
		<spring:url var="formUrl" value="/app/copyReport.do"/>
	</c:when>
	<c:otherwise>
		<spring:message code="page.title.editReport" var="pageTitle"/>
		<spring:url var="formUrl" value="/app/editReport.do"/>
	</c:otherwise>
</c:choose>

<spring:message code="select.text.nothingSelected" var="nothingSelectedText"/>
<spring:message code="select.text.noResultsMatch" var="noResultsMatchText"/>
<spring:message code="select.text.selectedCount" var="selectedCountText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-6 col-md-offset-3">

	<jsp:attribute name="javascript">
		<script type="text/javascript">
			$(document).ready(function() {
				$(function() {
					$('a[id="configure"]').parent().addClass('active');
					$('a[href*="reportsConfig.do"]').parent().addClass('active');
				});

				$(function() {
					//needed if tooltips shown on input-group element or button
					$("[data-toggle='tooltip']").tooltip({container: 'body'});
				});

				//Enable Bootstrap-Select
				$('.selectpicker').selectpicker({
					liveSearch: true,
					iconBase: 'fa',
					tickIcon: 'fa-check-square',
					noneSelectedText: '${nothingSelectedText}',
					noneResultsText: '${noResultsMatchText}',
					countSelectedText: '${selectedCountText}'
				});

				//activate dropdown-hover. to make bootstrap-select open on hover
				//must come after bootstrap-select initialization
				$('button.dropdown-toggle').dropdownHover({
					delay: 100
				});

				$('#name').focus();

			});
		</script>
	</jsp:attribute>

	<jsp:attribute name="aboveMainPanel">
		<div class="text-right">
			<a href="${pageContext.request.contextPath}/docs/manual.htm#user-groups">
				<spring:message code="page.link.help"/>
			</a>
		</div>
	</jsp:attribute>

	<jsp:body>
		<form:form class="form-horizontal" method="POST" action="${formUrl}" modelAttribute="report">
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
						<p>${encode:forHtmlContent(error)}</p>
					</div>
				</c:if>

				<div class="form-group">
					<label class="control-label col-md-4">
						<spring:message code="page.label.id"/>
					</label>
					<div class="col-md-8">
						<c:if test="${action == 'edit'}">
							<form:input path="reportId" readonly="true" class="form-control"/>
						</c:if>
						<c:if test="${action == 'copy'}">
							<form:hidden path="reportId"/>
						</c:if>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="name">
						<spring:message code="page.text.name"/>
					</label>
					<div class="col-md-8">
						<form:input path="name" maxlength="30" class="form-control"/>
						<form:errors path="name" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="reportGroup">
						<spring:message code="reports.label.reportGroup"/>
					</label>
					<div class="col-md-8">
						<form:select path="reportGroup" class="form-control selectpicker">
							<form:option value="0"><spring:message code="select.text.none"/></form:option>
								<option data-divider="true"></option>
							<form:options items="${reportGroups}" itemLabel="name" itemValue="reportGroupId"/>
						</form:select>
						<form:errors path="reportGroup" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4">
						<spring:message code="reports.text.status"/>
					</label>
					<div class="col-md-8">
						<c:forEach var="reportStatus" items="${reportStatuses}">
							<label class="radio-inline">
								<form:radiobutton path="reportStatus"
												  value="${reportStatus}"/>
								<spring:message code="${reportStatus.localisedDescription}"/>
							</label>
						</c:forEach>
						<form:errors path="reportStatus" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="shortDescription">
						<spring:message code="reports.label.shortDescription"/>
					</label>
					<div class="col-md-8">
						<div class="input-group">
							<form:input path="shortDescription" maxlength="250" class="form-control"/>
							<spring:message code="reports.help.shortDescription" var="help"/>
							<span class="input-group-btn" >
								<button class="btn btn-default" type="button"
										data-toggle="tooltip" title="${help}">
									<i class="fa fa-info"></i>
								</button>
							</span>
						</div>
						<form:errors path="shortDescription" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="description">
						<spring:message code="page.text.description"/>
					</label>
					<div class="col-md-8">
						<form:textarea path="description" rows="2" cols="40" class="form-control"/>
						<form:errors path="description" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="contactPerson">
						<spring:message code="reports.label.contactPerson"/>
					</label>
					<div class="col-md-8">
						<form:input path="contactPerson" maxlength="20" class="form-control"/>
						<form:errors path="contactPerson" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="reportType">
						<spring:message code="reports.label.reportType"/>
					</label>
					<div class="col-md-8">
						<form:select path="reportType" items="${reportType}"
									 itemLabel="description" itemValue="value" 
									 class="form-control"
									 />
						<form:errors path="reportType" cssClass="error"/>
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
