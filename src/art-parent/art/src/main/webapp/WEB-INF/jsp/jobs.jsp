<%-- 
    Document   : jobs
    Created on : 02-Oct-2013, 11:41:32
    Author     : Timothy Anyona

Display user jobs and jobs configuration
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<c:choose>
	<c:when test="${action == 'config'}">
		<spring:message code="page.title.jobsConfiguration" var="pageTitle"/>
	</c:when>
	<c:otherwise>
		<spring:message code="page.title.jobs" var="pageTitle"/>
	</c:otherwise>
</c:choose>

<spring:message code="datatables.text.showAllRows" var="dataTablesAllRowsText"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="dialog.button.cancel" var="cancelText"/>
<spring:message code="dialog.button.ok" var="okText"/>
<spring:message code="dialog.message.deleteRecord" var="deleteRecordText"/>
<spring:message code="page.message.recordDeleted" var="recordDeletedText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-12">

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/notify-combined-0.3.1.min.js"></script>
		<script type="text/javascript" charset="utf-8">
			$(document).ready(function() {
				$(function() {
					$('a[id="configure"]').parent().addClass('active');
					$('a[href*="jobs.do"]').parent().addClass('active');
				});

				var oTable = $('#jobs').dataTable({
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

				$('#jobs tbody').on('click', '.delete', function() {
					var row = $(this).closest("tr"); //jquery object
					var nRow = row[0]; //dom element/node
					var name = escapeHtmlContent(row.data("name"));
					var id = row.data("id");
					var msg;
					bootbox.confirm({
						message: "${deleteRecordText}: <b>" + name + "</b>",
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
									dataType: "json",
									url: "${pageContext.request.contextPath}/app/deleteJob.do",
									data: {id: id},
									success: function(response) {
										if (response.success) {
											oTable.fnDeleteRow(nRow);

											msg = alertCloseButton + "${recordDeletedText}: " + name;
											$("#ajaxResponse").attr("class", "alert alert-success alert-dismissable").html(msg);
											$.notify("${recordDeletedText}", "success");
										} else {
											msg = alertCloseButton + "<p>${errorOccurredText}</p><p>" + escapeHtmlContent(response.errorMessage) + "</p>";
											$("#ajaxResponse").attr("class", "alert alert-danger alert-dismissable").html(msg);
											$.notify("${errorOccurredText}", "error");
										}
									},
									error: function(xhr, status, error) {
										bootbox.alert(xhr.responseText);
									}
								}); //end ajax
							} //end if result
						} //end bootbox callback
					}); //end bootbox confirm
				}); //end on click

			}); //end document ready
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
		<c:if test="${not empty recordSavedMessage}">
			<div class="alert alert-success alert-dismissable">
				<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
				<spring:message code="${recordSavedMessage}"/>: ${encode:forHtmlContent(recordName)}
			</div>
		</c:if>

		<div id="ajaxResponse">
		</div>

		<c:if test="${action == 'config'}">
			<div style="margin-bottom: 10px;">
				<a class="btn btn-default" href="${pageContext.request.contextPath}/app/addJob.do">
					<i class="fa fa-plus"></i>
					<spring:message code="page.action.add"/>
				</a>
			</div>
		</c:if>

		<table id="jobs" class="table table-bordered table-striped table-condensed">
			<thead>
				<tr>
					<th><spring:message code="page.text.id"/></th>
					<th><spring:message code="page.text.name"/></th>
					<th><spring:message code="jobs.text.lastEndDate"/></th>
					<th><spring:message code="jobs.text.result"/></th>
					<th><spring:message code="jobs.text.nextRunDate"/></th>
					<th><spring:message code="page.text.action"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="job" items="${jobs}">
					<tr data-id="${job.jobId}" 
						data-name="${encode:forHtmlAttribute(job.name)}">

						<td>${job.jobId}</td>
						<td>${encode:forHtmlContent(job.name)}</td>
						<td><t:displayDate date="${job.lastEndDate}"/></td>
						<td>
							<a type="application/octet-stream" 
							   href="${pageContext.request.contextPath}/export/jobs/${job.lastFileName}">
								${job.lastFileName}
							</a>
							<br>
							${job.lastRunDetails}
						</td>
						<td><t:displayDate date="${job.nextRunDate}"/></td>
						<td>
							<c:if test="${sessionUser.userId == job.userId || action == 'config'}">
								<div class="btn-group">
									<a class="btn btn-default" 
									   href="${pageContext.request.contextPath}/app/editJob.do?id=${job.jobId}">
										<i class="fa fa-pencil-square-o"></i>
										<spring:message code="page.action.edit"/>
									</a>
									<button type="button" class="btn btn-default delete">
										<i class="fa fa-trash-o"></i>
										<spring:message code="page.action.delete"/>
									</button>
									<button type="button" class="btn btn-default run">
										<i class="fa fa-bolt"></i>
										<spring:message code="jobs.action.run"/>
									</button>
								</div>
							</c:if>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</jsp:body>
</t:mainPageWithPanel>

