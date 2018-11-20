<%-- 
    Document   : password
    Created on : 17-Jan-2014, 15:17:26
    Author     : Timothy Anyona

Display change password page
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.password" var="pageTitle"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-6 col-md-offset-3">

	<jsp:attribute name="javascript">
		<script type="text/javascript">
			$(document).ready(function () {
				$('a[href*="password"]').parent().addClass('active');

				$('#newPassword1').trigger("focus");
			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<form class="form-horizontal" method="POST" action="">
			<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
			<fieldset>
				<c:if test="${not empty message}">
					<div class="alert alert-danger alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
						<spring:message code="${message}"/>
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

				<div class="form-group">
					<label class="col-md-4 control-label" for="newPassword1">
						<spring:message code="password.label.newPassword"/>
					</label>
					<div class="col-md-8">
						<input type="password" name="newPassword1" id="newPassword1"
							   maxlength="100" class="form-control">
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label" for="newPassword2">
						<spring:message code="password.label.retypeNewPassword"/>
					</label>
					<div class="col-md-8">
						<input type="password" name="newPassword2" id="newPassword2"
							   maxlength="100" class="form-control">
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
		</form>
	</jsp:body>
</t:mainPageWithPanel>