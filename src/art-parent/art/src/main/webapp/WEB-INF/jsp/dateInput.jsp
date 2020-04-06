<%-- 
    Document   : dateInput
    Created on : 08-Mar-2016, 17:28:14
    Author     : Timothy Anyona

Display input for date and datetime parameters
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<div id="div-${encode:forHtmlAttribute(reportParam.htmlElementName)}" class='input-group date'>
	<input type='text' class="form-control"
		   placeholder="${encode:forHtmlAttribute(reportParam.parameter.getLocalizedPlaceholderText(locale))}"
		   name="${encode:forHtmlAttribute(reportParam.htmlElementName)}"
		   id="${encode:forHtmlAttribute(reportParam.htmlElementName)}"
		   value="${encode:forHtmlAttribute(reportParam.getHtmlValueWithLocale(locale))}">
	<span class="input-group-addon">
		<span class="glyphicon glyphicon-calendar"></span>
	</span>
</div>

<c:if test="${reportParam.parameter.allowNull}">
	<div>
		<label class="checkbox-inline">
			<input type="checkbox" name="${encode:forHtmlAttribute(reportParam.htmlElementName)}-null"
				   ${reportParam.actualParameterValues.contains(null) ? "checked" : ""}>
			<spring:message code="reports.checkbox.null"/>
		</label>
	</div>
</c:if>

<t:addDatePicker reportParam="${reportParam}" locale="${locale}"
				 defaultFormat="YYYY-MM-DD"/>

<c:if test="${not empty reportParam.parameter.template}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${encode:forHtmlAttribute(reportParam.parameter.template)}"></script>
</c:if>

<t:addRobinHerbotsMask reportParam="${reportParam}"/>

