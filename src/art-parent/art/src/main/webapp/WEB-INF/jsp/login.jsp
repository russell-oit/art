<%-- 
    Document   : login
    Created on : 18-Sep-2013, 16:56:48
    Author     : Timothy Anyona

Login page
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib  uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<spring:htmlEscape defaultHtmlEscape="true"/>

<c:set var="WINDOWS_DOMAIN_AUTHENTICATION"
value="<%= art.login.AuthenticationMethod.WindowsDomain.getValue()%>"/>

<c:set var="localeCode" value="${pageContext.response.locale}" />

<spring:message code="page.title.login" var="pageTitle" scope="page"/>

<t:genericPage title="ART - ${pageTitle}">
	<jsp:attribute name="metaContent">
		<meta http-equiv="pragma" content="no-cache">
		<meta http-equiv="cache-control" content="no-cache, must-revalidate">
	</jsp:attribute>

	<jsp:attribute name="pageFooter">
		<jsp:include page="/WEB-INF/jsp/footer.jsp"/>
	</jsp:attribute>

	<jsp:attribute name="pageJavascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-1.10.2.min.js"></script>
		<script type="text/javascript">
			$(document).ready(function() {
				$('#username').focus();
			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<div class="well col-lg-6 col-lg-offset-3">
			<form class="form-horizontal" method="POST" action="">
				<fieldset>
					<legend class="text-center">ART</legend>
					<div class="form-group">
						<img src="${pageContext.request.contextPath}/images/art-64px.jpg"
							 alt="" class="img-responsive centered">
					</div>

					<c:if test="${not empty message}">
						<div class="alert alert-danger">
							<spring:message code="${message}"/>
						</div>
					</c:if>
					<c:if test="${not empty autoLoginMessage}">
						<div class="alert alert-danger">
							<spring:message code="${autoLoginMessage}" arguments="${autoLoginUser}"/>
						</div>
					</c:if>
					<c:if test="${not empty messageDetails}">
						<div class="alert alert-danger">
							${messageDetails}
						</div>
					</c:if>

					<c:if test="${authenticationMethod eq WINDOWS_DOMAIN_AUTHENTICATION}">
						<div class="form-group">
							<label class="control-label col-lg-2" for="windowsDomain">
								<spring:message code="login.label.domain"/>
							</label>
							<div class="col-lg-10">
								<select name="windowsDomain" id="windowsDomain" class="form-control">
									<c:forTokens var="domain" items='${domains}' delims=",">
										<option value="${domain}" ${domain == selectedDomain ? "selected" : ""}>${domain}</option>
									</c:forTokens>
								</select>
							</div>
						</div>
					</c:if>
					<div class="form-group">
						<label class="control-label col-lg-2" for="username">
							<spring:message code="login.label.username"/>
						</label>
						<div class="col-lg-10">
							<input type="text" name="username" id="username" class="form-control">
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-lg-2" for="password">
							<spring:message code="login.label.password"/>
						</label>
						<div class="col-lg-10">
							<input type="password" name="password" id="password" class="form-control">
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-lg-2" for="lang">
							<spring:message code="login.label.language"/>
						</label>
						<div class="col-lg-10">
							<select name="lang" id="lang" class="form-control">
								<option value="en">English</option>
								<c:forEach var="language" items="${languages}">
									<option value="${language.key}" ${localeCode == language.key ? "selected" : ""}>${language.value}</option>
								</c:forEach>
							</select>
						</div>
					</div>
					<div class="form-group">
						<div class="col-lg-10 col-lg-offset-2">
							<button type="submit" class="btn btn-default">
								<spring:message code="login.button.login"/>
							</button>
						</div>
					</div>
				</fieldset>
			</form>
		</div>
	</jsp:body>
</t:genericPage>
