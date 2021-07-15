<%-- 
    Document   : showAwesomeChartJs
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>


<canvas id="${chartId}" width="${options.width}" height="${options.height}">

</canvas>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/AwesomeChartJS-0.2/awesomechart.js"></script>

<script>
	var dataString = '${encode:forJavaScript(data)}';
	var jsonData = [];
	if (dataString) {
		jsonData = JSON.parse(dataString);
	}

	var labelsString = '${encode:forJavaScript(labels)}';
	var jsonLabels = [];
	if (labelsString) {
		jsonLabels = JSON.parse(labelsString);
	}

	var chart = new AwesomeChart("${chartId}");
	chart.title = '${encode:forJavaScript(options.title)}';
	chart.data = jsonData;
	chart.labels = jsonLabels;
	chart.chartType = '${encode:forJavaScript(options.chartType)}';
	chart.draw();
</script>
