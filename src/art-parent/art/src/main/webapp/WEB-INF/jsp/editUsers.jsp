<%-- 
    Document   : editUsers
    Created on : 05-Apr-2016, 16:04:19
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.editUsers" var="pageTitle"/>

<spring:message code="switch.text.yes" var="yesText"/>
<spring:message code="switch.text.no" var="noText"/>
<spring:message code="select.text.nothingSelected" var="nothingSelectedText"/>
<spring:message code="select.text.noResultsMatch" var="noResultsMatchText"/>
<spring:message code="select.text.selectedCount" var="selectedCountText"/>
<spring:message code="select.text.selectAll" var="selectAllText"/>
<spring:message code="select.text.deselectAll" var="deselectAllText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-6 col-md-offset-3">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/css/bootstrap-select.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-switch/css/bootstrap3/bootstrap-switch.min.css">
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/js/bootstrap-select.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-switch/js/bootstrap-switch.min.js"></script>

		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="users"]').parent().addClass('active');

				//{container: 'body'} needed if tooltips shown on input-group element or button
				$("[data-toggle='tooltip']").tooltip({container: 'body'});

				//Enable Bootstrap-Select
				$('.selectpicker').selectpicker({
					liveSearch: true,
					noneSelectedText: '${nothingSelectedText}',
					noneResultsText: '${noResultsMatchText}',
					countSelectedText: '${selectedCountText}',
					selectAllText: '${selectAllText}',
					deselectAllText: '${deselectAllText}'
				});

//				//activate dropdown-hover. to make bootstrap-select open on hover
//				//must come after bootstrap-select initialization
//				$('button.dropdown-toggle').dropdownHover({
//					delay: 100
//				});

				//enable bootstrap-switch
				$('.switch-yes-no').bootstrapSwitch({
					onText: '${yesText}',
					offText: '${noText}'
				});

				$('#activeUnchanged').change(function () {
					toggleActiveEnabled();
				});

				toggleActiveEnabled();

				$('#canChangePasswordUnchanged').change(function () {
					toggleCanChangePasswordEnabled();
				});

				toggleCanChangePasswordEnabled();

				$('#accessLevelUnchanged').change(function () {
					toggleAccessLevelEnabled();
				});

				toggleAccessLevelEnabled();

				$('#userGroupsUnchanged').change(function () {
					toggleUserGroupsEnabled();
				});

				toggleUserGroupsEnabled();

			});
		</script>

		<script type="text/javascript">
			function toggleActiveEnabled() {
				if ($('#activeUnchanged').is(':checked')) {
//					$('#active').prop('disabled', true);
					$('#active').bootstrapSwitch('disabled', true);
				} else {
//					$('#active').prop('disabled', false);
					$('#active').bootstrapSwitch('disabled', false);
				}
			}

			function toggleCanChangePasswordEnabled() {
				if ($('#canChangePasswordUnchanged').is(':checked')) {
//					$('#active').prop('disabled', true);
					$('#canChangePassword').bootstrapSwitch('disabled', true);
				} else {
//					$('#active').prop('disabled', false);
					$('#canChangePassword').bootstrapSwitch('disabled', false);
				}
			}

			function toggleAccessLevelEnabled() {
				if ($('#accessLevelUnchanged').is(':checked')) {
					$('#accessLevel').prop('disabled', true);
				} else {
					$('#accessLevel').prop('disabled', false);
				}
			}

			function toggleUserGroupsEnabled() {
				if ($('#userGroupsUnchanged').is(':checked')) {
					$('#userGroups').prop('disabled', true);
				} else {
					$('#userGroups').prop('disabled', false);
				}
			}
		</script>
	</jsp:attribute>

	<jsp:body>
		<spring:url var="formUrl" value="/saveUsers"/>
		<form:form class="form-horizontal" method="POST" action="${formUrl}" modelAttribute="multipleUserEdit">
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

				<div class="form-group">
					<label class="control-label col-md-4">
						<spring:message code="page.label.ids"/>
					</label>
					<div class="col-md-8">
						<form:input path="ids" readonly="true" class="form-control"/>
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
						<div class="checkbox">
							<label>
								<form:checkbox path="activeUnchanged" id="activeUnchanged"/>
								<spring:message code="page.checkbox.unchanged"/>
							</label>
						</div>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="canChangePassword">
						<spring:message code="users.label.canChangePassword"/>
					</label>
					<div class="col-md-8">
						<div class="checkbox">
							<form:checkbox path="canChangePassword" id="canChangePassword" class="switch-yes-no"/>
						</div>
						<div class="checkbox">
							<label>
								<form:checkbox path="canChangePasswordUnchanged" id="canChangePasswordUnchanged"/>
								<spring:message code="page.checkbox.unchanged"/>
							</label>
						</div>
					</div>
				</div>
				<div class="formgroup">
					<label class="controllabel colmd4">
						<spring:message code="users.label.accessLevel"/>
					</label>
					<div class="colmd8">
						<form:select path="accessLevel" items="${accessLevels}"
									 itemLabel="description" 
									 class="formcontrol selectpicker"/>
						<div class="checkbox">
							<label>
								<form:checkbox path="accessLevelUnchanged" id="accessLevelUnchanged"/>
								<spring:message code="page.checkbox.unchanged"/>
							</label>
						</div>
						<form:errors path="accessLevel" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="userGroups">
						<spring:message code="page.text.userGroups"/>
					</label>
					<div class="col-md-8">
						<form:select path="userGroups" items="${userGroups}" multiple="true" 
									 itemLabel="name" itemValue="userGroupId" 
									 class="form-control selectpicker"
									 data-actions-box="true"
									 />
						<div class="checkbox">
							<label>
								<form:checkbox path="userGroupsUnchanged" id="userGroupsUnchanged"/>
								<spring:message code="page.checkbox.unchanged"/>
							</label>
						</div>
						<form:errors path="userGroups" cssClass="error"/>
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
