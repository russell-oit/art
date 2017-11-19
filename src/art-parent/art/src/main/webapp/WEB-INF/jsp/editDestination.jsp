<%-- 
    Document   : editDestination
    Created on : 14-Nov-2017, 19:29:46
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
		<spring:message code="page.title.addDestination" var="pageTitle"/>
		<c:set var="panelTitle" value="${pageTitle}"/>
	</c:when>
	<c:when test="${action == 'copy'}">
		<spring:message code="page.title.copyDestination" var="pageTitle"/>
		<c:set var="panelTitle" value="${pageTitle}"/>
	</c:when>
	<c:when test="${action == 'edit'}">
		<spring:message code="page.title.editDestination" var="panelTitle"/>
		<c:set var="pageTitle">
			${panelTitle} - ${destination.name}
		</c:set>
	</c:when>
</c:choose>

<spring:message code="switch.text.yes" var="yesText"/>
<spring:message code="switch.text.no" var="noText"/>
<spring:message code="select.text.noResultsMatch" var="noResultsMatchText"/>

<t:mainPageWithPanel title="${pageTitle}" mainPanelTitle="${panelTitle}"
					 mainColumnClass="col-md-6 col-md-offset-3">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/css/bootstrap-select.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-switch/css/bootstrap3/bootstrap-switch.min.css">
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/js/bootstrap-select.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-switch/js/bootstrap-switch.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/ace-min-noconflict-1.2.6/ace.js" charset="utf-8"></script>

		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="destinations"]').parent().addClass('active');

				//{container: 'body'} needed if tooltips shown on input-group element or button
				$("[data-toggle='tooltip']").tooltip({container: 'body'});

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

				$("#destinationType").change(function () {
					toggleVisibleFields();
				});

				toggleVisibleFields(); //show/hide on page load

				var jsonEditor = ace.edit("jsonEditor");
				jsonEditor.$blockScrolling = Infinity;
				jsonEditor.getSession().setMode("ace/mode/json");
				jsonEditor.setHighlightActiveLine(false);
				jsonEditor.setShowPrintMargin(false);
				jsonEditor.setOption("showLineNumbers", false);
				document.getElementById('jsonEditor').style.fontSize = '14px';

				var options = $('#options');
				jsonEditor.getSession().setValue(options.val());
				jsonEditor.getSession().on('change', function () {
					options.val(jsonEditor.getSession().getValue());
				});

			});
		</script>

		<script type="text/javascript">
			function toggleVisibleFields() {
				var destinationType = $('#destinationType option:selected').val();

				//show/hide server fields
				switch (destinationType) {
					case 'FTP':
					case 'SFTP':
					case 'NetworkShare':
						$("#serverFields").show();
						break;
					default:
						$("#serverFields").hide();
				}

				//show/hide options field
				switch (destinationType) {
					case 'FTP':
					case 'SFTP':
					case 'NetworkShare':
						$("#optionsDiv").show();
						break;
					default:
						$("#optionsDiv").hide();
				}

				//show/hide domain field
				switch (destinationType) {
					case 'NetworkShare':
						$("#domainDiv").show();
						break;
					default:
						$("#domainDiv").hide();
				}

				//show/hide sub-directory field
				switch (destinationType) {
					case 'NetworkShare':
					case 'S3':
					case 'Azure':
						$("#subDirectoryDiv").show();
						break;
					default:
						$("#subDirectoryDiv").hide();
				}
			}
		</script>
	</jsp:attribute>

	<jsp:attribute name="aboveMainPanel">
		<div class="text-right">
			<a href="${pageContext.request.contextPath}/docs/Manual.html#destinations">
				<spring:message code="page.link.help"/>
			</a>
		</div>
	</jsp:attribute>

	<jsp:body>
		<spring:url var="formUrl" value="/saveDestination"/>
		<form:form class="form-horizontal" method="POST" action="${formUrl}" modelAttribute="destination">
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
								<form:input path="destinationId" readonly="true" class="form-control"/>
							</c:when>
							<c:when test="${action == 'copy'}">
								<form:hidden path="destinationId"/>
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
					<label class="control-label col-md-4">
						<spring:message code="destinations.label.destinationType"/>
					</label>
					<div class="col-md-8">
						<form:select path="destinationType" class="form-control selectpicker">
							<c:forEach var="destinationType" items="${destinationTypes}">
								<form:option value="${destinationType}">
									${destinationType.description} 
								</form:option>
							</c:forEach>
						</form:select>
						<form:errors path="destinationType" cssClass="error"/>
					</div>
				</div>

				<fieldset id="serverFields">
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
							<form:input path="port" maxlength="8" class="form-control"/>
							<form:errors path="port" cssClass="error"/>
						</div>
					</div>
				</fieldset>

				<div class="form-group">
					<label class="control-label col-md-4" for="user">
						<spring:message code="page.text.user"/>
					</label>
					<div class="col-md-8">
						<form:input path="user" maxlength="50" class="form-control"/>
						<form:errors path="user" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="password">
						<spring:message code="page.label.password"/>
					</label>
					<div class="col-md-8">
						<div class="input-group">
							<form:password path="password" autocomplete="off" maxlength="100" class="form-control" />
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
				<div id="domainDiv" class="form-group">
					<label class="col-md-4 control-label " for="domain">
						<spring:message code="login.label.domain"/>
					</label>
					<div class="col-md-8">
						<form:input path="domain" maxlength="100" class="form-control"/>
						<form:errors path="domain" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="path">
						<spring:message code="destinations.label.path"/>
					</label>
					<div class="col-md-8">
						<form:input path="path" maxlength="1000" class="form-control"/>
						<form:errors path="path" cssClass="error"/>
					</div>
				</div>
				<div id="subDirectoryDiv" class="form-group">
					<label class="col-md-4 control-label " for="subDirectory">
						<spring:message code="destinations.label.subDirectory"/>
					</label>
					<div class="col-md-8">
						<form:input path="subDirectory" maxlength="100" class="form-control"/>
						<form:errors path="subDirectory" cssClass="error"/>
					</div>
				</div>
				<div id="optionsDiv" class="form-group">
					<label class="control-label col-md-12" style="text-align: center" for="options">
						<spring:message code="page.label.options"/>
					</label>
					<div class="col-md-12">
						<form:hidden path="options"/>
						<div id="jsonEditor" style="height: 200px; width: 100%; border: 1px solid black"></div>
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
