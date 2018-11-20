<%-- 
    Document   : reportRules
    Created on : 25-Apr-2014, 17:17:20
    Author     : Timothy Anyona

Display report filters
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.reportRules" var="pageTitle"/>

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="dialog.button.cancel" var="cancelText"/>
<spring:message code="dialog.button.ok" var="okText"/>
<spring:message code="dialog.message.deleteRecord" var="deleteRecordText"/>
<spring:message code="page.message.recordDeleted" var="recordDeletedText"/>
<spring:message code="page.message.recordsDeleted" var="recordsDeletedText"/>
<spring:message code="dialog.message.selectRecords" var="selectRecordsText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-8 col-md-offset-2"
					 hasTable="true" hasNotify="true">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/Select-1.2.0/css/select.bootstrap.min.css"/>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/css/buttons.dataTables.min.css"/>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/css/buttons.bootstrap.min.css"/>
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Select-1.2.0/js/dataTables.select.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/js/dataTables.buttons.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/js/buttons.bootstrap.min.js"></script>

		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="reportsConfig"]').parent().addClass('active');

				//{container: 'body'} needed if tooltips shown on input-group element or button
				$("[data-toggle='tooltip']").tooltip({container: 'body'});

				var tbl = $('#reportRules');

				var oTable = tbl.dataTable({
					columnDefs: [
						{orderable: true, targets: 1},
						{orderable: false, targets: '_all'},
						{
							orderable: false,
							className: 'select-checkbox',
							targets: 0
						}
					],
					order: [[1, 'asc']],
					dom: 'lBfrtip',
					buttons: [
						'selectAll',
						'selectNone'
					],
					select: {
						style: 'multi',
						selector: 'td:first-child'
					},
					orderClasses: false,
					pagingType: "full_numbers",
					lengthMenu: [[10, 20, 50, -1], [10, 20, 50, "${showAllRowsText}"]],
					pageLength: 10,
					language: {
						url: "${pageContext.request.contextPath}/js/dataTables/i18n/dataTables_${pageContext.response.locale}.json"
					},
					initComplete: datatablesInitComplete
				});

				tbl.find('tbody').on('click', '.deleteRecord', function () {
					var row = $(this).closest("tr"); //jquery object
					var recordName = escapeHtmlContent(row.attr("data-name"));
					var recordId = row.data("id");
					bootbox.confirm({
						message: "${deleteRecordText}: <b>" + recordName + "</b>",
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
									url: "${pageContext.request.contextPath}/deleteReportRule",
									data: {id: recordId},
									success: function (response) {
										if (response.success) {
											table.row(row).remove().draw(false); //draw(false) to prevent datatables from going back to page 1
											notifyActionSuccessReusable("${recordDeletedText}", recordName);
										} else {
											notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
										}
									},
									error: ajaxErrorHandler
								});
							} //end if result
						} //end callback
					}); //end bootbox confirm
				});

				var table = oTable.api();

				$('#deleteRecords').on("click", function () {
					var selectedRows = table.rows({selected: true});
					var data = selectedRows.data();
					if (data.length > 0) {
						var ids = $.map(data, function (item) {
							return item[1];
						});
//						var ids = [];
//						for (var i = 0; i < table.rows('.selected').data().length; i++) {
//							ids.push(table.rows('.selected').nodes()[i].attributes["data-name"].value);
//						}
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
										url: "${pageContext.request.contextPath}/deleteReportRules",
										data: {ids: ids},
										success: function (response) {
											if (response.success) {
												selectedRows.remove().draw(false);
												notifyActionSuccessReusable("${recordsDeletedText}", ids);
											} else {
												notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
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

				$('#ajaxResponseContainer').on("click", ".alert .close", function () {
					$(this).parent().hide();
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

		<div id="ajaxResponseContainer">
			<div id="ajaxResponse">
			</div>
		</div>

		<div class="text-center">
			<p>
				<b><spring:message code="page.text.report"/>:</b> ${reportName}
			</p>
		</div>
		<div style="margin-bottom: 10px;">
			<a class="btn btn-default" href="${pageContext.request.contextPath}/addRule?reportId=${reportId}">
				<i class="fa fa-plus"></i>
				<spring:message code="page.button.addNew"/>
			</a>
			<a class="btn btn-default" href="${pageContext.request.contextPath}/addReportRule?reportId=${reportId}">
				<i class="fa fa-plus"></i>
				<spring:message code="page.button.addExisting"/>
			</a>
			<button type="button" id="deleteRecords" class="btn btn-default">
				<i class="fa fa-trash-o"></i>
				<spring:message code="page.action.delete"/>
			</button>
			<a class="btn btn-default" href="${pageContext.request.contextPath}/reportConfig?reportId=${reportId}">
				<spring:message code="page.text.report"/>
			</a>
		</div>

		<table id="reportRules" class="table table-bordered table-striped table-condensed">
			<thead>
				<tr>
					<th class="noFilter"></th>
					<th><spring:message code="page.text.id"/></th>
					<th><spring:message code="page.text.rule"/></th>
					<th><spring:message code="page.text.reportColumn"/></th>
					<th class="noFilter"><spring:message code="page.text.action"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="reportRule" items="${reportRules}">
					<tr data-id="${reportRule.reportRuleId}" 
						data-name="${encode:forHtmlAttribute(reportRule.rule.name)}">

						<td></td>
						<td>${reportRule.reportRuleId}</td>
						<td>
							<a href="${pageContext.request.contextPath}/editRule?id=${reportRule.rule.ruleId}&returnReportId=${reportRule.reportId}">
								${encode:forHtmlContent(reportRule.rule.name)}
							</a>
						</td>							
						<td>${encode:forHtmlContent(reportRule.reportColumn)}</td>
						<td>
							<div class="btn-group">
								<a class="btn btn-default" 
								   href="${pageContext.request.contextPath}/editReportRule?id=${reportRule.reportRuleId}">
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
