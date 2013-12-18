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
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<spring:message code="page.title.reports" var="pageTitle" scope="page"/>

<spring:message code="datatables.text.showAllRows" var="dataTablesAllRowsText" scope="page"/>

<t:mainPage title="${pageTitle}">
	<jsp:attribute name="javascript">
		<script type="text/javascript">
			//put jstl variables into js variables
			var allRowsText = "${dataTablesAllRowsText}";
			var contextPath = "${pageContext.request.contextPath}";
			var localeCode = "${pageContext.response.locale}";
			var imagesPath = contextPath + "/images/";
		</script>
		<script type="text/javascript" charset="utf-8">
			$(document).ready(function() {
				$(function() {
					$('a[href*="reports.do"]').parent().addClass('active');
				});

				// Insert a 'details' column to the table
				//must be done before datatables initialisation
				var nCloneTh = document.createElement('th');
				var nCloneTd = document.createElement('td');
				nCloneTd.innerHTML = '<img src="' + imagesPath + 'details_open.png">';
				nCloneTd.className = "centered";

				$('#reports thead tr').each(function() {
					this.insertBefore(nCloneTh, this.childNodes[0]);
				});

				$('#reports tbody tr').each(function() {
					this.insertBefore(nCloneTd.cloneNode(true), this.childNodes[0]);

				});

				//Initialise DataTables, with no sorting on the 'details' column (column [0])
				var oTable = $('#reports').dataTable({
					"sPaginationType": "bs_full",
					"aLengthMenu": [[5, 10, 25, -1], [5, 10, 25, "${dataTablesAllRowsText}"]],
					"iDisplayLength": 10,
					"oLanguage": {
						"sUrl": "${pageContext.request.contextPath}/dataTables/dataTables_${pageContext.response.locale}.txt"
					},
					"aaSorting": [[3, "asc"]],
					'aoColumnDefs': [
						{"bSortable": false, "aTargets": [0]},
						{"bVisible": false, "aTargets": [1, 2]}
					]
				});
				/* Add event listener for opening and closing details
				 * Note that the indicator for showing which row is open is not controlled by DataTables,
				 * rather it is done here
				 */
				$('#reports tbody').on('click','tr img',function() {
					var nTr = $(this).parents('tr')[0];
					if (oTable.fnIsOpen(nTr))
					{
						/* This row is already open - close it */
						this.src = imagesPath + "details_open.png";
						oTable.fnClose(nTr);
					}
					else
					{
						/* Open this row */
						this.src = imagesPath + "details_close.png";
						oTable.fnOpen(nTr, fnFormatDetails(oTable, nTr), 'details');
					}
				});

			});

			/* Formating function for row details */
			function fnFormatDetails(oTable, nTr)
			{
				var aData = oTable.fnGetData(nTr);
				var sOut = '<table border="0" style="padding-left:100px;">';
				sOut += '<tr><td>Description:</td><td>' + aData[2] + '</td></tr>';
				sOut += '</table>';

				return sOut;
			}
		</script>
	</jsp:attribute>

	<jsp:body>
		<c:if test="${not empty error}">
			<div class="alert alert-danger">
				<p><spring:message code="page.message.errorOccurred"/></p>
				<p>${error}</p>
			</div>
		</c:if>

		<div class="row">
			<div class="col-md-6 col-md-offset-3">
				<div class="panel panel-success">
					<div class="panel-heading text-center">
						<h4 class="panel-title"><spring:message code="reports.text.reports"/></h4>
					</div>
					<div class="panel-body">
						<table id="reports" class="table table-bordered table-striped table-condensed">
							<thead>
								<tr>
									<th><spring:message code="reports.text.groupName"/></th>
									<th><spring:message code="reports.text.description"/></th>
									<th><spring:message code="reports.text.reportName"/></th>
								</tr>
							</thead>
							<tbody>
								<c:forEach var="report" items="${reports}">
									<tr>
										<td>${report.reportGroupName}</td>
										<td>${report.description}</td>
										<td>${report.name}</td>
									</tr>
								</c:forEach>
							</tbody>
						</table>
					</div>
				</div>
			</div>
		</div>
	</jsp:body>

</t:mainPage>
