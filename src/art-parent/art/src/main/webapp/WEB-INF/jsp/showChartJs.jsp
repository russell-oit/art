<%-- 
    Document   : showChartJs
    Created on : 21-Feb-2017, 15:22:53
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>


<canvas id="${chartId}" width="${options.width}" height="${options.height}">

</canvas>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/moment-2.22.2/moment-with-locales.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/Chart.js-2.9.4/Chart.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/randomColor-0.4.4/randomColor.min.js"></script>


<script type="text/javascript">
	//http://www.chartjs.org/docs/
	//https://www.sitepoint.com/introduction-chart-js-2-0-six-examples/
	var chartId = "${chartId}";
	var contextPath = "${pageContext.request.contextPath}";
	
	var dataString = '${encode:forJavaScript(data)}';
	var jsonData = [];
	if (dataString) {
		jsonData = JSON.parse(dataString);
	}
	
	var config = {};
</script>

<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${encode:forHtmlAttribute(templateFileName)}"></script>

<script type="text/javascript">
	var ctx = $("#${chartId}");
	new Chart(ctx,config);
</script>
