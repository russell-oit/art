<%-- 
    Document   : editSmtpServer
    Created on : 14-Dec-2017, 13:02:33
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
		<spring:message code="page.title.addSmtpServer" var="pageTitle"/>
		<c:set var="panelTitle" value="${pageTitle}"/>
	</c:when>
	<c:when test="${action == 'copy'}">
		<spring:message code="page.title.copySmtpServer" var="pageTitle"/>
		<c:set var="panelTitle" value="${pageTitle}"/>
	</c:when>
	<c:when test="${action == 'edit'}">
		<spring:message code="page.title.editSmtpServer" var="panelTitle"/>
		<c:set var="pageTitle">
			${panelTitle} - ${smtpServer.name}
		</c:set>
	</c:when>
</c:choose>

<spring:message code="switch.text.yes" var="yesText"/>
<spring:message code="switch.text.no" var="noText"/>

<t:mainPageWithPanel title="${pageTitle}" mainPanelTitle="${panelTitle}"
					 mainColumnClass="col-md-6 col-md-offset-3">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-switch/css/bootstrap3/bootstrap-switch.min.css">
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-switch/js/bootstrap-switch.min.js"></script>

		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="smtpServers"]').parent().addClass('active');

				//{container: 'body'} needed if tooltips shown on input-group element or button
				$("[data-toggle='tooltip']").tooltip({container: 'body'});
				
				$('#useSmtpAuthentication').on('switchChange.bootstrapSwitch', function (event, state) {
					toggleCredentialsFieldsEnabled();
				});
				
				// enable/disable on page load
				toggleCredentialsFieldsEnabled();

				//enable bootstrap-switch
				$('.switch-yes-no').bootstrapSwitch({
					onText: '${yesText}',
					offText: '${noText}'
				});

				$('#name').focus();
				
			});
			
			function toggleCredentialsFieldsEnabled() {
				if ($('#useSmtpAuthentication').is(':checked')) {
					$('#credentialsFields').prop('disabled', false);
				} else {
					$('#credentialsFields').prop('disabled', true);
				}
			}
		</script>
	</jsp:attribute>

	<jsp:attribute name="aboveMainPanel">
		<div class="text-right">
			<a href="${pageContext.request.contextPath}/docs/Manual.html#smtp-servers">
				<spring:message code="page.link.help"/>
			</a>
		</div>
	</jsp:attribute>

	<jsp:body>
		<spring:url var="formUrl" value="/saveSmtpServer"/>
		<form:form class="form-horizontal" method="POST" action="${formUrl}" modelAttribute="smtpServer">
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
				<c:if test="${not empty message}">
					<div class="alert alert-danger alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
						<spring:message code="${message}"/>
					</div>
				</c:if>

				<input type="hidden" name="action" value="${action}">
				<div class="form-group">
					<label class="control-label col-md-4">
						<spring:message code="page.label.id"/>
					</label>
					<div class="col-md-8">
						<c:choose>
							<c:when test="${action == 'edit'}">
								<form:input path="smtpServerId" readonly="true" class="form-control"/>
							</c:when>
							<c:when test="${action == 'copy'}">
								<form:hidden path="smtpServerId"/>
							</c:when>
						</c:choose>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="name">
						<spring:message code="page.label.name"/>
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
				<div class="form-group">
					<label class="control-label col-md-4" for="active">
						<spring:message code="page.label.active"/>
					</label>
					<div class="col-md-8">
						<div class="checkbox">
							<form:checkbox path="active" id="active" class="switch-yes-no"/>
						</div>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="server">
						<spring:message code="destinations.label.server"/>
					</label>
					<div class="col-md-8">
						<form:input path="server" maxlength="100" class="form-control"/>
						<form:errors path="server" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="port">
						<spring:message code="destinations.label.port"/>
					</label>
					<div class="col-md-8">
						<form:input path="port" maxlength="6" class="form-control"/>
						<form:errors path="port" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="useStartTls">
						<spring:message code="settings.label.smtpUseStartTls"/>
					</label>
					<div class="col-md-8">
						<div class="checkbox">
							<form:checkbox path="useStartTls" id="useStartTls" class="switch-yes-no"/>
						</div>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="useSmtpAuthentication">
						<spring:message code="settings.label.useSmtpAuthentication"/>
					</label>
					<div class="col-md-8">
						<div class="checkbox">
							<form:checkbox path="useSmtpAuthentication" id="useSmtpAuthentication" class="switch-yes-no"/>
						</div>
					</div>
				</div>
				
				<fieldset id="credentialsFields">
					<div class="form-group">
						<label class="control-label col-md-4" for="user">
							<spring:message code="page.text.user"/>
						</label>
						<div class="col-md-8">
							<form:input path="user" maxlength="100" class="form-control"/>
							<form:errors path="user" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-md-4" for="password">
							<spring:message code="page.label.password"/>
						</label>
						<div class="col-md-8">
							<div class="input-group">
								<form:password path="password" autocomplete="off" maxlength="50" class="form-control" />
								<spring:message code="page.help.password" var="help" />
								<span class="input-group-btn" >
									<button class="btn btn-default" type="button"
											data-toggle="tooltip" title="${help}">
										<i class="fa fa-info"></i>
									</button>
								</span>
							</div>
							<div class="checkbox">
								<label>
									<form:checkbox path="useBlankPassword"/>
									<spring:message code="page.checkbox.useBlankPassword"/>
								</label>
							</div>
							<form:errors path="password" cssClass="error"/>
						</div>
					</div>
				</fieldset>
				
				<div class="form-group">
					 <label class="control-label col-md-4" for="from">
						<spring:message code="smtpServers.label.from"/>
					</label>
					<div class="col-md-8">
						<form:input path="from" maxlength="100" class="form-control"/>
						<form:errors path="from" cssClass="error"/>
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
