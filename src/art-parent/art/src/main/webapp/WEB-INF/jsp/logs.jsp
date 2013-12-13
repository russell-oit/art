<%-- 
    Document   : logs
    Created on : 11-Dec-2013, 10:13:57
    Author     : Timothy Anyona

Display application logs
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib  uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<spring:message code="page.title.logs" var="pageTitle" scope="page"/>

<spring:message code="datatables.text.showAllRows" var="dataTablesAllRowsText" scope="page"/>

<t:mainPage title="${pageTitle}">
	<jsp:attribute name="javascript">
		<script type="text/javascript">
			var allRowsText = "${dataTablesAllRowsText}";
		</script>
		<script type="text/javascript" charset="utf-8">
			$(document).ready(function() {
				$('.datatable').dataTable({
					"sPaginationType": "bs_full",
					"aaSorting": [[0, "asc"]],
					"bSortCellsTop": true,
					"aLengthMenu": [[5, 10, 25, -1], [5, 10, 25, allRowsText]],
					"iDisplayLength": -1,
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
							<table class="datatable table table-striped table-bordered table-condensed">
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
										<c:set var="throwable" value="${log.throwableProxy}" />
										<c:if test="${throwable != null}">
											<tr>
												<td class="exception" colspan="7">
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
												</td>
											</tr>
										</c:if>
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

