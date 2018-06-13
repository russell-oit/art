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

<div id="pageHeader">

	<!-- Fixed navbar -->
	<div class="navbar navbar-default navbar-fixed-top">
		<div class="container-fluid">
			<div class="navbar-header">
				<button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
					<span class="icon-bar"></span>
					<span class="icon-bar"></span>
					<span class="icon-bar"></span>
				</button>
				<a class="navbar-brand" href="${pageContext.request.contextPath}">
					ART
				</a>
			</div>
			<div class="navbar-collapse collapse">
				<ul class="nav navbar-nav">
					<c:if test="${sessionUser.accessLevel.value >= 0}">
						<li>
							<a href="${pageContext.request.contextPath}/reports">
								<i class="fa fa-bar-chart-o"></i>
								<spring:message code="header.link.reports"/>
							</a>
						</li>
						<li>
							<%-- https://stackoverflow.com/questions/7368863/how-to-get-visitors-locale-in-jsp-using-spring-3s-sessionlocaleresolver --%>
							<a href="${pageContext.request.contextPath}/saiku3">
								<i class="fa fa-cubes"></i>
								<spring:message code="header.link.analytics"/>
							</a>
						</li>
						<li>
							<a href="${pageContext.request.contextPath}/jobs">
								<i class="fa fa-clock-o"></i> 
								<spring:message code="header.link.jobs"/>
							</a>
						</li>
						<li>
							<a href="${pageContext.request.contextPath}/archives">
								<i class="fa fa-archive"></i> 
								<spring:message code="header.link.archives"/>
							</a>
						</li>
						<li class="dropdown">
							<a id="selfService" href="#" class="dropdown-toggle" data-toggle="dropdown" data-hover="dropdown" data-delay="100">
								<i class="fa fa-cog"></i> 
								<spring:message code="header.link.selfService"/>
								<b class="caret"></b>
							</a>
							<ul class="dropdown-menu">
								<li>
									<a href="${pageContext.request.contextPath}/selfServiceDashboards">
										<spring:message code="header.link.selfServiceDashboards"/>
									</a>
								</li>
							</ul>
						</li>
					</c:if>
					<c:if test="${sessionUser.accessLevel.value >= 10 || sessionUser.accessLevel.value < 0}">
						<li class="dropdown">
							<a id="configure" href="#" class="dropdown-toggle" data-toggle="dropdown" data-hover="dropdown" data-delay="100">
								<i class="fa fa-wrench"></i> 
								<spring:message code="header.link.configure"/>
								<b class="caret"></b>
							</a>
							<%-- https://stackoverflow.com/questions/19227496/scrollable-menu-with-bootstrap-3-menu-expanding-its-container-when-it-should-n --%>
							<%-- https://stackoverflow.com/questions/39147226/fixed-height-for-bootstrap-pre-scrollable-div --%>
							<ul class="dropdown-menu pre-scrollable" style="max-height: 90vh">
								<c:if test="${sessionUser.accessLevel.value >= 100 || sessionUser.accessLevel.value < 0}">
									<li>
										<a href="${pageContext.request.contextPath}/artDatabase">
											<spring:message code="header.link.artDatabase"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 100}">
									<li>
										<a href="${pageContext.request.contextPath}/settings">
											<spring:message code="header.link.settings"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 80}">
									<li>
										<a href="${pageContext.request.contextPath}/datasources">
											<spring:message code="header.link.datasources"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 10}">
									<li class="divider"></li>
									<li>
										<a href="${pageContext.request.contextPath}/reportsConfig">
											<spring:message code="header.link.reportsConfiguration"/>
										</a>
									</li>
									<li>
										<a href="${pageContext.request.contextPath}/addReport">
											<spring:message code="page.title.addReport"/>
										</a>
									</li>
									<li class="divider"></li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 80}">
									<li>
										<a href="${pageContext.request.contextPath}/reportGroups">
											<spring:message code="header.link.reportGroups"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 40}">
									<li>
										<a href="${pageContext.request.contextPath}/reportGroupMembershipConfig">
											<spring:message code="header.link.reportGroupMembership"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 40 || sessionUser.accessLevel.value < 0}">
									<li>
										<a href="${pageContext.request.contextPath}/users">
											<spring:message code="header.link.users"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 40}">
									<li>
										<a href="${pageContext.request.contextPath}/userGroups">
											<spring:message code="header.link.userGroups"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 40}">
									<li>
										<a href="${pageContext.request.contextPath}/userGroupMembershipConfig">
											<spring:message code="header.link.userGroupMembership"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 30}">
									<li>
										<a href="${pageContext.request.contextPath}/accessRightsConfig">
											<spring:message code="header.link.accessRights"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 40}">
									<li>
										<a href="${pageContext.request.contextPath}/adminRightsConfig">
											<spring:message code="header.link.adminRights"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 10}">
									<li>
										<a href="${pageContext.request.contextPath}/parameters">
											<spring:message code="header.link.parameters"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 30}">
									<li>
										<a href="${pageContext.request.contextPath}/paramDefaultsConfig">
											<spring:message code="page.title.paramDefaults"/>
										</a>
									</li>
									<li>
										<a href="${pageContext.request.contextPath}/fixedParamValuesConfig">
											<spring:message code="page.title.fixedParamValues"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 80}">
									<li>
										<a href="${pageContext.request.contextPath}/rules">
											<spring:message code="header.link.rules"/>
										</a>
									</li>
									<li>
										<a href="${pageContext.request.contextPath}/ruleValuesConfig">
											<spring:message code="header.link.ruleValues"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 40}">
									<li>
										<a href="${pageContext.request.contextPath}/jobsConfig">
											<spring:message code="header.link.jobsConfiguration"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 80}">
									<li>
										<a href="${pageContext.request.contextPath}/schedules">
											<spring:message code="header.link.schedules"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 80}">
									<li>
										<a href="${pageContext.request.contextPath}/holidays">
											<spring:message code="page.title.holidays"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 80}">
									<li>
										<a href="${pageContext.request.contextPath}/destinations">
											<spring:message code="page.title.destinations"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 80}">
									<li>
										<a href="${pageContext.request.contextPath}/smtpServers">
											<spring:message code="page.title.smtpServers"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 80}">
									<li>
										<a href="${pageContext.request.contextPath}/encryptors">
											<spring:message code="page.title.encryptors"/>
										</a>
									</li>
								</c:if>
								<c:if test="${sessionUser.accessLevel.value >= 80}">
									<li class="divider"></li>
									<li>
										<a href="${pageContext.request.contextPath}/caches">
											<spring:message code="header.link.caches"/>
										</a>
									</li>
									<li>
										<a href="${pageContext.request.contextPath}/connections">
											<spring:message code="header.link.connections"/>
										</a>
									</li>
									<li>
										<a href="${pageContext.request.contextPath}/loggers">
											<spring:message code="header.link.loggers"/>
										</a>
									</li>
								</c:if>
							</ul>
						</li>
					</c:if>
					<c:if test="${sessionUser.accessLevel.value >= 80}">
						<li>
							<a href="${pageContext.request.contextPath}/logs">
								<i class="fa fa-bars"></i> 
								<spring:message code="header.link.logs"/>
							</a>
						</li>
					</c:if>
					<c:if test="${sessionUser.accessLevel.value >= 10}">
						<li>
							<a href="${pageContext.request.contextPath}/docs/index.html">
								<i class="fa fa-book"></i> 
								<spring:message code="header.link.documentation"/>
							</a>
						</li>
					</c:if>
					<c:if test="${authenticationMethod eq internalAuthentication && sessionUser.canChangePassword}">
						<li>
							<a href="${pageContext.request.contextPath}/password">
								<i class="fa fa-lock"></i> 
								<spring:message code="header.link.password"/>
							</a>
						</li>
					</c:if>
					<li>
						<a href="${pageContext.request.contextPath}/language">
							<i class="fa fa-comment"></i> 
							<spring:message code="header.link.language"/>
						</a>
					</li>
					<li>
						<form method="POST" action="${pageContext.request.contextPath}/logout">
							<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
							<button type="submit" class="btn btn-link navbar-btn">
								<i class="fa fa-sign-out"></i> 
								<spring:message code="header.link.logout"/>
							</button>
						</form>
					</li>
				</ul>
				<div class="nav navbar-nav navbar-right navbar-text">
					<i class="fa fa-user"></i> ${sessionUser.username} 
				</div>
			</div><!--/.nav-collapse -->
		</div>
	</div>
</div>