<%-- 
    Document   : logs
    Created on : 11-Dec-2013, 10:13:57
    Author     : Timothy Anyona

Display application logs
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib  uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<spring:message code="page.title.logs" var="pageTitle" scope="page"/>

<t:mainPage title="${pageTitle}">
	<jsp:attribute name="javascript">
		<script type="text/javascript" charset="utf-8">
			$(document).ready(function() {
				$('.datatable').dataTable({
					"sPaginationType": "bs_full",
					"bPaginate": false,
					"aaSorting": [[0, "asc"]],
					"bSortCellsTop": true,
					"aLengthMenu": [[5, 10, 25, -1], [5, 10, 25, "${dataTablesAllRowsText}"]],
					"iDisplayLength": 5,
					"oLanguage": {
						"sUrl": "${pageContext.request.contextPath}/dataTables/dataTables_${pageContext.response.locale}.txt"
					}
				});
				$('.datatable').each(function() {
					var datatable = $(this);
					// SEARCH - Add the placeholder for Search and Turn this into in-line form control
					var search_input = datatable.closest('.dataTables_wrapper').find('div[id$=_filter] input');
					search_input.attr('placeholder', 'Search');
					search_input.addClass('form-control input-sm');
					// LENGTH - Inline-Form control
					var length_sel = datatable.closest('.dataTables_wrapper').find('div[id$=_length] select');
					length_sel.addClass('form-control input-sm');
				});

				$(function() {
					$('a[href*="logs.do"]').parent().addClass('active');
				});
			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<div class="row">
			<div class="col-md-8 col-md-offset-1">
				<table class="datatable table table-bordered table-condensed">
					<thead>
						<tr>
							<th><spring:message code="logs.text.ipAddress"/></th>
							<th><spring:message code="logs.text.url"/></th>
							<th><spring:message code="logs.text.user"/></th>
							<th><spring:message code="logs.text.date"/></th>
							<th><spring:message code="logs.text.level"/></th>
							<th><spring:message code="logs.text.logger"/></th>
							<th><spring:message code="logs.text.message"/></th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="log" items="${logs}">
							<tr>
								<td>${log.MDCPropertyMap['req.remoteHost']}</td>
								<td>${log.MDCPropertyMap['req.requestURI']}</td>
								<td>${log.MDCPropertyMap['username']}</td>
								<td>
									<jsp:useBean id="dateValue" class="java.util.Date" />
									<jsp:setProperty name="dateValue" property="time" value="${log.timeStamp}" />
									<t:displayDate date="${dateValue}"></t:displayDate>
								</td>
								<td>${log.level}</td>
								<td>${log.loggerName}</td>
								<td>${log.formattedMessage}</td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
			</div>
		</div>
	</jsp:body>
</t:mainPage>

