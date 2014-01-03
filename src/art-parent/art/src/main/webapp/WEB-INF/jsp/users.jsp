<%-- 
    Document   : users
    Created on : 05-Nov-2013, 14:58:19
    Author     : Timothy Anyona

Display user configuration page
--%>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<spring:message code="page.title.configureUsers" var="pageTitle"/>

<spring:message code="datatables.text.showAllRows" var="dataTablesAllRowsText"/>
<spring:message code="users.message.userDeleted" var="userDeletedText"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="dialog.button.cancel" var="cancelText"/>
<spring:message code="dialog.button.ok" var="okText"/>
<spring:message code="dialog.message.deleteUser" var="deleteUserText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-8 col-md-offset-2">

	<jsp:attribute name="javascript">
		<script type="text/javascript" charset="utf-8">
			$(document).ready(function() {
				$(function() {
					$('a[id="configure"]').parent().addClass('active');
					$('a[href*="users.do"]').parent().addClass('active');
				});
				var oTable = $('#users').dataTable({
					"sPaginationType": "bs_full",
//					"bPaginate": false,
//					"sScrollY": "365px",
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

				$('#users tbody').on('click', '.delete', function() {
					var row = $(this).closest("tr"); //jquery object
					var nRow = row[0]; //dom element/node
					var aPos = oTable.fnGetPosition(nRow);
					var username = row.data("username");
					var msg;
					bootbox.confirm({
						message: "${deleteUserText}: " + username + " ?",
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
									url: "${pageContext.request.contextPath}/app/deleteUser.do",
									data: {username: username},
									success: function(response) {
										if (response.success) {
											msg="${userDeletedText}: " + username;
											$("#response").addClass("alert alert-success").html(msg);
											oTable.fnDeleteRow(aPos);
											$.notify("${userDeletedText}","success");
										} else {
											msg="<p>${errorOccurredText}</p><p>" + response.errorMessage + "</p>";
											$("#response").addClass("alert alert-danger").html(msg);
											$.notify("${errorOccurredText}","error");
										}
									},
									error: function(xhr, status, error) {
										alert(xhr.responseText);
									}
								});
							}
						}
					});
				});

			});

		</script>
	</jsp:attribute>

	<jsp:body>
		<c:if test="${not empty error}">
			<div class="alert alert-danger">
				<p><spring:message code="page.message.errorOccurred"/></p>
				<p>${fn:escapeXml(error)}</p>
			</div>
		</c:if>

		<div id="response">

		</div>

		<table id="users" class="table table-bordered table-striped table-condensed">
			<thead>
				<tr>
					<th><spring:message code="users.text.username"/></th>
					<th><spring:message code="users.text.fullName"/></th>
					<th><spring:message code="users.text.active"/></th>
					<th><spring:message code="users.text.action"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="user" items="${users}">
					<tr data-username="${user.username}">
						<td>${user.username}</td>
						<td>${user.fullName}</td>
						<td>${user.active}</td>
						<td>
							<div class="btn-group">
								<a class="btn btn-default" href="#">
									<i class="fa fa-pencil-square-o"></i>
									<spring:message code="users.action.edit"/>
								</a>
								<button type="button" class="btn btn-default delete">
									<i class="fa fa-trash-o"></i>
									<spring:message code="users.action.delete"/>
								</button>
							</div>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</jsp:body>
</t:mainPageWithPanel>

