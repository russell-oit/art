<%-- 
    Document   : showApexChartsJs
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<div id="${chartId}">

</div>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/apexcharts.js-3.26.0/apexcharts.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/apexcharts.js-3.26.0/apexcharts.min.js"></script>

<script>
	var dataString = '${encode:forJavaScript(data)}';
	var jsonData = [];
	if (dataString) {
		jsonData = JSON.parse(dataString);
	}

	var options = {};

	var localeString = '${encode:forJavaScript(localeString)}';
	var localeContentString = '${encode:forJavaScript(localeContent)}';
	if (localeContentString) {
		var localeContent = JSON.parse(localeContentString);
		$.extend(options, {
			chart: {
				locales: [localeContent],
				defaultLocale: localeString
			}
		});
	}
</script>

<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${encode:forHtmlAttribute(templateFileName)}"></script>

<script>
	var chart = new ApexCharts(document.querySelector("#${chartId}"), options);
	chart.render();
</script>
