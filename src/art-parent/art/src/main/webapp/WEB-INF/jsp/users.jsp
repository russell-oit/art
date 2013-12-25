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

<t:mainPageWithPanel title="${pageTitle}" mainPanelTitle="${pageTitle}"
					 mainColumnClass="col-md-8 col-md-offset-2">

	<jsp:attribute name="javascript">
		<script type="text/javascript" charset="utf-8">
			$(document).ready(function() {
				$(function() {
					$('a[id="configure"]').parent().addClass('active');
					$('a[href*="users.do"]').parent().addClass('active');
				});

				$('#users').dataTable({
					"sPaginationType": "bs_full",
					"bPaginate": false,
					"sScrollY": "365px",
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
				<tr>
					<td>${user.username}</td>
					<td>${user.fullName}</td>
					<td>${user.active}</td>
					<td>
						<div class="btn-group">
							<a class="btn btn-default" href="#">
								<i class="fa fa-pencil-square-o"></i>
								<spring:message code="users.action.edit"/>
							</a>
							<a class="btn btn-default" href="#">
								<i class="fa fa-trash-o"></i>
								<spring:message code="users.action.delete"/>
							</a>
						</div>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</jsp:body>
</t:mainPageWithPanel>

