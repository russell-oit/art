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

<select class="form-control"
		name="${encode:forHtmlAttribute(reportParam.htmlElementName)}"
		id="${encode:forHtmlAttribute(reportParam.htmlElementName)}">
	<option value="">--</option>
</select>

<spring:message code="chainedSelect.text.loading" var="loadingText"/>

<script type="text/javascript">
	$("#${reportParam.htmlElementName}").remoteChained({
		parents: "${encode:forJavaScript(reportParam.chainedParentsHtmlIds)}",
		url: "${pageContext.request.contextPath}/app/getLovValues.do?reportId=${reportParam.parameter.lovReportId}",
		loading: "${loadingText}",
		depends: "${encode:forJavaScript(reportParam.chainedDependsHtmlIds)}"
	});
</script>
