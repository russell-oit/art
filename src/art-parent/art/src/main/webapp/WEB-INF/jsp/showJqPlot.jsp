<%-- 
    Document   : showJqPlot
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>


<div id="${chartId}">

</div>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jqPlot-1.0.9/jquery.jqplot.min.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jqPlot-1.0.9/jquery.jqplot.min.js"></script>

<c:if test="${not empty options.cssFile}">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js-templates/${encode:forHtmlAttribute(options.cssFile)}">
</c:if>
	
<c:forEach var="plugin" items="${options.plugins}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/jqPlot-1.0.9/plugins/jqplot.${encode:forHtmlAttribute(plugin)}.js"></script>
</c:forEach>

<script>
	//http://www.music.mcgill.ca/~ich/classes/mumt301_11/js/jqPlot/docs/files/jqPlotOptions-txt.html
	var chartId = "${chartId}";
	var contextPath = "${pageContext.request.contextPath}";

	var dataString = '${encode:forJavaScript(data)}';
	var jsonData = [];
	if (dataString) {
		jsonData = JSON.parse(dataString);
	}

	var data = [];

	var options = {};
	var optionsString = '${encode:forJavaScript(optionsString)}';
	if (optionsString) {
		options = JSON.parse(optionsString);
	}
</script>

<c:if test="${not empty templateFileName}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${encode:forHtmlAttribute(templateFileName)}"></script>
</c:if>

<script>
	var plot = $.jqplot('${chartId}', data, options);
</script>

<c:if test="${not empty postTemplateFileName}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${encode:forHtmlAttribute(postTemplateFileName)}"></script>
</c:if>


