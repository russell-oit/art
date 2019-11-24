<%-- 
    Document   : userGroups
    Created on : 12-Feb-2014, 09:24:43
    Author     : Timothy Anyona

Display user groups
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<spring:message code="page.title.userGroups" var="pageTitle"/>

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText" javaScriptEscape="true"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText" javaScriptEscape="true"/>
<spring:message code="dialog.button.cancel" var="cancelText" javaScriptEscape="true"/>
<spring:message code="dialog.button.ok" var="okText" javaScriptEscape="true"/>
<spring:message code="dialog.message.deleteRecord" var="deleteRecordText" javaScriptEscape="true"/>
<spring:message code="page.message.recordDeleted" var="recordDeletedText" javaScriptEscape="true"/>
<spring:message code="page.message.recordsDeleted" var="recordsDeletedText" javaScriptEscape="true"/>
<spring:message code="dialog.message.selectRecords" var="selectRecordsText" javaScriptEscape="true"/>
<spring:message code="page.message.someRecordsNotDeleted" var="someRecordsNotDeletedText" javaScriptEscape="true"/>
<spring:message code="page.message.cannotDeleteRecord" var="cannotDeleteRecordText" javaScriptEscape="true"/>
<spring:message code="userGroups.message.linkedUsersExist" var="linkedUsersExistText" javaScriptEscape="true"/>

