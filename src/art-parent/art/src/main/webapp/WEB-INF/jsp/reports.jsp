<%-- 
    Document   : reports
    Created on : 01-Oct-2013, 09:53:44
    Author     : Timothy Anyona

Reports page. Also main/home page
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.reports" var="pageTitle"/>

<spring:message code="datatables.text.showAllRows" var="showAllRowsText"/>
<spring:message code="page.text.description" var="descriptionText"/>
<spring:message code="page.text.reports" var="mainPanelTitle"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-8 col-md-offset-2">

	<jsp:attribute name="javascript">
		<script type="text/javascript" charset="utf-8">
			$(document).ready(function() {
				$(function() {
					$('a[href*="reports.do"]').parent().addClass('active');
				});

				var tbl = $('#reports');
				
				var columnFilterRow = createColumnFilters(tbl);

				var oTable = tbl.dataTable({
					columnDefs: [
						{
							targets: "detailsCol",
							orderable: false,
							searchable: false
						},
						{
							targets: ["descriptionCol"], //target name matches class name of th.
							visible: false
						}
					],
					orderClasses: false,
					order: [3, "asc"], //sort by report name. 0 is the details column
					pagingType: "full_numbers",
					lengthMenu: [[5, 10, 25, -1], [5, 10, 25, "${showAllRowsText}"]],
					pageLength: 10,
					language: {
						url: "${pageContext.request.contextPath}/js/dataTables-1.10.11/i18n/dataTables_${pageContext.response.locale}.txt"
					},
					initComplete: function() {
						$('div.dataTables_filter input').focus();
					}
				});
				
				//move column filter row after heading row
				columnFilterRow.insertAfter(columnFilterRow.next());

				//get datatables api object
				var table = oTable.api();
				
				// Apply the column filter
				applyColumnFilters(tbl, table);

				//show/hide details
				//http://datatables.net/examples/server_side/row_details.html

				// Array to track the ids of the details displayed rows
				var detailRows = [];

				tbl.find('tbody').on('click', 'tr td:first-child', function() {
					var tr = $(this).closest('tr');
					var row = table.row(tr);
					var idx = $.inArray(tr, detailRows);

					if (row.child.isShown()) {
						tr.removeClass('details');
						row.child.hide();

						// Remove from the 'open' array
						detailRows.splice(idx, 1);
					}
					else {
						tr.addClass('details');
						row.child(formatDetails(row.data()), 'details').show(); //add details class to child row td

						// Add to the 'open' array
						if (idx === -1) {
							detailRows.push(tr);
						}
					}
				});

			});

			/* Formating function for row details */
			function formatDetails(data) {
				var descriptionText = "${descriptionText}";
				return '<div class="details">' + descriptionText + ': '
						+ data[2] + '</div>';
			}

		</script>
	</jsp:attribute>

	<jsp:attribute name="aboveMainPanel">
		<c:if test="${error != null}">
			<div class="alert alert-danger alert-dismissable">
				<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
				<p><spring:message code="page.message.errorOccurred"/></p>
				<p><encode:forHtmlContent value="${error}"/></p>
			</div>
		</c:if>
	</jsp:attribute>

	<jsp:body>
		<table id="reports" class="expandable table table-bordered">
			<thead>
				<tr>
					<th class="detailsCol noFilter"></th> <%-- details control column --%>
					<th class="reportGroupCol"><spring:message code="reports.text.groupName"/></th> <%-- group name. --%>
					<th class="descriptionCol"></th> <%-- description column. hidden --%>
					<th><spring:message code="reports.text.reportName"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="report" items="${reports}">
					<tr>
						<td class="details-control"></td> <%-- details control column --%>
						<td><encode:forHtmlContent value="${report.reportGroup.name}"/></td>
						<td><encode:forHtmlContent value="${report.description}"/></td>
						<td>
							<a href="${pageContext.request.contextPath}/app/selectReportParameters.do?reportId=${report.reportId}">
								<encode:forHtmlContent value="${report.name}"/>
							</a> &nbsp;
							<t:displayNewLabel creationDate="${report.creationDate}"
											   updateDate="${report.updateDate}"/>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</jsp:body>
</t:mainPageWithPanel>
