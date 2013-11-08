<%-- 
    Document   : artDatabase
    Created on : 08-Nov-2013, 09:28:05
    Author     : Timothy Anyona

Display art database configuration page
--%>

<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<spring:htmlEscape defaultHtmlEscape="true"/>

<spring:message code="page.title.configureArtDatabase" var="pageTitle" scope="page"/>

<t:mainPage title="${pageTitle}">
	<jsp:body>
		<div style="text-align: center">
			${title}
		</div>

		<div class="col-lg-6 col-lg-offset-3">
			<form:form class="form-horizontal" method="POST" action="" modelAttribute="artDatabase">
				<fieldset>
					<legend class="text-center">ART</legend>

					<c:if test="${not empty successMessage}">
						<div class="alert alert-success">
							<spring:message code="${successMessage}"/>
						</div>
					</c:if>
					<c:if test="${not empty errorMessage}">
						<div class="alert alert-danger">
							${errorMessage}
						</div>
					</c:if>

					<div class="form-group">
						<label class="control-label col-lg-4" for="driver">
							<spring:message code="artDatabase.label.jdbcDriver"/>
						</label>
						<div class="col-lg-8">
							<form:input path="driver" type="text" name="driver" id="driver" class="form-control" />
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-lg-4" for="url">
							<spring:message code="artDatabase.label.jdbcUrl"/>
						</label>
						<div class="col-lg-8">
							<form:input path="url" name="url" id="url" class="form-control" />
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-lg-4" for="username">
							<spring:message code="artDatabase.label.username"/>
						</label>
						<div class="col-lg-8">
							<form:input path="username" name="username" id="username"
										class="form-control" />
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-lg-4" for="password">
							<spring:message code="artDatabase.label.password"/>
						</label>
						<div class="col-lg-8">
							<form:password path="password" name="password" id="password" class="form-control" />
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-lg-4" for="connectionPoolTimeout">
							<spring:message code="artDatabase.label.connectionPoolTimeout"/>
						</label>
						<div class="col-lg-8">
							<form:input path="connectionPoolTimeout" name="connectionPoolTimeout"
										id="connectionPoolTimeout" class="form-control" />
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-lg-4" for="connectionTestSql">
							<spring:message code="artDatabase.label.connectionTestSql"/>
						</label>
						<div class="col-lg-8">
							<form:input path="connectionTestSql" name="connectionTestSql"
										id="connectionTestSql" class="form-control" />
						</div>
					</div>

					<div class="form-group">
						<label class="control-label col-lg-2" for="databaseType">
							<spring:message code="artDatabase.label.databaseType"/>
						</label>
						<div class="col-lg-10">
							<select name="databaseType" id="databaseType" class="form-control">
								<c:forEach var="dbType" items="${databaseTypes}">
									<option value="${dbType.key}">${dbType.value}</option>
								</c:forEach>
							</select>
						</div>
					</div>
					<div class="form-group">
						<div class="pull-right">
							<button type="submit" class="btn btn-primary">
								<spring:message code="artDatabase.button.save"/>
							</button>
						</div>
					</div>
				</fieldset>
			</form:form>
		</div>
</jsp:body>
</t:mainPage>