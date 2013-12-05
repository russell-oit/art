<%-- 
    Document   : reports
    Created on : 01-Oct-2013, 09:53:44
    Author     : Timothy Anyona

Reports page. Also main/home page
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<spring:message code="page.title.reports" var="pageTitle" scope="page"/>

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
					$('a[href*="reports.do"]').parent().addClass('active');
				});
			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<div class="text-right">
			<a href="#">
				<spring:message code="page.link.help"/>
			</a>
		</div>

		<c:if test="${not empty error}">
			<div class="alert alert-danger">
				<p><spring:message code="page.message.errorOccurred"/></p>
				<p>${error}</p>
			</div>
		</c:if>

		<div class="row">
			<div class="col-md-4 col-md-offset-1">
				<div class="panel panel-info">
					<div class="panel-heading">
						<spring:message code="reports.text.groups"/>
					</div>
					<div class="panel-body">
						<table class="datatable table table-bordered table-striped table-condensed">
							<thead>
								<tr>
									<th><spring:message code="reports.text.group"/></th>
								</tr>
								<tr>
									<td><spring:message code="reports.text.allGroups"/></td>
								</tr>
							</thead>
							<tbody>
								<c:forEach var="group" items="${reportGroups}">
									<tr>
										<td>${fn:escapeXml(group.name)}</td>
									</tr>
								</c:forEach>
							</tbody>
						</table>
					</div>
				</div>
				<div class="alert alert-info">
					<spring:message code="reports.info.usage"/>
				</div>
			</div>
		</div>
	</jsp:body>

</t:mainPage>
