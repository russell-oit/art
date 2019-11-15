<%-- 
    Document   : userPermissions
    Created on : 28-Jun-2018, 16:15:53
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.userPermissions" var="pageTitle"/>

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText" javaScriptEscape="true"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText" javaScriptEscape="true"/>
<spring:message code="page.message.permissionRemoved" var="permissionRemovedText" javaScriptEscape="true"/>
<spring:message code="page.action.remove" var="removeText" javaScriptEscape="true"/>
<spring:message code="dialog.button.cancel" var="cancelText" javaScriptEscape="true"/>
<spring:message code="dialog.button.ok" var="okText" javaScriptEscape="true"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-6 col-md-offset-3"
					 hasTable="true" hasNotify="true">

	<jsp:attribute name="javascript">
		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="users"]').parent().addClass('active');

				var tbl = $('#permissions');

				var columnFilterRow = createColumnFilters(tbl);

				//initialize datatable and process delete action
				var oTable = tbl.dataTable({
					orderClasses: false,
					pagingType: "full_numbers",
					lengthMenu: [[10, 20, 50, -1], [10, 20, 50, "${showAllRowsText}"]],
					pageLength: 10,
					language: {
						url: "${pageContext.request.contextPath}/js/dataTables/i18n/dataTables_${pageContext.response.locale}.json"
					},
					initComplete: datatablesInitComplete
				});

				//move column filter row after heading row
				columnFilterRow.insertAfter(columnFilterRow.next());

				//get datatables api object
				var table = oTable.api();

				// Apply the column filter
				applyColumnFilters(tbl, table);

				tbl.find('tbody').on('click', '.deleteRecord', function () {
					var row = $(this).closest("tr"); //jquery object
					var recordName = escapeHtmlContent(row.attr("data-name"));
					var recordId = row.data("id");
					bootbox.confirm({
						message: "${removeText}: <b>" + recordName + "</b>",
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
									url: "${pageContext.request.contextPath}/deleteUserPermission",
									data: {id: recordId},
									success: function (response) {
										if (response.success) {
											table.row(row).remove().draw(false); //draw(false) to prevent datatables from going back to page 1
											notifyActionSuccessReusable("${permissionRemovedText}", recordName);
										} else {
											notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
										}
									},
									error: ajaxErrorHandler
								});
							} //end if result
						} //end callback
					}); //end bootbox confirm
				});

				$('#ajaxResponseContainer').on("click", ".alert .close", function () {
					$(this).parent().hide();
				});

			});
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

		<div id="ajaxResponseContainer">
			<div id="ajaxResponse">
			</div>
		</div>

		<div class="text-center">
			<b><spring:message code="page.text.user"/>:</b> ${encode:forHtmlContent(user.username)}
		</div>

		<table id="permissions" class="table table-striped table-bordered">
			<thead>
				<tr>
					<th><spring:message code="page.text.permission"/></th>
					<th class="noFilter"><spring:message code="page.text.action"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="userPermission" items="${userPermissions}">
					<tr data-name="${encode:forHtmlAttribute(userPermission.user.username)} -
						${encode:forHtmlAttribute(userPermission.permission.name)}"
						data-id="${userPermission.user.userId}-${userPermission.permission.permissionId}">

						<td>${encode:forHtmlAttribute(userPermission.permission.name)}</td>
						<td>
							<button type="button" class="btn btn-default deleteRecord">
								<i class="fa fa-trash-o"></i>
								<spring:message code="page.action.remove"/>
							</button>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</jsp:body>
</t:mainPageWithPanel>
