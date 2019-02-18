<%-- 
    Document   : userGroupMembershipConfig
    Created on : 23-Apr-2014, 12:04:56
    Author     : Timothy Anyona

User group membership configuration
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.userGroupMembershipConfiguration" var="pageTitle"/>

<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="page.message.membershipAdded" var="membershipAddedText"/>
<spring:message code="page.message.membershipRemoved" var="membershipRemovedText"/>
<spring:message code="userGroupMembership.message.selectUser" var="selectUserText"/>
<spring:message code="userGroupMembership.message.selectUserGroup" var="selectUserGroupText"/>
<spring:message code="page.text.available" var="availableText"/>
<spring:message code="page.text.selected" var="selectedText"/>
<spring:message code="page.text.search" var="searchText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-8 col-md-offset-2"
					 hasNotify="true">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/multi-select-0.9.12/css/multi-select.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/multiSelect.css">
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/multi-select-0.9.12/js/jquery.multi-select.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.quicksearch.js"></script>
		
		<script type="text/javascript">
			$(document).ready(function() {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="userGroupMembershipConfig"]').parent().addClass('active');

				$('.multi-select').multiSelect({
					cssClass: 'wide-multi-select',
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
										that.$selectableUl.trigger("focus");
										return false;
									}
								});

						that.qs2 = $selectionSearch.quicksearch(selectionSearchString)
								.on('keydown', function(e) {
									if (e.which === 40) {
										that.$selectionUl.trigger("focus");
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

			function updateMembership(action) {
				var users = $('#users').val();
				var userGroups = $('#userGroups').val();

				if (users === null) {
					bootbox.alert("${selectUserText}");
					return;
				}
				if (userGroups === null) {
					bootbox.alert("${selectUserGroupText}");
					return;
				}

				var recordsUpdatedMessage;
				if (action === 'ADD') {
					recordsUpdatedMessage = "${membershipAddedText}";
				} else {
					recordsUpdatedMessage = "${membershipRemovedText}";
				}

				$.ajax({
					type: "POST",
					dataType: "json",
					url: "${pageContext.request.contextPath}/updateUserGroupMembership",
					data: {action: action, users: users, userGroups: userGroups},
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

			$('#select-all-users').on("click", function() {
				$('#users').multiSelect('select_all');
				return false;
			});
			$('#deselect-all-users').on("click", function() {
				$('#users').multiSelect('deselect_all');
				return false;
			});

			$('#select-all-userGroups').on("click", function() {
				$('#userGroups').multiSelect('select_all');
				return false;
			});
			$('#deselect-all-userGroups').on("click", function() {
				$('#userGroups').multiSelect('deselect_all');
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
					<label class="control-label col-md-2" for="users">
						<spring:message code="page.text.users"/>
					</label>
					<div class="col-md-10">
						<select name="users" id="users" multiple="multiple" class="form-control multi-select">
							<c:forEach var="user" items="${users}">
								<option value="${user.userId}">
									${encode:forHtmlContent(user.username)} ${empty user.fullName? "": " (".concat(encode:forHtmlContent(user.fullName)).concat(")")}
								</option>
							</c:forEach>
						</select>
						<a href='#' id='select-all-users'><spring:message code="page.text.selectAll"/></a> / 
						<a href='#' id='deselect-all-users'><spring:message code="page.text.deselectAll"/></a>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-2" for="userGroups">
						<spring:message code="page.text.userGroups"/>
					</label>
					<div class="col-md-10">
						<select name="userGroups" id="userGroups" multiple="multiple" class="form-control multi-select">
							<c:forEach var="userGroup" items="${userGroups}">
								<option value="${userGroup.userGroupId}">
									<encode:forHtmlContent value="${userGroup.name}"/>
								</option>
							</c:forEach>
						</select>
						<a href='#' id='select-all-userGroups'><spring:message code="page.text.selectAll"/></a> / 
						<a href='#' id='deselect-all-userGroups'><spring:message code="page.text.deselectAll"/></a>
					</div>
				</div>
				<div class="form-group">
					<div class="col-md-12">
						<div class="pull-right">
							<a class="btn btn-default" 
							   href="${pageContext.request.contextPath}/userGroupMembership">
								<spring:message code="page.action.show"/>
							</a>
							<button type="button" class="btn btn-default" onclick="updateMembership('ADD');">
								<spring:message code="page.action.add"/>
							</button>
							<button type="button" class="btn btn-default" onclick="updateMembership('REMOVE');">
								<spring:message code="page.action.remove"/>
							</button>
						</div>
					</div>
				</div>
			</fieldset>
		</form>
	</jsp:body>
</t:mainPageWithPanel>
