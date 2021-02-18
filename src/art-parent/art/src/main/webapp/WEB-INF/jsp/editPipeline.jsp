<%-- 
    Document   : editPipeline
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
		<spring:message code="page.title.addPipeline" var="pageTitle"/>
		<c:set var="panelTitle" value="${pageTitle}"/>
	</c:when>
	<c:when test="${action == 'copy'}">
		<spring:message code="page.title.copyPipeline" var="pageTitle"/>
		<c:set var="panelTitle" value="${pageTitle}"/>
	</c:when>
	<c:when test="${action == 'edit'}">
		<spring:message code="page.title.editPipeline" var="panelTitle"/>
		<c:set var="pageTitle">
			${panelTitle} - ${pipeline.name}
		</c:set>
	</c:when>
</c:choose>

<spring:message code="switch.text.yes" var="yesText" javaScriptEscape="true"/>
<spring:message code="switch.text.no" var="noText" javaScriptEscape="true"/>

<t:mainPageWithPanel title="${pageTitle}" panelTitle="${panelTitle}"
					 mainColumnClass="col-md-6 col-md-offset-3">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-switch/css/bootstrap3/bootstrap-switch.min.css">
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-switch/js/bootstrap-switch.min.js"></script>

		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="pipelines"]').parent().addClass('active');

				//{container: 'body'} needed if tooltips shown on input-group element or button
				$("[data-toggle='tooltip']").tooltip({container: 'body'});

				//Enable Bootstrap-Select
				$('.selectpicker').selectpicker({
					liveSearch: true,
					noneResultsText: '${noResultsMatchText}'
				});

				//activate dropdown-hover. to make bootstrap-select open on hover
				//must come after bootstrap-select initialization
				initializeSelectHover();

				//enable bootstrap-switch
				$('.switch-yes-no').bootstrapSwitch({
					onText: '${yesText}',
					offText: '${noText}'
				});

				$('#name').trigger("focus");

			});
		</script>
	</jsp:attribute>

	<jsp:attribute name="abovePanel">
		<div class="text-right">
			<a href="${pageContext.request.contextPath}/docs/Manual.html#pipelines">
				<spring:message code="page.link.help"/>
			</a>
		</div>
	</jsp:attribute>

	<jsp:body>
		<spring:url var="formUrl" value="/savePipeline"/>
		<form:form class="form-horizontal" method="POST" action="${formUrl}" modelAttribute="pipeline">
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

				<form:hidden path="quartzCalendarNames" />

				<div class="form-group">
					<label class="control-label col-md-4">
						<spring:message code="page.label.id"/>
					</label>
					<div class="col-md-8">
						<c:choose>
							<c:when test="${action == 'edit'}">
								<form:input path="pipelineId" readonly="true" class="form-control"/>
							</c:when>
							<c:when test="${action == 'copy'}">
								<form:hidden path="pipelineId"/>
							</c:when>
						</c:choose>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="name">
						<spring:message code="page.text.name"/>
					</label>
					<div class="col-md-8">
						<form:input path="name" maxlength="50" class="form-control"/>
						<form:errors path="name" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="description">
						<spring:message code="page.text.description"/>
					</label>
					<div class="col-md-8">
						<form:textarea path="description" rows="2" cols="40" class="form-control" maxlength="200"/>
						<form:errors path="description" cssClass="error"/>
					</div>
				</div>

				<fieldset>
					<legend><spring:message code="pipelines.label.serial"/></legend>
					<div class="form-group">
						<label class="control-label col-md-4" for="serial">
							<spring:message code="pipelines.label.serial"/>
						</label>
						<div class="col-md-8">
							<form:input path="serial" maxlength="100" class="form-control"/>
							<form:errors path="serial" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-md-4" for="continueOnError">
							<spring:message code="pipelines.label.continueOnError"/>
						</label>
						<div class="col-md-8">
							<div class="checkbox">
								<form:checkbox path="continueOnError" id="continueOnError" class="switch-yes-no"/>
							</div>
						</div>
					</div>
				</fieldset>

				<fieldset>
					<legend><spring:message code="pipelines.label.parallel"/></legend>
					<div class="form-group">
						<label class="control-label col-md-4" for="parallel">
							<spring:message code="pipelines.label.parallel"/>
						</label>
						<div class="col-md-8">
							<form:input path="parallel" maxlength="100" class="form-control"/>
							<form:errors path="parallel" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-md-4" for="parallelPerMinute">
							<spring:message code="pipelines.label.perMinute"/>
						</label>
						<div class="col-md-8">
							<form:input type="number" path="parallelPerMinute" maxlength="4" class="form-control"/>
							<form:errors path="parallelPerMinute" cssClass="error"/>
						</div>
					</div>
				</fieldset>

				<hr>

				<div class="form-group">
					<label class="col-md-4 control-label " for="schedule">
						<spring:message code="jobs.text.schedule"/>
					</label>
					<div class="col-md-8">
						<form:select path="schedule" class="form-control selectpicker">
							<form:option value="0">--</form:option>
								<option data-divider="true"></option>
							<form:options items="${schedules}" itemLabel="name" itemValue="scheduleId"/>
						</form:select>
						<form:errors path="schedule" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="startCondition">
						<spring:message code="page.text.startCondition"/>
					</label>
					<div class="col-md-8">
						<form:select path="startCondition" class="form-control selectpicker">
							<form:option value="0">--</form:option>
								<option data-divider="true"></option>
							<form:options items="${startConditions}" itemLabel="name" itemValue="startConditionId"/>
						</form:select>
						<form:errors path="startCondition" cssClass="error"/>
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
