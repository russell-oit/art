<%-- 
    Document   : users
    Created on : 05-Nov-2013, 14:58:19
    Author     : Timothy Anyona

Display user configuration page
--%>

>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib  uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<spring:htmlEscape defaultHtmlEscape="true"/>

<spring:message code="page.title.configureUsers" var="pageTitle" scope="page"/>

<spring:message code="datatables.text.all" var="datatablesAllText" scope="page"/>

<t:configurationPage title="${pageTitle}" datatablesAllText="${datatablesAllText}">
	<jsp:attribute name="javascript">
		<script type="text/javascript" charset="utf-8">
			$(document).ready(function() {
				$(function() {
					$('a[href*="users.do"]').parent().addClass('active');
				});
			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<div>
			<table class="datatable table table-bordered table-striped table-condensed">
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
								<a href="#" data-toggle="tooltip" title="<spring:message code="users.action.edit"/>">
									<i class="fa fa-pencil-square-o"></i>
								</a>
								<a href="#" data-toggle="tooltip" title="<spring:message code="users.action.delete"/>">
									<i class="fa fa-trash-o"></i>
								</a>
							</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</div>
	</jsp:body>
</t:configurationPage>

