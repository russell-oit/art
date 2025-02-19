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
	<c:when test="${action == 'copy'}">
		<spring:message code="page.title.copyDatasource" var="pageTitle"/>
		<c:set var="panelTitle" value="${pageTitle}"/>
	</c:when>
	<c:when test="${action == 'edit'}">
		<spring:message code="page.title.editDatasource" var="panelTitle"/>
		<c:set var="pageTitle">
			${panelTitle} - ${datasource.name}
		</c:set>
	</c:when>
</c:choose>

<spring:message code="select.text.noResultsMatch" var="noResultsMatchText" javaScriptEscape="true"/>
<spring:message code="datasources.message.connectionSuccessful" var="connectionSuccessfulText" javaScriptEscape="true"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText" javaScriptEscape="true"/>
<spring:message code="switch.text.yes" var="yesText" javaScriptEscape="true"/>
<spring:message code="switch.text.no" var="noText" javaScriptEscape="true"/>

<t:mainPageWithPanel title="${pageTitle}" panelTitle="${panelTitle}"
					 mainColumnClass="col-md-8 col-md-offset-2" hasNotify="true">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-switch/css/bootstrap3/bootstrap-switch.min.css">
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-switch/js/bootstrap-switch.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/ace-min-noconflict-1.4.2/ace.js" charset="utf-8"></script>

		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="datasources"]').parent().addClass('active');

				//{container: 'body'} needed if tooltips shown on input-group element or button
				$("[data-toggle='tooltip']").tooltip({container: 'body'});

				$('#testConnection').on('click', function () {
					var action = '${action}';
					var id = 0;
					if (action === 'edit' || action === 'copy') {
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
						url: "${pageContext.request.contextPath}/testDatasource",
						data: {id: id, jndi: jndi, driver: driver, url: url, username: username,
							password: password, useBlankPassword: useBlankPassword,
							action: action},
						success: function (response) {
							if (response.success) {
								notifyActionSuccessReusable("${connectionSuccessfulText}");
							} else {
								notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
							}
						},
						error: function (xhr) {
							ajaxErrorHandler(xhr);
						}
					});
				});

				$('#ajaxResponseContainer').on("click", ".alert .close", function () {
					$(this).parent().hide();
				});

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

				var jsonEditor = ace.edit("jsonEditor");
				jsonEditor.getSession().setMode("ace/mode/json");
				jsonEditor.setHighlightActiveLine(false);
				jsonEditor.setShowPrintMargin(false);
				jsonEditor.setOption("showLineNumbers", false);
				jsonEditor.setOption("maxLines", 20);
				jsonEditor.setOption("minLines", 7);
				document.getElementById('jsonEditor').style.fontSize = '14px';

				var options = $('#options');
				jsonEditor.getSession().setValue(options.val());
				jsonEditor.getSession().on('change', function () {
					options.val(jsonEditor.getSession().getValue());
				});

				$("#datasourceType").on("change", function () {
					toggleVisibleFields();
					setMongoDBHint();
				});

				toggleVisibleFields(); //show/hide on page load

				$('#name').trigger("focus");
			});
		</script>

		<script type="text/javascript">
			function toggleVisibleFields() {
				var datasourceType = $('#datasourceType option:selected').val();
				
				//show/hide database type
				switch (datasourceType) {
					case 'MongoDB':
						$("#databaseTypeDiv").hide();
						break;
					default:
						$("#databaseTypeDiv").show();
				}
				
				//show/hide jndi
				switch (datasourceType) {
					case 'JDBC':
						$("#jndiDiv").show();
						break;
					default:
						$("#jndiDiv").hide();
				}
				
				//show/hide database protocol
				switch (datasourceType) {
					case 'JDBC':
						$("#protocolDiv").show();
						break;
					default:
						$("#protocolDiv").hide();
				}
				
				//show/hide driver
				switch (datasourceType) {
					case 'MongoDB':
						$("#driverDiv").hide();
						break;
					default:
						$("#driverDiv").show();
				}
				
				//show/hide test sql
				switch (datasourceType) {
					case 'JDBC':
						$("#testSqlDiv").show();
						break;
					default:
						$("#testSqlDiv").hide();
				}
				
				//show/hide connection pool timeout
				switch (datasourceType) {
					case 'JDBC':
						$("#connectionPoolTimeoutDiv").show();
						break;
					default:
						$("#connectionPoolTimeoutDiv").hide();
				}
				
				//show/hide options
				switch (datasourceType) {
					case 'JDBC':
						$("#optionsDiv").show();
						break;
					default:
						$("#optionsDiv").hide();
				}
			}

			function setMongoDBHint() {
				var datasourceType = $('#datasourceType option:selected').val();

				switch (datasourceType) {
					case 'MongoDB':
						$("#driver").val('');
						$("#url").val('mongodb://<server>');
						$("#testSql").val('');
					default:
						break;
				}
			}
		</script>
	</jsp:attribute>

	<jsp:attribute name="abovePanel">
		<div class="text-right">
			<a href="${pageContext.request.contextPath}/docs/Manual.html#datasources">
				<spring:message code="page.link.help"/>
			</a>
		</div>
	</jsp:attribute>

	<jsp:attribute name="belowPanel">
		<div class="col-md-8 col-md-offset-2">
			<div class="alert alert-info">
				<jsp:include page="/WEB-INF/jsp/datasourceNotes.jsp"/>
			</div>
		</div>
	</jsp:attribute>

	<jsp:body>
		<spring:url var="formUrl" value="/saveDatasource"/>
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

				<div id="ajaxResponseContainer">
					<div id="ajaxResponse">
					</div>
				</div>

				<input type="hidden" name="action" value="${action}">
				<form:hidden path="passwordAlgorithm" />

				<div class="form-group">
					<label class="control-label col-md-4">
						<spring:message code="page.label.id"/>
					</label>
					<div class="col-md-8">
						<c:choose>
							<c:when test="${action == 'edit'}">
								<form:input path="datasourceId" readonly="true" class="form-control"/>
							</c:when>
							<c:when test="${action == 'copy'}">
								<form:hidden path="datasourceId"/>
							</c:when>
						</c:choose>
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
						<spring:message code="datasources.label.datasourceType"/>
					</label>
					<div class="col-md-8">
						<form:select path="datasourceType" class="form-control selectpicker">
							<c:forEach var="datasourceType" items="${datasourceTypes}">
								<form:option value="${datasourceType}">
									${datasourceType.description} 
								</form:option>
							</c:forEach>
						</form:select>
						<form:errors path="datasourceType" cssClass="error"/>
					</div>
				</div>
				<div id="databaseTypeDiv" class="form-group">
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
						<form:errors path="databaseType" cssClass="error"/>
					</div>
				</div>
				<div id="jndiDiv" class="form-group">
					<label class="control-label col-md-4" for="jndi">
						<spring:message code="page.label.jndi"/>
					</label>
					<div class="col-md-8">
						<div class="checkbox">
							<form:checkbox path="jndi" id="jndi" class="switch-yes-no"/>
						</div>
					</div>
				</div>
				<div id="protocolDiv" class="form-group">
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
						<form:errors path="databaseProtocol" cssClass="error"/>
					</div>
				</div>
				<div id="driverDiv" class="form-group">
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
						<form:input path="username" maxlength="100" class="form-control"/>
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
								<form:checkbox path="useBlankPassword" id="useBlankPassword"/>
								<spring:message code="page.checkbox.useBlankPassword"/>
							</label>
						</div>
						<form:errors path="password" cssClass="error"/>
					</div>
				</div>
				<div id="testSqlDiv" class="form-group">
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
				<div id="connectionPoolTimeoutDiv" class="form-group">
					<label class="control-label col-md-4" for="connectionPoolTimeoutMins">
						<spring:message code="page.label.connectionPoolTimeoutMins"/>
					</label>
					<div class="col-md-8">
						<div class="input-group">
							<form:input type="number" path="connectionPoolTimeoutMins" maxlength="5" class="form-control"/>
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
				<div id="optionsDiv" class="form-group">
					<label class="control-label col-md-8 col-md-offset-4" style="text-align: center" for="options">
						<spring:message code="page.label.options"/>
					</label>
					<div class="col-md-8 col-md-offset-4">
						<form:hidden path="options"/>
						<div id="jsonEditor" style="height: 200px; width: 100%; border: 1px solid black"></div>
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
