<%-- 
    Document   : showTextReport
    Created on : 28-Oct-2014, 11:23:49
    Author     : Timothy Anyona

display text report output
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<t:mainPage title="${reportName}">
	<jsp:body>
		<c:if test="${not empty reportName}">
			<div class="reportBanner">
				<encode:forHtmlContent value="${reportName}"/>
			</div>
		</c:if>
		<div id="reportOutput" class="col-md-10 col-md-offset-1">
			<jsp:include page="/WEB-INF/jsp/showTextInline.jsp"/>
		</div>
	</jsp:body>
</t:mainPage>
