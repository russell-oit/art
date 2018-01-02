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

<spring:message code="switch.text.yes" var="yesText"/>
<spring:message code="switch.text.no" var="noText"/>
<spring:message code="reports.text.selectFile" var="selectFileText"/>
<spring:message code="reports.text.change" var="changeText"/>
<spring:message code="select.text.noResultsMatch" var="noResultsMatchText"/>

<t:mainPageWithPanel title="${pageTitle}" mainPanelTitle="${panelTitle}"
					 mainColumnClass="col-md-6 col-md-offset-3">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/css/bootstrap-select.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-switch/css/bootstrap3/bootstrap-switch.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jasny-bootstrap-3.1.3/css/jasny-bootstrap.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jquery-file-upload-9.14.2/css/jquery.fileupload.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jquery-file-upload-9.14.2/css/jquery.fileupload-ui.css">
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/js/bootstrap-select.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-switch/js/bootstrap-switch.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jasny-bootstrap-3.1.3/js/jasny-bootstrap.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-file-upload-9.14.2/js/vendor/jquery.ui.widget.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-file-upload-9.14.2/js/jquery.iframe-transport.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-file-upload-9.14.2/js/jquery.fileupload.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-file-upload-9.14.2/js/jquery.fileupload-process.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-file-upload-9.14.2/js/jquery.fileupload-validate.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-file-upload-9.14.2/js/jquery.fileupload-ui.js"></script>

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
				$('button.dropdown-toggle').dropdownHover({
					delay: 100
				});

				//enable bootstrap-switch
				$('.switch-yes-no').bootstrapSwitch({
					onText: '${yesText}',
					offText: '${noText}'
				});

				$("#encryptorType").change(function () {
					toggleVisibleFields();
				});

				toggleVisibleFields(); //show/hide on page load

				$('#name').focus();

			});
		</script>

		<script type="text/javascript">
			function toggleVisibleFields() {
				var encryptorType = $('#encryptorType option:selected').val();

				if (encryptorType === 'AESCrypt') {
					$("#aesCryptFields").show();
					$("#openPgpFields").hide();
				} else if (encryptorType === 'OpenPGP') {
					$("#aesCryptFields").hide();
					$("#openPgpFields").show();
				}
			}
		</script>
	</jsp:attribute>

	<jsp:attribute name="aboveMainPanel">
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
									<span class="fileinput-new">${selectFileText}</span>
									<span class="fileinput-exists">${changeText}</span>
									<input type="file" name="publicKeyFile">
								</span>
								<span class="fileinput-filename"></span>
								<a href="#" class="close fileinput-exists" data-dismiss="fileinput" style="float: none">&times;</a>
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
									<span class="fileinput-new">${selectFileText}</span>
									<span class="fileinput-exists">${changeText}</span>
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
