<%-- 
    Document   : login
    Created on : 18-Sep-2013, 16:56:48
    Author     : Timothy Anyona

Login page
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form" %>

<c:set var="localeCode" value="${pageContext.response.locale}" />

<spring:message code="page.title.login" var="pageTitle" scope="page"/>

<t:genericPage title="ART - ${pageTitle}">
	<jsp:attribute name="metaContent">
		<meta http-equiv="pragma" content="no-cache">
		<meta http-equiv="cache-control" content="no-cache, must-revalidate">
	</jsp:attribute>

	<jsp:attribute name="footer">
		<jsp:include page="/WEB-INF/jsp/footer.jsp"/>
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-1.10.2.min.js"></script>
		<script type="text/javascript">
			$(document).ready(function() {
				$('#username').focus();
			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<div class="row">
			<div class="well col-md-6 col-md-offset-3 spacer60">
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
						<c:if test="${not empty details}">
							<div class="alert alert-danger">
								${fn:escapeXml(details)}
							</div>
						</c:if>
						<c:if test="${not empty error}">
							<div class="alert alert-danger">
								${fn:escapeXml(error)}
							</div>
						</c:if>
						<c:if test="${not empty result}">
							<div class="alert alert-danger">
								<p><spring:message code="${result.message}"/></p>
								<p>${fn:escapeXml(result.details)}</p>
								<p>${fn:escapeXml(result.error)}</p>
							</div>
						</c:if>

						<c:if test="${authenticationMethod eq windowsDomainAuthentication}">
							<div class="form-group">
								<label class="control-label col-md-2" for="windowsDomain">
									<spring:message code="login.label.domain"/>
								</label>
								<div class="col-md-10">
									<select name="windowsDomain" id="windowsDomain" class="form-control">
										<c:forTokens var="domain" items='${domains}' delims=",">
											<option value="${fn:escapeXml(domain)}" ${domain == selectedDomain ? "selected" : ""}>${fn:escapeXml(domain)}</option>
										</c:forTokens>
									</select>
								</div>
							</div>
						</c:if>
						<div class="form-group">
							<label class="control-label col-md-2" for="username">
								<spring:message code="page.label.username"/>
							</label>
							<div class="col-md-10">
								<input type="text" name="username" id="username" class="form-control" value="${fn:escapeXml(selectedUsername)}">
							</div>
						</div>
						<div class="form-group">
							<label class="control-label col-md-2" for="password">
								<spring:message code="page.label.password"/>
							</label>
							<div class="col-md-10">
								<input type="password" name="password" id="password" class="form-control">
							</div>
						</div>
						<div class="form-group">
							<label class="control-label col-md-2" for="lang">
								<spring:message code="login.label.language"/>
							</label>
							<div class="col-md-10">
								<select name="lang" id="lang" class="form-control">
									<option value="en">English</option>
									<c:forEach var="language" items="${languages}">
										<option value="${language.key}" ${localeCode == language.key ? "selected" : ""}>${language.value}</option>
									</c:forEach>
								</select>
							</div>
						</div>
						<div class="form-group">
							<div class="col-md-10 col-md-offset-2">
								<button type="submit" class="btn btn-default">
									<spring:message code="login.button.login"/>
								</button>
							</div>
						</div>
					</fieldset>
				</form>
			</div>
		</div>
	</jsp:body>
</t:genericPage>
