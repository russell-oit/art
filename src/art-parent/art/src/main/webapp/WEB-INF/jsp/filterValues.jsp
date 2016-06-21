<%-- 
    Document   : filterValues
    Created on : 19-May-2014, 11:28:15
    Author     : Timothy Anyona

Display filter values
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.filterValues" var="pageTitle"/>

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="filterValues.message.valueRemoved" var="valueRemovedText"/>
<spring:message code="dataTables.text.selectAll" var="selectAllText"/>
<spring:message code="dataTables.text.deselectAll" var="deselectAllText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-8 col-md-offset-2">

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/notify-combined-0.3.1.min.js"></script>
		<script type="text/javascript">
			$(document).ready(function() {
				$(function() {
					$('a[href*="filterValuesConfig.do"]').parent().addClass('active');
				});

				var tbl = $('#values');

				//initialize datatable and process delete action
				initConfigPage(tbl,
						undefined, //pageLength. pass undefined to use the default
						"${showAllRowsText}",
						"${pageContext.request.contextPath}",
						"${pageContext.response.locale}",
						undefined, //addColumnFilters. pass undefined to use default
						".deleteRecord", //deleteButtonSelector
						false, //showConfirmDialog
						undefined, //deleteRecordText
						undefined, //okText
						undefined, //cancelText
						"deleteFilterValue.do", //deleteUrl
						"${valueRemovedText}", //recordDeletedText
						"${errorOccurredText}",
						true, //deleteRow
						undefined, //cannotDeleteRecordText
						undefined, //linkedRecordsExistText
						"${selectAllText}",
						"${deselectAllText}"
						);


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
					<th><spring:message code="page.text.filter"/></th>
					<th><spring:message code="page.text.value"/></th>
					<th class="noFilter"><spring:message code="page.text.action"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="userFilterValue" items="${userFilterValues}">
					<tr data-name="${encode:forHtmlAttribute(userFilterValue.user.username)} -
						${encode:forHtmlAttribute(userFilterValue.filter.name)} -
						${encode:forHtmlAttribute(userFilterValue.filterValue)}"
						data-id="userFilterValue~${userFilterValue.filterValueKey}">

						<td><encode:forHtmlContent value="${userFilterValue.user.username}"/></td>
						<td></td>
						<td><encode:forHtmlContent value="${userFilterValue.filter.name}"/></td>
						<td><encode:forHtmlContent value="${userFilterValue.filterValue}"/></td>
						<td>
							<div class="btn-group">
								<a class="btn btn-default" 
								   href="${pageContext.request.contextPath}/app/editUserFilterValue.do?id=${userFilterValue.filterValueKey}">
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

				<c:forEach var="userGroupFilterValue" items="${userGroupFilterValues}">
					<tr data-name="${encode:forHtmlAttribute(userGroupFilterValue.userGroup.name)} -
						${encode:forHtmlAttribute(userGroupFilterValue.filter.name)} - 
						${encode:forHtmlAttribute(userGroupFilterValue.filterValue)}"
						data-id="userGroupFilterValue~${userGroupFilterValue.filterValueKey}">

						<td></td>
						<td><encode:forHtmlContent value="${userGroupFilterValue.userGroup.name}"/></td>
						<td><encode:forHtmlContent value="${userGroupFilterValue.filter.name}"/></td>
						<td><encode:forHtmlContent value="${userGroupFilterValue.filterValue}"/></td>
						<td>
							<div class="btn-group">
								<a class="btn btn-default" 
								   href="${pageContext.request.contextPath}/app/editUserGroupFilterValue.do?id=${userGroupFilterValue.filterValueKey}">
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
