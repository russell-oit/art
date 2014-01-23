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

<spring:message code="page.title.reports" var="pageTitle"/>

<spring:message code="datatables.text.showAllRows" var="allRowsText"/>
<spring:message code="reports.text.description" var="descriptionText"/>
<spring:message code="reports.text.reports" var="mainPanelTitle"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-6 col-md-offset-3">
	
	<jsp:attribute name="javascript">
		<script type="text/javascript">
			//put jsp variables into js variables
			var allRowsText = "${allRowsText}";
			var contextPath = "${pageContext.request.contextPath}";
			var localeCode = "${pageContext.response.locale}";
			var imagesPath = "${pageContext.request.contextPath}/images/";
			var descriptionText = "${descriptionText}";
		</script>
		<script type="text/javascript" charset="utf-8">
			$(document).ready(function() {
				$(function() {
					$('a[href*="reports.do"]').parent().addClass('active');
				});

				// Insert a 'details' column to the table
				var nCloneTh = document.createElement('th');
				var nCloneTd = document.createElement('td');
				nCloneTd.innerHTML = '<img src="' + imagesPath + 'details_open.png">';
				nCloneTd.className = "text-center expandable";

				$('#reports thead tr').each(function() {
					this.insertBefore(nCloneTh, this.childNodes[0]);
				});

				$('#reports tbody tr').each(function() {
					this.insertBefore(nCloneTd.cloneNode(true), this.childNodes[0]);
				});

				//Initialise DataTables, with no sorting on the 'details' column (column [0])
				var oTable = $('#reports').dataTable({
					"sPaginationType": "bs_full",
//					"sScrollY": "400px",
					"aLengthMenu": [[5, 10, 25, -1], [5, 10, 25, allRowsText]],
					"iDisplayLength": -1,
					"oLanguage": {
						"sUrl": contextPath + "/dataTables/dataTables_" + localeCode + ".txt"
					},
					"aaSorting": [[3, "asc"]],
					'aoColumnDefs': [
						{"bVisible": false, "aTargets": [1, 2]},
						{"bSortable": false, "aTargets": [0]}
					],
					"fnInitComplete": function() {
						$('div.dataTables_filter input').focus();
					}
				});

				//array to keep a reference to any TR rows that we 'open'
				var anOpen = [];

				// Add event listener for opening and closing details
				$('#reports tbody').on('click', 'td.expandable', function() {
					if ($(this).parent('tr').hasClass('details')) {
						return; //only expandable rows can have details
					}

					var nTr = this.parentNode;

					//see if the row should be opened or if it is already in the open array, and thus close it
					var i = $.inArray(nTr, anOpen);
					if (i === -1) {
						// Row is not in open array so it's currently closed. Open it
						var nDetailsRow = oTable.fnOpen(nTr, fnFormatDetails(oTable, nTr), 'details');
						$('div.innerDetails', nDetailsRow).slideDown('fast');

						//add row to open array
						anOpen.push(nTr);

						//change icon to indicate the row is now due for closing
						$('img', this).attr('src', imagesPath + "details_close.png");
					} else {
						// Close this row
						$('div.innerDetails', $(nTr).next()[0]).slideUp('fast', function() {
							oTable.fnClose(nTr);
							//remove row from open array
							anOpen.splice(i, 1);
						});

						//change icon to indicate the row is now due for opening
						$('img', this).attr('src', imagesPath + "details_open.png");
					}
				});

			});

			/* Formating function for row details */
			function fnFormatDetails(oTable, nTr)
			{
				var aData = oTable.fnGetData(nTr);
				var sOut = '<div class="innerDetails">';
				sOut += '<table style="margin-left:30px;">';
				sOut += '<tbody>';
				sOut += '<tr><td>' + descriptionText + ':</td><td>' + aData[2] + '</td></tr>';
				sOut += '</tbody>';
				sOut += '</table>';
				sOut += '</div>';

				return sOut;
			}
		</script>
	</jsp:attribute>

	<jsp:attribute name="aboveMainPanel">
		<c:if test="${error != null}">
			<div class="alert alert-danger">
				<p><spring:message code="page.message.errorOccurred"/></p>
				<p>${fn:escapeXml(error)}</p>
			</div>
		</c:if>
	</jsp:attribute>

	<jsp:body>
		<table id="reports" class="expandable table table-bordered">
			<thead>
				<tr>
					<th></th> <%-- group name. hidden --%>
					<th></th> <%-- description column. hidden --%>
					<th><spring:message code="reports.text.reportName"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="report" items="${reports}">
					<tr>
						<td>${fn:escapeXml(report.reportGroupName)}</td>
						<td>${fn:escapeXml(report.description)}</td>
						<td>
							<a href="#">
								${fn:escapeXml(report.name)}
							</a>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</jsp:body>
</t:mainPageWithPanel>
