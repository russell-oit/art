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

<spring:message code="page.title.userGroups" var="pageTitle"/>

<spring:message code="datatables.text.showAllRows" var="showAllRowsText"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="dialog.button.cancel" var="cancelText"/>
<spring:message code="dialog.button.ok" var="okText"/>
<spring:message code="dialog.message.deleteRecord" var="deleteRecordText"/>
<spring:message code="page.message.recordDeleted" var="recordDeletedText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-8 col-md-offset-2">

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/notify-combined-0.3.1.min.js"></script>
		<script type="text/javascript">
			$(document).ready(function() {
				$('a[href*="userGroups.do"]').parent().addClass('active');

				var tbl = $('#userGroups');

				//initialize datatable and process delete action
				initConfigPage(tbl,
						undefined, //pageLength. pass undefined to use the default
						"${showAllRowsText}",
						"${pageContext.request.contextPath}",
						"${pageContext.response.locale}",
						undefined, //addColumnFilters. pass undefined to use default
						".deleteRecord", //deleteButtonSelector
						true, //showConfirmDialog
						"${deleteRecordText}",
						"${okText}",
						"${cancelText}",
						"deleteUserGroup.do", //deleteUrl
						"${recordDeletedText}",
						"${errorOccurredText}",
						true, //deleteRow
						undefined, //cannotDeleteRecordText
						undefined //linkedRecordsExistText
						);

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

		<div id="ajaxResponse">
		</div>

		<div id="headerActions" class="dtHeader">
			<a class="btn btn-default" href="${pageContext.request.contextPath}/app/addUserGroup.do">
				<i class="fa fa-plus"></i>
				<spring:message code="page.action.add"/>
			</a>
		</div>

		<table id="userGroups" class="table table-bordered table-striped table-condensed">
			<thead>
				<tr>
					<th><spring:message code="page.text.id"/></th>
					<th><spring:message code="page.text.name"/></th>
					<th><spring:message code="page.text.description"/></th>
					<th><spring:message code="page.text.action"/></th>
				</tr>
			</thead>

			<tbody>
				<c:forEach var="group" items="${groups}">
					<tr data-id="${group.userGroupId}" 
						data-name="${encode:forHtmlAttribute(group.name)}">

						<td>${group.userGroupId}</td>
						<td>${encode:forHtmlContent(group.name)} &nbsp;
							<t:displayNewLabel creationDate="${group.creationDate}"
											   updateDate="${group.updateDate}"/>
						</td>
						<td>${encode:forHtmlContent(group.description)}</td>
						<td>
							<div class="btn-group">
								<a class="btn btn-default" 
								   href="${pageContext.request.contextPath}/app/editUserGroup.do?id=${group.userGroupId}">
									<i class="fa fa-pencil-square-o"></i>
									<spring:message code="page.action.edit"/>
								</a>
								<button type="button" class="btn btn-default deleteRecord">
									<i class="fa fa-trash-o"></i>
									<spring:message code="page.action.delete"/>
								</button>
							</div>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>

	</jsp:body>
</t:mainPageWithPanel>
