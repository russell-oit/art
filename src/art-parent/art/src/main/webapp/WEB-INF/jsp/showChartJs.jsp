<%-- 
    Document   : showChartJs
    Created on : 21-Feb-2017, 15:22:53
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<canvas id="chart" width="${width}" height="${height}">

</canvas>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/moment-2.17.1/moment-with-locales.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/Chart.js-2.5.0/Chart.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/randomColor-0.4.4/randomColor.min.js"></script>


<script type="text/javascript">
	var jsonData = ${data};
	var config = {};
</script>

<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${templateFileName}"></script>

<script type="text/javascript">
	var ctx = document.getElementById("chart");
	new Chart(ctx,config);
</script>
