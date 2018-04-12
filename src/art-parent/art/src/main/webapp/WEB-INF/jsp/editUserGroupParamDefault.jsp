<%-- 
    Document   : editUserGroupParamDefault
    Created on : 02-Apr-2018, 12:51:17
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.editParamDefault" var="pageTitle"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-6 col-md-offset-3">

	<jsp:attribute name="javascript">
		<script type="text/javascript">
			$(document).ready(function() {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="paramDefaultsConfig"]').parent().addClass('active');

				$('#value').focus();

			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<spring:url var="formUrl" value="/saveUserGroupParamDefault"/>
		<form:form class="form-horizontal" method="POST" action="${formUrl}" modelAttribute="paramDefault">
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
				
				<input type="hidden" name="returnParameterId" value="${returnParameterId}">

				<form:hidden path="paramDefaultKey"/>
				<div class="form-group">
					<label class="control-label col-md-4" for="userGroup.name">
						<spring:message code="page.text.userGroup"/>
					</label>
					<div class="col-md-8">
						<form:input path="userGroup.name" readonly="true" class="form-control"/>
						<form:errors path="userGroup.name" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="parameter.name">
						<spring:message code="page.text.parameter"/>
					</label>
					<div class="col-md-8">
						<form:input path="parameter.name" readonly="true" class="form-control"/>
						<form:errors path="parameter.name" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="value">
						<spring:message code="page.text.value"/>
					</label>
					<div class="col-md-8">
						<form:textarea path="value" rows="4" class="form-control"/>
						<form:errors path="value" cssClass="error"/>
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
