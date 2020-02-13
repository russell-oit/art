<%-- 
    Document   : textareaInput
    Created on : 08-Mar-2016, 17:19:51
    Author     : Timothy Anyona

Display report parameter that uses textarea input
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<%-- https://stackoverflow.com/questions/18106503/bootstrap-textarea-adding-space-characters-inside-text-field --%>
<%-- https://stackoverflow.com/questions/2202999/why-is-textarea-filled-with-mysterious-white-spaces --%>
<textarea rows="5" class="form-control"
		  placeholder="${encode:forHtmlAttribute(reportParam.parameter.getLocalizedPlaceholderText(locale))}"
		  name="${encode:forHtmlAttribute(reportParam.htmlElementName)}"
		  id="${encode:forHtmlAttribute(reportParam.htmlElementName)}">${encode:forHtmlContent(reportParam.getHtmlValueWithLocale(locale))}</textarea>

<c:if test="${reportParam.parameter.allowNull}">
	<div>
		<label class="checkbox-inline">
			<input type="checkbox" name="${encode:forHtmlAttribute(reportParam.htmlElementName)}-null"
				   ${reportParam.actualParameterValues.contains(null) ? "checked" : ""}>
			<spring:message code="reports.checkbox.null"/>
		</label>
	</div>
</c:if>

<c:if test="${not empty reportParam.parameter.template}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${encode:forHtmlAttribute(reportParam.parameter.template)}"></script>
</c:if>

<t:addRobinHerbotsMask reportParam="${reportParam}"/>