<t:mainPageWithPanel title="${pageTitle}" configPage="true">

	<jsp:attribute name="javascript">
		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="userGroups"]').parent().addClass('active');

				var tbl = $('#userGroups');
				
				var pageLength = undefined; //pass undefined to use the default
				var showAllRowsText = "${showAllRowsText}";
				var contextPath = "${pageContext.request.contextPath}";
				var localeCode = "${pageContext.response.locale}";
				var addColumnFilters = undefined; //pass undefined to use the default
				var deleteButtonSelector = ".deleteRecord";
				var deleteRecordText = "${deleteRecordText}";
				var okText = "${okText}";
				var cancelText = "${cancelText}";
				var deleteUrl = "deleteUserGroup";
				var recordDeletedText = "${recordDeletedText}";
				var errorOccurredText = "${errorOccurredText}";
				var cannotDeleteRecordText = "${cannotDeleteRecordText}";
				var linkedRecordsExistText = "${linkedUsersExistText}";
				var columnDefs = undefined;

				//initialize datatable and process delete action
				var oTable = initConfigPage(tbl,
						pageLength,
						showAllRowsText,
						contextPath,
						localeCode,
						addColumnFilters,
						deleteButtonSelector,
						deleteRecordText,
						okText,
						cancelText,
						deleteUrl,
						recordDeletedText,
						errorOccurredText,
						cannotDeleteRecordText,
						linkedRecordsExistText,
						columnDefs
						);

				var table = oTable.api();

				$('#deleteRecords').on("click", function () {
					var selectedRows = table.rows({selected: true});
					var data = selectedRows.data();
					if (data.length > 0) {
						var ids = $.map(data, function (item) {
							return item[1];
						});

						bootbox.confirm({
							message: "${deleteRecordText}: <b>" + ids + "</b>",
							buttons: {
								cancel: {
									label: "${cancelText}"
								},
								confirm: {
									label: "${okText}"
								}
							},
							callback: function (result) {
								if (result) {
									//user confirmed delete. make delete request
									$.ajax({
										type: "POST",
										dataType: "json",
										url: "${pageContext.request.contextPath}/deleteUserGroups",
										data: {ids: ids},
										success: function (response) {
											var nonDeletedRecords = response.data;
											if (response.success) {
												selectedRows.remove().draw(false);
												notifyActionSuccessReusable("${recordsDeletedText}", ids);
											} else if (nonDeletedRecords !== null && nonDeletedRecords.length > 0) {
												notifySomeRecordsNotDeletedReusable(nonDeletedRecords, "${someRecordsNotDeletedText}");
											} else {
												notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
											}
										},
										error: ajaxErrorHandler
									});
								} //end if result
							} //end callback
						}); //end bootbox confirm
					} else {
						bootbox.alert("${selectRecordsText}");
					}
				});

				$('#exportRecords').on("click", function () {
					var selectedRows = table.rows({selected: true});
					var data = selectedRows.data();
					if (data.length > 0) {
						var ids = $.map(data, function (item) {
							return item[1];
						});
						window.location.href = '${pageContext.request.contextPath}/exportRecords?type=UserGroups&ids=' + ids;
					} else {
						bootbox.alert("${selectRecordsText}");
					}
				});

				$('#ajaxResponseContainer').on("click", ".alert .close", function () {
					$(this).parent().hide();
				});

			}); //end document ready
		</script>
	</jsp:attribute>

	<jsp:body>
		<c:if test="${error != null}">
			<div class="alert alert-danger alert-dismissable">
				<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
				<p><spring:message code="page.message.errorOccurred"/></p>
				<c:if test="${showErrors}">
					<p>${encode:forHtmlContent(error)}</p>
				</c:if>
			</div>
		</c:if>
		<c:if test="${not empty recordSavedMessage}">
			<div class="alert alert-success alert-dismissable">
				<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
				<spring:message code="${recordSavedMessage}"/>: ${encode:forHtmlContent(recordName)}
			</div>
		</c:if>

		<div id="ajaxResponseContainer">
			<div id="ajaxResponse">
			</div>
		</div>

		<div class="dtHeader">
			<div class="btn-group">
				<a class="btn btn-default" href="${pageContext.request.contextPath}/addUserGroup">
					<i class="fa fa-plus"></i>
					<spring:message code="page.action.add"/>
				</a>
				<button type="button" id="deleteRecords" class="btn btn-default">
					<i class="fa fa-trash-o"></i>
					<spring:message code="page.action.delete"/>
				</button>
			</div>
			<c:if test="${sessionUser.hasPermission('migrate_records')}">
				<div class="btn-group">
					<a class="btn btn-default" href="${pageContext.request.contextPath}/importRecords?type=UserGroups">
						<spring:message code="page.text.import"/>
					</a>
					<button type="button" id="exportRecords" class="btn btn-default">
						<spring:message code="page.text.export"/>
					</button>
				</div>
			</c:if>
		</div>

		<table id="userGroups" class="table table-bordered table-striped table-condensed">
			<thead>
				<tr>
					<th class="noFilter selectCol"></th>
					<th><spring:message code="page.text.id"/></th>
					<th><spring:message code="page.text.name"/></th>
					<th><spring:message code="page.text.description"/></th>
					<th class="noFilter actionCol"><spring:message code="page.text.action"/></th>
				</tr>
			</thead>

			<tbody>
				<c:forEach var="group" items="${groups}">
					<tr data-id="${group.userGroupId}" 
						data-name="${encode:forHtmlAttribute(group.name)}">

						<td></td>
						<td>${group.userGroupId}</td>
						<td>${encode:forHtmlContent(group.name)} &nbsp;
							<t:displayNewLabel creationDate="${group.creationDate}"
											   updateDate="${group.updateDate}"/>
						</td>
						<td>${encode:forHtmlContent(group.description)}</td>
						<td>
							<div class="btn-group">
								<a class="btn btn-default" 
								   href="${pageContext.request.contextPath}/editUserGroup?id=${group.userGroupId}">
									<i class="fa fa-pencil-square-o"></i>
									<spring:message code="page.action.edit"/>
								</a>
								<button type="button" class="btn btn-default deleteRecord">
									<i class="fa fa-trash-o"></i>
									<spring:message code="page.action.delete"/>
								</button>
								<a class="btn btn-default" 
								   href="${pageContext.request.contextPath}/copyUserGroup?id=${group.userGroupId}">
									<i class="fa fa-copy"></i>
									<spring:message code="page.action.copy"/>
								</a>
							</div>
							<div class="btn-group">
								<button type="button" class="btn btn-default dropdown-toggle"
										data-toggle="dropdown" data-hover="dropdown"
										data-delay="100">
									<spring:message code="reports.action.more"/>
									<span class="caret"></span>
								</button>
								<ul class="dropdown-menu">
									<c:if test="${sessionUser.hasPermission('configure_user_group_membership')}">
										<li>
											<a 
												href="${pageContext.request.contextPath}/userGroupUserGroupMembership?userGroupId=${group.userGroupId}">
												<spring:message code="page.text.users"/>
											</a>
										</li>
									</c:if>
									<c:if test="${sessionUser.hasPermission('configure_access_rights')}">
										<li>
											<a 
												href="${pageContext.request.contextPath}/userGroupAccessRights?userGroupId=${group.userGroupId}">
												<spring:message code="page.action.accessRights"/>
											</a>
										</li>
									</c:if>
									<c:if test="${sessionUser.hasPermission('configure_permissions')}">
										<li>
											<a 
												href="${pageContext.request.contextPath}/userGroupPermissions?userGroupId=${group.userGroupId}">
												<spring:message code="page.text.permissions"/>
											</a>
										</li>
									</c:if>
								</ul>
							</div>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>

	</jsp:body>
</t:mainPageWithPanel>
