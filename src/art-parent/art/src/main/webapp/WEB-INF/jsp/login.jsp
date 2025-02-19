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
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.login" var="pageTitle"/>

<t:genericPage title="${pageTitle} - ART">
	<jsp:attribute name="metaContent">
		<meta http-equiv="pragma" content="no-cache">
		<meta http-equiv="cache-control" content="no-cache, must-revalidate">
	</jsp:attribute>

	<jsp:attribute name="footer">
		<jsp:include page="/WEB-INF/jsp/footer.jsp"/>
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-1.12.4.min.js"></script>
		
		<script type="text/javascript">
			$(document).ready(function () {
				$('#username').trigger("focus");
			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<div class="row">
			<div class="well col-md-6 col-md-offset-3 spacer60">
				<spring:url var="formUrl" value="/login"/>
				<form class="form-horizontal" method="POST" action="${formUrl}">
					<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
					<fieldset>
						<legend class="text-center">ART</legend>
						<div class="form-group">
							<img src="${pageContext.request.contextPath}/images/art-64px.jpg"
								 alt="" class="img-responsive centered">
						</div>

						<c:if test="${invalidAutoLogin != null}">
							<div class="alert alert-danger alert-dismissable">
								<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
								<spring:message code="login.message.invalidAutoLoginUser" arguments="${autoLoginUser}"/>
							</div>
						</c:if>
								 
						<c:if test="${invalidCasLogin != null}">
							<div class="alert alert-danger alert-dismissable">
								<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
								<spring:message code="login.message.invalidCasLoginUser" arguments="${casLoginUser}"/>
							</div>
						</c:if>

						<c:choose>
							<c:when test="${showErrors}">
								<c:if test="${error != null}">
									<div class="alert alert-danger alert-dismissable">
										<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
										<p><spring:message code="page.message.errorOccurred"/></p>
										<p>${encode:forHtmlContent(error)}</p>
									</div>
								</c:if>
								<c:if test="${result != null}">
									<div class="alert alert-danger alert-dismissable">
										<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
										<p><spring:message code="${result.message}"/></p>
										<p>${encode:forHtmlContent(result.error)}</p>
									</div>
								</c:if>
								<c:if test="${not empty message}">
									<div class="alert alert-danger alert-dismissable">
										<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
										<p><spring:message code="${message}"/></p>
									</div>
								</c:if>
							</c:when>
							<c:otherwise>
								<c:if test="${invalidLogin != null}">
									<div class="alert alert-danger alert-dismissable">
										<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
										<spring:message code="login.message.invalidCredentials"/>
									</div>
								</c:if>
							</c:otherwise>
						</c:choose>

						<c:if test="${authenticationMethod == windowsDomainAuthentication}">
							<div class="form-group">
								<label class="control-label col-md-2" for="windowsDomain">
									<spring:message code="login.label.domain"/>
								</label>
								<div class="col-md-10">
									<select name="windowsDomain" id="windowsDomain" class="form-control">
										<c:forTokens var="domain" items='${domains}' delims=",">
											<option value="${encode:forHtmlAttribute(domain)}"
													${domain == selectedDomain ? "selected" : ""}>
												${encode:forHtmlContent(domain)}
											</option>
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
								<input type="text" name="username" id="username"
									   maxlength="50" class="form-control"
									   value="${encode:forHtmlAttribute(selectedUsername)}">
							</div>
						</div>
						<div class="form-group">
							<label class="control-label col-md-2" for="password">
								<spring:message code="page.label.password"/>
							</label>
							<div class="col-md-10">
								<input type="password" name="password" id="password"
									   maxlength="70" class="form-control">
							</div>
						</div>
						<div class="form-group">
							<label class="control-label col-md-2" for="lang">
								<spring:message code="page.label.language"/>
							</label>
							<div class="col-md-10">
								<%-- select must have name of "lang" as per configuration in dispatcher-servlet.xml --%>
								<c:set var="localeCode" value="${pageContext.response.locale}"/>
								<select name="lang" id="lang" class="form-control">
									<c:forEach var="language" items="${languages}">
										<option value="${encode:forHtmlAttribute(language.value)}" ${localeCode == language.value ? "selected" : ""}>${encode:forHtmlContent(language.key)}</option>
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
