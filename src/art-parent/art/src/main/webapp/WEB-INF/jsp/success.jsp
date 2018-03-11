<%-- 
    Document   : success
    Created on : 23-Dec-2013, 11:29:05
    Author     : Timothy Anyona

Page to display success message
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.success" var="pageTitle"/>

<t:mainPage title="${pageTitle}">
	<jsp:body>
		<c:if test="${not empty message}">
			<div class="row">
				<div class="col-md-6 col-md-offset-3">
					<div class="alert alert-success text-center">
						<p>
							<spring:message code="${message}"/>
						</p>
						<c:if test="${not empty exportFileName}">
							<div style="text-align: center">
								<a type="application/octet-stream" href="${pageContext.request.contextPath}/export/records/${encode:forUriComponent(exportFileName)}">
									<spring:message code="page.link.download"/>
								</a>
							</div>

							<c:set var="reportFileName" value="${exportFileName}" scope="session"/>

							<script type="text/javascript">
								var url = "${pageContext.request.contextPath}/export/records/${encode:forJavaScript(encode:forUriComponent(exportFileName))}";
									window.open(url);
							</script>
						</c:if>
					</div>
				</div>
			</div>
		</c:if>

		<c:if test="${error != null}">
			<div class="alert alert-danger alert-dismissable">
				<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
				<p><spring:message code="page.message.errorOccurred"/></p>
				<c:if test="${showErrors}">
					<p>${encode:forHtmlContent(error)}</p>
				</c:if>
			</div>
		</c:if>
	</jsp:body>
</t:mainPage>
