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
		id="${encode:forHtmlAttribute(reportParam.htmlElementName)}"
		${reportParam.parameter.parameterType == 'MultiValue' ? 'multiple data-actions-box="true"' : ""}>
	<option value="">--</option>
</select>

<spring:message code="page.text.loading" var="loadingText"/>

<script type="text/javascript">
	//parents and depends parameter names should not contain spaces or special characters
	//https://sourceforge.net/p/art/discussion/352129/thread/3320849d/
	//https://stackoverflow.com/questions/70579/what-are-valid-values-for-the-id-attribute-in-html
	//https://api.jquery.com/category/selectors/
	var url = "${pageContext.request.contextPath}/getLovValues?reportId=${reportParam.parameter.lovReport.reportId}";

	<c:forEach var="defaultValue" items="${reportParam.defaultValues}">
		url += "&defaultValues=" + ${encode:forUriComponent(defaultValue)};
	</c:forEach>

		$("#${encode:forJavaScript(reportParam.htmlElementName)}").remoteChained({
			parents: "${encode:forJavaScript(reportParam.chainedParentsHtmlIds)}",
			url: url,
			loading: "${loadingText}...",
			depends: "${encode:forJavaScript(reportParam.chainedDependsHtmlIds)}"
		});
</script>

<spring:message code="select.text.nothingSelected" var="nothingSelectedText"/>
<spring:message code="select.text.noResultsMatch" var="noResultsMatchText"/>
<spring:message code="select.text.selectedCount" var="selectedCountText"/>
<spring:message code="select.text.selectAll" var="selectAllText"/>
<spring:message code="select.text.deselectAll" var="deselectAllText"/>

<script type="text/javascript">
	$('#${encode:forJavaScript(reportParam.htmlElementName)}').selectpicker({
		liveSearch: true,
		noneSelectedText: '${nothingSelectedText}',
		noneResultsText: '${noResultsMatchText}',
		countSelectedText: '${selectedCountText}',
		selectAllText: '${selectAllText}',
		deselectAllText: '${deselectAllText}'
	});

	//https://stackoverflow.com/questions/43653231/combine-bootstrap-select-and-jquery-chained
	//https://stackoverflow.com/questions/31475457/chained-dropdown-with-bootstrap-selectpicker-not-working?rq=1
	$('#${encode:forJavaScript(reportParam.htmlElementName)}').on('change', function () {
		$(this).selectpicker('refresh');
	});
</script>
