<%-- 
    Document   : accessRightsConfig
    Created on : 22-Apr-2014, 12:03:03
    Author     : Timothy Anyona

Access rights configuration
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.accessRightsConfiguration" var="pageTitle"/>

<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="page.message.rightsRevoked" var="rightsRevokedText"/>
<spring:message code="page.message.rightsGranted" var="rightsGrantedText"/>
<spring:message code="accessRights.message.selectUserOrUserGroup" var="selectUserOrUserGroupText"/>
<spring:message code="accessRights.message.selectReportOrReportGroup" var="selectReportOrReportGroupText"/>
<spring:message code="page.text.available" var="availableText"/>
<spring:message code="page.text.selected" var="selectedText"/>
<spring:message code="page.text.search" var="searchText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-6 col-md-offset-3">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/lou-multi-select-0.9.11/css/multi-select.css">
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/notify-combined-0.3.1.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/lou-multi-select-0.9.11/js/jquery.multi-select.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.quicksearch.js"></script>
		<script type="text/javascript">
			$(document).ready(function() {
				$(function() {
					$('a[id="configure"]').parent().addClass('active');
					$('a[href*="accessRightsConfig.do"]').parent().addClass('active');
				});

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

			});

			function updateRights(action) {
				var users = $('#users').val();
				var userGroups = $('#userGroups').val();
				var reports = $('#reports').val();
				var reportGroups = $('#reportGroups').val();

				if (users === null && userGroups === null) {
					bootbox.alert("${selectUserOrUserGroupText}");
					return;
				}
				if (reports === null && reportGroups === null) {
					bootbox.alert("${selectReportOrReportGroupText}");
					return;
				}

				var rightsUpdatedMessage;
				if (action === 'GRANT') {
					rightsUpdatedMessage = "${rightsGrantedText}";
				} else {
					rightsUpdatedMessage = "${rightsRevokedText}";
				}

				$.ajax({
					type: "POST",
					dataType: "json",
					url: "${pageContext.request.contextPath}/app/updateAccessRight.do",
					data: {action: action, users: users, userGroups: userGroups,
						reports: reports, reportGroups: reportGroups},
					success: function(response) {
						var msg;
						if (response.success) {
							msg = alertCloseButton + rightsUpdatedMessage;
							$("#ajaxResponse").attr("class", "alert alert-success alert-dismissable").html(msg);
							$.notify(rightsUpdatedMessage, "success");
						} else {
							msg = alertCloseButton + "<p>${errorOccurredText}</p><p>" + escapeHtmlContent(response.errorMessage) + "</p>";
							$("#ajaxResponse").attr("class", "alert alert-danger alert-dismissable").html(msg);
							$.notify("${errorOccurredText}", "error");
						}
					},
					error: function(xhr, status, error) {
						bootbox.alert(xhr.responseText);
					}
				}); //end ajax
			}

			$('#select-all-users').click(function() {
				$('#users').multiSelect('select_all');
				return false;
			});
			$('#deselect-all-users').click(function() {
				$('#users').multiSelect('deselect_all');
				return false;
			});

			$('#select-all-userGroups').click(function() {
				$('#userGroups').multiSelect('select_all');
				return false;
			});
			$('#deselect-all-userGroups').click(function() {
				$('#userGroups').multiSelect('deselect_all');
				return false;
			});

			$('#select-all-reports').click(function() {
				$('#reports').multiSelect('select_all');
				return false;
			});
			$('#deselect-all-reports').click(function() {
				$('#reports').multiSelect('deselect_all');
				return false;
			});

			$('#select-all-reportGroups').click(function() {
				$('#reportGroups').multiSelect('select_all');
				return false;
			});
			$('#deselect-all-reportGroups').click(function() {
				$('#reportGroups').multiSelect('deselect_all');
				return false;
			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<form class="form-horizontal" method="POST" action="">
			<fieldset>
				<c:if test="${error != null}">
					<div class="alert alert-danger alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
						<p><spring:message code="page.message.errorOccurred"/></p>
						<c:if test="${showErrors}">
							<p><encode:forHtmlContent value="${error}"/></p>
						</c:if>
					</div>
				</c:if>

				<div id="ajaxResponse">
				</div>

				<div class="form-group">
					<label class="control-label col-md-3" for="users">
						<spring:message code="page.text.users"/>
					</label>
					<div class="col-md-9">
						<select name="users" id="users" multiple="multiple" class="form-control multi-select">
							<c:forEach var="user" items="${users}">
								<option value="${user.userId}-${encode:forHtmlAttribute(user.username)}">
									<encode:forHtmlContent value="${user.username}"/>
								</option>
							</c:forEach>
						</select>
						<a href='#' id='select-all-users'><spring:message code="page.text.selectAll"/></a> / 
						<a href='#' id='deselect-all-users'><spring:message code="page.text.deselectAll"/></a>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-3" for="userGroups">
						<spring:message code="page.text.userGroups"/>
					</label>
					<div class="col-md-9">
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
					<label class="control-label col-md-3" for="reports">
						<spring:message code="page.text.reports"/>
					</label>
					<div class="col-md-9">
						<select name="reports" id="reports" multiple="multiple" class="form-control multi-select">
							<c:forEach var="report" items="${reports}">
								<option value="${report.reportId}">
									<encode:forHtmlContent value="${report.name}"/>
								</option>
							</c:forEach>
						</select>
						<a href='#' id='select-all-reports'><spring:message code="page.text.selectAll"/></a> / 
						<a href='#' id='deselect-all-reports'><spring:message code="page.text.deselectAll"/></a>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-3" for="reportGroups">
						<spring:message code="page.text.reportGroups"/>
					</label>
					<div class="col-md-9">
						<select name="reportGroups" id="reportGroups" multiple="multiple" class="form-control multi-select">
							<c:forEach var="reportGroup" items="${reportGroups}">
								<option value="${reportGroup.reportGroupId}">
									<encode:forHtmlContent value="${reportGroup.name}"/>
								</option>
							</c:forEach>
						</select>
						<a href='#' id='select-all-reportGroups'><spring:message code="page.text.selectAll"/></a> / 
						<a href='#' id='deselect-all-reportGroups'><spring:message code="page.text.deselectAll"/></a>
					</div>
				</div>
				<div class="form-group">
					<div class="col-md-12">
						<div class="pull-right">
							<a class="btn btn-default" 
							   href="${pageContext.request.contextPath}/app/accessRights.do">
								<spring:message code="page.action.show"/>
							</a>
							<button type="button" class="btn btn-default" onclick="updateRights('GRANT');">
								<spring:message code="page.action.grant"/>
							</button>
							<button type="button" class="btn btn-default" onclick="updateRights('REVOKE');">
								<spring:message code="page.action.revoke"/>
							</button>
						</div>
					</div>
				</div>
			</fieldset>
		</form>
	</jsp:body>
</t:mainPageWithPanel>
