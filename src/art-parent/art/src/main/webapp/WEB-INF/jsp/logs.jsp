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

<spring:message code="page.title.logs" var="pageTitle" scope="page"/>

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
					$('a[href*="logs.do"]').parent().addClass('active');
				});

				// Insert a 'details' column to the table
				//must be done before datatables initialisation
				var nCloneTh = document.createElement('th');
				var nCloneTd = document.createElement('td');
				nCloneTd.innerHTML = '<img src="' + imagesPath + 'details_open.png">';
				nCloneTd.className = "text-center";
				var nCloneTdBlank = document.createElement('td');

				$('#logs thead tr').each(function() {
					this.insertBefore(nCloneTh, this.childNodes[0]);
				});

				$('#logs tbody tr').each(function() {
					if ($(this).attr("class") === "ERROR") {
						this.insertBefore(nCloneTd.cloneNode(true), this.childNodes[0]);
					} else {
						this.insertBefore(nCloneTdBlank.cloneNode(true), this.childNodes[0]);
					}

				});

				//Initialise DataTables, with no sorting on the 'details' column (column [0])
				var oTable = $('#logs').dataTable({
					"sPaginationType": "bs_full",
					"aoColumnDefs": [
						{"bSortable": false, "aTargets": [0]},
						{"bVisible": false, "aTargets": [-1]} //hide last column (exception details)
					],
					"aaSorting": [[1, "asc"]],
					"aLengthMenu": [[5, 10, 25, -1], [5, 10, 25, allRowsText]],
					"iDisplayLength": -1,
					"oLanguage": {
						"sUrl"
								: contextPath + "/dataTables/dataTables_" + localeCode + ".txt"
					}
				});

				/* Add event listener for opening and closing details
				 * Note that the indicator for showing which row is open is not controlled by DataTables,
				 * rather it is done here
				 */
				$('#logs tbody').on('click', 'tr img', function() {
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
				
				//open all rows
				//openAllRows(oTable);

			});
			
			/* Formating function for row details */
			function fnFormatDetails(oTable, nTr)
			{
				var aData = oTable.fnGetData(nTr);
				var sOut = '<table style="margin-left:50px;">';
				sOut += '<tr><td>' + aData[8] + '</td></tr>';
				sOut += '</table>';

				return sOut;
			}
			
			function openAllRows(oTable){
				oTable.$('tr').each( function () {
					if ( !oTable.fnIsOpen( this ) ) {
						alert($(this).text());
						var nTr = $(this);
						oTable.fnOpen(nTr, fnFormatDetails(oTable, this), 'details');
					}
				});
			}

		</script>
	</jsp:attribute>

	<jsp:body>
		<div class="row">
			<div class="col-md-12">
				<div class="panel panel-success">
					<div class="panel-heading">
						<h4 class="panel-title text-center">${fn:escapeXml(pageTitle)}</h4>
					</div>
					<div class="panel-body">
						<div class="text-center" style="margin-bottom: 10px;">
							<fmt:formatDate var="nowFormatted" value="${now}" pattern="${displayDatePattern}"/>
							<spring:message code="logs.message.showingRecentEvents" arguments="${nowFormatted}"/>
							<span class="pull-right">
								<a href="#bottom">
									<spring:message code="logs.button.bottom"/>
								</a>
							</span>
						</div>
						<c:if test="${not empty message}">
							<div class="alert alert-info">
								<spring:message code="${message}"/>
							</div>
						</c:if>
						<div>
							<table id="logs" class="datatable table table-striped table-bordered table-condensed">
								<thead>
									<tr>
										<th><spring:message code="logs.text.date"/></th>
										<th><spring:message code="logs.text.level"/></th>
										<th><spring:message code="logs.text.logger"/></th>
										<th><spring:message code="logs.text.message"/></th>
										<th><spring:message code="logs.text.user"/></th>
										<th><spring:message code="logs.text.ipAddress"/></th>
										<th><spring:message code="logs.text.url"/></th>
										<th></th> <%-- exception details column. hidden --%>
									</tr>
								</thead>
								<tbody>
									<c:forEach var="log" items="${logs}">
										<tr class="${log.level}">
											<td>
												<jsp:useBean id="dateValue" class="java.util.Date"/>
												<jsp:setProperty name="dateValue" property="time" value="${log.timeStamp}"/>
												<t:displayDate date="${dateValue}"/>
											</td>
											<td>${log.level}</td>
											<td>${log.loggerName}</td>
											<td>${fn:escapeXml(log.formattedMessage)}</td>
											<td>${fn:escapeXml(log.MDCPropertyMap['username'])}</td>
											<td>${fn:escapeXml(log.MDCPropertyMap['req.remoteHost'])}</td>
											<td>${fn:escapeXml(log.MDCPropertyMap['req.requestURI'])}</td>
											<td>
												<c:set var="throwable" value="${log.throwableProxy}" />
												<c:if test="${throwable != null}">
													<c:forEach begin="0" end="10" varStatus="loop">
														<c:if test="${throwable != null}">
															<c:set var="commonFrames" value="${throwable.commonFrames}" />
															<c:if test="${commonFrames gt 0}">
																<br> Caused by: 
															</c:if>
															${throwable.className}: ${throwable.message}
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
						<div id="bottom" class="pull-right">
							<a href="#top">
								<spring:message code="logs.button.top"/>
							</a>
						</div>
					</div>
				</div>
			</div>
		</div>
	</jsp:body>
</t:mainPage>

