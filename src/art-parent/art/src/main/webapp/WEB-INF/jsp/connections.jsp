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

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText" javaScriptEscape="true"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText" javaScriptEscape="true"/>
<spring:message code="connections.message.connectionReset" var="connectionResetText" javaScriptEscape="true"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-10 col-md-offset-1"
					 hasTable="true" hasNotify="true">

	<jsp:attribute name="javascript">

		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="connections"]').parent().addClass('active');

				var tbl = $("#connections");
				
				var pageLength = undefined; //pass undefined to use the default
				var showAllRowsText = "${showAllRowsText}";
				var contextPath = "${pageContext.request.contextPath}";
				var localeCode = "${pageContext.response.locale}";
				var addColumnFilters = false; //pass undefined to use the default
				var columnDefs = undefined; //pass undefined to use the default

				//initialize datatable
				var oTable = initBasicTable(tbl, pageLength, showAllRowsText,
						contextPath, localeCode, addColumnFilters, columnDefs);

				var table = oTable.api();

				tbl.find('tbody').on('click', '.reset', function () {
					var row = $(this).closest("tr"); //jquery object
					var recordName = escapeHtmlContent(row.attr("data-name"));
					var recordId = row.data("id");

					$.ajax({
						type: "POST",
						dataType: "json",
						url: "${pageContext.request.contextPath}/refreshConnectionPool",
						data: {id: recordId},
						success: function (response) {
							if (response.success) {
								var pool = response.data;

								table.cell(row, 3).data(pool.highestReachedPoolSize);
								table.cell(row, 4).data(pool.currentPoolSize);
								table.cell(row, 5).data(pool.inUseCount);
								table.cell(row, 6).data(pool.totalConnectionRequests);

								notifyActionSuccessReusable("${connectionResetText}", recordName);
							} else {
								notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
							}
						},
						error: ajaxErrorHandler
					});
				});

				$('#ajaxResponseContainer').on("click", ".alert .close", function () {
					$(this).parent().hide();
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
				<c:if test="${showErrors}">
					<p>${encode:forHtmlContent(error)}</p>
				</c:if>
			</div>
		</c:if>

		<div id="ajaxResponseContainer">
			<div id="ajaxResponse">
			</div>
		</div>

		<table id="connections" class="table table-bordered table-striped table-condensed">
			<thead>
				<tr>
					<th><spring:message code="connections.text.datasourceId"/></th>
					<th><spring:message code="connections.text.datasourceName"/></th>
					<th><spring:message code="connections.text.maxConnectionCount"/></th>

					<c:if test="${usingArtDBCPConnectionPoolLibrary}">
						<th><spring:message code="connections.text.highestReachedConnectionCount"/></th>
						<th><spring:message code="connections.text.currentConnectionCount"/></th>
						<th><spring:message code="connections.text.inUseCount"/></th>
						<th><spring:message code="connections.text.totalConnectionRequests"/></th>
						<th class="noFilter actionCol"><spring:message code="page.text.action"/></th>
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

						<c:if test="${usingArtDBCPConnectionPoolLibrary}">
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
