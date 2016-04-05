<%-- 
    Document   : reportsConfig
    Created on : 25-Feb-2014, 10:46:51
    Author     : Timothy Anyona

Reports configuration page
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.reportsConfiguration" var="pageTitle"/>

<spring:message code="datatables.text.showAllRows" var="showAllRowsText"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="dialog.button.cancel" var="cancelText"/>
<spring:message code="dialog.button.ok" var="okText"/>
<spring:message code="dialog.message.deleteRecord" var="deleteRecordText"/>
<spring:message code="page.message.recordDeleted" var="recordDeletedText"/>
<spring:message code="reports.message.linkedJobsExist" var="linkedJobsExistText"/>
<spring:message code="page.message.cannotDeleteRecord" var="cannotDeleteRecordText"/>
<spring:message code="page.message.recordsDeleted" var="recordsDeletedText"/>
<spring:message code="dialog.message.selectRecords" var="selectRecordsText"/>
<spring:message code="page.message.someRecordsNotDeleted" var="someRecordsNotDeletedText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-12">

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/notify-combined-0.3.1.min.js"></script>
		<script type="text/javascript" charset="utf-8">
			$(document).ready(function() {
				$(function() {
					$('a[href*="reportsConfig.do"]').parent().addClass('active');
				});
				var tbl = $('#reports');

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
						"deleteReport.do", //deleteUrl
						"${recordDeletedText}",
						"${errorOccurredText}",
						true, //deleteRow
						"${cannotDeleteRecordText}", //cannotDeleteRecordText
						"${linkedJobsExistText}" //linkedRecordsExistText
						);
				
				var table = oTable.api();

				$('#deleteRecords').click(function () {
					var selectedRows = table.rows({selected: true});
					var data = selectedRows.data();
					if (data.length > 0) {
						var ids = $.map(data, function (item) {
							return item[0];
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
										url: "${pageContext.request.contextPath}/app/deleteReports.do",
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

			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<c:if test="${error != null}">
			<div class="alert alert-danger alert-dismissable">
				<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
				<p><spring:message code="page.message.errorOccurred"/></p>
				<c:if test="${showErrors}">
					<p>${encode:forHtmlContent(error)}</p>
				</c:if>
			</div>
		</c:if>
		<c:if test="${not empty recordSavedMessage}">
			<div class="alert alert-success alert-dismissable">
				<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
				<spring:message code="${recordSavedMessage}"/>: ${encode:forHtmlContent(recordName)}
			</div>
		</c:if>

		<div id="ajaxResponse">
		</div>

		<div style="margin-bottom: 10px;">
			<a class="btn btn-default" href="${pageContext.request.contextPath}/app/addReport.do">
				<i class="fa fa-plus"></i>
				<spring:message code="page.action.add"/>
			</a>
			<button type="button" id="deleteRecords" class="btn btn-default">
				<i class="fa fa-trash-o"></i>
				<spring:message code="page.action.delete"/>
			</button>
		</div>

		<table id="reports" class="table table-bordered table-striped table-condensed">
			<thead>
				<tr>
					<th><spring:message code="page.text.id"/></th>
					<th><spring:message code="page.text.name"/></th>
					<th><spring:message code="page.text.description"/></th>
					<th><spring:message code="reports.text.status"/></th>
					<th class="noFilter"><spring:message code="page.text.action"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="report" items="${reports}">
					<tr data-name="${encode:forHtmlAttribute(report.name)}"
						data-id="${report.reportId}">

						<td>${report.reportId}</td>
						<td>${encode:forHtmlContent(report.name)} &nbsp;
							<t:displayNewLabel creationDate="${report.creationDate}"
											   updateDate="${report.updateDate}"/>
						</td>
						<td>${encode:forHtmlContent(report.description)}</td>
						<td>
							<c:choose>
								<c:when test="${report.reportStatus.value == activeStatus}">
									<span class="label label-success">
										<spring:message code="${report.reportStatus.localizedDescription}"/>
									</span>
								</c:when>
								<c:when test="${report.reportStatus.value == disabledStatus}">
									<span class="label label-danger">
										<spring:message code="${report.reportStatus.localizedDescription}"/>
									</span>
								</c:when>
								<c:otherwise>
									<span class="label label-default">
										<spring:message code="${report.reportStatus.localizedDescription}"/>
									</span>
								</c:otherwise>
							</c:choose>
						</td>
						<td>
							<div class="btn-group">
								<a class="btn btn-default" 
								   href="${pageContext.request.contextPath}/app/editReport.do?id=${report.reportId}">
									<i class="fa fa-pencil-square-o"></i>
									<spring:message code="page.action.edit"/>
								</a>
								<button type="button" class="btn btn-default deleteRecord">
									<i class="fa fa-trash-o"></i>
									<spring:message code="page.action.delete"/>
								</button>
								<a class="btn btn-default" 
								   href="${pageContext.request.contextPath}/app/copyReport.do?id=${report.reportId}">
									<i class="fa fa-copy"></i>
									<spring:message code="reports.action.copy"/>
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
										   href="${pageContext.request.contextPath}/app/reportParameterConfig.do?reportId=${report.reportId}">
											<spring:message code="reports.action.parameters"/>
										</a>
									</li>
									<li>
										<a 
										   href="${pageContext.request.contextPath}/app/reportRules.do?reportId=${report.reportId}">
											<spring:message code="reports.action.rules"/>
										</a>
									</li>
									<li>
										<a 
										   href="${pageContext.request.contextPath}/app/drilldowns.do?reportId=${report.reportId}">
											<spring:message code="reports.action.drilldowns"/>
										</a>
									</li>
									<li class="divider"></li>
									<li>
										<a 
										   href="${pageContext.request.contextPath}/app/selectReportParameters.do?reportId=${report.reportId}">
											<spring:message code="reports.action.preview"/>
										</a>
									</li>
								</ul>
							</div>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</jsp:body>
</t:mainPageWithPanel>
