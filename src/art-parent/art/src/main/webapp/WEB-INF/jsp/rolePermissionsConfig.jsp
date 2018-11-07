<%-- 
    Document   : rolePermissionsConfig
    Created on : 27-Jun-2018, 12:48:54
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.rolePermissionsConfiguration" var="pageTitle"/>

<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="page.message.permissionsAdded" var="permissionsAddedText"/>
<spring:message code="page.message.permissionsRemoved" var="permissionsRemovedText"/>
<spring:message code="rolePermissions.message.selectRole" var="selectRoleText"/>
<spring:message code="rolePermissions.message.selectPermission" var="selectPermissionText"/>
<spring:message code="page.text.available" var="availableText"/>
<spring:message code="page.text.selected" var="selectedText"/>
<spring:message code="page.text.search" var="searchText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-8 col-md-offset-2"
					 hasNotify="true">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/lou-multi-select-0.9.11/css/multi-select.css">
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/lou-multi-select-0.9.11/js/jquery.multi-select.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.quicksearch.js"></script>
		
		<script type="text/javascript">
			$(document).ready(function() {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="rolePermissionsConfig"]').parent().addClass('active');

				$('.multi-select').multiSelect({
					selectableHeader: "<div>${availableText}</div>\n\
					<input type='text' class='form-control input-sm' autocomplete='off' placeholder='${searchText}'>",
					selectionHeader: "<div>${selectedText}</div>\n\
					<input type='text' class='form-control input-sm' autocomplete='off' placeholder='${searchText}'>",
					afterInit: function(ms) {
						var that = this,
								$selectableSearch = that.$selectableUl.prev(),
								$selectionSearch = that.$selectionUl.prev(),
								selectableSearchString = '#' + that.$container.attr('id') + ' .ms-elem-selectable:not(.ms-selected)',
								selectionSearchString = '#' + that.$container.attr('id') + ' .ms-elem-selection.ms-selected';

						that.qs1 = $selectableSearch.quicksearch(selectableSearchString)
								.on('keydown', function(e) {
									if (e.which === 40) {
										that.$selectableUl.focus();
										return false;
									}
								});

						that.qs2 = $selectionSearch.quicksearch(selectionSearchString)
								.on('keydown', function(e) {
									if (e.which === 40) {
										that.$selectionUl.focus();
										return false;
									}
								});
					},
					afterSelect: function() {
						this.qs1.cache();
						this.qs2.cache();
					},
					afterDeselect: function() {
						this.qs1.cache();
						this.qs2.cache();
					}
				});
				
				$('#ajaxResponseContainer').on("click", ".alert .close", function () {
					$(this).parent().hide();
				});

			});

			function updatePermissions(action) {
				var roles = $('#roles').val();
				var permissions = $('#permissions').val();

				if (roles === null) {
					bootbox.alert("${selectRoleText}");
					return;
				}
				if (permissions === null) {
					bootbox.alert("${selectPermissionText}");
					return;
				}

				var recordsUpdatedMessage;
				if (action === 'ADD') {
					recordsUpdatedMessage = "${permissionsAddedText}";
				} else {
					recordsUpdatedMessage = "${permissionsRemovedText}";
				}

				$.ajax({
					type: "POST",
					dataType: "json",
					url: "${pageContext.request.contextPath}/updateRolePermissions",
					data: {action: action, roles: roles, permissions: permissions},
					success: function(response) {
						if (response.success) {
							notifyActionSuccessReusable(recordsUpdatedMessage);
						} else {
							notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
						}
					},
					error: function(xhr) {
						ajaxErrorHandler(xhr);
					}
				}); //end ajax
			}

			$('#select-all-roles').click(function() {
				$('#roles').multiSelect('select_all');
				return false;
			});
			$('#deselect-all-roles').click(function() {
				$('#roles').multiSelect('deselect_all');
				return false;
			});

			$('#select-all-permissions').click(function() {
				$('#permissions').multiSelect('select_all');
				return false;
			});
			$('#deselect-all-permissions').click(function() {
				$('#permissions').multiSelect('deselect_all');
				return false;
			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<form class="form-horizontal" method="POST" action="">
			<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
			<fieldset>
				<c:if test="${error != null}">
					<div class="alert alert-danger alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
						<p><spring:message code="page.message.errorOccurred"/></p>
						<c:if test="${showErrors}">
							<p>${encode:forHtmlContent(error)}</p>
						</c:if>
					</div>
				</c:if>

				<div id="ajaxResponseContainer">
					<div id="ajaxResponse">
					</div>
				</div>

				<div class="form-group">
					<label class="control-label col-md-3" for="roles">
						<spring:message code="page.text.roles"/>
					</label>
					<div class="col-md-9">
						<select name="roles" id="roles" multiple="multiple" class="form-control multi-select">
							<c:forEach var="role" items="${roles}">
								<option value="${role.roleId}">
									<encode:forHtmlContent value="${role.name}"/>
								</option>
							</c:forEach>
						</select>
						<a href='#' id='select-all-roles'><spring:message code="page.text.selectAll"/></a> / 
						<a href='#' id='deselect-all-roles'><spring:message code="page.text.deselectAll"/></a>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-3" for="permissions">
						<spring:message code="page.text.permissions"/>
					</label>
					<div class="col-md-9">
						<select name="permissions" id="permissions" multiple="multiple" class="form-control multi-select">
							<c:forEach var="permission" items="${permissions}">
								<option value="${permission.permissionId}">
									<encode:forHtmlContent value="${permission.name}"/>
								</option>
							</c:forEach>
						</select>
						<a href='#' id='select-all-permissions'><spring:message code="page.text.selectAll"/></a> / 
						<a href='#' id='deselect-all-permissions'><spring:message code="page.text.deselectAll"/></a>
					</div>
				</div>
				<div class="form-group">
					<div class="col-md-12">
						<div class="pull-right">
							<button type="button" class="btn btn-default" onclick="updatePermissions('ADD');">
								<spring:message code="page.action.add"/>
							</button>
							<button type="button" class="btn btn-default" onclick="updatePermissions('REMOVE');">
								<spring:message code="page.action.remove"/>
							</button>
						</div>
					</div>
				</div>
			</fieldset>
		</form>
	</jsp:body>
</t:mainPageWithPanel>
