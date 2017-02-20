<%-- 
    Document   : showC3
    Created on : 19-Feb-2017, 13:47:07
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div id="chart">

</div>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/d3-3.5.17/d3.min.js"></script>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/c3-0.4.11/c3.min.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/c3-0.4.11/c3.min.js"></script>


<script type="text/javascript">
	var data = {
		json: ${data}
	};

	var options = {
		bindto: '#chart',
		data: data
	};
</script>

<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${templateFileName}"></script>

<script type="text/javascript">
	c3.generate(options);
</script>