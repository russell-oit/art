<%-- 
    Document   : header
    Created on : 17-Sep-2013, 11:45:05
    Author     : Timothy Anyona

Header that appears at the top of all pages, except the login and logs pages
--%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

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
						<a href="${pageContext.request.contextPath}/user/home.do">
							<i class="icon-home"></i> <fmt:message key="home"/>
						</a>
					</li>
					<li>
						<a href="${pageContext.request.contextPath}/admin/admin.do">
							<i class="icon-wrench"></i> <fmt:message key="admin"/>
						</a>
					</li>
					<li>
						<a href="${pageContext.request.contextPath}/user/myJobs.do">
							<i class="icon-time"></i> <fmt:message key="myJobs"/>
						</a>
					</li>
					<li>
						<a href="${pageContext.request.contextPath}/user/sharedJobs.do">
							<i class="icon-th-large"></i> <fmt:message key="sharedJobs"/>
						</a>
					</li>
					<li>
						<a href="${pageContext.request.contextPath}/user/jobArchives.do">
							<i class="icon-archive"></i> <fmt:message key="jobArchives"/>
						</a>
					</li>
					<li>
						<a href="${pageContext.request.contextPath}/admin/logs">
							<i class="icon-reorder"></i> <fmt:message key="logs"/>
						</a>
					</li>
					<li>
						<a href="${pageContext.request.contextPath}/help.jsp" target="_blank">
							<i class="icon-info"></i> <fmt:message key="help"/>
						</a>
					</li>
					<li>
						<a href="${pageContext.request.contextPath}/logOff.do">
							<i class="icon-signout"></i> <fmt:message key="logOff"/>
						</a>
					</li>
				</ul>
				<ul class="nav navbar-nav navbar-right">
					<li class="dropdown">
						<a href="#" class="dropdown-toggle" data-toggle="dropdown">
							<i class="icon-user"></i> ${sessionUser.username} <b class="caret"></b>
						</a>
						<ul class="dropdown-menu">
							<li>
								<a href="${pageContext.request.contextPath}/changePassword.do">
									<fmt:message key="changePassword"/>
								</a>
							</li>
							<li>
								<a href="${pageContext.request.contextPath}/changeLanguage.do">
									<fmt:message key="changeLanguage"/>
								</a>
							</li>
						</ul>
					</li>
				</ul>
			</div><!--/.nav-collapse -->
		</div>
	</div>
</div>