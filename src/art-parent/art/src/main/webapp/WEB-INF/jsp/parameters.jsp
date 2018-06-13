<%-- 
    Document   : parameters
    Created on : 29-Apr-2014, 12:20:24
    Author     : Timothy Anyona

Display parameters
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<spring:message code="page.title.parameters" var="pageTitle"/>

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="dialog.button.cancel" var="cancelText"/>
<spring:message code="dialog.button.ok" var="okText"/>
<spring:message code="dialog.message.deleteRecord" var="deleteRecordText"/>
<spring:message code="page.message.recordDeleted" var="recordDeletedText"/>
<spring:message code="page.message.cannotDeleteRecord" var="cannotDeleteRecordText"/>
<spring:message code="parameters.message.linkedReportsExist" var="linkedReportsExistText"/>
<spring:message code="page.message.recordsDeleted" var="recordsDeletedText"/>
<spring:message code="dialog.message.selectRecords" var="selectRecordsText"/>
<spring:message code="page.message.someRecordsNotDeleted" var="someRecordsNotDeletedText"/>

<t:mainConfigPage title="${pageTitle}" mainColumnClass="col-md-12">

	<jsp:attribute name="javascript">
		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="parameters"]').parent().addClass('active');

				var tbl = $('#parameters');

				//initialize datatable and process delete action
				var oTable = initConfigPage(tbl,
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
						"deleteParameter", //deleteUrl
						"${recordDeletedText}",
						"${errorOccurredText}",
						true, //deleteRow
						"${cannotDeleteRecordText}", //cannotDeleteRecordText
						"${linkedReportsExistText}" //linkedRecordsExistText
						);

				var table = oTable.api();

				$('#deleteRecords').click(function () {
					var selectedRows = table.rows({selected: true});
					var data = selectedRows.data();
					if (data.length > 0) {
						var ids = $.map(data, function (item) {
							return item[1];
						});
						bootbox.confirm({
							message: "${deleteRecordText}: <b>" + ids + "</b>",
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
										url: "${pageContext.request.contextPath}/deleteParameters",
										data: {ids: ids},
										success: function (response) {
											var nonDeletedRecords = response.data;
											if (response.success) {
												selectedRows.remove().draw(false);
												notifyActionSuccess("${recordsDeletedText}", ids);
											} else if (nonDeletedRecords !== null && nonDeletedRecords.length > 0) {
												notifySomeRecordsNotDeleted(nonDeletedRecords, "${someRecordsNotDeletedText}");
											} else {
												notifyActionError("${errorOccurredText}", escapeHtmlContent(response.errorMessage));
											}
										},
										error: ajaxErrorHandler
									});
								} //end if result
							} //end callback
						}); //end bootbox confirm
					} else {
						bootbox.alert("${selectRecordsText}");
					}
				});

				$('#exportRecords').click(function () {
					var selectedRows = table.rows({selected: true});
					var data = selectedRows.data();
					if (data.length > 0) {
						var ids = $.map(data, function (item) {
							return item[1];
						});
						window.location.href = '${pageContext.request.contextPath}/exportRecords?type=Parameters&ids=' + ids;
					} else {
						bootbox.alert("${selectRecordsText}");
					}
				});

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

		<div style="margin-bottom: 10px;">
			<div class="btn-group">
				<a class="btn btn-default" href="${pageContext.request.contextPath}/addParameter">
					<i class="fa fa-plus"></i>
					<spring:message code="page.action.add"/>
				</a>
				<button type="button" id="deleteRecords" class="btn btn-default">
					<i class="fa fa-trash-o"></i>
					<spring:message code="page.action.delete"/>
				</button>
			</div>
			<c:if test="${sessionUser.accessLevel.value >= 80}">
				<div class="btn-group">
					<a class="btn btn-default" href="${pageContext.request.contextPath}/importRecords?type=Parameters">
						<spring:message code="page.text.import"/>
					</a>
					<button type="button" id="exportRecords" class="btn btn-default">
						<spring:message code="page.text.export"/>
					</button>
				</div>
			</c:if>
		</div>

		<table id="parameters" class="table table-bordered table-striped table-condensed">
			<thead>
				<tr>
					<th class="noFilter"></th>
					<th><spring:message code="page.text.id"/></th>
					<th><spring:message code="page.text.name"/></th>
					<th><spring:message code="page.text.description"/></th>
					<th class="noFilter"><spring:message code="page.text.action"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="parameter" items="${parameters}">
					<tr data-id="${parameter.parameterId}" 
						data-name="${encode:forHtmlAttribute(parameter.name)} (${parameter.parameterId})">

						<td></td>
						<td>${parameter.parameterId}</td>
						<td>${encode:forHtmlContent(parameter.name)} &nbsp;
							<t:displayNewLabel creationDate="${parameter.creationDate}"
											   updateDate="${parameter.updateDate}"/>
						</td>
						<td>${encode:forHtmlContent(parameter.description)}
							<c:if test="${parameter.shared}">
								&nbsp;
								<span class="label label-success">
									<spring:message code="parameters.label.shared"/>
								</span>
							</c:if>
						</td>
						<td>
							<div class="btn-group">
								<a class="btn btn-default" 
								   href="${pageContext.request.contextPath}/editParameter?id=${parameter.parameterId}">
									<i class="fa fa-pencil-square-o"></i>
									<spring:message code="page.action.edit"/>
								</a>
								<button type="button" class="btn btn-default deleteRecord">
									<i class="fa fa-trash-o"></i>
									<spring:message code="page.action.delete"/>
								</button>
								<a class="btn btn-default" 
								   href="${pageContext.request.contextPath}/copyParameter?id=${parameter.parameterId}">
									<i class="fa fa-copy"></i>
									<spring:message code="page.action.copy"/>
								</a>
							</div>
							<div class="btn-group">
								<button type="button" class="btn btn-default dropdown-toggle"
										data-toggle="dropdown" data-hover="dropdown"
										data-delay="100">
									<spring:message code="reports.action.more"/>
									<span class="caret"></span>
								</button>
								<ul class="dropdown-menu">
									<li>
										<a 
											href="${pageContext.request.contextPath}/reportsForParameter?parameterId=${parameter.parameterId}">
											<spring:message code="page.text.usage"/>
										</a>
									</li>
									<li>
										<a 
											href="${pageContext.request.contextPath}/parameterParamDefaults?parameterId=${parameter.parameterId}">
											<spring:message code="parameters.text.defaults"/>
										</a>
									</li>
									<c:if test="${parameter.fixedValue}">
										<li>
											<a 
												href="${pageContext.request.contextPath}/parameterFixedParamValues?parameterId=${parameter.parameterId}">
												<spring:message code="parameters.text.fixedValues"/>
											</a>
										</li>
									</c:if>
								</ul>
							</div>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</jsp:body>
</t:mainConfigPage>
