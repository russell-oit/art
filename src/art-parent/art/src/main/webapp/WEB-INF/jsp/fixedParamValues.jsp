<%-- 
    Document   : fixedParamValues
    Created on : 02-Apr-2018, 22:06:47
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.fixedParamValues" var="pageTitle"/>

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText" javaScriptEscape="true"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText" javaScriptEscape="true"/>
<spring:message code="page.message.valueRemoved" var="valueRemovedText" javaScriptEscape="true"/>
<spring:message code="page.action.remove" var="removeText" javaScriptEscape="true"/>
<spring:message code="dialog.button.cancel" var="cancelText" javaScriptEscape="true"/>
<spring:message code="dialog.button.ok" var="okText" javaScriptEscape="true"/>

<t:mainPageWithPanel title="${pageTitle}" hasTable="true" hasNotify="true">

	<jsp:attribute name="javascript">
		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="fixedParamValuesConfig"]').parent().addClass('active');

				var tbl = $('#values');

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
									url: "${pageContext.request.contextPath}/deleteFixedParamValue",
									data: {id: recordId},
									success: function (response) {
										if (response.success) {
											table.row(row).remove().draw(false); //draw(false) to prevent datatables from going back to page 1
											notifyActionSuccessReusable("${valueRemovedText}", recordName);
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

		<table id="values" class="table table-striped table-bordered">
			<thead>
				<tr>
					<th><spring:message code="page.text.user"/></th>
					<th><spring:message code="page.text.userGroup"/></th>
					<th><spring:message code="page.text.parameter"/></th>
					<th><spring:message code="page.text.value"/></th>
					<th class="noFilter actionCol"><spring:message code="page.text.action"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="userFixedParamValue" items="${userFixedParamValues}">
					<tr data-name="${encode:forHtmlAttribute(userFixedParamValue.user.username)} -
						${encode:forHtmlAttribute(userFixedParamValue.parameter.name)} (${userFixedParamValue.parameter.parameterId}) -
						${encode:forHtmlAttribute(userFixedParamValue.value)}"
						data-id="userFixedParamValue~${encode:forHtmlAttribute(userFixedParamValue.fixedParamValueKey)}">

						<td><encode:forHtmlContent value="${userFixedParamValue.user.username}"/></td>
						<td></td>
						<td>${encode:forHtmlAttribute(userFixedParamValue.parameter.name)} (${userFixedParamValue.parameter.parameterId})</td>
						<td><encode:forHtmlContent value="${userFixedParamValue.value}"/></td>
						<td>
							<div class="btn-group">
								<a class="btn btn-default" 
								   href="${pageContext.request.contextPath}/editUserFixedParamValue?id=${userFixedParamValue.fixedParamValueKey}">
									<i class="fa fa-pencil-square-o"></i>
									<spring:message code="page.action.edit"/>
								</a>
								<button type="button" class="btn btn-default deleteRecord">
									<i class="fa fa-trash-o"></i>
									<spring:message code="page.action.remove"/>
								</button>
							</div>
						</td>
					</tr>
				</c:forEach>

				<c:forEach var="userGroupFixedParamValue" items="${userGroupFixedParamValues}">
					<tr data-name="${encode:forHtmlAttribute(userGroupFixedParamValue.userGroup.name)} -
						${encode:forHtmlAttribute(userGroupFixedParamValue.parameter.name)} (${userGroupFixedParamValue.parameter.parameterId}) - 
						${encode:forHtmlAttribute(userGroupFixedParamValue.value)}"
						data-id="userGroupFixedParamValue~${encode:forHtmlAttribute(userGroupFixedParamValue.fixedParamValueKey)}">

						<td></td>
						<td><encode:forHtmlContent value="${userGroupFixedParamValue.userGroup.name}"/></td>
						<td>${encode:forHtmlAttribute(userGroupFixedParamValue.parameter.name)} (${userGroupFixedParamValue.parameter.parameterId})</td>
						<td><encode:forHtmlContent value="${userGroupFixedParamValue.value}"/></td>
						<td>
							<div class="btn-group">
								<a class="btn btn-default" 
								   href="${pageContext.request.contextPath}/editUserGroupFixedParamValue?id=${userGroupFixedParamValue.fixedParamValueKey}">
									<i class="fa fa-pencil-square-o"></i>
									<spring:message code="page.action.edit"/>
								</a>
								<button type="button" class="btn btn-default deleteRecord">
									<i class="fa fa-trash-o"></i>
									<spring:message code="page.action.remove"/>
								</button>
							</div>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</jsp:body>
</t:mainPageWithPanel>
