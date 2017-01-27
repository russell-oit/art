<%-- 
    Document   : ruleValues
    Created on : 19-May-2014, 11:28:15
    Author     : Timothy Anyona

Display rule values
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.ruleValues" var="pageTitle"/>

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="ruleValues.message.valueRemoved" var="valueRemovedText"/>
<spring:message code="page.action.remove" var="removeText"/>
<spring:message code="dialog.button.cancel" var="cancelText"/>
<spring:message code="dialog.button.ok" var="okText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-10 col-md-offset-1">

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/notify-combined-0.3.1.min.js"></script>
		
		<script type="text/javascript">
			$(document).ready(function() {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="ruleValuesConfig.do"]').parent().addClass('active');

				var tbl = $('#values');
				
				var columnFilterRow = createColumnFilters(tbl);

				//initialize datatable and process delete action
				var oTable = tbl.dataTable({
					orderClasses: false,
					pagingType: "full_numbers",
					lengthMenu: [[5, 10, 25, -1], [5, 10, 25, "${showAllRowsText}"]],
					pageLength: 10,
					language: {
						url: "${pageContext.request.contextPath}/js/dataTables-1.10.11/i18n/dataTables_${pageContext.response.locale}.json"
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
					var recordName = escapeHtmlContent(row.data("name"));
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
									url: "${pageContext.request.contextPath}/app/deleteRuleValue.do",
									data: {id: recordId},
									success: function (response) {
										if (response.success) {
											table.row(row).remove().draw(false); //draw(false) to prevent datatables from going back to page 1
											notifyActionSuccess("${valueRemovedText}", recordName);
										} else {
											notifyActionError("${errorOccurredText}", escapeHtmlContent(response.errorMessage));
										}
									},
									error: ajaxErrorHandler
								});
							} //end if result
						} //end callback
					}); //end bootbox confirm
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

		<div id="ajaxResponse">
		</div>

		<table id="values" class="table table-striped table-bordered">
			<thead>
				<tr>
					<th><spring:message code="page.text.user"/></th>
					<th><spring:message code="page.text.userGroup"/></th>
					<th><spring:message code="page.text.rule"/></th>
					<th><spring:message code="page.text.value"/></th>
					<th class="noFilter"><spring:message code="page.text.action"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="userRuleValue" items="${userRuleValues}">
					<tr data-name="${encode:forHtmlAttribute(userRuleValue.user.username)} -
						${encode:forHtmlAttribute(userRuleValue.rule.name)} -
						${encode:forHtmlAttribute(userRuleValue.ruleValue)}"
						data-id="userRuleValue~${encode:forHtmlAttribute(userRuleValue.ruleValueKey)}">

						<td><encode:forHtmlContent value="${userRuleValue.user.username}"/></td>
						<td></td>
						<td><encode:forHtmlContent value="${userRuleValue.rule.name}"/></td>
						<td><encode:forHtmlContent value="${userRuleValue.ruleValue}"/></td>
						<td>
							<div class="btn-group">
								<a class="btn btn-default" 
								   href="${pageContext.request.contextPath}/app/editUserRuleValue.do?id=${userRuleValue.ruleValueKey}">
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

				<c:forEach var="userGroupRuleValue" items="${userGroupRuleValues}">
					<tr data-name="${encode:forHtmlAttribute(userGroupRuleValue.userGroup.name)} -
						${encode:forHtmlAttribute(userGroupRuleValue.rule.name)} - 
						${encode:forHtmlAttribute(userGroupRuleValue.ruleValue)}"
						data-id="userGroupRuleValue~${encode:forHtmlAttribute(userGroupRuleValue.ruleValueKey)}">

						<td></td>
						<td><encode:forHtmlContent value="${userGroupRuleValue.userGroup.name}"/></td>
						<td><encode:forHtmlContent value="${userGroupRuleValue.rule.name}"/></td>
						<td><encode:forHtmlContent value="${userGroupRuleValue.ruleValue}"/></td>
						<td>
							<div class="btn-group">
								<a class="btn btn-default" 
								   href="${pageContext.request.contextPath}/app/editUserGroupRuleValue.do?id=${userGroupRuleValue.ruleValueKey}">
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
