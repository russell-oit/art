<%-- 
    Document   : editEncryptor
    Created on : 02-Nov-2017, 19:43:59
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
		<spring:message code="page.title.addEncryptor" var="pageTitle"/>
		<c:set var="panelTitle" value="${pageTitle}"/>
	</c:when>
	<c:when test="${action == 'edit'}">
		<spring:message code="page.title.editEncryptor" var="panelTitle"/>
		<c:set var="pageTitle">
			${panelTitle} - ${encryptor.name}
		</c:set>
	</c:when>
</c:choose>

<spring:message code="switch.text.yes" var="yesText" javaScriptEscape="true"/>
<spring:message code="switch.text.no" var="noText" javaScriptEscape="true"/>
<spring:message code="select.text.noResultsMatch" var="noResultsMatchText" javaScriptEscape="true"/>

<t:mainPageWithPanel title="${pageTitle}" panelTitle="${panelTitle}"
					 mainColumnClass="col-md-6 col-md-offset-3">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-switch/css/bootstrap3/bootstrap-switch.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jasny-bootstrap-4.0.0/css/jasny-bootstrap.min.css">
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-switch/js/bootstrap-switch.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jasny-bootstrap-4.0.0/js/jasny-bootstrap.min.js"></script>

		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="encryptors"]').parent().addClass('active');

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

				$("#encryptorType").on("change", function () {
					toggleVisibleFields();
				});

				toggleVisibleFields(); //show/hide on page load

				$('#name').trigger("focus");

			});
		</script>

		<script type="text/javascript">
			function toggleVisibleFields() {
				var encryptorType = $('#encryptorType option:selected').val();

				//https://stackoverflow.com/questions/14910760/switch-case-as-string
				switch (encryptorType) {
					case 'AESCrypt':
						$("#aesCryptFields").show();
						$("#openPgpFields").hide();
						$("#passwordEncryptorFields").hide();
						break;
					case 'OpenPGP':
						$("#aesCryptFields").hide();
						$("#openPgpFields").show();
						$("#passwordEncryptorFields").hide();
						break;
					case 'Password':
						$("#aesCryptFields").hide();
						$("#openPgpFields").hide();
						$("#passwordEncryptorFields").show();
						break;
					default:
						break;
				}
			}
		</script>
	</jsp:attribute>

	<jsp:attribute name="abovePanel">
		<div class="text-right">
			<a href="${pageContext.request.contextPath}/docs/Manual.html#encryptors">
				<spring:message code="page.link.help"/>
			</a>
		</div>
	</jsp:attribute>

	<jsp:body>
		<spring:url var="formUrl" value="/saveEncryptor"/>
		<form:form class="form-horizontal" method="POST" action="${formUrl}" modelAttribute="encryptor" enctype="multipart/form-data">
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
				<c:if test="${not empty plainMessage}">
					<div class="alert alert-danger alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
						${encode:forHtmlContent(plainMessage)}
					</div>
				</c:if>

				<input type="hidden" name="action" value="${action}">
				<div class="form-group">
					<label class="control-label col-md-4">
						<spring:message code="page.label.id"/>
					</label>
					<div class="col-md-8">
						<c:if test="${action == 'edit'}">
							<form:input path="encryptorId" readonly="true" class="form-control"/>
						</c:if>
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
						<spring:message code="encryptors.label.encryptorType"/>
					</label>
					<div class="col-md-8">
						<form:select path="encryptorType" class="form-control selectpicker">
							<c:forEach var="encryptorType" items="${encryptorTypes}">
								<form:option value="${encryptorType}">
									${encryptorType.description} 
								</form:option>
							</c:forEach>
						</form:select>
						<form:errors path="encryptorType" cssClass="error"/>
					</div>
				</div>

				<fieldset id="aesCryptFields">
					<div class="form-group">
						<label class="control-label col-md-4" for="aesCryptPassword">
							<spring:message code="encryptors.label.aesCryptPassword"/>
						</label>
						<div class="col-md-8">
							<form:password path="aesCryptPassword" autocomplete="off" maxlength="100" class="form-control" />
							<form:errors path="aesCryptPassword" cssClass="error"/>
						</div>
					</div>
				</fieldset>

				<fieldset id="openPgpFields">
					<div class="form-group">
						<label class="control-label col-md-4" for="openPgpPublicKeyFile">
							<spring:message code="encryptors.label.openPgpPublicKeyFile"/>
						</label>
						<div class="col-md-8">
							<div>
								<form:input path="openPgpPublicKeyFile" maxlength="100" class="form-control"/>
								<form:errors path="openPgpPublicKeyFile" cssClass="error"/>
							</div>
							<div class="fileinput fileinput-new" data-provides="fileinput">
								<span class="btn btn-default btn-file">
									<span class="fileinput-new">
										<spring:message code="reports.text.selectFile"/>
									</span>
									<span class="fileinput-exists">
										<spring:message code="reports.text.change"/>
									</span>
									<input type="file" name="publicKeyFile">
								</span>
								<span class="fileinput-filename"></span>
								<a href="#" class="close fileinput-exists" data-dismiss="fileinput" style="float: none">&times;</a>
							</div>
							<div class="checkbox">
								<label>
									<form:checkbox path="overwriteFiles"/>
									<spring:message code="page.checkbox.overwriteFiles"/>
								</label>
							</div>
						</div>
					</div>
					<div class="form-group">
						<label class="col-md-4 control-label " for="openPgpPublicKeyString">
							<spring:message code="encryptors.label.openPgpPublicKeyString"/>
						</label>
						<div class="col-md-8">
							<form:textarea path="openPgpPublicKeyString" rows="4" cols="40" class="form-control"/>
							<form:errors path="openPgpPublicKeyString" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-md-4" for="openPgpSigningKeyFile">
							<spring:message code="encryptors.label.openPgpSigningKeyFile"/>
						</label>
						<div class="col-md-8">
							<div>
								<form:input path="openPgpSigningKeyFile" maxlength="100" class="form-control"/>
								<form:errors path="openPgpSigningKeyFile" cssClass="error"/>
							</div>
							<div class="fileinput fileinput-new" data-provides="fileinput">
								<span class="btn btn-default btn-file">
									<span class="fileinput-new">
										<spring:message code="reports.text.selectFile"/>
									</span>
									<span class="fileinput-exists">
										<spring:message code="reports.text.change"/>
									</span>
									<input type="file" name="signingKeyFile">
								</span>
								<span class="fileinput-filename"></span>
								<a href="#" class="close fileinput-exists" data-dismiss="fileinput" style="float: none">&times;</a>
							</div>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-md-4" for="openPgpSigningKeyPassphrase">
							<spring:message code="encryptors.label.openPgpSigningKeyPassphrase"/>
						</label>
						<div class="col-md-8">
							<form:password path="openPgpSigningKeyPassphrase" autocomplete="off" maxlength="1000" class="form-control" />
							<form:errors path="openPgpSigningKeyPassphrase" cssClass="error"/>
						</div>
					</div>
				</fieldset>

				<fieldset id="passwordEncryptorFields">
					<div class="form-group">
						<label class="control-label col-md-4" for="openPassword">
							<spring:message code="reports.label.openPassword"/>
						</label>
						<div class="col-md-8">
							<div>
								<form:password path="openPassword" autocomplete="off" maxlength="100" class="form-control"/>
							</div>
							<div>
								<label class="checkbox-inline">
									<form:checkbox path="useNoneOpenPassword" id="useNoneOpenPassword"/>
									<spring:message code="reports.checkbox.none"/>
								</label>
							</div>
							<form:errors path="openPassword" cssClass="error"/>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-md-4" for="modifyPassword">
							<spring:message code="reports.label.modifyPassword"/>
						</label>
						<div class="col-md-8">
							<div>
								<form:password path="modifyPassword" autocomplete="off" maxlength="100" class="form-control"/>
							</div>
							<div>
								<label class="checkbox-inline">
									<form:checkbox path="useNoneModifyPassword" id="useNoneModifyPassword"/>
									<spring:message code="reports.checkbox.none"/>
								</label>
							</div>
							<form:errors path="modifyPassword" cssClass="error"/>
						</div>
					</div>
				</fieldset>

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
