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
			/* Formating function for row details */
			function fnFormatDetails(oTable, nTr)
			{
				var aData = oTable.fnGetData(nTr);
				var sOut = '<table cellpadding="5" cellspacing="0" border="0" style="padding-left:50px;">';
				sOut += '<tr><td>Rendering engine:</td><td>' + aData[1] + ' ' + aData[4] + '</td></tr>';
				sOut += '<tr><td>Link to source:</td><td>Could provide a link here</td></tr>';
				sOut += '<tr><td>Extra info:</td><td>And any further details here (images etc)</td></tr>';
				sOut += '</table>';

				return sOut;
			}

			$(document).ready(function() {
				$(function() {
					$('a[href*="logs.do"]').parent().addClass('active');
				});

				// Insert a 'details' column to the table
				//must be done before datatables initialisation
				var nCloneTh = document.createElement('th');
				var nCloneTd = document.createElement('td');
				nCloneTd.innerHTML = '<img src="' + imagesPath + 'details_open.png">';
				nCloneTd.className = "centered";
				var nCloneTdBlank = document.createElement('td');

				$('#logs thead tr').each(function() {
					this.insertBefore(nCloneTh, this.childNodes[0]);
				});

				$('#logs tbody tr').each(function() {
					if($(this).attr("class")==="ERROR"){
						this.insertBefore(nCloneTd.cloneNode(true), this.childNodes[0]);
					} else {
						this.insertBefore(nCloneTdBlank.cloneNode(true), this.childNodes[0]);
					}
					
				});

				//Initialise DataTables, with no sorting on the 'details' column (column [0])
				var oTable = $('#logs').dataTable({
					"sPaginationType": "bs_full",
					"aoColumnDefs": [
						{"bSortable": false, "aTargets": [0]}
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
				$('#logs tbody td img').on('click', function() {
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

		</script>
	</jsp:attribute>

	<jsp:body>
		<div class="row">
			<div class="col-md-12">
				<div class="panel panel-success">
					<div class="panel-heading">
						<h4 class="panel-title text-center">${pageTitle}</h4>
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
											<td>${log.formattedMessage}</td>
											<td>${log.MDCPropertyMap['username']}</td>
											<td>${log.MDCPropertyMap['req.remoteHost']}</td>
											<td>${log.MDCPropertyMap['req.requestURI']}</td>
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

