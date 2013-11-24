<%-- 
    Document   : settings
    Created on : 23-Nov-2013, 20:53:16
    Author     : Timothy Anyona

Settings configuration page
--%>

<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<spring:message code="page.title.configureSettings" var="pageTitle" scope="page"/>

<t:mainPage title="${pageTitle}">
	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/art.js"></script>
		<script type="text/javascript" charset="utf-8">
			$(document).ready(function() {
				$(function() {
					$('a[href*="configureSettings.do"]').parent().addClass('active');
				});

				$(function() {
					$("[data-toggle='tooltip']").tooltip({container: 'body'});
				});
			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<div style="text-align: center">
			${title}
		</div>

		<div class="row">
			<div class="col-md-6 col-md-offset-3">
				<form:form class="form-horizontal" method="POST" action="" modelAttribute="settings">
					<fieldset>
						<legend class="text-center">
							<spring:message code="settings.text.configureSettings"/>
						</legend>

						<c:if test="${not empty success}">
							<div class="alert alert-success alert-dismissable">
								<a class="close" data-dismiss="alert" href="#">x</a>
								<spring:message code="settings.message.settingsSaved"/>
							</div>
						</c:if>
						<c:if test="${not empty error}">
							<div class="alert alert-danger alert-dismissable">
								<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
								<p><spring:message code="page.message.errorOccurred"/></p>
								<p>${error}</p>
							</div>
						</c:if>

						<fieldset>
							<legend>SMTP</legend>
							<div class="form-group">
								<label class="control-label col-md-4" for="smtpServer">
									<spring:message code="settings.label.smtpServer"/>
								</label>
								<div class="col-md-8">
									<form:input path="smtpServer" name="smtpServer" id="smtpServer" class="form-control" />
								</div>
							</div>
							<div class="form-group">
								<label class="control-label col-md-4" for="smtpPort">
									<spring:message code="settings.label.smtpPort"/>
								</label>
								<div class="col-md-8">
									<form:input path="smtpPort" name="smtpPort" id="smtpPort" maxlength="6" class="form-control" />
									<form:errors path="smtpPort" cssClass="error" />
								</div>
							</div>
						</fieldset>
						<fieldset>
							<legend>PDF</legend>
							<div class="form-group">
								<label class="control-label col-md-4">
									<spring:message code="settings.label.pdfPageSize"/>
								</label>
								<div class="col-md-8">
									<c:forEach var="pageSize" items="${pdfPageSizes}">
										<label class="radio-inline">
											<form:radiobutton path="pdfPageSize" name="pdfPageSize" value="${pageSize}"/> ${pageSize.value}
										</label>
									</c:forEach>
								</div>
							</div>
						</fieldset>
						<div class="form-group">
							<div class="col-md-12">
								<button type="submit" class="btn btn-primary pull-right">
									<spring:message code="settings.button.save"/>
								</button>
							</div>
						</div>
					</fieldset>
				</form:form>
			</div>
		</div>
	</jsp:body>
</t:mainPage>
