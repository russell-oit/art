<%-- 
    Document   : reports
    Created on : 01-Oct-2013, 09:53:44
    Author     : Timothy Anyona

Reports page. Also main/home page
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.reports" var="pageTitle"/>

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText" javaScriptEscape="true"/>
<spring:message code="page.text.description" var="descriptionText" javaScriptEscape="true"/>
<spring:message code="reports.text.selectValue" var="selectValueText" javaScriptEscape="true"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText" javaScriptEscape="true"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-10 col-md-offset-1"
					 hasTable="true">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/yadcf-0.9.3/jquery.dataTables.yadcf.css"/>
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/yadcf-0.9.3/jquery.dataTables.yadcf.js"></script>

		<script type="text/javascript">
			$(document).ready(function () {
				$('a[href*="reports"]').parent().addClass('active');

				var tbl = $('#reports');

				var columnDefs = [
					{
						targets: "detailsCol",
						orderable: false,
						searchable: false
					},
					{
						targets: ["descriptionCol"], //target name matches class name of th.
						visible: false
					},
					{
						targets: ["reportIdCol"], //target name matches class name of th.
						visible: false
					}
				];

				var columns = [
					{"data": null, defaultContent: "", className: "details-control"},
					{"data": {
							_: "reportGroupNames2",
							filter: "reportGroupNamesFilter",
							display: "reportGroupNames2"
						}
					},
					{"data": "description2"},
					{"data": "name2"},
					{"data": "reportId"}
				];

				var oTable = tbl.dataTable({
					columnDefs: columnDefs,
					orderClasses: false,
					order: [3, "asc"], //sort by report name. 0 is the details column
					pagingType: "full_numbers",
					lengthMenu: [[10, 20, 50, -1], [10, 20, 50, "${showAllRowsText}"]],
					pageLength: 10,
					language: {
						url: "${pageContext.request.contextPath}/js/dataTables/i18n/dataTables_${pageContext.response.locale}.json"
					},
					initComplete: function () {
						datatablesInitComplete();
					},
					deferRender: true,
					ajax: {
						type: "GET",
						dataType: "json",
						url: "${pageContext.request.contextPath}/getAvailableReports",
						dataSrc: function (response) {
							//https://stackoverflow.com/questions/35475964/datatables-ajax-call-error-handle
							if (response.success) {
								return response.data;
							} else {
								notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
								return "";
							}
						},
						error: function (xhr) {
							showUserAjaxError(xhr, '${errorOccurredText}');
						}
					},
					columns: columns,
					autoWidth: false,
					drawCallback: function () {
						initializeButtonHover();
					}
				});

				//get datatables api object
				var table = oTable.api();

				//add thead row with yadcf filters
				var headingRow = tbl.find('thead tr:first');
				var visibleColCount = 3;
				var cols = '';
				for (var i = 1; i <= visibleColCount; i++) {
					cols += '<th></th>';
				}
				var filterRow = '<tr>' + cols + '</tr>';
				headingRow.after(filterRow);

				yadcf.init(table,
						[
							{
								column_number: 1,
								filter_default_label: '${selectValueText}',
								text_data_delimiter: ", ",
								select_value_delimiter: ";",
								filter_match_mode: "regex",
								case_insensitive: false,
								sort_as: "alphaNum"
							},
							{
								column_number: 3,
								filter_type: 'text',
								filter_default_label: ""
							}
						],
						{filters_tr_index: 1}
				);

			<c:if test="${not empty sessionUser.effectiveDefaultReportGroup}">
				yadcf.exFilterColumn(oTable, [[1, '${encode:forHtml(sessionUser.effectiveDefaultReportGroup.name)}']]);
			</c:if>

				//show/hide details
				//http://datatables.net/examples/server_side/row_details.html

				// Array to track the ids of the details displayed rows
				var detailRows = [];

				tbl.find('tbody').on('click', 'tr td:first-child', function () {
					var tr = $(this).closest('tr');
					var row = table.row(tr);
					var idx = $.inArray(tr, detailRows);

					if (row.child.isShown()) {
						tr.removeClass('details');
						row.child.hide();

						// Remove from the 'open' array
						detailRows.splice(idx, 1);
					} else {
						tr.addClass('details');
						row.child(formatDetails(row.data()), 'details').show(); //add details class to child row td

						// Add to the 'open' array
						if (idx === -1) {
							detailRows.push(tr);
						}
					}
				});

				$("#refreshRecords").on("click", function () {
					table.ajax.reload();
				});

				$('#ajaxResponseContainer').on("click", ".alert .close", function () {
					$(this).parent().hide();
				});

			});

			/* Formating function for row details */
			function formatDetails(data) {
				var descriptionText = "${descriptionText}";
				return '<div class="details">' + descriptionText + ': '
						+ data.description2 + '</div>';
			}
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

		<div id="ajaxResponseContainer">
			<div id="ajaxResponse">
			</div>
		</div>

		<div style="margin-bottom: 10px; text-align: right">
			<button type="button" id="refreshRecords" class="btn btn-default">
				<i class="fa fa-refresh"></i>
				<spring:message code="page.action.refresh"/>
			</button>
		</div>

		<table id="reports" class="expandable table table-bordered">
			<thead>
				<tr>
					<th class="detailsCol noFilter"></th> <%-- details control column --%>
					<th class="reportGroupCol"><spring:message code="reports.text.groupName"/></th>
					<th class="descriptionCol noFilter"></th> <%-- description column. hidden --%>
					<th><spring:message code="reports.text.reportName"/></th>
					<th class="reportIdCol noFilter"></th> <%-- report id column. hidden --%>
				</tr>
			</thead>
		</table>
	</jsp:body>
</t:mainPageWithPanel>
