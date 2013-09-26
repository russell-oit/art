<%-- 
    Document   : login
    Created on : 18-Sep-2013, 16:56:48
    Author     : Timothy Anyona

Login page
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<spring:htmlEscape defaultHtmlEscape="true"/>

<t:genericPage title="ART - Login">
	<jsp:attribute name="metaContent">
		<meta http-equiv="pragma" content="no-cache">
        <meta http-equiv="cache-control" content="no-cache, must-revalidate">
	</jsp:attribute>

	<jsp:attribute name="pageFooter">
		<jsp:include page="/WEB-INF/jsp/footer.jsp"/>
	</jsp:attribute>

	<jsp:attribute name="pageJavascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-1.10.2.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap.min.js"></script>
		<script type="text/javascript">
			$(document).ready(function() {
				$('#username').focus();
			});
		</script>
	</jsp:attribute>

	<jsp:body>

		<div class="well col-lg-6 col-lg-offset-3">

			<form class="form-horizontal" method="POST" action="" accept-charset="UTF-8">
				<fieldset>
					<legend class="text-center">ART</legend>
					<div class="form-group">
						<div class="col-lg-12">
							<img src="${pageContext.request.contextPath}/images/art-64px.jpg"
								 alt="ART" class="img-responsive" style="margin: 0 auto">
						</div>
					</div>
					<div style="color:red">
						${requestScope.message}
					</div>
					<c:if test="${not empty standardMessage}">
						<div id="message" style="color:red"><spring:message code="${standardMessage}"/></div>
					</c:if>
					<c:if test="${not empty autoLoginMessage}">
						<div id="autologinMessage" style="color:red"><spring:message code="${autoLoginMessage}" arguments="${username}"/></div>
					</c:if>

					<c:if test="${loginMode eq 'windowsDomain'}">
						<div class="form-group">
							<label class="control-label col-lg-2" for="windowsDomain">Domain</label>
							<div class="col-lg-10">
								<select name="windowsDomain" id="windowsDomain" class="form-control">
									<c:forTokens var="domain" items='${domains}' delims=",">
										<option value="${domain}">${domain}</option>
									</c:forTokens>
								</select>
							</div>
						</div>
					</c:if>
					<div class="form-group">
						<label class="control-label col-lg-2" for="username">Username</label>
						<div class="col-lg-10">
							<input type="text" name="username" id="username" class="form-control input-xlarge">
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-lg-2" for="password">Password</label>
						<div class="col-lg-10">
							<input type="password" name="password" id="password" class="form-control input-xlarge">
						</div>
					</div>
					<div class="form-group">
						<div class="col-lg-10 col-lg-offset-2">
							<button type="submit" class="btn btn-default">Login</button>
						</div>
					</div>
				</fieldset>
			</form>
		</div>
	</div>


</jsp:body>
</t:genericPage>
