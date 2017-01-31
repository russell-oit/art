<%-- 
    Document   : editUserRuleValue
    Created on : 20-May-2014, 10:27:03
    Author     : Timothy Anyona

Edit user rule value
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.editRuleValue" var="pageTitle"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-6 col-md-offset-3">

	<jsp:attribute name="javascript">
		<script type="text/javascript">
			$(document).ready(function() {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="ruleValuesConfig"]').parent().addClass('active');

				$('#ruleValue').focus();

			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<spring:url var="formUrl" value="/saveUserRuleValue"/>
		<form:form class="form-horizontal" method="POST" action="${formUrl}" modelAttribute="value">
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

				<form:hidden path="ruleValueKey"/>
				<div class="form-group">
					<label class="control-label col-md-4" for="user.username">
						<spring:message code="page.text.user"/>
					</label>
					<div class="col-md-8">
						<form:input path="user.username" readonly="true" class="form-control"/>
						<form:errors path="user.username" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="rule.name">
						<spring:message code="page.text.rule"/>
					</label>
					<div class="col-md-8">
						<form:input path="rule.name" readonly="true" class="form-control"/>
						<form:errors path="rule.name" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="ruleValue">
						<spring:message code="page.text.value"/>
					</label>
					<div class="col-md-8">
						<form:input path="ruleValue" maxlength="100" class="form-control"/>
						<form:errors path="ruleValue" cssClass="error"/>
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
