<%-- 
    Document   : caches
    Created on : 26-Feb-2014, 11:52:09
    Author     : Timothy Anyona

Page to allow manual clearing of caches
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.caches" var="pageTitle"/>

<spring:message code="datatables.text.showAllRows" var="showAllRowsText"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="caches.message.cacheCleared" var="cacheClearedText"/>
<spring:message code="caches.message.cachesCleared" var="cachesClearedText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-6 col-md-offset-3">

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/notify-combined-0.3.1.min.js"></script>
		<script type="text/javascript">
			$(document).ready(function () {
				$(function () {
					$('a[href*="caches.do"]').parent().addClass('active');
				});

				var tbl = $('#caches');

				//initialize datatable and process delete action
				initConfigPage(tbl,
						undefined, //pageLength. pass undefined to use the default
						"${showAllRowsText}",
						"${pageContext.request.contextPath}",
						"${pageContext.response.locale}",
						false, //addColumnFilters
						".clearCache", //deleteButtonSelector
						false, //showConfirmDialog
						undefined, //deleteRecordText
						undefined, //okText
						undefined, //cancelText
						"clearCache.do", //deleteUrl
						"${cacheClearedText}", //recordDeletedText
						"${errorOccurredText}",
						false, //deleteRow
						undefined, //cannotDeleteRecordText
						undefined //linkedRecordsExistText
						);

			});
		</script>

		<script type="text/javascript">
			$(function () {
				$('#clearAll').click(function () {
					$.ajax({
						type: 'POST',
						url: '${pageContext.request.contextPath}/app/clearAllCaches.do',
						dataType: 'json',
						success: function (response) 
						{
							notifyActionSuccess("${cachesClearedText}", undefined);
						},
						error: ajaxErrorHandler
					});
				});
			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<c:if test="${error != null}">
			<div class="alert alert-danger alert-dismissable">
				<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
				<p><spring:message code="page.message.errorOccurred"/></p>
				<c:if test="${showErrors}">
					<p>${encode:forHtmlContent(error)}</p>
				</c:if>
			</div>
		</c:if>

		<div id="ajaxResponse">
		</div>

		<div style="margin-bottom: 10px;">
			<button id="clearAll" type="button" class="btn btn-default">
				<spring:message code="caches.button.clearAll"/>
			</button>
		</div>

		<table id="caches" class="table table-striped table-bordered">
			<thead>
				<tr>
					<th><spring:message code="caches.text.cache"/></th>
					<th class="noFilter"><spring:message code="page.text.action"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="cache" items="${caches}">
					<tr data-id="${cache.value}"
						data-name="${cache.value}">

						<td><spring:message code="${cache.localizedDescription}"/></td>
						<td>
							<button type="button" class="btn btn-default clearCache">
								<i class="fa fa-trash-o"></i>
								<spring:message code="caches.action.clear"/>
							</button>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</jsp:body>
</t:mainPageWithPanel>
