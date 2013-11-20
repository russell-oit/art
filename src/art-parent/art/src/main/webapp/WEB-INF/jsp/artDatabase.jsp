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

<spring:message code="page.title.configureArtDatabase" var="pageTitle" scope="page"/>

<t:mainPage title="${pageTitle}">
	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/art.js"></script>
		<script type="text/javascript" charset="utf-8">
			$(document).ready(function() {
				$(function() {
					$('a[href*="artDatabase.do"]').parent().addClass('active');
				});

				$(function() {
					$("[data-toggle='tooltip']").tooltip({container: 'body'});
				});
			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<div style="text-align: center">
			${title}
		</div>

		<div class="row">
			<div class="col-md-6 col-md-offset-3">
				<form:form class="form-horizontal" method="POST" action="" modelAttribute="artDatabaseForm">
					<fieldset>
						<legend class="text-center">
							<spring:message code="artDatabase.text.configureArtDatabase"/>
						</legend>

						<c:if test="${not empty success}">
							<div class="alert alert-success alert-dismissable">
								<a class="close" data-dismiss="alert" href="#">x</a>
								<spring:message code="artDatabase.message.configurationSaved"/>
							</div>
						</c:if>
						<c:if test="${not empty error}">
							<div class="alert alert-danger alert-dismissable">
								<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
								<p><spring:message code="page.message.errorOccurred"/></p>
								<p>${error}</p>
							</div>
						</c:if>

						<div class="form-group">
							<label class="control-label col-md-4" for="databaseType">
								<spring:message code="artDatabase.label.databaseType"/>
							</label>
							<div class="col-md-8">
								<div class='input-group'>
									<select name="databaseType" id="databaseType" class="form-control"
											onchange="setDatasourceFields(this.value, 'driver', 'url', 'connectionTestSql');">
										<option value="">
											<spring:message code="artDatabase.text.selectDatabaseType"/>
										</option>
										<c:forEach var="dbType" items="${databaseTypes}">
											<option value="${dbType.key}">${dbType.value}</option>
										</c:forEach>
									</select>
									<spring:message code="artDatabase.help.databaseType"
													var="help" javaScriptEscape="true" />
									<span class="input-group-btn" >
										<button class="btn btn-default" type="button"
												data-toggle="tooltip" title="${help}">
											<i class="fa fa-info"></i>
										</button>
									</span>
								</div>
							</div>
						</div>
						<div class="form-group">
							<label class="control-label col-md-4" for="driver">
								<spring:message code="artDatabase.label.jdbcDriver"/>
							</label>
							<div class="col-md-8">
								<form:input path="driver" type="text" name="driver" id="driver" class="form-control" />
							</div>
						</div>
						<div class="form-group">
							<label class="control-label col-md-4" for="url">
								<spring:message code="artDatabase.label.jdbcUrl"/>
							</label>
							<div class="col-md-8">
								<form:input path="url" name="url" id="url" class="form-control" />
							</div>
						</div>
						<div class="form-group">
							<label class="control-label col-md-4" for="username">
								<spring:message code="artDatabase.label.username"/>
							</label>
							<div class="col-md-8">
								<form:input path="username" name="username" id="username"
											class="form-control" />
							</div>
						</div>
						<div class="form-group">
							<label class="control-label col-md-4" for="password">
								<spring:message code="artDatabase.label.password"/>
							</label>
							<div class="col-md-8">
								<form:password path="password" name="password" id="password" class="form-control" />
							</div>
						</div>
						<div class="form-group">
							<label class="control-label col-md-4" for="connectionTestSql">
								<spring:message code="artDatabase.label.connectionTestSql"/>
							</label>
							<div class="col-md-8">
								<div class="input-group">
									<form:input path="connectionTestSql" name="connectionTestSql"
												id="connectionTestSql" class="form-control" />
									<spring:message code="artDatabase.help.connectionTestSql"
													var="help" />
									<span class="input-group-btn" >
										<button class="btn btn-default" type="button"
												data-toggle="tooltip" title="${help}">
											<i class="fa fa-info"></i>
										</button>
									</span>
								</div>
							</div>
						</div>
						<div class="form-group">
							<label class="control-label col-md-4" for="connectionPoolTimeout">
								<spring:message code="artDatabase.label.connectionPoolTimeout"/>
							</label>
							<div class="col-md-8">
								<div class="input-group">
									<form:input path="connectionPoolTimeout" name="connectionPoolTimeout"
												id="connectionPoolTimeout" class="form-control" />
									<spring:message code="artDatabase.help.connectionPoolTimeout"
													var="help" htmlEscape="false" />
									<span class="input-group-btn" >
										<button class="btn btn-default" type="button"
												data-toggle="tooltip" data-html="true" title="${help}">
											<i class="fa fa-info"></i>
										</button>
									</span>
									</span>
								</div>
								<form:errors path="connectionPoolTimeout" cssClass="error" />
							</div>
						</div>
						<div class="form-group">
							<label class="control-label col-md-4" for="maxPoolConnections">
								<spring:message code="artDatabase.label.maxPoolConnections"/>
							</label>
							<div class="col-md-8">
								<div class="input-group">
									<form:input path="maxPoolConnections" name="maxPoolConnections"
												id="maxPoolConnections" class="form-control" />
									<spring:message code="artDatabase.help.maxPoolConnections"
													var="help" />
									<span class="input-group-btn" >
										<button class="btn btn-default" type="button"
												data-toggle="tooltip" title="${help}">
											<i class="fa fa-info"></i>
										</button>
									</span>
								</div>
								<form:errors path="maxPoolConnections" cssClass="error" />
							</div>
						</div>
						<div class="form-group">
							<div class="col-md-12">
								<button type="submit" class="btn btn-primary pull-right">
									<spring:message code="artDatabase.button.save"/>
								</button>
							</div>
						</div>
					</fieldset>
				</form:form>
			</div>
			<c:if test="${not empty initialSetup}">
				<div class="col-md-3">
					<div class="alert alert-info">
						<p>
							Welcome to the ART Reporting Tool. You need to configure the
							<b>ART Database</b> before being able to use ART. The ART Database
							stores data used by the application e.g. users, report definitions etc.
						</p>
						<p>
							After saving the ART Database configuration, use the
							<b>Configure | Users</b> menu to create some users. Create at least
							one Super Admin user which you can use to administer all aspects
							of the application. After creating users, <b>Log Out</b> and log in 
							using one of the users, and continue using the application.
						</p>
						<p>
							A demo database is provided with a few sample reports.
							To use it, select <b>Demo</b> as the Database Type and Save,
							The demo database has 2 users with username/password of admin/admin
							and auser/auser.
						</p>
					</div>
				</div>
			</c:if>
		</div>
	</jsp:body>
</t:mainPage>