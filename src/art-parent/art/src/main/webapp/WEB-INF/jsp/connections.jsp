<%-- 
    Document   : connections
    Created on : 27-Feb-2014, 07:33:53
    Author     : Timothy Anyona

Page to display connections status
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.connections" var="pageTitle"/>

<spring:message code="datatables.text.showAllRows" var="showAllRowsText"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="connections.message.connectionReset" var="connectionResetText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-10 col-md-offset-1">

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/notify-combined-0.3.1.min.js"></script>
		<script type="text/javascript" charset="utf-8">
			$(document).ready(function () {
				$(function () {
					$('a[id="configure"]').parent().addClass('active');
					$('a[href*="connections.do"]').parent().addClass('active');
				});

				var tbl = $("#connections");

				var oTable = initConfigTable(tbl,
						undefined, //pageLength. pass undefined to use the default
						"${showAllRowsText}",
						"${pageContext.request.contextPath}",
						"${pageContext.response.locale}",
						false //addColumnFilters
						);

				//get datatables api instance
				var table = oTable.api();

				tbl.find('tbody').on('click', '.reset', function () {
					var row = $(this).closest("tr"); //jquery object
					var recordName = escapeHtmlContent(row.data("name"));
					var recordId = row.data("id");

					$.ajax({
						type: "POST",
						dataType: "json",
						url: "${pageContext.request.contextPath}/app/refreshConnectionPool.do",
						data: {id: recordId},
						success: function (response) {
							var pool = response.data;

							table.cell(row, 3).data(pool.highestReachedPoolSize);
							table.cell(row, 4).data(pool.currentPoolSize);
							table.cell(row, 5).data(pool.inUseCount);
							table.cell(row, 6).data(pool.totalConnectionRequests);

							notifyActionSuccess("${connectionResetText}", recordName);
						},
						error: ajaxErrorHandler
					});
				});

			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<c:if test="${not empty message}">
			<div class="alert alert-success alert-dismissable">
				<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
				<spring:message code="${message}"/>
			</div>
		</c:if>
		<c:if test="${error != null}">
			<div class="alert alert-danger alert-dismissable">
				<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
				<p><spring:message code="page.message.errorOccurred"/></p>
				<p>${error}</p>
			</div>
		</c:if>

		<div id="ajaxResponse">
		</div>

		<table id="connections" class="table table-bordered table-striped table-condensed">
			<thead>
				<tr>
					<th><spring:message code="connections.text.datasourceId"/></th>
					<th><spring:message code="connections.text.datasourceName"/></th>
					<th><spring:message code="connections.text.maxConnectionCount"/></th>

					<c:if test="${pool.usingArtDBCPConnectionPoolLibrary}">
						<th><spring:message code="connections.text.highestReachedConnectionCount"/></th>
						<th><spring:message code="connections.text.currentConnectionCount"/></th>
						<th><spring:message code="connections.text.inUseCount"/></th>
						<th><spring:message code="connections.text.totalConnectionRequests"/></th>
						<th class="noFilter"><spring:message code="page.text.action"/></th>
					</c:if>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="pool" items="${connectionPoolDetails}">
					<tr data-id="${pool.poolId}"
						data-name="${encode:forHtmlAttribute(pool.name)}">

						<td>${pool.poolId}</td>
						<td>${encode:forHtmlContent(pool.name)}</td>
						<td>${pool.maxPoolSize}</td>

						<c:if test="${pool.usingArtDBCPConnectionPoolLibrary}">
							<td>${pool.highestReachedPoolSize}</td>
							<td>${pool.currentPoolSize}</td>
							<td>${pool.inUseCount}</td>
							<td>${pool.totalConnectionRequests}</td>
							<td>
								<button type="button" class="btn btn-default reset">
									<i class="fa fa-bolt"></i>
									<spring:message code="connections.action.reset"/>
								</button>
							</td>
						</c:if>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</jsp:body>
</t:mainPageWithPanel>
