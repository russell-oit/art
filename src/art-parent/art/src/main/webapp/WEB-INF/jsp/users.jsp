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

<t:configurationPage title="${pageTitle}">
	<jsp:body>
		<div>
			<table class="datatable table table-bordered table-striped">
				<thead>
					<tr>
						<th><spring:message code="users.text.username"/></th>
						<th><spring:message code="users.text.fullName"/></th>
						<th><spring:message code="users.text.active"/></th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="user" items="${users}">
						<tr>
							<td>${user.username}</td>
							<td>${user.fullName}</td>
							<td>${user.active}</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</div>
	</jsp:body>
</t:configurationPage>

