<%-- 
    Document   : holidays
    Created on : 13-Nov-2017, 08:51:56
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<spring:message code="page.title.holidays" var="pageTitle"/>

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="dialog.button.cancel" var="cancelText"/>
<spring:message code="dialog.button.ok" var="okText"/>
<spring:message code="dialog.message.deleteRecord" var="deleteRecordText"/>
<spring:message code="page.message.recordDeleted" var="recordDeletedText"/>
<spring:message code="page.message.recordsDeleted" var="recordsDeletedText"/>
<spring:message code="dialog.message.selectRecords" var="selectRecordsText"/>
<spring:message code="page.message.someRecordsNotDeleted" var="someRecordsNotDeletedText"/>
<spring:message code="page.message.cannotDeleteRecord" var="cannotDeleteRecordText"/>
<spring:message code="holidays.message.linkedRecordsExist" var="linkedRecordsExistText"/>

<t:mainPageWithPanel title="${pageTitle}" configPage="true">

	<jsp:attribute name="javascript">
		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="holidays"]').parent().addClass('active');

				var tbl = $('#holidays');
				
				var pageLength = undefined; //pass undefined to use the default
				var showAllRowsText = "${showAllRowsText}";
				var contextPath = "${pageContext.request.contextPath}";
				var localeCode = "${pageContext.response.locale}";
				var addColumnFilters = undefined; //pass undefined to use the default
				var deleteButtonSelector = ".deleteRecord";
				var deleteRecordText = "${deleteRecordText}";
				var okText = "${okText}";
				var cancelText = "${cancelText}";
				var deleteUrl = "deleteHoliday";
				var recordDeletedText = "${recordDeletedText}";
				var errorOccurredText = "${errorOccurredText}";
				var cannotDeleteRecordText = "${cannotDeleteRecordText}";
				var linkedRecordsExistText = "${linkedRecordsExistText}";
				var columnDefs = undefined;

				//initialize datatable and process delete action
				var oTable = initConfigPage(tbl,
						pageLength,
						showAllRowsText,
						contextPath,
						localeCode,
						addColumnFilters,
						deleteButtonSelector,
						deleteRecordText,
						okText,
						cancelText,
						deleteUrl,
						recordDeletedText,
						errorOccurredText,
						cannotDeleteRecordText,
						linkedRecordsExistText,
						columnDefs
						);

				var table = oTable.api();

				$('#deleteRecords').on("click", function () {
					var selectedRows = table.rows({selected: true});
					var data = selectedRows.data();
					if (data.length > 0) {
						var ids = $.map(data, function (item) {
							return item[1];
						});

						bootbox.confirm({
							message: "${deleteRecordText}: <b>" + ids + "</b>",
							buttons: {
								cancel: {
									label: "${cancelText}"
								},
								confirm: {
									label: "${okText}"
								}
							},
							callback: function (result) {
								if (result) {
									//user confirmed delete. make delete request
									$.ajax({
										type: "POST",
										dataType: "json",
										url: "${pageContext.request.contextPath}/deleteHolidays",
										data: {ids: ids},
										success: function (response) {
											var nonDeletedRecords = response.data;
											if (response.success) {
												selectedRows.remove().draw(false);
												notifyActionSuccessReusable("${recordsDeletedText}", ids);
											} else if (nonDeletedRecords !== null && nonDeletedRecords.length > 0) {
												notifySomeRecordsNotDeletedReusable(nonDeletedRecords, "${someRecordsNotDeletedText}");
											} else {
												notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
											}
										},
										error: ajaxErrorHandler
									});
								} //end if result
							} //end callback
						}); //end bootbox confirm
					} else {
						bootbox.alert("${selectRecordsText}");
					}
				});

				$('#exportRecords').on("click", function () {
					var selectedRows = table.rows({selected: true});
					var data = selectedRows.data();
					if (data.length > 0) {
						var ids = $.map(data, function (item) {
							return item[1];
						});
						window.location.href = '${pageContext.request.contextPath}/exportRecords?type=Holidays&ids=' + ids;
					} else {
						bootbox.alert("${selectRecordsText}");
					}
				});

				$('#ajaxResponseContainer').on("click", ".alert .close", function () {
					$(this).parent().hide();
				});

			}); //end document ready
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
		<c:if test="${not empty recordSavedMessage}">
			<div class="alert alert-success alert-dismissable">
				<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
				<spring:message code="${recordSavedMessage}"/>: ${encode:forHtmlContent(recordName)}
			</div>
		</c:if>

		<div id="ajaxResponseContainer">
			<div id="ajaxResponse">
			</div>
		</div>

		<div style="margin-bottom: 10px;">
			<div class="btn-group">
				<a class="btn btn-default" href="${pageContext.request.contextPath}/addHoliday">
					<i class="fa fa-plus"></i>
					<spring:message code="page.action.add"/>
				</a>
				<button type="button" id="deleteRecords" class="btn btn-default">
					<i class="fa fa-trash-o"></i>
					<spring:message code="page.action.delete"/>
				</button>
			</div>
			<c:if test="${sessionUser.hasPermission('migrate_records')}">
				<div class="btn-group">
					<a class="btn btn-default" href="${pageContext.request.contextPath}/importRecords?type=Holidays">
						<spring:message code="page.text.import"/>
					</a>
					<button type="button" id="exportRecords" class="btn btn-default">
						<spring:message code="page.text.export"/>
					</button>
				</div>
			</c:if>
		</div>

		<table id="holidays" class="table table-bordered table-striped table-condensed">
			<thead>
				<tr>
					<th class="noFilter"></th>
					<th><spring:message code="page.text.id"/></th>
					<th><spring:message code="page.text.name"/></th>
					<th><spring:message code="page.text.description"/></th>
					<th class="noFilter"><spring:message code="page.text.action"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="holiday" items="${holidays}">
					<tr data-id="${holiday.holidayId}" 
						data-name="${encode:forHtmlAttribute(holiday.name)}">

						<td></td>
						<td>${holiday.holidayId}</td>
						<td>${encode:forHtmlContent(holiday.name)} &nbsp;
							<t:displayNewLabel creationDate="${holiday.creationDate}"
											   updateDate="${holiday.updateDate}"/>
						</td>
						<td>${encode:forHtmlContent(holiday.description)}</td>
						<td>
							<div class="btn-group">
								<a class="btn btn-default" 
								   href="${pageContext.request.contextPath}/editHoliday?id=${holiday.holidayId}">
									<i class="fa fa-pencil-square-o"></i>
									<spring:message code="page.action.edit"/>
								</a>
								<button type="button" class="btn btn-default deleteRecord">
									<i class="fa fa-trash-o"></i>
									<spring:message code="page.action.delete"/>
								</button>
								<a class="btn btn-default" 
								   href="${pageContext.request.contextPath}/copyHoliday?id=${holiday.holidayId}">
									<i class="fa fa-copy"></i>
									<spring:message code="page.action.copy"/>
								</a>
							</div>
							<div class="btn-group">
								<button type="button" class="btn btn-default dropdown-toggle"
										data-toggle="dropdown" data-hover="dropdown"
										data-delay="100">
									<spring:message code="reports.action.more"/>
									<span class="caret"></span>
								</button>
								<ul class="dropdown-menu">
									<li>
										<a 
											href="${pageContext.request.contextPath}/recordsWithHoliday?holidayId=${holiday.holidayId}">
											<spring:message code="page.text.usage"/>
										</a>
									</li>
								</ul>
							</div>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</jsp:body>
</t:mainPageWithPanel>
