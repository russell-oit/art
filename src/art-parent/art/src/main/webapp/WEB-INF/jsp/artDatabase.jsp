<%-- 
    Document   : artDatabase
    Created on : 08-Nov-2013, 09:28:05
    Author     : Timothy Anyona

Display art database configuration page
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.artDatabase" var="pageTitle"/>

<spring:message code="select.text.noResultsMatch" var="noResultsMatchText" javaScriptEscape="true"/>
<spring:message code="switch.text.yes" var="yesText" javaScriptEscape="true"/>
<spring:message code="switch.text.no" var="noText" javaScriptEscape="true"/>

<c:set var="mainColumnClass">
	${empty initialSetup ? "col-md-8 col-md-offset-2" : "col-md-8"}
</c:set>

<c:set var="belowPanelClass">
	${empty initialSetup ? "col-md-8 col-md-offset-2" : "col-md-8 col-md-offset-4"}
</c:set>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="${mainColumnClass}">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-switch/css/bootstrap3/bootstrap-switch.min.css">
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-switch/js/bootstrap-switch.min.js"></script>

		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="artDatabase"]').parent().addClass('active');

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

				$('#databaseType').on("change", function () {
					setDatasourceFields(this.value, 'driver', 'url', 'testSql', 'databaseProtocol');
				});

			});
		</script>
	</jsp:attribute>

	<jsp:attribute name="abovePanel">
		<div class="text-right">
			<a href="${pageContext.request.contextPath}/docs/Manual.html#art-database">
				<spring:message code="page.link.help"/>
			</a>
		</div>
	</jsp:attribute>

	<jsp:attribute name="leftPanel">
		<c:if test="${not empty initialSetup}">
			<div class="col-md-4">
				<div class="alert alert-info">
					<jsp:include page="/WEB-INF/jsp/welcomeNotes.jsp"/>
				</div>
			</div>
		</c:if>
	</jsp:attribute>

	<jsp:attribute name="belowPanel">
		<div class="${belowPanelClass}">
			<div class="alert alert-info">
				<jsp:include page="/WEB-INF/jsp/datasourceNotes.jsp"/>
			</div>
		</div>
	</jsp:attribute>

	<jsp:body>
		<form:form class="form-horizontal" method="POST" action="" modelAttribute="artDatabase">
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

				<div class="form-group">
					<label class="control-label col-md-4" for="databaseType">
						<spring:message code="page.label.databaseType"/>
					</label>
					<div class="col-md-8">
						<form:select path="databaseType" class="form-control selectpicker">
							<option value="">--</option>
							<option data-divider="true"></option>
							<c:forEach var="databaseType" items="${databaseTypes}">
								<form:option value="${databaseType}">
									${encode:forHtmlContent(databaseType.description)} 
								</form:option>
							</c:forEach>
						</form:select>
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
					<label class="control-label col-md-4" for="databaseProtocol">
						<spring:message code="page.label.databaseProtocol"/>
					</label>
					<div class="col-md-8">
						<form:select path="databaseProtocol" class="form-control">
							<option value="">--</option>
							<c:forEach var="databaseProtocol" items="${databaseProtocols}">
								<form:option value="${databaseProtocol}">
									${encode:forHtmlContent(databaseProtocol.description)} 
								</form:option>
							</c:forEach>
						</form:select>
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
						<spring:message code="datasources.label.url"/>
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
							<form:password path="password" autocomplete="off" maxlength="100" class="form-control"/>
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
								<form:checkbox path="useBlankPassword"/>
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
				<hr>
				<div class="form-group">
					<label class="control-label col-md-4" for="connectionPoolTimeoutMins">
						<spring:message code="page.label.connectionPoolTimeoutMins"/>
					</label>
					<div class="col-md-8">
						<div class="input-group">
							<form:input type="number" path="connectionPoolTimeoutMins" maxlength="5" class="form-control"/>
							<spring:message code="page.help.connectionPoolTimeout"
											var="help" />
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
					<label class="control-label col-md-4" for="maxPoolConnections">
						<spring:message code="artDatabase.label.maxPoolConnections"/>
					</label>
					<div class="col-md-8">
						<div class="input-group">
							<form:input type="number" path="maxPoolConnections" maxlength="3" class="form-control"/>
							<spring:message code="artDatabase.help.maxPoolConnections" var="help"/>
							<span class="input-group-btn" >
								<button class="btn btn-default" type="button"
										data-toggle="tooltip" title="${help}">
									<i class="fa fa-info"></i>
								</button>
							</span>
						</div>
						<form:errors path="maxPoolConnections" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4">
						<spring:message code="artDatabase.label.connectionPoolLibrary"/>
					</label>
					<div class="col-md-8">
						<c:forEach var="connectionPoolLibrary" items="${connectionPoolLibraries}">
							<label class="radio-inline">
								<form:radiobutton path="connectionPoolLibrary"
												  value="${connectionPoolLibrary}"/> ${connectionPoolLibrary.description}
							</label>
						</c:forEach>
						<form:errors path="connectionPoolLibrary" cssClass="error"/>
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