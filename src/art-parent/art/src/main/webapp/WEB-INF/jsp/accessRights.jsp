<%-- 
    Document   : accessRights
    Created on : 22-Apr-2014, 11:36:37
    Author     : Timothy Anyona

Display access rights
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.accessRights" var="pageTitle"/>

<spring:message code="datatables.text.showAllRows" var="dataTablesAllRowsText"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="page.message.rightsRevoked" var="rightsRevokedText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-8 col-md-offset-2">

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/notify-combined-0.3.1.min.js"></script>
		<script type="text/javascript">
			$(document).ready(function() {
				$(function() {
					$('a[id="configure"]').parent().addClass('active');
					$('a[href*="accessRightsConfig.do"]').parent().addClass('active');
				});

				var oTable = $('#rights').dataTable({
					"sPaginationType": "bs_full",
					"aaSorting": [],
					"aLengthMenu": [[5, 10, 25, -1], [5, 10, 25, "${dataTablesAllRowsText}"]],
					"iDisplayLength": 25,
					"oLanguage": {
						"sUrl": "${pageContext.request.contextPath}/js/dataTables-1.9.4/i18n/dataTables_${pageContext.response.locale}.txt"
					},
					"fnInitComplete": function() {
						$('div.dataTables_filter input').focus();
					}
				});

				$('#rights tbody').on('click', '.revoke', function() {
					var row = $(this).closest("tr"); //jquery object
					var nRow = row[0]; //dom element/node
					var name = escapeHtmlContent(row.data("name"));
					var id = row.data("id");

					$.ajax({
						type: "POST",
						dataType: "json",
						url: "${pageContext.request.contextPath}/app/deleteAccessRight.do",
						data: {id: id},
						success: function(response) {
							var msg;
							if (response.success) {
								oTable.fnDeleteRow(nRow);

								msg = alertCloseButton + "${rightsRevokedText}: " + name;
								$("#ajaxResponse").attr("class", "alert alert-success alert-dismissable").html(msg);
								$.notify("${rightsRevokedText}", "success");
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
				}); //end on click

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

		<div id="ajaxResponse">
		</div>

		<table id="rights" class="table table-striped table-bordered">
			<thead>
				<tr>
					<th><spring:message code="page.text.user"/></th>
					<th><spring:message code="page.text.userGroup"/></th>
					<th><spring:message code="page.text.report"/></th>
					<th><spring:message code="page.text.reportGroup"/></th>
					<th><spring:message code="page.text.action"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="userReportRight" items="${userReportRights}">
					<tr data-name="${encode:forHtmlAttribute(userReportRight.user.username)} -
						${encode:forHtmlAttribute(userReportRight.report.name)}"
						data-id="userReportRight-${userReportRight.user.userId}-${userReportRight.report.reportId}">

						<td><encode:forHtmlContent value="${userReportRight.user.username}"/></td>
						<td></td>
						<td><encode:forHtmlContent value="${userReportRight.report.name}"/></td>
						<td></td>
						<td>
							<button type="button" class="btn btn-default revoke">
								<i class="fa fa-trash-o"></i>
								<spring:message code="page.action.revoke"/>
							</button>
						</td>
					</tr>
				</c:forEach>

				<c:forEach var="userReportGroupRight" items="${userReportGroupRights}">
					<tr data-name="${encode:forHtmlAttribute(userReportGroupRight.user.username)} -
						${encode:forHtmlAttribute(userReportGroupRight.reportGroup.name)}"
						data-id="userReportGroupRight-${userReportGroupRight.user.userId}-${userReportGroupRight.reportGroup.reportGroupId}">

						<td><encode:forHtmlContent value="${userReportGroupRight.user.username}"/></td>
						<td></td>
						<td></td>
						<td><encode:forHtmlContent value="${userReportGroupRight.reportGroup.name}"/></td>
						<td>
							<button type="button" class="btn btn-default revoke">
								<i class="fa fa-trash-o"></i>
								<spring:message code="page.action.revoke"/>
							</button>
						</td>
					</tr>
				</c:forEach>

				<c:forEach var="userGroupReportRight" items="${userGroupReportRights}">
					<tr data-name="${encode:forHtmlAttribute(userGroupReportRight.userGroup.name)} -
						${encode:forHtmlAttribute(userGroupReportRight.report.name)}"
						data-id="userGroupReportRight-${userGroupReportRight.userGroup.userGroupId}-${userGroupReportRight.report.reportId}">

						<td></td>
						<td><encode:forHtmlContent value="${userGroupReportRight.userGroup.name}"/></td>
						<td><encode:forHtmlContent value="${userGroupReportRight.report.name}"/></td>
						<td></td>
						<td>
							<button type="button" class="btn btn-default revoke">
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
						<td></td>
						<td><encode:forHtmlContent value="${userGroupReportGroupRight.reportGroup.name}"/></td>
						<td>
							<button type="button" class="btn btn-default revoke">
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
