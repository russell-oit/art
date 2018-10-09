<%-- 
    Document   : logs
    Created on : 11-Dec-2013, 10:13:57
    Author     : Timothy Anyona

Display application logs
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="/WEB-INF/tlds/functions.tld" prefix="f" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<spring:message code="page.title.logs" var="pageTitle"/>

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-12">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/Select-1.2.0/css/select.bootstrap.min.css"/>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/css/buttons.dataTables.min.css"/>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/css/buttons.bootstrap.min.css"/>
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Select-1.2.0/js/dataTables.select.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/js/dataTables.buttons.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/js/buttons.bootstrap.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/JSZip-2.5.0/jszip.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/pdfmake-0.1.18/pdfmake.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/pdfmake-0.1.18/vfs_fonts.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/js/buttons.html5.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/js/buttons.print.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/js/buttons.colVis.min.js"></script>

		<script type="text/javascript">
			$(document).ready(function () {
				$('a[href*="logs"]').parent().addClass('active');

				var tbl = $('#logs');

				var columnFilterRow = createColumnFilters(tbl);

				//make error rows expandable
				tbl.find('tbody tr.ERROR td:first-child').each(function () {
					$(this).addClass('details-control');
				});

				var oTable = tbl.dataTable({
					columnDefs: [
						{
							targets: "detailsCol",
							orderable: false,
							searchable: false
						},
						{
							targets: "dtHidden", //target name matches class name of th.
							visible: false
						}
					],
					dom: 'lBfrtip',
					buttons: [
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
					orderClasses: false,
					order: [1, "asc"], //sort by log date
					pagingType: "full_numbers",
					lengthMenu: [[10, 20, 50, -1], [10, 20, 50, "${showAllRowsText}"]],
					pageLength: -1,
					language: {
						url: "${pageContext.request.contextPath}/js/dataTables/i18n/dataTables_${pageContext.response.locale}.json"
					},
					initComplete: function () {
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

				tbl.find('tbody').on('click', 'tr.ERROR', function () {
					var tr = $(this);
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

//				// On each draw, loop over the `detailRows` array and show any child rows
//				table.on('draw', function() {
//					$.each(detailRows, function(i, id) {
//						$('#' + id + ' td:first-child').trigger('click');
//					});
//				});

			});

			/* Formating function for row details */
			function formatDetails(data) {
				return '<div class="details">' + data[8] + '</div>';
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
						<th class="detailsCol noFilter"></th> <%-- details control column --%>
						<th><spring:message code="logs.text.date"/></th>
						<th><spring:message code="page.text.level"/></th>
						<th><spring:message code="logs.text.logger"/></th>
						<th><spring:message code="logs.text.message"/></th>
						<th><spring:message code="page.text.user"/></th>
						<th class="dtHidden"><spring:message code="logs.text.page"/></th>
						<th class="dtHidden"><spring:message code="logs.text.ipAddress"/></th>
						<th class="dtHidden"><spring:message code="logs.text.details"/></th> <%-- exception details column. must be last column. hidden --%>
							<%-- if change number of columns, must modify array index in format function --%>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="log" items="${logs}">
						<tr class="${encode:forHtmlAttribute(log.level)}">
							<td></td> <%-- details control column --%>
							<td data-sort="${log.timeStamp}">
								<fmt:formatDate value="${f:getDate(log.timeStamp)}" pattern="${dateDisplayPattern}"/>
							</td>
							<td><encode:forHtmlContent value="${log.level}"/></td>
							<td><encode:forHtmlContent value="${log.loggerName}"/></td>
							<td><encode:forHtmlContent value="${log.formattedMessage}"/></td>
							<td><encode:forHtmlContent value="${log.MDCPropertyMap['user']}"/></td>
							<td><encode:forHtmlContent value="${log.MDCPropertyMap['requestURI']}"/></td>
							<td><encode:forHtmlContent value="${log.MDCPropertyMap['remoteAddr']}"/></td>
							<td>
								<%-- based on ch.qos.logback.classic.html.DefaultThrowableRenderer --%>
								<c:set var="throwable" value="${log.throwableProxy}" />
								<c:if test="${throwable != null}">
									<c:forEach begin="0" end="5" varStatus="loop">
										<c:if test="${throwable != null}">
											<c:set var="commonFrames" value="${throwable.commonFrames}" />
											<c:if test="${commonFrames gt 0}">
												<br> <spring:message code="logs.text.causedBy"/>: 
											</c:if>
											${throwable.className}: <encode:forHtmlContent value="${throwable.message}"/>
											<c:set var="traceArray" value="${throwable.stackTraceElementProxyArray}" />
											<c:forEach begin="0" end="${fn:length(traceArray) - commonFrames - 1}" varStatus="loop">
												<br>&nbsp;&nbsp;&nbsp;&nbsp; ${traceArray[loop.index]}
											</c:forEach>
											<c:if test="${commonFrames gt 0}">
												<br>&nbsp;&nbsp;&nbsp;&nbsp; ... ${commonFrames} <spring:message code="logs.text.commonFramesOmitted"/> 
											</c:if>
										</c:if>
										<c:if test="${loop.last && throwable != null}">
											<spring:message code="logs.text.moreCausesNotListed"/>...
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