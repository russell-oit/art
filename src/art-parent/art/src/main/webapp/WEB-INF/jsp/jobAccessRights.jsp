<%-- 
    Document   : jobAccessRights
    Created on : 04-Jul-2017, 19:17:31
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.jobAccessRights" var="pageTitle"/>

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText" javaScriptEscape="true"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText" javaScriptEscape="true"/>
<spring:message code="page.message.rightsRevoked" var="rightsRevokedText" javaScriptEscape="true"/>
<spring:message code="page.action.revoke" var="revokeText" javaScriptEscape="true"/>
<spring:message code="dialog.button.cancel" var="cancelText" javaScriptEscape="true"/>
<spring:message code="dialog.button.ok" var="okText" javaScriptEscape="true"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-8 col-md-offset-2"
					 hasTable="true" hasNotify="true">

	<jsp:attribute name="javascript">
		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="jobsConfig"]').parent().addClass('active');

				var tbl = $('#rights');

				var columnFilterRow = createColumnFilters(tbl);

				//initialize datatable and process delete action
				var oTable = tbl.dataTable({
					orderClasses: false,
					pagingType: "full_numbers",
					lengthMenu: [[10, 20, 50, -1], [10, 20, 50, "${showAllRowsText}"]],
					pageLength: 10,
					columnDefs: [
						{
							targets: "actionCol",
							orderable: false,
							searchable: false
						}
					],
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
						message: "${revokeText}: <b>" + recordName + "</b>",
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
									url: "${pageContext.request.contextPath}/deleteAccessRight",
									data: {id: recordId},
									success: function (response) {
										if (response.success) {
											table.row(row).remove().draw(false); //draw(false) to prevent datatables from going back to page 1
											notifyActionSuccessReusable("${rightsRevokedText}", recordName);
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
			<b><spring:message code="jobs.text.job"/>:</b> ${encode:forHtmlContent(job.name)} (${job.jobId})
		</div>

		<table id="rights" class="table table-striped table-bordered">
			<thead>
				<tr>
					<th><spring:message code="page.text.user"/></th>
					<th><spring:message code="page.text.userGroup"/></th>
					<th class="noFilter actionCol"><spring:message code="page.text.action"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="userJobRight" items="${userJobRights}">
					<tr data-name="${encode:forHtmlAttribute(userJobRight.user.username)} -
						${encode:forHtmlAttribute(userJobRight.job.name)}"
						data-id="userJobRight-${userJobRight.user.userId}-${userJobRight.job.jobId}">

						<td><encode:forHtmlContent value="${userJobRight.user.username}"/></td>
						<td></td>
						<td>
							<button type="button" class="btn btn-default deleteRecord">
								<i class="fa fa-trash-o"></i>
								<spring:message code="page.action.revoke"/>
							</button>
						</td>
					</tr>
				</c:forEach>

				<c:forEach var="userGroupJobRight" items="${userGroupJobRights}">
					<tr data-name="${encode:forHtmlAttribute(userGroupJobRight.userGroup.name)} -
						${encode:forHtmlAttribute(userGroupJobRight.job.name)}"
						data-id="userGroupJobRight-${userGroupJobRight.userGroup.userGroupId}-${userGroupJobRight.job.jobId}">

						<td></td>
						<td><encode:forHtmlContent value="${userGroupJobRight.userGroup.name}"/></td>
						<td>
							<button type="button" class="btn btn-default deleteRecord">
								<i class="fa fa-trash-o"></i>
								<spring:message code="page.action.revoke"/>
							</button>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</jsp:body>
</t:mainPageWithPanel>
