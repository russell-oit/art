<%-- 
    Document   : editDatasource
    Created on : 12-Feb-2014, 16:08:55
    Author     : Timothy Anyona

Edit datasource page
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
		<spring:message code="page.title.addDatasource" var="pageTitle"/>
		<c:set var="panelTitle" value="${pageTitle}"/>
	</c:when>
	<c:when test="${action == 'edit'}">
		<spring:message code="page.title.editDatasource" var="panelTitle"/>
		<c:set var="pageTitle">
			${panelTitle} - ${datasource.name}
		</c:set>
	</c:when>
</c:choose>

<spring:message code="select.text.noResultsMatch" var="noResultsMatchText"/>
<spring:message code="datasources.message.connectionSuccessful" var="connectionSuccessfulText"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="switch.text.yes" var="yesText"/>
<spring:message code="switch.text.no" var="noText"/>

<t:mainPageWithPanel title="${pageTitle}" mainPanelTitle="${panelTitle}"
					 mainColumnClass="col-md-6 col-md-offset-3">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/css/bootstrap-select.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-switch/css/bootstrap3/bootstrap-switch.min.css">
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/notify-combined-0.3.1.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/js/bootstrap-select.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-switch/js/bootstrap-switch.min.js"></script>
		<script type="text/javascript">
			$(document).ready(function () {
				$(function () {
					$('a[id="configure"]').parent().addClass('active');
					$('a[href*="datasources.do"]').parent().addClass('active');
				});

				$(function () {
					//needed if tooltips shown on input-group element or button
					$("[data-toggle='tooltip']").tooltip({container: 'body'});
				});

				$('#testConnection').on('click', function () {
					var action = '${action}';
					var id = 0;
					if (action === 'edit') {
						id = $("#datasourceId").val();
					}
					var jndi = $("#jndi").is(":checked");
					var driver = $("#driver").val();
					var url = $("#url").val();
					var username = $("#username").val();
					var password = $("#password").val();
					var useBlankPassword = $("#useBlankPassword").is(":checked");

					$.ajax({
						type: "POST",
						dataType: "json",
						url: "${pageContext.request.contextPath}/app/testDatasource.do",
						data: {id: id, jndi: jndi, driver: driver, url: url, username: username,
							password: password, useBlankPassword: useBlankPassword,
							action: action},
						success: function (response) {
							if (response.success) {
								msg = alertCloseButton + "${connectionSuccessfulText}";
								$("#ajaxResponse").attr("class", "alert alert-success alert-dismissable").html(msg);
								$.notify("${connectionSuccessfulText}", "success");
							} else {
								msg = alertCloseButton + "<p>${errorOccurredText}</p><p>" + escapeHtmlContent(response.errorMessage) + "</p>";
								$("#ajaxResponse").attr("class", "alert alert-danger alert-dismissable").html(msg);
								$.notify("${errorOccurredText}", "error");
							}
						},
						error: function (xhr, status, error) {
							bootbox.alert(xhr.responseText);
						}
					});
				});

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
			<a href="${pageContext.request.contextPath}/docs/Manual.html#datasources">
				<spring:message code="page.link.help"/>
			</a>
		</div>
	</jsp:attribute>

	<jsp:attribute name="belowMainPanel">
		<div class="col-md-6 col-md-offset-3">
			<div class="alert alert-info">
				<jsp:include page="/WEB-INF/html/datasourceNotes.html"/>
			</div>
		</div>
	</jsp:attribute>

	<jsp:body>
		<spring:url var="formUrl" value="/app/saveDatasource.do"/>
		<form:form class="form-horizontal" method="POST" action="${formUrl}" modelAttribute="datasource">
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

				<div id="ajaxResponse">
				</div>

				<input type="hidden" name="action" value="${action}">
				<form:hidden path="passwordAlgorithm" />

				<div class="form-group">
					<label class="control-label col-md-4">
						<spring:message code="page.label.id"/>
					</label>
					<div class="col-md-8">
						<c:if test="${action == 'edit'}">
							<form:input path="datasourceId" readonly="true" class="form-control"/>
						</c:if>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="name">
						<spring:message code="page.text.name"/>
					</label>
					<div class="col-md-8">
						<form:input path="name" maxlength="25" class="form-control"/>
						<form:errors path="name" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="description">
						<spring:message code="page.text.description"/>
					</label>
					<div class="col-md-8">
						<form:input path="description" maxlength="200" class="form-control"/>
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
					<label class="control-label col-md-4" for="databaseType">
						<spring:message code="page.label.databaseType"/>
					</label>
					<div class="col-md-8">
						<div class="input-group">
							<select name="databaseType" id="databaseType" class="form-control selectpicker"
									onchange="setDatasourceFields(this.value, 'driver', 'url', 'testSql');">
								<option value="">--</option>
								<option data-divider="true"></option>
								<c:forEach var="dbType" items="${databaseTypes}">
									<option value="${dbType.key}">${dbType.value}</option>
								</c:forEach>
							</select>
							<spring:message code="page.help.databaseType" var="help"/>
							<span class="input-group-btn" >
								<button class="btn btn-default" type="button"
										data-toggle="tooltip" title="${help}">
									<i class="fa fa-info"></i>
								</button>
							</span>
						</div>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="jndi">
						<spring:message code="page.label.jndi"/>
					</label>
					<div class="col-md-8">
						<div class="checkbox">
							<form:checkbox path="jndi" id="jndi" class="switch-yes-no"/>
						</div>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="driver">
						<spring:message code="page.label.jdbcDriver"/>
					</label>
					<div class="col-md-8">
						<form:input path="driver" maxlength="200" class="form-control"/>
						<form:errors path="driver" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="url">
						<spring:message code="page.label.jdbcUrl"/>
					</label>
					<div class="col-md-8">
						<div class="input-group">
							<form:input path="url" maxlength="2000" class="form-control"/>
							<spring:message code="page.help.jdbcUrl" var="help"/>
							<span class="input-group-btn" >
								<button class="btn btn-default" type="button"
										data-toggle="tooltip" title="${help}">
									<i class="fa fa-info"></i>
								</button>
							</span>
						</div>
						<form:errors path="url" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="username">
						<spring:message code="page.label.username"/>
					</label>
					<div class="col-md-8">
						<form:input path="username" maxlength="30" class="form-control"/>
						<form:errors path="username" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="password">
						<spring:message code="page.label.password"/>
					</label>
					<div class="col-md-8">
						<div class="input-group">
							<form:password path="password" autocomplete="off" maxlength="50" class="form-control"/>
							<spring:message code="page.help.password" var="help"/>
							<span class="input-group-btn" >
								<button class="btn btn-default" type="button"
										data-toggle="tooltip" title="${help}">
									<i class="fa fa-info"></i>
								</button>
							</span>
						</div>
						<div class="checkbox">
							<label>
								<form:checkbox path="useBlankPassword" id="useBlankPassword"/>
								<spring:message code="page.checkbox.useBlankPassword"/>
							</label>
						</div>
						<form:errors path="password" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="testSql">
						<spring:message code="page.label.testSql"/>
					</label>
					<div class="col-md-8">
						<div class="input-group">
							<form:input path="testSql" maxlength="60" class="form-control"/>
							<spring:message code="page.help.testSql" var="help"/>
							<span class="input-group-btn" >
								<button class="btn btn-default" type="button"
										data-toggle="tooltip" title="${help}">
									<i class="fa fa-info"></i>
								</button>
							</span>
						</div>
						<form:errors path="testSql" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="connectionPoolTimeoutMins">
						<spring:message code="page.label.connectionPoolTimeout"/>
					</label>
					<div class="col-md-8">
						<div class="input-group">
							<form:input path="connectionPoolTimeoutMins" maxlength="5" class="form-control"/>
							<spring:message code="page.help.connectionPoolTimeout" var="help" />
							<span class="input-group-btn" >
								<button class="btn btn-default" type="button"
										data-toggle="tooltip" data-html="true" title="${help}">
									<i class="fa fa-info"></i>
								</button>
							</span>
						</div>
						<form:errors path="connectionPoolTimeoutMins" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<div class="col-md-12">
						<span class="pull-right">
							<button type="button" id="testConnection" class="btn btn-default">
								<spring:message code="datasources.button.test"/>
							</button>
							<button type="submit" class="btn btn-primary">
								<spring:message code="page.button.save"/>
							</button>
						</span>
					</div>
				</div>
			</fieldset>
		</form:form>
	</jsp:body>
</t:mainPageWithPanel>
