<%-- 
    Document   : showPivotTableJs
    Created on : 05-Feb-2017, 21:38:29
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div id="pivotTableJsOutput">

</div>


<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/pivottable-2.7.0/pivot.min.css">

<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-1.12.4.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-ui-1.11.4-all-smoothness/jquery-ui.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/pivottable-2.7.0/pivot.min.js"></script>
<!-- optional: mobile support with jqueryui-touch-punch -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.ui.touch-punch-0.2.3.min.js"></script>

<script type="text/javascript">
	//set default values. can be overridden in template file
	//https://github.com/nicolaskruchten/pivottable/wiki/Parameters
	var options = {};
	var overwrite = false;
	var locale = 'en';
</script>

<c:if test="${not empty templateFileName}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${templateFileName}"></script>
</c:if>

<c:if test="${not empty locale}">
	<script type="text/javascript">
	locale = '${locale}';
	</script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/pivottable-2.7.0/pivot.${locale}.js"></script>
</c:if>

<script type="text/javascript">
	$("#pivotTableJsOutput").pivotUI(${input}, options, overwrite, locale);
</script>