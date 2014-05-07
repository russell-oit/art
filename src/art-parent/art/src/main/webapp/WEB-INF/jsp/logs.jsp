<%-- 
    Document   : logs
    Created on : 11-Dec-2013, 10:13:57
    Author     : Timothy Anyona

Display application logs
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<spring:message code="page.title.logs" var="pageTitle"/>

<spring:message code="datatables.text.showAllRows" var="dataTablesAllRowsText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-12">

	<jsp:attribute name="javascript">
		<script type="text/javascript">
			$(document).ready(function() {
				$('a[href*="logs.do"]').parent().addClass('active');

				var tbl = $('#logs');

				//make error rows expandable
				tbl.find('tbody tr.ERROR td:first-child').each(function() {
					$(this).addClass('details-control');
				});

				var oTable = tbl.dataTable({
					"columnDefs": [
						{
							"targets": "detailsCol",
							"orderable": false
						},
						{
							"targets": "exceptionCol", //target name matches class name of th.
							"visible": false
						}
					],
					"orderClasses": false,
					"pagingType": "full_numbers",
					"lengthMenu": [[5, 10, 25, -1], [5, 10, 25, "${dataTablesAllRowsText}"]],
					"pageLength": -1,
					"language": {
						"url": "${pageContext.request.contextPath}/js/dataTables-1.10.0/i18n/dataTables_${pageContext.response.locale}.txt"
					},
					"initComplete": function() {
						$('div.dataTables_filter input').focus();
					}
				});

				//show/hide details
				//http://datatables.net/examples/server_side/row_details.html

				//get datatables api instance
				var dt = oTable.api();

				// Array to track the ids of the details displayed rows
				var detailRows = [];

				tbl.find('tbody').on('click', 'tr.ERROR', function() {
					var tr = $(this);
					var row = dt.row(tr);
					var idx = $.inArray(tr, detailRows);

					if (row.child.isShown()) {
						tr.removeClass('details');
						row.child.hide();

						// Remove from the 'open' array
						detailRows.splice(idx, 1);
					}
					else {
						tr.addClass('details');
						row.child(format(row.data()),'details').show(); //add details class to child row td

						// Add to the 'open' array
						if (idx === -1) {
							detailRows.push(tr);
						}
					}
				});

//				// On each draw, loop over the `detailRows` array and show any child rows
//				dt.on('draw', function() {
//					$.each(detailRows, function(i, id) {
//						$('#' + id + ' td:first-child').trigger('click');
//					});
//				});

			});

			/* Formating function for row details */
			function format(data)
			{
				return '<div class="details">' + data[7] + '</div>';
			}

		</script>
	</jsp:attribute>

	<jsp:body>
		<c:if test="${not empty message}">
			<div class="alert alert-info alert-dismissable">
				<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
				<spring:message code="${message}"/>
			</div>
		</c:if>
		<div>
			<table id="logs" class="expandable table table-striped table-bordered table-condensed">
				<thead>
					<tr>
						<th class="detailsCol"></th> <%-- details control column --%>
						<th><spring:message code="logs.text.date"/></th>
						<th><spring:message code="page.text.level"/></th>
						<th><spring:message code="logs.text.logger"/></th>
						<th><spring:message code="logs.text.message"/></th>
						<th><spring:message code="page.text.user"/></th>
						<th><spring:message code="logs.text.page"/></th>
						<th class="exceptionCol"></th> <%-- exception details column. must be last column. hidden --%>
							<%-- if change number of columns, must modify array index in format function --%>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="log" items="${logs}">
						<tr class="${log.level}">
							<td></td> <%-- details control column --%>
							<td>
								<t:displayDate timestamp="${log.timeStamp}"/>
							</td>
							<td>${log.level}</td>
							<td>${log.loggerName}</td>
							<td>${fn:escapeXml(log.formattedMessage)}</td>
							<td>${fn:escapeXml(log.MDCPropertyMap['user'])}</td>
							<td>${fn:escapeXml(log.MDCPropertyMap['requestURI'])}</td>
							<td>
								<c:set var="throwable" value="${log.throwableProxy}" />
								<c:if test="${throwable != null}">
									<c:forEach begin="0" end="5" varStatus="loop">
										<c:if test="${throwable != null}">
											<c:set var="commonFrames" value="${throwable.commonFrames}" />
											<c:if test="${commonFrames gt 0}">
												<br> Caused by: 
											</c:if>
											${throwable.className}: ${fn:escapeXml(throwable.message)}
											<c:set var="traceArray" value="${throwable.stackTraceElementProxyArray}" />
											<c:forEach begin="0" end="${fn:length(traceArray) - commonFrames - 1}" varStatus="loop">
												<br>&nbsp;&nbsp;&nbsp;&nbsp; ${traceArray[loop.index]}
											</c:forEach>
											<c:if test="${commonFrames gt 0}">
												<br>&nbsp;&nbsp;&nbsp;&nbsp; ... ${commonFrames} common frames omitted 
											</c:if>
										</c:if>
										<c:if test="${loop.last && throwable != null}">
											More causes not listed...
										</c:if>
										<c:set var="throwable" value="${throwable.cause}" />
									</c:forEach>
								</c:if>
							</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</div>
	</jsp:body>
</t:mainPageWithPanel>