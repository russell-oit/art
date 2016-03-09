<%-- 
    Document   : dateInput
    Created on : 08-Mar-2016, 17:28:14
    Author     : Timothy Anyona

Display input for date and datetime parameters
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/eyecon-datepicker/css/datepicker.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/eyecon-datepicker/js/bootstrap-datepicker.js"></script>

<input type="text" size="20" class="datepicker"
	   id="${reportParam.htmlElementId}"
	   name="${reportParam.htmlElementName}"
	   value="${reportParam.htmlValue}">

<script type="text/javascript">
	$(function () {
		$('.datepicker').datepicker();
	});
</script>
