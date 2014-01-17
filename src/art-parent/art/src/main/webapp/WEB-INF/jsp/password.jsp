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

<spring:message code="page.title.changePassword" var="pageTitle"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-6 col-md-offset-3">

	<jsp:attribute name="javascript">
		<script type="text/javascript">
			$(document).ready(function() {
				$(function() {
					$('a[href*="password.do"]').parent().addClass('active');
				});

				$('#newPassword1').focus();
			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<form class="form-horizontal" method="POST" action="">
			<fieldset>
				<c:if test="${not empty success}">
					<div class="alert alert-success">
						<spring:message code="password.message.passwordUpdated"/>
					</div>
				</c:if>
				<c:if test="${not empty errorMessage}">
					<div class="alert alert-danger">
						<spring:message code="${errorMessage}"/>
					</div>
				</c:if>
				<c:if test="${not empty error}">
					<div class="alert alert-danger">
						<p><spring:message code="page.message.errorOccurred"/></p>
						<p>${error}</p>
					</div>
				</c:if>
				
				<div class="form-group">
					<label class="control-label col-md-4" for="newPassword1">
						<spring:message code="password.label.newPassword"/>
					</label>
					<div class="col-md-8">
						<input type="password" name="newPassword1" id="newPassword1"
							   maxlength="40" class="form-control">
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="newPassword2">
						<spring:message code="password.label.retypeNewPassword"/>
					</label>
					<div class="col-md-8">
						<input type="password" name="newPassword2" id="newPassword2"
							   maxlength="40" class="form-control">
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