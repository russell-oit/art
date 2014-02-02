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
					iconBase: "fa",
					tickIcon: 'fa-check-square'
				});
				
//				$('.dropdown-toggle').dropdownHover();
				
				

			});
		</script>
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
					<label class="col-md-4 control-label " for="userGroups">
						<spring:message code="users.label.userGroups"/>
					</label>
					<div class="col-md-8">
						<div class="btn-group bootstrap-select show-tick">
							<button class="btn dropdown-toggle selectpicker" data-toggle="dropdown">Button <span class="caret"></span></button>
							<div class="dropdown-menu open" style="max-height: 156.4px; overflow: hidden; min-height: 0px;">
								<ul class="dropdown-menu selectpicker inner"
									role="menu" style="max-height: 469px; overflow-x: hidden; overflow-y: hidden; min-height: 0px; ">
									<li class="selected" rel="0"><a tabindex="0" class="" style=""><i class="fa fa-check-square icon-ok check-mark"></i><span class="text">test</span></a></li><li class="selected" rel="1"><a tabindex="0" class="" style=""><i class="fa fa-check-square icon-ok check-mark"></i><span class="text">a group</span></a></li>
								</ul>
							</div>
						</div>  
					</div>  
				</div>  


				<div class="form-group">
					<label class="col-md-4 control-label " for="userGroups">
						<spring:message code="users.label.userGroups"/>
					</label>
					<div class="col-md-8">
						<spring:message code="select.option.none" var="noneText"/>
						<form:select path="userGroups" items="${userGroups}"
									 itemLabel="name" itemValue="userGroupId"
									 class="form-control selectpicker"
									 />
					</div>
				</div>

				<input type="hidden" name="action" value="${action}">
				<div class="form-group">
					<label class="col-md-4 control-label">
						<spring:message code="page.label.id"/>
					</label>
					<div class="col-md-8">
						<c:if test="${action != 'add'}">
							<form:input path="userId" readonly="true" class="form-control"/>
						</c:if>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="username">
						<spring:message code="page.label.username"/>
					</label>
					<div class="col-md-8">
						<form:input path="username" maxlength="50" class="form-control"/>
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
					<label class="col-md-4 control-label " for="fullName">
						<spring:message code="users.label.fullName"/>
					</label>
					<div class="col-md-8">
						<form:input path="fullName" maxlength="40" class="form-control"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="email">
						<spring:message code="users.label.email"/>
					</label>
					<div class="col-md-8">
						<form:input path="email" maxlength="40" class="form-control"/>
					</div>
				</div>

				<div class="form-group">
					<label class="col-md-4 control-label " for="startReport">
						<spring:message code="users.label.startReport"/>
					</label>
					<div class="col-md-8">
						<form:input path="startReport" maxlength="500" class="form-control"/>
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
