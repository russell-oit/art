<%-- 
    Document   : roles
    Created on : 25-Jun-2018, 19:12:22
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.roles" var="pageTitle"/>

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
<spring:message code="page.message.linkedRecordsExist" var="linkedRecordsExistText" javaScriptEscape="true"/>

<t:mainPageWithPanel title="${pageTitle}" configPage="true">

	<jsp:attribute name="javascript">
		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="roles"]').parent().addClass('active');

				var tbl = $('#roles');

				var pageLength = undefined; //pass undefined to use the default
				var showAllRowsText = "${showAllRowsText}";
				var contextPath = "${pageContext.request.contextPath}";
				var localeCode = "${pageContext.response.locale}";
				var addColumnFilters = undefined; //pass undefined to use the default
				var deleteRecordText = "${deleteRecordText}";
				var okText = "${okText}";
				var cancelText = "${cancelText}";
				var deleteRecordUrl = "${pageContext.request.contextPath}/deleteRole";
				var deleteRecordsUrl = "${pageContext.request.contextPath}/deleteRoles";
				var recordDeletedText = "${recordDeletedText}";
				var recordsDeletedText = "${recordsDeletedText}";
				var errorOccurredText = "${errorOccurredText}";
				var showErrors = ${showErrors};
				var cannotDeleteRecordText = "${cannotDeleteRecordText}";
				var linkedRecordsExistText = "${linkedRecordsExistText}";
				var selectRecordsText = "${selectRecordsText}";
				var someRecordsNotDeletedText = "${someRecordsNotDeletedText}";
				var exportRecordsUrl = "${pageContext.request.contextPath}/exportRecords?type=Roles";
				var columnDefs = undefined;

				//initialize datatable
				var oTable = initConfigTable(tbl, pageLength,
						showAllRowsText, contextPath, localeCode,
						addColumnFilters, columnDefs);

				var table = oTable.api();

				addDeleteRecordHandler(tbl, table, deleteRecordText, okText,
						cancelText, deleteRecordUrl, recordDeletedText,
						errorOccurredText, showErrors, cannotDeleteRecordText,
						linkedRecordsExistText);

				addDeleteRecordsHandler(table, deleteRecordText, okText,
						cancelText, deleteRecordsUrl, recordsDeletedText,
						errorOccurredText, showErrors, selectRecordsText,
						someRecordsNotDeletedText);
						
				addExportRecordsHandler(table, exportRecordsUrl, selectRecordsText);

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
					<p><encode:forHtmlContent value="${error}"/></p>
				</c:if>
			</div>
		</c:if>
		<c:if test="${not empty recordSavedMessage}">
			<div class="alert alert-success alert-dismissable">
				<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
				<spring:message code="${recordSavedMessage}"/>: <encode:forHtmlContent value="${recordName}"/>
			</div>
		</c:if>

		<div id="ajaxResponseContainer">
			<div id="ajaxResponse">
			</div>
		</div>

		<div style="margin-bottom: 10px;">
			<div class="btn-group">
				<a class="btn btn-default" href="${pageContext.request.contextPath}/addRole">
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
					<a class="btn btn-default" href="${pageContext.request.contextPath}/importRecords?type=Roles">
						<spring:message code="page.text.import"/>
					</a>
					<button type="button" id="exportRecords" class="btn btn-default">
						<spring:message code="page.text.export"/>
					</button>
				</div>
			</c:if>
		</div>

		<table id="roles" class="table table-bordered table-striped table-condensed">
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
				<c:forEach var="role" items="${roles}">
					<tr id="row_${role.roleId}"
						data-id="${role.roleId}"
						data-name="${encode:forHtmlAttribute(role.name)}">

						<td></td>
						<td>${role.roleId}</td>
						<td>${encode:forHtmlContent(role.name)} &nbsp;
							<t:displayNewLabel creationDate="${role.creationDate}"
											   updateDate="${role.updateDate}"/>
						</td>
						<td>${encode:forHtmlContent(role.description)}</td>
						<td>
							<div class="btn-group">
								<a class="btn btn-default" 
								   href="${pageContext.request.contextPath}/editRole?id=${role.roleId}">
									<i class="fa fa-pencil-square-o"></i>
									<spring:message code="page.action.edit"/>
								</a>
								<button type="button" class="btn btn-default deleteRecord">
									<i class="fa fa-trash-o"></i>
									<spring:message code="page.action.delete"/>
								</button>
								<a class="btn btn-default" 
								   href="${pageContext.request.contextPath}/copyRole?id=${role.roleId}">
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
									<li>
										<a 
											href="${pageContext.request.contextPath}/roleUsage?roleId=${role.roleId}">
											<spring:message code="page.text.usage"/>
										</a>
									</li>
								</ul>
							</div>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</jsp:body>
</t:mainPageWithPanel>
