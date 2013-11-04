<%-- 
    Document   : header
    Created on : 17-Sep-2013, 11:45:05
    Author     : Timothy Anyona

Header that appears at the top of all pages, except the login and logs pages
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<spring:htmlEscape defaultHtmlEscape="true"/>

<c:set var="INTERNAL_AUTHENTICATION"
value="<%= art.login.AuthenticationMethod.Internal.getValue()%>"/>

<div id="header">
	<!-- Fixed navbar -->
	<div class="navbar navbar-default navbar-fixed-top">
		<div class="container">
			<div class="navbar-header">
				<button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
					<span class="icon-bar"></span>
					<span class="icon-bar"></span>
					<span class="icon-bar"></span>
				</button>
				<a class="navbar-brand" href="${pageContext.request.contextPath}/user/showGroups.jsp">
					ART
				</a>
			</div>
			<div class="navbar-collapse collapse">
				<ul class="nav navbar-nav">
					<li>
						<a href="${pageContext.request.contextPath}/app/reports.do">
							<i class="fa fa-bar-chart-o"></i> <spring:message code="header.link.reports"/>
						</a>
					</li>
					<li>
						<a href="${pageContext.request.contextPath}/app/jobs.do">
							<i class="fa fa-clock-o"></i> <spring:message code="header.link.jobs"/>
						</a>
					</li>
					<li class="dropdown">
						<a href="#" class="dropdown-toggle" data-toggle="dropdown" data-hover="dropdown">
							<i class="fa fa-wrench"></i> <spring:message code="header.link.configure"/> <b class="caret"></b>
						</a>
						<ul class="dropdown-menu">
							<li>
								<a href="${pageContext.request.contextPath}/app/configureArtDatabase.do">
									<spring:message code="header.link.configureArtDatabase"/>
								</a>
							</li>
							<li>
								<a href="${pageContext.request.contextPath}/app/configureSettings.do">
									<spring:message code="header.link.configureSettings"/>
								</a>
							</li>
							<li>
								<a href="${pageContext.request.contextPath}/app/configureDatasources.do">
									<spring:message code="header.link.configureDatasources"/>
								</a>
							</li>
							<li>
								<a href="${pageContext.request.contextPath}/app/configureReports.do">
									<spring:message code="header.link.configureReports"/>
								</a>
							</li>
							<li>
								<a href="${pageContext.request.contextPath}/app/configureReportGroups.do">
									<spring:message code="header.link.configureReportGroups"/>
								</a>
							</li>
							<li>
								<a href="${pageContext.request.contextPath}/app/configureUsers.do">
									<spring:message code="header.link.configureUsers"/>
								</a>
							</li>
							<li>
								<a href="${pageContext.request.contextPath}/app/configureUserGroups.do">
									<spring:message code="header.link.configureUserGroups"/>
								</a>
							</li>
							<li>
								<a href="${pageContext.request.contextPath}/app/configureUserGroupMembership.do">
									<spring:message code="header.link.configureUserGroupMembership"/>
								</a>
							</li>
							<li>
								<a href="${pageContext.request.contextPath}/app/configureAccessRights.do">
									<spring:message code="header.link.configureAccessRights"/>
								</a>
							</li>
							<li>
								<a href="${pageContext.request.contextPath}/app/configureAdminRights.do">
									<spring:message code="header.link.configureAdminRights"/>
								</a>
							</li>
							<li>
								<a href="${pageContext.request.contextPath}/app/configureRules.do">
									<spring:message code="header.link.configureRules"/>
								</a>
							</li>
							<li>
								<a href="${pageContext.request.contextPath}/app/configureRuleValues.do">
									<spring:message code="header.link.configureRuleValues"/>
								</a>
							</li>
							<li>
								<a href="${pageContext.request.contextPath}/app/configureJobs.do">
									<spring:message code="header.link.configureJobs"/>
								</a>
							</li>
							<li>
								<a href="${pageContext.request.contextPath}/app/configureSchedules.do">
									<spring:message code="header.link.configureSchedules"/>
								</a>
							</li>
						</ul>
					</li>
					<li>
						<a href="${pageContext.request.contextPath}/app/logs" target="_blank">
							<i class="fa fa-reorder"></i> <spring:message code="header.link.logs"/>
						</a>
					</li>
					<li>
						<a href="${pageContext.request.contextPath}/docs" target="_blank">
							<i class="fa fa-info"></i> <spring:message code="header.link.documentation"/>
						</a>
					</li>
					<li>
						<a href="${pageContext.request.contextPath}/logout.do">
							<i class="fa fa-sign-out"></i> <spring:message code="header.link.logout"/>
						</a>
					</li>
				</ul>
				<ul class="nav navbar-nav navbar-right">
					<li class="dropdown">
						<a href="#" class="dropdown-toggle" data-toggle="dropdown" data-hover="dropdown">
							<i class="fa fa-user"></i> ${sessionUser.username} <b class="caret"></b>
						</a>
						<ul class="dropdown-menu">
							<c:if test="${authenticationMethod eq INTERNAL_AUTHENTICATION}">
								<li>
									<a href="${pageContext.request.contextPath}/app/changePassword.do">
										<spring:message code="header.link.changePassword"/>
									</a>
								</li>
							</c:if>
						</ul>
					</li>
				</ul>
			</div><!--/.nav-collapse -->
		</div>
	</div>
</div>