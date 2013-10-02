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
	value="<%= art.enums.AuthenticationMethod.Internal.getValue() %>"/>


<%-- need buffer with fixed navbar so that main body content isn't hidden under it
<div id="fix-for-navbar-fixed-top-spacing" style="height: 60px;">&nbsp;</div>
--%>
<%-- or add padding-top to the main html body tag --%>

<div id="pageHeader">

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
					<li class="active">
						<a href="${pageContext.request.contextPath}/app/home.do">
							<i class="icon-home"></i> <spring:message code="header.link.home"/>
						</a>
					</li>
					<li>
						<a href="${pageContext.request.contextPath}/app/admin.do">
							<i class="icon-wrench"></i> <spring:message code="header.link.admin"/>
						</a>
					</li>
					<li>
						<a href="${pageContext.request.contextPath}/app/myJobs.do">
							<i class="icon-time"></i> <spring:message code="header.link.myJobs"/>
						</a>
					</li>
					<li>
						<a href="${pageContext.request.contextPath}/app/sharedJobs.do">
							<i class="icon-exchange"></i> <spring:message code="header.link.sharedJobs"/>
						</a>
					</li>
					<li>
						<a href="${pageContext.request.contextPath}/app/jobArchives.do">
							<i class="icon-archive"></i> <spring:message code="header.link.jobArchives"/>
						</a>
					</li>
					<li>
						<a href="${pageContext.request.contextPath}/admin/logs" target="_blank">
							<i class="icon-reorder"></i> <spring:message code="header.link.logs"/>
						</a>
					</li>
					<li>
						<a href="${pageContext.request.contextPath}/help.jsp" target="_blank">
							<i class="icon-info"></i> <spring:message code="header.link.help"/>
						</a>
					</li>
					<li>
						<a href="${pageContext.request.contextPath}/logout.do">
							<i class="icon-signout"></i> <spring:message code="header.link.logout"/>
						</a>
					</li>
				</ul>
				<ul class="nav navbar-nav navbar-right">
					<li class="dropdown">
						<a href="#" class="dropdown-toggle" data-toggle="dropdown">
							<i class="icon-user"></i> ${sessionUser.username} <b class="caret"></b>
						</a>
						<ul class="dropdown-menu">
							<c:if test="${authenticationMethod eq INTERNAL_AUTHENTICATION}">
								<li>
									<a href="${pageContext.request.contextPath}/changePassword.do">
										<spring:message code="header.link.changePassword"/>
									</a>
								</li>
							</c:if>
							<li>
								<a href="${pageContext.request.contextPath}/changeLanguage.do">
									<spring:message code="header.link.changeLanguage"/>
								</a>
							</li>
						</ul>
					</li>
				</ul>
			</div><!--/.nav-collapse -->
		</div>
	</div>
</div>