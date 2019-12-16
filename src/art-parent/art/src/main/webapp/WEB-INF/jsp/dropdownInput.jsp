<%-- 
    Document   : dropdownInput
    Created on : 08-Mar-2016, 17:33:50
    Author     : Timothy Anyona

Display report parameter that uses dropdown input
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<spring:message code="select.text.nothingSelected" var="nothingSelectedText" javaScriptEscape="true"/>
<spring:message code="select.text.noResultsMatch" var="noResultsMatchText" javaScriptEscape="true"/>
<spring:message code="select.text.selectedCount" var="selectedCountText" javaScriptEscape="true"/>
<spring:message code="select.text.selectAll" var="selectAllText" javaScriptEscape="true"/>
<spring:message code="select.text.deselectAll" var="deselectAllText" javaScriptEscape="true"/>

<select class="form-control"
		name="${encode:forHtmlAttribute(reportParam.htmlElementName)}"
		id="${encode:forHtmlAttribute(reportParam.htmlElementName)}"
		${reportParam.parameter.parameterType == 'MultiValue' ? 'multiple data-actions-box="true"' : ""}>

	<c:forEach var="lovValue" items="${lovValues}">
		<option value="${encode:forHtmlAttribute(lovValue.key)}" ${reportParam.selectLovValue(lovValue.key) ? "selected" : ""}>${encode:forHtmlContent(lovValue.value)}</option>
	</c:forEach>
</select>
		
<script type="text/javascript">
	$('#${encode:forJavaScript(reportParam.htmlElementName)}').selectpicker({
		liveSearch: true,
		noneSelectedText: '${nothingSelectedText}',
		noneResultsText: '${noResultsMatchText}',
		countSelectedText: '${selectedCountText}',
		selectAllText: '${selectAllText}',
		deselectAllText: '${deselectAllText}'
	});
</script>
