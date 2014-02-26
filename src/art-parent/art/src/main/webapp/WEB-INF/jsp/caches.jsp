<%-- 
    Document   : caches
    Created on : 26-Feb-2014, 11:52:09
    Author     : Timothy Anyona

Page to allow manual clearing of caches
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<spring:message code="page.title.caches" var="pageTitle"/>

<spring:message code="datatables.text.showAllRows" var="dataTablesAllRowsText"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="dialog.button.cancel" var="cancelText"/>
<spring:message code="dialog.button.ok" var="okText"/>
<spring:message code="dialog.title.confirm" var="confirmText"/>
<spring:message code="dialog.message.clearCache" var="clearCacheText"/>
<spring:message code="caches.message.cacheCleared" var="cacheClearedText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-6 col-md-offset-3">
	
	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootbox-4.1.0.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/notify-combined-0.3.1.min.js"></script>
		<script type="text/javascript">
			$(document).ready(function() {
				$(function() {
					$('a[id="configure"]').parent().addClass('active');
					$('a[href*="caches.do"]').parent().addClass('active');
				});

				var oTable = $('#caches').dataTable({
					"sPaginationType": "bs_full",
					"aaSorting": [],
					"aLengthMenu": [[5, 10, 25, -1], [5, 10, 25, "${dataTablesAllRowsText}"]],
					"iDisplayLength": -1,
					"oLanguage": {
						"sUrl": "${pageContext.request.contextPath}/dataTables/dataTables_${pageContext.response.locale}.txt"
					},
					"fnInitComplete": function() {
						$('div.dataTables_filter input').focus();
					}
				});

				$('#caches tbody').on('click', '.clear', function() {
					var row = $(this).closest("tr"); //jquery object
					var nRow = row[0]; //dom element/node
					var aPos = oTable.fnGetPosition(nRow);
					var name = escapeHtmlContent(row.data("name"));
					var msg;
					bootbox.confirm({
						message: "${clearCacheText}: <b>" + name + "</b>",
						title: "${confirmText}",
						buttons: {
							'cancel': {
								label: "${cancelText}"
							},
							'confirm': {
								label: "${okText}"
							}
						},
						callback: function(result) {
							if (result) {
								$.ajax({
									type: "POST",
									url: "${pageContext.request.contextPath}/app/clearCache.do",
									data: {name: name},
									success: function(response) {
										if (response.success) {
											msg = alertCloseButton + "${cacheClearedText}: " + name;
											$("#ajaxResponse").attr("class", "alert alert-success alert-dismissable").html(msg);
											oTable.fnDeleteRow(aPos);
											$.notify("${cacheClearedText}", "success");
										} else {
											msg = alertCloseButton + "<p>${errorOccurredText}</p><p>" + escapeHtmlContent(response.errorMessage) + "</p>";
											$("#ajaxResponse").attr("class", "alert alert-danger alert-dismissable").html(msg);
											$.notify("${errorOccurredText}", "error");
										}
									},
									error: function(xhr, status, error) {
										alert(xhr.responseText);
									}
								}); //end ajax
							} //end if result
						} //end bootbox callback
					}); //end bootbox confirm
				}); //end on click
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

		<table id="caches" class="table table-striped table-bordered">
			<thead>
				<tr>
					<th><spring:message code="caches.text.cache"/></th>
					<th><spring:message code="page.text.action"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="cache" items="${caches}">
					<tr data-name="${cache.value}">
						<td><spring:message code="${cache.localisedDescription}"/></td>
						<td>
							<button type="button" class="btn btn-default clear">
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
