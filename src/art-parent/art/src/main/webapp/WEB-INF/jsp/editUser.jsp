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

<c:choose>
	<c:when test="${action == 'add'}">
		<spring:message code="page.title.addUser" var="pageTitle"/>
	</c:when>
	<c:otherwise>
		<spring:message code="page.title.editUser" var="pageTitle"/>
	</c:otherwise>
</c:choose>

<spring:message code="select.text.nothingSelected" var="nothingSelectedText"/>
<spring:message code="select.text.noResultsMatch" var="noResultsMatchText"/>
<spring:message code="select.text.selectedCount" var="selectedCountText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-6 col-md-offset-3">

	<jsp:attribute name="javascript">
		<script type="text/javascript">
			$(document).ready(function() {
				$(function() {
					$('a[id="configure"]').parent().addClass('active');
					$('a[href*="users.do"]').parent().addClass('active');
				});

				$(function() {
					//needed if tooltips shown on input-group element or button
					$("[data-toggle='tooltip']").tooltip({container: 'body'});
				});
				
				//Enable Bootstrap-Select
				$('.selectpicker').selectpicker({
					liveSearch: true,
					iconBase: 'fa',
					tickIcon: 'fa-check-square',
					noneSelectedText: '${nothingSelectedText}',
					noneResultsText: '${noResultsMatchText}',
					countSelectedText: '${selectedCountText}'
				});

				//activate dropdown-hover. to make bootstrap-select open on hover
				//must come after bootstrap-select initialization
				$('button.dropdown-toggle').dropdownHover({
					delay: 100
				});

				$('#userGroupsAll').change(function() {
					if ($('#userGroupsAll').is(':checked')) {
						$('#userGroups').selectpicker('selectAll');
					} else {
						$('#userGroups').selectpicker('deselectAll');
					}
				});

				//must come after selectpicker initialization (which creates button with appropriate data-id
				$('#userGroupsDiv .dropdown-menu > li > a').on('click', function() {
					$('#userGroupsAll').prop('checked', false);
				});

				$('#username').focus();

			});
		</script>
	</jsp:attribute>

	<jsp:attribute name="aboveMainPanel">
		<div class="text-right">
			<a href="${pageContext.request.contextPath}/docs/manual.htm#users">
				<spring:message code="page.link.help"/>
			</a>
		</div>
	</jsp:attribute>

	<jsp:body>
		<form:form class="form-horizontal" method="POST" action="" modelAttribute="user">
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
						<p>${error}</p>
					</div>
				</c:if>

				<div class="form-group">
					<label class="control-label col-md-4">
						<spring:message code="page.label.id"/>
					</label>
					<div class="col-md-8">
						<c:if test="${action != 'add'}">
							<form:input path="userId" readonly="true" class="form-control"/>
						</c:if>
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
							<form:password path="password" autocomplete="off" maxlength="50" class="form-control" />
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
				<div class="form-group">
					<label class="col-md-4 control-label " for="fullName">
						<spring:message code="users.label.fullName"/>
					</label>
					<div class="col-md-8">
						<form:input path="fullName" maxlength="40" class="form-control"/>
						<form:errors path="fullName" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="email">
						<spring:message code="users.label.email"/>
					</label>
					<div class="col-md-8">
						<form:input path="email" maxlength="40" class="form-control"/>
						<form:errors path="email" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="active">
						<spring:message code="page.label.active"/>
					</label>
					<div class="col-md-8">
						<div class="checkbox">
							<form:checkbox path="active" id="active"/>
						</div>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="canChangePassword">
						<spring:message code="users.label.canChangePassword"/>
					</label>
					<div class="col-md-8">
						<div class="checkbox">
							<form:checkbox path="canChangePassword" id="canChangePassword"/>
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
						<div class="text-right">
							<!-- TODO use correct link -->
							<a href="${pageContext.request.contextPath}/docs/manual.htm">
								<spring:message code="page.link.help"/>
							</a>
						</div>
						<form:errors path="accessLevel" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="defaultReportGroup">
						<spring:message code="page.label.defaultReportGroup"/>
					</label>
					<div class="col-md-8">
						<form:select path="defaultReportGroup" class="form-control selectpicker">
							<form:option value="0"><spring:message code="select.text.none"/></form:option>
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
				<div class="form-group" id="userGroupsDiv">
					<label class="col-md-4 control-label " for="userGroups">
						<spring:message code="users.label.userGroups"/>
					</label>
					<div class="col-md-8">
						<form:select path="userGroups" items="${userGroups}" multiple="true" 
									 itemLabel="name" itemValue="userGroupId" 
									 class="form-control selectpicker"
									 />
						<div class="checkbox">
							<label>
								<input type="checkbox" name="userGroupsAll" id="userGroupsAll">
								<spring:message code="page.checkbox.all"/>
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
