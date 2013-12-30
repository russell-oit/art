<%-- 
    Document   : header
    Created on : 17-Sep-2013, 11:45:05
    Author     : Timothy Anyona

Header that appears at the top of all pages, except the login and logs pages
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

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
				<a class="navbar-brand" href="${pageContext.request.contextPath}/app/reports.do">
					ART
				</a>
			</div>
			<div class="navbar-collapse collapse">
				<ul class="nav navbar-nav">
					<li>
						<a href="${pageContext.request.contextPath}/app/reports.do">
							<i class="fa fa-bar-chart-o"></i>
							<spring:message code="header.link.reports"/>
						</a>
					</li>
					<li>
						<a href="${pageContext.request.contextPath}/app/jobs.do">
							<i class="fa fa-clock-o"></i> 
							<spring:message code="header.link.jobs"/>
						</a>
					</li>
					<li class="dropdown">
						<a id="configure" href="#" class="dropdown-toggle" data-toggle="dropdown" data-hover="dropdown">
							<i class="fa fa-wrench"></i> 
							<spring:message code="header.link.configure"/>
							<b class="caret"></b>
						</a>
						<ul class="dropdown-menu">
							<li>
								<a href="${pageContext.request.contextPath}/app/artDatabase.do">
									<spring:message code="header.link.artDatabase"/>
								</a>
							</li>
							<li>
								<a href="${pageContext.request.contextPath}/app/settings.do">
									<spring:message code="header.link.settings"/>
								</a>
							</li>
							<li>
								<a href="${pageContext.request.contextPath}/app/datasources.do">
									<spring:message code="header.link.datasources"/>
								</a>
							</li>
							<li>
								<a href="${pageContext.request.contextPath}/app/reportsConfiguration.do">
									<spring:message code="header.link.reportsConfiguration"/>
								</a>
							</li>
							<li>
								<a href="${pageContext.request.contextPath}/app/reportGroups.do">
									<spring:message code="header.link.reportGroups"/>
								</a>
							</li>
							<li>
								<a href="${pageContext.request.contextPath}/app/users.do">
									<spring:message code="header.link.users"/>
								</a>
							</li>
							<li>
								<a href="${pageContext.request.contextPath}/app/userGroups.do">
									<spring:message code="header.link.userGroups"/>
								</a>
							</li>
							<li>
								<a href="${pageContext.request.contextPath}/app/userGroupMembership.do">
									<spring:message code="header.link.userGroupMembership"/>
								</a>
							</li>
							<li>
								<a href="${pageContext.request.contextPath}/app/accessRights.do">
									<spring:message code="header.link.accessRights"/>
								</a>
							</li>
							<li>
								<a href="${pageContext.request.contextPath}/app/adminRights.do">
									<spring:message code="header.link.adminRights"/>
								</a>
							</li>
							<li>
								<a href="${pageContext.request.contextPath}/app/rules.do">
									<spring:message code="header.link.rules"/>
								</a>
							</li>
							<li>
								<a href="${pageContext.request.contextPath}/app/ruleValues.do">
									<spring:message code="header.link.ruleValues"/>
								</a>
							</li>
							<li>
								<a href="${pageContext.request.contextPath}/app/jobsConfiguration.do">
									<spring:message code="header.link.jobsConfiguration"/>
								</a>
							</li>
							<li>
								<a href="${pageContext.request.contextPath}/app/schedules.do">
									<spring:message code="header.link.schedules"/>
								</a>
							</li>
							<li class="divider"></li>
							<li>
								<a href="${pageContext.request.contextPath}/app/serverInfo.do">
									<spring:message code="header.link.serverInfo"/>
								</a>
							</li>
						</ul>
					</li>
					<li>
						<a href="${pageContext.request.contextPath}/app/logs.do">
							<i class="fa fa-reorder"></i> 
							<spring:message code="header.link.logs"/>
						</a>
					</li>
					<li>
						<a href="${pageContext.request.contextPath}/docs">
							<i class="fa fa-book"></i> 
							<spring:message code="header.link.documentation"/>
						</a>
					</li>
					<li>
						<a href="${pageContext.request.contextPath}/logout.do">
							<i class="fa fa-sign-out"></i> 
							<spring:message code="header.link.logout"/>
						</a>
					</li>
				</ul>
				<ul class="nav navbar-nav navbar-right">
					<li class="dropdown">
						<a href="#" class="dropdown-toggle" data-toggle="dropdown" data-hover="dropdown">
							<i class="fa fa-user"></i> ${sessionUser.username} 
							<b class="caret"></b>
						</a>
						<ul class="dropdown-menu">
							<c:if test="${authenticationMethod eq internalAuthentication}">
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