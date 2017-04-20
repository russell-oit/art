<%-- 
    Document   : editParameter
    Created on : 30-Apr-2014, 11:21:35
    Author     : Timothy Anyona

Edit parameter definition
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
		<spring:message code="page.title.addParameter" var="pageTitle"/>
		<c:set var="panelTitle" value="${pageTitle}"/>
	</c:when>
	<c:when test="${action == 'edit'}">
		<spring:message code="page.title.editParameter" var="panelTitle"/>
		<c:set var="pageTitle">
			${panelTitle} - ${parameter.name}
		</c:set>
	</c:when>
</c:choose>

<spring:message code="select.text.noResultsMatch" var="noResultsMatchText"/>
<spring:message code="switch.text.yes" var="yesText"/>
<spring:message code="switch.text.no" var="noText"/>

<t:mainPageWithPanel title="${pageTitle}" mainPanelTitle="${panelTitle}"
					 mainColumnClass="col-md-6 col-md-offset-3">
	
	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/css/bootstrap-select.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-switch/css/bootstrap3/bootstrap-switch.min.css">
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/js/bootstrap-select.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-switch/js/bootstrap-switch.min.js"></script>
		
		<script type="text/javascript">
			$(document).ready(function() {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="parameters"]').parent().addClass('active');

				//{container: 'body'} needed if tooltips shown on input-group element or button
				$("[data-toggle='tooltip']").tooltip({container: 'body'});

				//Enable Bootstrap-Select
				$('.selectpicker').selectpicker({
					liveSearch: true,
					noneResultsText: '${noResultsMatchText}'
				});

				//activate dropdown-hover. to make bootstrap-select open on hover
				//must come after bootstrap-select initialization
				$('button.dropdown-toggle').dropdownHover({
					delay: 100
				});
				
				//enable bootstrap-switch
				$('.switch-yes-no').bootstrapSwitch({
					onText: '${yesText}',
					offText: '${noText}'
				});

				$('#name').focus();

			});
		</script>
	</jsp:attribute>

	<jsp:attribute name="aboveMainPanel">
		<div class="text-right">
			<a href="${pageContext.request.contextPath}/docs/Manual.html#parameters">
				<spring:message code="page.link.help"/>
			</a>
		</div>
	</jsp:attribute>

	<jsp:body>
		<spring:url var="formUrl" value="/saveParameter"/>
		<form:form class="form-horizontal" method="POST" action="${formUrl}" modelAttribute="parameter">
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
							<p><encode:forHtmlContent value="${error}"/></p>
						</c:if>
					</div>
				</c:if>

				<input type="hidden" name="action" value="${action}">
				<div class="form-group">
					<label class="control-label col-md-4">
						<spring:message code="page.label.id"/>
					</label>
					<div class="col-md-8">
						<c:if test="${action == 'edit'}">
							<form:input path="parameterId" readonly="true" class="form-control"/>
						</c:if>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="name">
						<spring:message code="page.text.name"/>
					</label>
					<div class="col-md-8">
						<div class="input-group">
							<form:input path="name" maxlength="60" class="form-control"/>
							<spring:message code="parameters.help.name" var="help"/>
							<span class="input-group-btn" >
								<button class="btn btn-default" type="button"
										data-toggle="tooltip" title="${help}">
									<i class="fa fa-info"></i>
								</button>
							</span>
						</div>
						<form:errors path="name" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="description">
						<spring:message code="page.text.description"/>
					</label>
					<div class="col-md-8">
						<form:input path="description" maxlength="50" class="form-control"/>
						<form:errors path="description" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="parameterType">
						<spring:message code="parameters.label.parameterType"/>
					</label>
					<div class="col-md-8">
						<c:forEach var="parameterType" items="${parameterTypes}">
							<label class="radio-inline">
								<form:radiobutton path="parameterType"
												  value="${parameterType}"/> ${parameterType.description}
							</label>
						</c:forEach>
						<form:errors path="parameterType" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="label">
						<spring:message code="page.text.label"/>
					</label>
					<div class="col-md-8">
						<div class="input-group">
							<form:input path="label" maxlength="50" class="form-control"/>
							<spring:message code="parameters.help.label" var="help"/>
							<span class="input-group-btn" >
								<button class="btn btn-default" type="button"
										data-toggle="tooltip" title="${help}">
									<i class="fa fa-info"></i>
								</button>
							</span>
						</div>
						<form:errors path="label" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="helpText">
						<spring:message code="parameters.label.helpText"/>
					</label>
					<div class="col-md-8">
						<div class="input-group">
							<form:input path="helpText" maxlength="500" class="form-control"/>
							<spring:message code="parameters.help.helpText" var="help"/>
							<span class="input-group-btn" >
								<button class="btn btn-default" type="button"
										data-toggle="tooltip" title="${help}">
									<i class="fa fa-info"></i>
								</button>
							</span>
						</div>
						<form:errors path="helpText" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="dataType">
						<spring:message code="page.label.dataType"/>
					</label>
					<div class="col-md-8">
						<form:select path="dataType" class="form-control selectpicker">
							<form:options items="${dataTypes}" itemLabel="description" itemValue="value"/>
						</form:select>
						<form:errors path="dataType" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="defaultValue">
						<spring:message code="parameters.label.defaultValue"/>
					</label>
					<div class="col-md-8">
						<form:textarea path="defaultValue" rows="3" class="form-control"/>
						<form:errors path="defaultValue" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="hidden">
						<spring:message code="parameters.label.hidden"/>
					</label>
					<div class="col-md-8">
						<div class="checkbox">
							<form:checkbox path="hidden" id="hidden" class="switch-yes-no"/>
						</div>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="useLov">
						<spring:message code="parameters.label.useLov"/>
					</label>
					<div class="col-md-8">
						<div class="checkbox">
							<form:checkbox path="useLov" id="useLov" class="switch-yes-no"/>
						</div>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="lovReportId">
						<spring:message code="parameters.label.lovReport"/>
					</label>
					<div class="col-md-8">
						<form:select path="lovReportId" class="form-control selectpicker">
							<form:option value="0">--</form:option>
							<option data-divider="true"></option>
							<form:options items="${lovReports}" itemLabel="name" itemValue="reportId"/>
						</form:select>
						<form:errors path="lovReportId" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="drilldownColumnIndex">
						<spring:message code="parameters.label.drilldownColumnIndex"/>
					</label>
					<div class="col-md-8">
						<form:input path="drilldownColumnIndex" maxlength="2" class="form-control"/>
						<form:errors path="drilldownColumnIndex" cssClass="error"/>
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
