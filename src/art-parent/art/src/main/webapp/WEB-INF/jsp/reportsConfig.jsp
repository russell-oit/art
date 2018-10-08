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
<spring:message code="reports.text.selectValue" var="selectValueText"/>
<spring:message code="page.message.recordUpdated" var="recordUpdatedText"/>
<spring:message code="select.text.nothingSelected" var="nothingSelectedText"/>
<spring:message code="select.text.noResultsMatch" var="noResultsMatchText"/>
<spring:message code="select.text.selectedCount" var="selectedCountText"/>
<spring:message code="select.text.selectAll" var="selectAllText"/>
<spring:message code="select.text.deselectAll" var="deselectAllText"/>
<spring:message code="switch.text.yes" var="yesText"/>
<spring:message code="switch.text.no" var="noText"/>
<spring:message code="reports.label.reportSource" var="reportSourceText"/>

<t:mainConfigPage title="${pageTitle}" mainColumnClass="col-md-12">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/yadcf-0.9.2/jquery.dataTables.yadcf.css"/>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/yadcf.css"/>
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/yadcf-0.9.2/jquery.dataTables.yadcf.js"></script>

		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="reportsConfig"]').parent().addClass('active');

				var tbl = $('#reports');

				var oTable = tbl.dataTable({
					orderClasses: false,
					order: [[1, 'asc']],
					deferRender: true,
					pagingType: "full_numbers",
					lengthMenu: [[5, 10, 25, -1], [5, 10, 25, '${showAllRowsText}']],
					pageLength: 10,
					ajax: {
						type: "GET",
						dataType: "json",
						url: "${pageContext.request.contextPath}/getConfigReports",
						dataSrc: function (response) {
							//https://stackoverflow.com/questions/35475964/datatables-ajax-call-error-handle
							if (response.success) {
								return response.data;
							} else {
								notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
								return "";
							}
						},
						error: ajaxErrorHandler
					},
					columns: [
						{"data": null, defaultContent: ""},
						{"data": "reportId"},
						{"data": "name"},
						{"data": "reportGroupNamesHtml"},
						{"data": "description"},
						{"data": "dtActiveStatus"},
						{"data": "dtAction", width: '370px'}
					],
					//https://datatables.net/reference/option/rowId
					rowId: "dtRowId",
					autoWidth: false,
					columnDefs: [
						{
							targets: 0,
							orderable: false,
							className: 'select-checkbox'
						},
						{
							targets: "dtHidden", //target name matches class name of th.
							visible: false
						}
					],
					dom: 'lBfrtip',
					buttons: [
						'selectAll',
						'selectNone',
						{
							extend: 'colvis',
							postfixButtons: ['colvisRestore']
						},
						{
							extend: 'excel',
							exportOptions: {
								columns: ':visible'
							}
						},
						{
							extend: 'pdf',
							exportOptions: {
								columns: ':visible'
							}
						},
						{
							extend: 'print',
							exportOptions: {
								columns: ':visible'
							}
						}
					],
					select: {
						style: 'multi',
						selector: 'td:first-child'
					},
					language: {
						url: "${pageContext.request.contextPath}/js/dataTables/i18n/dataTables_${pageContext.response.locale}.json"
					},
					createdRow: function (row, data, dataIndex) {
						$(row).attr('data-id', data.reportId);
						$(row).attr('data-name', data.name);
					},
					drawCallback: function () {
						$('button.dropdown-toggle').dropdownHover({
							delay: 100
						});
					},
					initComplete: function () {
						$('div.dataTables_filter input').focus();
					}
				});

				var table = oTable.api();

				yadcf.init(table,
						[
							{
								column_number: 1,
								filter_type: 'text',
								filter_default_label: "",
								style_class: "yadcf-id-filter"
							},
							{
								column_number: 2,
								filter_type: 'text',
								filter_default_label: "",
								style_class: "yadcf-report-name-filter"
							},
							{
								column_number: 3,
								filter_default_label: '${selectValueText}',
								text_data_delimiter: ","
							},
							{
								column_number: 4,
								filter_type: 'text',
								filter_default_label: "",
								style_class: "yadcf-report-description-filter"
							},
							{
								column_number: 5,
								filter_default_label: '${selectValueText}',
								column_data_type: "html",
								html_data_type: "text"
							}
						]
						);

				tbl.find('tbody').on('click', '.deleteRecord', function () {
					var row = $(this).closest("tr"); //jquery object
					var recordName = escapeHtmlContent(row.data("name"));
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
									url: "${pageContext.request.contextPath}/deleteReport",
									data: {id: recordId},
									success: function (response) {
										if (response.success) {
											table.row(row).remove().draw(false); //draw(false) to prevent datatables from going back to page 1
											notifyActionSuccess("${recordDeletedText}", recordName);
										} else {
											notifyActionError("${errorOccurredText}", escapeHtmlContent(response.errorMessage));
										}
									},
									error: ajaxErrorHandler
								});
							} //end if result
						} //end callback
					}); //end bootbox confirm
				});

				$('#deleteRecords').click(function () {
					var selectedRows = table.rows({selected: true});
					var data = selectedRows.data();
					if (data.length > 0) {
						var ids = $.map(data, function (item) {
							return item.reportId;
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
												notifyActionSuccessReusable("${recordsDeletedText}", ids);
											} else if (nonDeletedRecords !== null && nonDeletedRecords.length > 0) {
												notifySomeRecordsNotDeletedReusable(nonDeletedRecords, "${someRecordsNotDeletedText}");
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

				$('#editRecords').click(function () {
					var selectedRows = table.rows({selected: true});
					var data = selectedRows.data();
					if (data.length > 0) {
						var ids = $.map(data, function (item) {
							return item.reportId;
						});
						window.location.href = '${pageContext.request.contextPath}/editReports?ids=' + ids;
					} else {
						bootbox.alert("${selectRecordsText}");
					}
				});

				$('#exportRecords').click(function () {
					var selectedRows = table.rows({selected: true});
					var data = selectedRows.data();
					if (data.length > 0) {
						var ids = $.map(data, function (item) {
							return item.reportId;
						});
						window.location.href = '${pageContext.request.contextPath}/exportRecords?type=Reports&ids=' + ids;
					} else {
						bootbox.alert("${selectRecordsText}");
					}
				});

				$("#refreshRecords").click(function () {
					table.ajax.reload();
				});

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

		<div style="margin-bottom: 10px;">
			<div class="btn-group">
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
				<button type="button" id="refreshRecords" class="btn btn-default">
					<i class="fa fa-refresh"></i>
					<spring:message code="page.action.refresh"/>
				</button>
			</div>
			<c:if test="${sessionUser.hasPermission('migrate_records')}">
				<div class="btn-group">
					<a class="btn btn-default" href="${pageContext.request.contextPath}/importRecords?type=Reports">
						<spring:message code="page.text.import"/>
					</a>
					<button type="button" id="exportRecords" class="btn btn-default">
						<spring:message code="page.text.export"/>
					</button>
				</div>
			</c:if>
		</div>

		<div class="table-responsive">
			<table id="reports" class="table table-bordered table-striped table-condensed" style='width: 100%'>
				<thead>
					<tr>
						<th class="noFilter"></th>
						<th><spring:message code="page.text.id"/><p></p></th>
						<th><spring:message code="page.text.name"/><p></p></th>
						<th><spring:message code="reports.text.groupName"/><p></p></th>
						<th><spring:message code="page.text.description"/><p></p></th>
						<th><spring:message code="page.text.active"/><p></p></th>
						<th class="noFilter"><spring:message code="page.text.action"/><p></p></th>
					</tr>
				</thead>
			</table>
		</div>
	</jsp:body>
</t:mainConfigPage>
