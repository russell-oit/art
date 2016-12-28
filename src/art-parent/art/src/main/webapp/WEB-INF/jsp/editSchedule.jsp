<%-- 
    Document   : editSchedule
    Created on : 01-Apr-2014, 11:23:24
    Author     : Timothy Anyona

Edit schedule page
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
		<spring:message code="page.title.addSchedule" var="pageTitle"/>
		<c:set var="panelTitle" value="${pageTitle}"/>
	</c:when>
	<c:when test="${action == 'edit'}">
		<spring:message code="page.title.editSchedule" var="panelTitle"/>
		<c:set var="pageTitle">
			${panelTitle} - ${schedule.name}
		</c:set>
	</c:when>
</c:choose>

<t:mainPageWithPanel title="${pageTitle}" mainPanelTitle="${panelTitle}"
					 mainColumnClass="col-md-6 col-md-offset-3">

	<jsp:attribute name="javascript">
		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="schedules.do"]').parent().addClass('active');

				//{container: 'body'} needed if tooltips shown on input-group element or button
				$("[data-toggle='tooltip']").tooltip({container: 'body'});

				$('#name').focus();

			});
		</script>
	</jsp:attribute>

	<jsp:attribute name="aboveMainPanel">
		<div class="text-right">
			<a href="${pageContext.request.contextPath}/docs/Manual.html#saved-schedules">
				<spring:message code="page.link.help"/>
			</a>
		</div>
	</jsp:attribute>

	<jsp:attribute name="belowMainPanel">
		<div class="col-md-6 col-md-offset-3">
			<div class="alert alert-info">
				<jsp:include page="/WEB-INF/html/scheduleNotes.html" />
			</div>
		</div>
	</jsp:attribute>

	<jsp:body>
		<spring:url var="formUrl" value="/app/saveSchedule.do"/>
		<form:form class="form-horizontal" method="POST" action="${formUrl}" modelAttribute="schedule">
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
				<div class="form-group">
					<label class="control-label col-md-4">
						<spring:message code="page.label.id"/>
					</label>
					<div class="col-md-8">
						<c:if test="${action == 'edit'}">
							<form:input path="scheduleId" readonly="true" class="form-control"/>
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
					<label class="col-md-4 control-label " for="description">
						<spring:message code="page.text.description"/>
					</label>
					<div class="col-md-8">
						<form:input path="description" maxlength="200" class="form-control"/>
						<form:errors path="description" cssClass="error"/>
					</div>
				</div>

				<hr>
				<div class="form-group">
					<label class="col-md-4 control-label " for="minute">
						<spring:message code="schedules.label.minute"/>
					</label>
					<div class="col-md-8">
						<form:input path="minute" maxlength="100" class="form-control"/>
						<form:errors path="minute" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="hour">
						<spring:message code="schedules.label.hour"/>
					</label>
					<div class="col-md-8">
						<form:input path="hour" maxlength="100" class="form-control"/>
						<form:errors path="hour" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="day">
						<spring:message code="schedules.label.day"/>
					</label>
					<div class="col-md-8">
						<form:input path="day" maxlength="100" class="form-control"/>
						<form:errors path="day" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="month">
						<spring:message code="schedules.label.month"/>
					</label>
					<div class="col-md-8">
						<form:input path="month" maxlength="100" class="form-control"/>
						<form:errors path="month" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="weekday">
						<spring:message code="schedules.label.weekday"/>
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
