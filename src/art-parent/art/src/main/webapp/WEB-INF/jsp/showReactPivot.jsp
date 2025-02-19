<%-- 
    Document   : showReactPivot
    Created on : 02-Feb-2017, 17:47:19
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<div id="${outputDivId}">

</div>
<br>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/react-pivot-1.18.3/react-pivot-standalone-1.18.2.min.js"></script>

<script type="text/javascript">
	//set default values. can be overridden in template file
	//https://github.com/davidguttman/react-pivot
	//https://github.com/davidguttman/react-pivot/blob/master/index.jsx
	var rowsString = '${encode:forJavaScript(rows)}';
	var rows = [];
	if (rowsString) {
		rows = JSON.parse(rowsString);
	}

	var options = {
		rows: rows,
		dimensions: [],
		reduce: function () {},
		calculations: []
	};
</script>

<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${encode:forHtmlAttribute(templateFileName)}"></script>

<script type="text/javascript">
	ReactPivot(document.getElementById('${outputDivId}'), options);
</script>
