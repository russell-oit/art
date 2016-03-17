<%-- 
    Document   : editJob
    Created on : 16-Mar-2016, 17:57:30
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
		<spring:message code="page.title.addJob" var="pageTitle"/>
	</c:when>
	<c:when test="${action == 'edit'}">
		<spring:message code="page.title.editJob" var="pageTitle"/>
	</c:when>
</c:choose>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-6 col-md-offset-3">

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/tinymce-4.0.19/tinymce.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-select-1.4.3/bootstrap-select-modified.min.js"></script>

		<script type="text/javascript">
			$(document).ready(function () {
				$(function () {
					$('a[id="configure"]').parent().addClass('active');
					$('a[href*="job.do"]').parent().addClass('active');
				});

				$(function () {
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
		<spring:url var="formUrl" value="/app/saveJob.do"/>
		<form:form class="form-horizontal" method="POST" action="${formUrl}" modelAttribute="job">
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

				<fieldset>
					<legend><spring:message code="jobs.text.job"/></legend>
					<div class="form-group">
						<label class="control-label col-md-4">
							<spring:message code="page.label.id"/>
						</label>
						<div class="col-md-8">
							<c:if test="${action == 'edit'}">
								<form:input path="jobId" readonly="true" class="form-control"/>
							</c:if>
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
						<label class="col-md-4 control-label " for="jobType">
							<spring:message code="jobs.text.jobType"/>
						</label>
						<div class="col-md-8">
							<form:select path="jobType" class="form-control selectpicker">
								<c:forEach var="jobType" items="${jobTypes}">
									<form:option value="${jobType}">
										<spring:message code="${jobType.localizedDescription}"/>
									</form:option>
								</c:forEach>
							</form:select>
							<form:errors path="jobType" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-md-4" for="active">
							<spring:message code="page.label.active"/>
						</label>
						<div class="col-md-8">
							<div class="checkbox">
								<form:checkbox path="active" id="active"/>
							</div>
						</div>
					</div>
					<div class="form-group">
						<label class="col-sm-4 control-label">
							<spring:message code="jobs.label.owner"/>
						</label>
						<div class="col-sm-8">
							<p class="form-control-static">${job.user.username}</p>
						</div>
					</div>
					<div class="form-group">
						<label class="col-sm-4 control-label">
							<spring:message code="page.text.report"/>
						</label>
						<div class="col-sm-8">
							<p class="form-control-static">${job.report.name}</p>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="outputFormat">
							<spring:message code="jobs.label.outputFormat"/>
						</label>
						<div class="col-md-8">
							<form:select path="outputFormat" class="form-control selectpicker">
								<c:forEach var="outputFormat" items="${outputFormats}">
									<form:option value="${outputFormat}">
										<spring:message code="reports.format.${outputFormat}"/>
									</form:option>
								</c:forEach>
							</form:select>
							<form:errors path="outputFormat" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-md-4" for="enableAuditing">
							<spring:message code="jobs.label.enableAuditing"/>
						</label>
						<div class="col-md-8">
							<div class="checkbox">
								<form:checkbox path="enableAuditing" id="enableAuditing"/>
							</div>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="runsToArchive">
							<spring:message code="jobs.label.runsToArchive"/>
						</label>
						<div class="col-md-8">
							<div class="input-group">
								<form:input path="runsToArchive" maxlength="2" class="form-control"/>
							</div>
							<form:errors path="runsToArchive" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="cachedTableName">
							<spring:message code="jobs.label.cachedTableName"/>
						</label>
						<div class="col-md-8">
							<form:input path="cachedTableName" maxlength="200" class="form-control"/>
							<form:errors path="cachedTableName" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-md-4" for="allowSharing">
							<spring:message code="jobs.label.allowSharing"/>
						</label>
						<div class="col-md-8">
							<div class="checkbox">
								<form:checkbox path="allowSharing" id="allowSharing"/>
							</div>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-md-4" for="allowSplitting">
							<spring:message code="jobs.label.allowSplitting"/>
						</label>
						<div class="col-md-8">
							<div class="checkbox">
								<form:checkbox path="allowSplitting" id="allowSplitting"/>
							</div>
						</div>
					</div>
				</fieldset>

				<div class="form-group">
					<label class="col-md-4 control-label " for="description">
						<spring:message code="page.text.description"/>
					</label>
					<div class="col-md-8">
						<form:input path="description" maxlength="200" class="form-control"/>
						<form:errors path="description" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="minute">
						<spring:message code="jobs.label.minute"/>
					</label>
					<div class="col-md-8">
						<form:input path="minute" maxlength="100" class="form-control"/>
						<form:errors path="minute" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="hour">
						<spring:message code="jobs.label.hour"/>
					</label>
					<div class="col-md-8">
						<form:input path="hour" maxlength="100" class="form-control"/>
						<form:errors path="hour" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="day">
						<spring:message code="jobs.label.day"/>
					</label>
					<div class="col-md-8">
						<form:input path="day" maxlength="100" class="form-control"/>
						<form:errors path="day" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="month">
						<spring:message code="jobs.label.month"/>
					</label>
					<div class="col-md-8">
						<form:input path="month" maxlength="100" class="form-control"/>
						<form:errors path="month" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="weekday">
						<spring:message code="jobs.label.weekday"/>
					</label>
					<div class="col-md-8">
						<form:input path="weekday" maxlength="100" class="form-control"/>
						<form:errors path="weekday" cssClass="error"/>
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

