<%-- 
    Document   : textInput
    Created on : 08-Mar-2016, 17:04:08
    Author     : Timothy Anyona

Display report parameter that uses text input
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<input type='text' class="form-control"
	   placeholder="${encode:forHtmlAttribute(reportParam.parameter.getLocalizedPlaceholderText(requestContext.locale))}"
	   name="${encode:forHtmlAttribute(reportParam.htmlElementName)}"
	   id="${encode:forHtmlAttribute(reportParam.htmlElementName)}"
	   value="${encode:forHtmlAttribute(reportParam.getHtmlValueWithLocale(requestContext.locale))}">

<c:if test="${not empty reportParam.parameter.template}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${encode:forHtmlAttribute(reportParam.parameter.template)}"></script>
</c:if>

<t:addRobinHerbotsMask reportParam="${reportParam}"/>
