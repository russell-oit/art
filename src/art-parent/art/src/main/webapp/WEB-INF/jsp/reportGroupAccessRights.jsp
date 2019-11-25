<%-- 
    Document   : reportGroupAccessRights
    Created on : 04-Jul-2017, 18:48:32
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.reportGroupAccessRights" var="pageTitle"/>

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
				$('a[href*="reportGroups"]').parent().addClass('active');

				var tbl = $('#rights');
				
				var pageLength = 10; //pass undefined to use the default
				var showAllRowsText = "${showAllRowsText}";
				var contextPath = "${pageContext.request.contextPath}";
				var localeCode = "${pageContext.response.locale}";
				var addColumnFilters = undefined; //pass undefined to use the default
				var columnDefs = undefined; //pass undefined to use the default

				//initialize datatable
				var oTable = initBasicTable(tbl, pageLength, showAllRowsText,
						contextPath, localeCode, addColumnFilters, columnDefs);

				var table = oTable.api();

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
			<b><spring:message code="page.text.reportGroup"/>:</b> ${encode:forHtmlContent(reportGroup.name)}
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
				<c:forEach var="userReportGroupRight" items="${userReportGroupRights}">
					<tr data-name="${encode:forHtmlAttribute(userReportGroupRight.user.username)} -
						${encode:forHtmlAttribute(userReportGroupRight.reportGroup.name)}"
						data-id="userReportGroupRight-${userReportGroupRight.user.userId}-${userReportGroupRight.reportGroup.reportGroupId}">

						<td><encode:forHtmlContent value="${userReportGroupRight.user.username}"/></td>
						<td></td>
						<td>
							<button type="button" class="btn btn-default deleteRecord">
								<i class="fa fa-trash-o"></i>
								<spring:message code="page.action.revoke"/>
							</button>
						</td>
					</tr>
				</c:forEach>

				<c:forEach var="userGroupReportGroupRight" items="${userGroupReportGroupRights}">
					<tr data-name="${encode:forHtmlAttribute(userGroupReportGroupRight.userGroup.name)} -
						${encode:forHtmlAttribute(userGroupReportGroupRight.reportGroup.name)}"
						data-id="userGroupReportGroupRight-${userGroupReportGroupRight.userGroup.userGroupId}-${userGroupReportGroupRight.reportGroup.reportGroupId}">

						<td></td>
						<td><encode:forHtmlContent value="${userGroupReportGroupRight.userGroup.name}"/></td>
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
