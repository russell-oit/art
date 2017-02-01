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
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<spring:message code="page.title.reportsConfiguration" var="pageTitle"/>

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText"/>
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

		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="reportsConfig"]').parent().addClass('active');

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
						"deleteReport", //deleteUrl
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
										url: "${pageContext.request.contextPath}/deleteReports",
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

				$('#editRecords').click(function () {
					var selectedRows = table.rows({selected: true});
					var data = selectedRows.data();
					if (data.length > 0) {
						var ids = $.map(data, function (item) {
							return item[1];
						});
						window.location.href = '${pageContext.request.contextPath}/editReports?ids=' + ids;
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
				<c:if test="${record != null}">
					&nbsp;
					<div class="btn-group">
						<a class="btn btn-default" 
						   href="${pageContext.request.contextPath}/editReport?id=${record.reportId}">
							<spring:message code="page.action.edit"/>
						</a>
					</div>
					<div class="btn-group">
						<a class="btn btn-default"
						   href="${pageContext.request.contextPath}/reportParameterConfig?reportId=${record.reportId}">
							<spring:message code="reports.action.parameters"/>
						</a>
						<a class="btn btn-default"
						   href="${pageContext.request.contextPath}/reportRules?reportId=${record.reportId}">
							<spring:message code="reports.action.rules"/>
						</a>
						<a class="btn btn-default"
						   href="${pageContext.request.contextPath}/drilldowns?reportId=${record.reportId}">
							<spring:message code="reports.action.drilldowns"/>
						</a>
					</div>
					<c:if test="${record.reportType != 'LovStatic'}">
						<div class="btn-group">
							<a class="btn btn-default"
							   href="${pageContext.request.contextPath}/addJob?reportId=${record.reportId}">
								<spring:message code="reports.action.schedule"/>
							</a>
							<a class="btn btn-default"
							   href="${pageContext.request.contextPath}/selectReportParameters?reportId=${record.reportId}">
								<spring:message code="reports.action.preview"/>
							</a>
						</div>
					</c:if>
				</c:if>
			</div>
		</c:if>

		<div id="ajaxResponse">
		</div>

		<div style="margin-bottom: 10px;">
			<a class="btn btn-default" href="${pageContext.request.contextPath}/addReport">
				<i class="fa fa-plus"></i>
				<spring:message code="page.action.add"/>
			</a>
			<button type="button" id="editRecords" class="btn btn-default">
				<i class="fa fa-pencil-square-o"></i>
				<spring:message code="page.action.edit"/>
			</button>
			<button type="button" id="deleteRecords" class="btn btn-default">
				<i class="fa fa-trash-o"></i>
				<spring:message code="page.action.delete"/>
			</button>
		</div>

		<table id="reports" class="table table-bordered table-striped table-condensed">
			<thead>
				<tr>
					<th class="noFilter"></th>
					<th><spring:message code="page.text.id"/></th>
					<th><spring:message code="page.text.name"/></th>
					<th><spring:message code="page.text.description"/></th>
					<th><spring:message code="page.text.active"/></th>
					<th class="dtHidden"><spring:message code="page.text.createdBy"/></th>
					<th class="dtHidden"><spring:message code="page.text.creationDate"/></th>
					<th class="dtHidden"><spring:message code="page.text.updatedBy"/></th>
					<th class="dtHidden"><spring:message code="page.text.updateDate"/></th>
					<th class="noFilter"><spring:message code="page.text.action"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="report" items="${reports}">
					<tr data-name="${encode:forHtmlAttribute(report.name)}"
						data-id="${report.reportId}">

						<td></td>
						<td>${report.reportId}</td>
						<td>${encode:forHtmlContent(report.name)} &nbsp;
							<t:displayNewLabel creationDate="${report.creationDate}"
											   updateDate="${report.updateDate}"/>
						</td>
						<td>${encode:forHtmlContent(report.description)}</td>
						<td><t:displayActiveStatus active="${report.active}"
											   activeText="${activeText}"
											   disabledText="${disabledText}"/>
						</td>
						<td>${encode:forHtmlContent(report.createdBy)}</td>
						<td data-sort="${report.creationDate.time}">
							<fmt:formatDate value="${report.creationDate}" pattern="${dateDisplayPattern}"/>
						</td>
						<td>${encode:forHtmlContent(report.updatedBy)}</td>
						<td data-sort="${report.updateDate.time}">
							<fmt:formatDate value="${report.updateDate}" pattern="${dateDisplayPattern}"/>
						</td>
						<td>
							<div class="btn-group">
								<a class="btn btn-default" 
								   href="${pageContext.request.contextPath}/editReport?id=${report.reportId}">
									<i class="fa fa-pencil-square-o"></i>
									<spring:message code="page.action.edit"/>
								</a>
								<button type="button" class="btn btn-default deleteRecord">
									<i class="fa fa-trash-o"></i>
									<spring:message code="page.action.delete"/>
								</button>
								<a class="btn btn-default" 
								   href="${pageContext.request.contextPath}/copyReport?id=${report.reportId}">
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
											href="${pageContext.request.contextPath}/reportParameterConfig?reportId=${report.reportId}">
											<spring:message code="reports.action.parameters"/>
										</a>
									</li>
									<li>
										<a 
											href="${pageContext.request.contextPath}/reportRules?reportId=${report.reportId}">
											<spring:message code="reports.action.rules"/>
										</a>
									</li>
									<li>
										<a 
											href="${pageContext.request.contextPath}/drilldowns?reportId=${report.reportId}">
											<spring:message code="reports.action.drilldowns"/>
										</a>
									</li>
									<c:if test="${report.reportType != 'LovStatic'}">
										<li class="divider"></li>
										<li>
											<a 
												href="${pageContext.request.contextPath}/addJob?reportId=${report.reportId}">
												<spring:message code="reports.action.schedule"/>
											</a>
										</li>
										<li>
											<a 
												href="${pageContext.request.contextPath}/selectReportParameters?reportId=${report.reportId}">
												<spring:message code="reports.action.preview"/>
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
</t:mainPageWithPanel>
