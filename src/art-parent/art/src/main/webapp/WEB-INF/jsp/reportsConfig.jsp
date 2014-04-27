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

<spring:message code="datatables.text.showAllRows" var="dataTablesAllRowsText"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="dialog.button.cancel" var="cancelText"/>
<spring:message code="dialog.button.ok" var="okText"/>
<spring:message code="dialog.message.deleteRecord" var="deleteRecordText"/>
<spring:message code="page.message.recordDeleted" var="recordDeletedText"/>
<spring:message code="reports.message.linkedJobsExist" var="linkedJobsExistText"/>
<spring:message code="page.message.cannotDeleteRecord" var="cannotDeleteRecordText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-10 col-md-offset-1">

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/notify-combined-0.3.1.min.js"></script>
		<script type="text/javascript" charset="utf-8">
			$(document).ready(function() {
				$(function() {
					$('a[id="configure"]').parent().addClass('active');
					$('a[href*="reportsConfig.do"]').parent().addClass('active');
				});
				var oTable = $('#reports').dataTable({
					"sPaginationType": "bs_full",
					"aaSorting": [],
					"aLengthMenu": [[5, 10, 25, -1], [5, 10, 25, "${dataTablesAllRowsText}"]],
					"iDisplayLength": -1,
					"oLanguage": {
						"sUrl": "${pageContext.request.contextPath}/js/dataTables-1.9.4/i18n/dataTables_${pageContext.response.locale}.txt"
					},
					"fnInitComplete": function() {
						$('div.dataTables_filter input').focus();
					}
				});

				$('#reports tbody').on('click', '.delete', function() {
					var row = $(this).closest("tr"); //jquery object
					var nRow = row[0]; //dom element/node
					var name = escapeHtmlContent(row.data("name"));
					var id = row.data("id");
					bootbox.confirm({
						message: "${deleteRecordText}: <b>" + name + "</b>",
						buttons: {
							'cancel': {
								label: "${cancelText}"
							},
							'confirm': {
								label: "${okText}"
							}
						},
						callback: function(result) {
							if (result) {
								$.ajax({
									type: "POST",
									dataType: "json",
									url: "${pageContext.request.contextPath}/app/deleteReport.do",
									data: {id: id},
									success: function(response) {
										var msg;
										var linkedJobs = response.data;
										if (response.success) {
											oTable.fnDeleteRow(nRow);

											msg = alertCloseButton + "${recordDeletedText}: " + name;
											$("#ajaxResponse").attr("class", "alert alert-success alert-dismissable").html(msg);
											$.notify("${recordDeletedText}", "success");
										} else if (linkedJobs.length > 0) {
											msg = alertCloseButton + "${linkedJobsExistText}" + "<ul>";

											$.each(linkedJobs, function(index, value) {
												msg += "<li>" + value + "</li>";
											});

											msg += "</ul>";

											$("#ajaxResponse").attr("class", "alert alert-danger alert-dismissable").html(msg);
											$.notify("${cannotDeleteRecordText}", "error");
										} else {
											msg = alertCloseButton + "<p>${errorOccurredText}</p><p>" + escapeHtmlContent(response.errorMessage) + "</p>";
											$("#ajaxResponse").attr("class", "alert alert-danger alert-dismissable").html(msg);
											$.notify("${errorOccurredText}", "error");
										}
									},
									error: function(xhr, status, error) {
										bootbox.alert(xhr.responseText);
									}
								});
							}
						}
					});
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
		</div>

		<table id="reports" class="table table-bordered table-striped table-condensed">
			<thead>
				<tr>
					<th><spring:message code="page.text.id"/></th>
					<th><spring:message code="page.text.name"/></th>
					<th><spring:message code="page.text.description"/></th>
					<th><spring:message code="reports.text.status"/></th>
					<th><spring:message code="page.text.action"/></th>
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
								<button type="button" class="btn btn-default delete">
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
									<spring:message code="reports.action.extras"/>
									<span class="caret"></span>
								</button>
								<ul class="dropdown-menu">
									<li>
										<a 
										   href="${pageContext.request.contextPath}/app/reportParameters.do?reportId=${report.reportId}">
											<i class="fa fa-paperclip"></i>
											<spring:message code="reports.action.parameters"/>
										</a>
									</li>
									<li>
										<a 
										   href="${pageContext.request.contextPath}/app/reportFilters.do?reportId=${report.reportId}">
											<i class="fa fa-filter"></i>
											<spring:message code="reports.action.filters"/>
										</a>
									</li>
									<li>
										<a 
										   href="${pageContext.request.contextPath}/app/drilldowns.do?reportId=${report.reportId}">
											<i class="fa fa-level-down"></i>
											<spring:message code="reports.action.drilldowns"/>
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
