<%-- 
    Document   : editUser
    Created on : 19-Jan-2014, 11:14:57
    Author     : Timothy Anyona

Display edit user page
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
		<spring:message code="page.title.addUser" var="pageTitle"/>
		<c:set var="panelTitle" value="${pageTitle}"/>
	</c:when>
	<c:when test="${action == 'copy'}">
		<spring:message code="page.title.copyUser" var="pageTitle"/>
		<c:set var="panelTitle" value="${pageTitle}"/>
	</c:when>
	<c:when test="${action == 'edit'}">
		<spring:message code="page.title.editUser" var="panelTitle"/>
		<c:set var="pageTitle">
			${panelTitle} - ${user.username}
		</c:set>
	</c:when>
</c:choose>

<spring:message code="select.text.nothingSelected" var="nothingSelectedText"/>
<spring:message code="select.text.noResultsMatch" var="noResultsMatchText"/>
<spring:message code="select.text.selectedCount" var="selectedCountText"/>
<spring:message code="select.text.selectAll" var="selectAllText"/>
<spring:message code="select.text.deselectAll" var="deselectAllText"/>
<spring:message code="switch.text.yes" var="yesText"/>
<spring:message code="switch.text.no" var="noText"/>

<t:mainPageWithPanel title="${pageTitle}" mainPanelTitle="${panelTitle}"
					 mainColumnClass="col-md-6 col-md-offset-3">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-switch/css/bootstrap3/bootstrap-switch.min.css">
	</jsp:attribute>

	<jsp:attribute name="javascript">
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

				//activate dropdown-hover. to make bootstrap-select open on hover
				//must come after bootstrap-select initialization
				initializeSelectHover();

				//enable bootstrap-switch
				$('.switch-yes-no').bootstrapSwitch({
					onText: '${yesText}',
					offText: '${noText}'
				});

				$('#username').trigger("focus");

			});
		</script>
	</jsp:attribute>

	<jsp:attribute name="aboveMainPanel">
		<div class="text-right">
			<a href="${pageContext.request.contextPath}/docs/Manual.html#users">
				<spring:message code="page.link.help"/>
			</a>
		</div>
	</jsp:attribute>

	<jsp:body>
		<spring:url var="formUrl" value="/saveUser"/>
		<form:form class="form-horizontal" method="POST" action="${formUrl}" modelAttribute="user">
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
								<form:input path="userId" readonly="true" class="form-control"/>
							</c:when>
							<c:when test="${action == 'copy'}">
								<form:hidden path="userId"/>
							</c:when>
						</c:choose>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="username">
						<spring:message code="page.label.username"/>
					</label>
					<div class="col-md-8">
						<form:input path="username" maxlength="50" class="form-control"/>
						<form:errors path="username" cssClass="error"/>
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
						<div>
							<label class="checkbox-inline">
								<form:checkbox path="useBlankPassword"/>
								<spring:message code="page.checkbox.useBlankPassword"/>
							</label>
							<label class="checkbox-inline">
								<form:checkbox path="generateAndSend"/>
								<spring:message code="users.checkbox.generateAndSend"/>
							</label>
						</div>
						<form:errors path="password" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="fullName">
						<spring:message code="users.label.fullName"/>
					</label>
					<div class="col-md-8">
						<form:input path="fullName" maxlength="100" class="form-control"/>
						<form:errors path="fullName" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="email">
						<spring:message code="users.label.email"/>
					</label>
					<div class="col-md-8">
						<form:input path="email" maxlength="100" class="form-control"/>
						<form:errors path="email" cssClass="error"/>
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
					<label class="control-label col-md-4" for="canChangePassword">
						<spring:message code="users.label.canChangePassword"/>
					</label>
					<div class="col-md-8">
						<div class="checkbox">
							<form:checkbox path="canChangePassword" id="canChangePassword" class="switch-yes-no"/>
						</div>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="publicUser">
						<spring:message code="users.label.publicUser"/>
					</label>
					<div class="col-md-8">
						<div class="checkbox">
							<form:checkbox path="publicUser" id="publicUser" class="switch-yes-no"/>
						</div>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4">
						<spring:message code="users.label.accessLevel"/>
					</label>
					<div class="col-md-8">
						<form:select path="accessLevel" items="${accessLevels}"
									 itemLabel="description" 
									 class="form-control selectpicker"/>
						<form:errors path="accessLevel" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="defaultReportGroup">
						<spring:message code="page.label.defaultReportGroup"/>
					</label>
					<div class="col-md-8">
						<form:select path="defaultReportGroup" class="form-control selectpicker">
							<form:option value="0">--</form:option>
								<option data-divider="true"></option>
							<form:options items="${reportGroups}" itemLabel="name" itemValue="reportGroupId"/>
						</form:select>
						<form:errors path="defaultReportGroup" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="startReport">
						<spring:message code="page.label.startReport"/>
					</label>
					<div class="col-md-8">
						<div class="input-group">
							<form:input path="startReport" maxlength="500" class="form-control"/>
							<spring:message code="page.help.startReport" var="help"/>
							<span class="input-group-btn" >
								<button class="btn btn-default" type="button"
										data-toggle="tooltip" title="${help}">
									<i class="fa fa-info"></i>
								</button>
							</span>
						</div>
						<form:errors path="startReport" cssClass="error"/>
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
						<form:errors path="userGroups" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="roles">
						<spring:message code="page.text.roles"/>
					</label>
					<div class="col-md-8">
						<form:select path="roles" items="${roles}" multiple="true" 
									 itemLabel="name" itemValue="roleId" 
									 class="form-control selectpicker"
									 data-actions-box="true"
									 />
						<form:errors path="roles" cssClass="error"/>
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
