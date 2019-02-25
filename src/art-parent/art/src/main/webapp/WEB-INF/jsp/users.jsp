<%-- 
    Document   : users
    Created on : 05-Nov-2013, 14:58:19
    Author     : Timothy Anyona

Display user configuration page
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<spring:message code="page.title.users" var="pageTitle"/>

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="dialog.button.cancel" var="cancelText"/>
<spring:message code="dialog.button.ok" var="okText"/>
<spring:message code="dialog.message.deleteRecord" var="deleteRecordText"/>
<spring:message code="page.message.recordDeleted" var="recordDeletedText"/>
<spring:message code="users.message.linkedJobsExist" var="linkedJobsExistText"/>
<spring:message code="page.message.cannotDeleteRecord" var="cannotDeleteRecordText"/>
<spring:message code="page.message.recordsDeleted" var="recordsDeletedText"/>
<spring:message code="dialog.message.selectRecords" var="selectRecordsText"/>
<spring:message code="page.message.someRecordsNotDeleted" var="someRecordsNotDeletedText"/>
<spring:message code="reports.text.selectValue" var="selectValueText"/>
<spring:message code="activeStatus.option.active" var="activeText"/>
<spring:message code="activeStatus.option.disabled" var="disabledText"/>

<t:mainPageWithPanel title="${pageTitle}" configPage="true">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/yadcf-0.9.3/jquery.dataTables.yadcf.css"/>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/yadcf.css"/>
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/yadcf-0.9.3/jquery.dataTables.yadcf.js"></script>

		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="users"]').parent().addClass('active');

				var tbl = $('#users');

				var pageLength = undefined; //pass undefined to use the default
				var showAllRowsText = "${showAllRowsText}";
				var contextPath = "${pageContext.request.contextPath}";
				var localeCode = "${pageContext.response.locale}";
				var dataUrl = "getUsers";
				var errorOccurredText = "${errorOccurredText}";
				var showErrors = ${showErrors};
				var columnDefs = undefined;

				var activeSpan = "<span class='label label-success'>${activeText}</span>";
				var disabledSpan = "<span class='label label-danger'>${disabledText}</span>";
				var columns = [
					{"data": null, defaultContent: ""},
					{"data": "userId"},
					{"data": "username2"},
					{"data": function (row, type, val, meta) {
							return escapeHtmlContent(row.fullName);
						}
					},
					{"data": function (row, type, val, meta) {
							if (row.active) {
								return activeSpan;
							} else {
								return disabledSpan;
							}
						}
					},
					{"data": "dtAction", width: '370px'}
				];

				//initialize datatable
				var oTable = initAjaxTable(tbl,
						pageLength,
						showAllRowsText,
						contextPath,
						localeCode,
						dataUrl,
						errorOccurredText,
						showErrors,
						columnDefs,
						columns
						);

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
								filter_type: 'text',
								filter_default_label: "",
								style_class: "yadcf-report-description-filter"
							},
							{
								column_number: 4,
								filter_default_label: '${selectValueText}',
								//https://github.com/vedmack/yadcf/issues/95
								column_data_type: "rendered_html"
							}
						]
						);

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
									url: "${pageContext.request.contextPath}/deleteUser",
									data: {id: recordId},
									success: function (response) {
										if (response.success) {
											table.row(row).remove().draw(false); //draw(false) to prevent datatables from going back to page 1
											notifyActionSuccessReusable("${recordDeletedText}", recordName);
										} else {
											notifyActionErrorReusable("${errorOccurredText}", escapeHtmlContent(response.errorMessage));
										}
									},
									error: ajaxErrorHandler
								});
							} //end if result
						} //end callback
					}); //end bootbox confirm
				});

				$('#deleteRecords').on("click", function () {
					var selectedRows = table.rows({selected: true});
					var data = selectedRows.data();
					if (data.length > 0) {
						var ids = $.map(data, function (item) {
							return item.userId;
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
										url: "${pageContext.request.contextPath}/deleteUsers",
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

				$('#editRecords').on("click", function () {
					var selectedRows = table.rows({selected: true});
					var data = selectedRows.data();
					if (data.length > 0) {
						var ids = $.map(data, function (item) {
							return item.userId;
						});
						window.location.href = '${pageContext.request.contextPath}/editUsers?ids=' + ids;
					} else {
						bootbox.alert("${selectRecordsText}");
					}
				});

				$('#exportRecords').on("click", function () {
					var selectedRows = table.rows({selected: true});
					var data = selectedRows.data();
					if (data.length > 0) {
						var ids = $.map(data, function (item) {
							return item.userId;
						});
						window.location.href = '${pageContext.request.contextPath}/exportRecords?type=Users&ids=' + ids;
					} else {
						bootbox.alert("${selectRecordsText}");
					}
				});

				$("#refreshRecords").on("click", function () {
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
				<a class="btn btn-default" href="${pageContext.request.contextPath}/addUser">
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
					<a class="btn btn-default" href="${pageContext.request.contextPath}/importRecords?type=Users">
						<spring:message code="page.text.import"/>
					</a>
					<button type="button" id="exportRecords" class="btn btn-default">
						<spring:message code="page.text.export"/>
					</button>
				</div>
			</c:if>
		</div>

		<%-- https://stackoverflow.com/questions/26500010/responsive-bootstrap-datatable-not-collapsing-columns-at-the-correct-point --%>
		<table id="users" class="table table-bordered table-striped table-condensed">
			<thead>
				<tr>
					<th class="noFilter"></th>
					<th><spring:message code="page.text.id"/><p></p></th>
					<th><spring:message code="users.text.username"/><p></p></th>
					<th><spring:message code="users.text.fullName"/><p></p></th>
					<th><spring:message code="page.text.active"/><p></p></th>
					<th class="noFilter actionCol"><spring:message code="page.text.action"/><p></p></th>
				</tr>
			</thead>
		</table>
	</jsp:body>
</t:mainPageWithPanel>
