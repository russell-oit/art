<%-- 
    Document   : userAccessRights
    Created on : 20-Nov-2017, 19:13:02
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.userAccessRights" var="pageTitle"/>

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText" javaScriptEscape="true"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText" javaScriptEscape="true"/>
<spring:message code="page.message.rightsRevoked" var="rightsRevokedText" javaScriptEscape="true"/>
<spring:message code="page.action.revoke" var="revokeText" javaScriptEscape="true"/>
<spring:message code="dialog.button.cancel" var="cancelText" javaScriptEscape="true"/>
<spring:message code="dialog.button.ok" var="okText" javaScriptEscape="true"/>

<t:mainPageWithPanel title="${pageTitle}" hasTable="true" hasNotify="true">

	<jsp:attribute name="javascript">
		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="users"]').parent().addClass('active');

				var tbl = $('#rights');
				
				var pageLength = 10; //pass undefined to use the default
				var showAllRowsText = "${showAllRowsText}";
				var contextPath = "${pageContext.request.contextPath}";
				var localeCode = "${pageContext.response.locale}";
				var addColumnFilters = undefined; //pass undefined to use the default
				var deleteRecordText = "${revokeText}";
				var okText = "${okText}";
				var cancelText = "${cancelText}";
				var deleteRecordUrl = "${pageContext.request.contextPath}/deleteAccessRight";
				var recordDeletedText = "${rightsRevokedText}";
				var errorOccurredText = "${errorOccurredText}";
				var showErrors = ${showErrors};
				var cannotDeleteRecordText = undefined;
				var linkedRecordsExistText = undefined;
				var columnDefs = undefined; //pass undefined to use the default

				//initialize datatable
				var oTable = initBasicTable(tbl, pageLength, showAllRowsText,
						contextPath, localeCode, addColumnFilters, columnDefs);

				var table = oTable.api();
				
				addDeleteRecordHandler(tbl, table, deleteRecordText, okText,
						cancelText, deleteRecordUrl, recordDeletedText,
						errorOccurredText, showErrors, cannotDeleteRecordText,
						linkedRecordsExistText);

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

		<table id="rights" class="table table-striped table-bordered">
			<thead>
				<tr>
					<th><spring:message code="page.text.report"/></th>
					<th><spring:message code="page.text.reportGroup"/></th>
					<th><spring:message code="jobs.text.job"/></th>
					<th class="noFilter actionCol"><spring:message code="page.text.action"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="userReportRight" items="${userReportRights}">
					<tr data-name="${encode:forHtmlAttribute(userReportRight.user.username)} -
						${encode:forHtmlAttribute(userReportRight.report.name)}"
						data-id="userReportRight-${userReportRight.user.userId}-${userReportRight.report.reportId}">

						<td><encode:forHtmlContent value="${userReportRight.report.name}"/></td>
						<td></td>
						<td></td>
						<td>
							<button type="button" class="btn btn-default deleteRecord">
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

						<td></td>
						<td><encode:forHtmlContent value="${userReportGroupRight.reportGroup.name}"/></td>
						<td></td>
						<td>
							<button type="button" class="btn btn-default deleteRecord">
								<i class="fa fa-trash-o"></i>
								<spring:message code="page.action.revoke"/>
							</button>
						</td>
					</tr>
				</c:forEach>

				<c:forEach var="userJobRight" items="${userJobRights}">
					<tr data-name="${encode:forHtmlAttribute(userJobRight.user.username)} -
						${encode:forHtmlAttribute(userJobRight.job.name)}"
						data-id="userJobRight-${userJobRight.user.userId}-${userJobRight.job.jobId}">

						<td></td>
						<td></td>
						<td><encode:forHtmlContent value="${userJobRight.job.name}"/> (${userJobRight.job.jobId})</td>
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
