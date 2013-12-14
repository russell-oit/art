<%-- 
    Document   : users
    Created on : 05-Nov-2013, 14:58:19
    Author     : Timothy Anyona

Display user configuration page
--%>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<spring:message code="page.title.configureUsers" var="pageTitle" scope="page"/>

<spring:message code="datatables.text.showAllRows" var="dataTablesAllRowsText" scope="page"/>

<t:configurationPage title="${pageTitle}">
	<jsp:attribute name="javascript">
		<script type="text/javascript" charset="utf-8">
			$(document).ready(function() {
				$(function() {
					$('a[href*="users.do"]').parent().addClass('active');
				});
				
				$('.datatable').dataTable({
					"sPaginationType": "bs_full",
					"aaSorting": [],
					"aLengthMenu": [[5, 10, 25, -1], [5, 10, 25, "All"]],
					"iDisplayLength": 10,
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
			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<div>
			<table class="datatable table table-bordered table-striped table-condensed">
				<caption>test caption</caption>
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
		</div>
	</jsp:body>
</t:configurationPage>

