<%-- 
    Document   : parameterParamDefaults
    Created on : 02-Apr-2018, 13:15:57
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.parameterParamDefaults" var="pageTitle"/>

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText" javaScriptEscape="true"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText" javaScriptEscape="true"/>
<spring:message code="page.message.valueRemoved" var="valueRemovedText" javaScriptEscape="true"/>
<spring:message code="page.action.remove" var="removeText" javaScriptEscape="true"/>
<spring:message code="dialog.button.cancel" var="cancelText" javaScriptEscape="true"/>
<spring:message code="dialog.button.ok" var="okText" javaScriptEscape="true"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-10 col-md-offset-1"
					 hasTable="true" hasNotify="true">

	<jsp:attribute name="javascript">
		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="parameters"]').parent().addClass('active');

				var tbl = $('#defaults');
				
				var pageLength = 10; //pass undefined to use the default
				var showAllRowsText = "${showAllRowsText}";
				var contextPath = "${pageContext.request.contextPath}";
				var localeCode = "${pageContext.response.locale}";
				var addColumnFilters = undefined; //pass undefined to use the default
				var deleteRecordText = "${removeText}";
				var okText = "${okText}";
				var cancelText = "${cancelText}";
				var deleteRecordUrl = "${pageContext.request.contextPath}/deleteParamDefault";
				var recordDeletedText = "${valueRemovedText}";
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

		<div class="text-center">
			<b><spring:message code="page.text.parameter"/>:</b> ${encode:forHtmlContent(parameter.name)} (${parameter.parameterId})
		</div>

		<table id="defaults" class="table table-striped table-bordered">
			<thead>
				<tr>
					<th><spring:message code="page.text.user"/></th>
					<th><spring:message code="page.text.userGroup"/></th>
					<th><spring:message code="page.text.value"/></th>
					<th class="noFilter actionCol"><spring:message code="page.text.action"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="userParamDefault" items="${userParamDefaults}">
					<tr data-name="${encode:forHtmlAttribute(userParamDefault.user.username)} -
						${encode:forHtmlAttribute(userParamDefault.parameter.name)} (${userParamDefault.parameter.parameterId}) -
						${encode:forHtmlAttribute(userParamDefault.value)}"
						data-id="userParamDefault~${encode:forHtmlAttribute(userParamDefault.paramDefaultKey)}">

						<td><encode:forHtmlContent value="${userParamDefault.user.username}"/></td>
						<td></td>
						<td><encode:forHtmlContent value="${userParamDefault.value}"/></td>
						<td>
							<div class="btn-group">
								<a class="btn btn-default" 
								   href="${pageContext.request.contextPath}/editUserParamDefault?id=${userParamDefault.paramDefaultKey}&returnParameterId=${parameter.parameterId}">
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

				<c:forEach var="userGroupParamDefault" items="${userGroupParamDefaults}">
					<tr data-name="${encode:forHtmlAttribute(userGroupParamDefault.userGroup.name)} -
						${encode:forHtmlAttribute(userGroupParamDefault.parameter.name)} (${userGroupParamDefault.parameter.parameterId}) - 
						${encode:forHtmlAttribute(userGroupParamDefault.value)}"
						data-id="userGroupParamDefault~${encode:forHtmlAttribute(userGroupParamDefault.paramDefaultKey)}">

						<td></td>
						<td><encode:forHtmlContent value="${userGroupParamDefault.userGroup.name}"/></td>
						<td><encode:forHtmlContent value="${userGroupParamDefault.value}"/></td>
						<td>
							<div class="btn-group">
								<a class="btn btn-default" 
								   href="${pageContext.request.contextPath}/editUserGroupParamDefault?id=${userGroupParamDefault.paramDefaultKey}&returnParameterId=${parameter.parameterId}">
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
