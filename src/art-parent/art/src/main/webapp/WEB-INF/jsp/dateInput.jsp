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

<div class='input-group date datepicker'>
	<input type='text' class="form-control"
		   name="${reportParam.htmlElementName}"
		   value="${reportParam.htmlValue}">
	<span class="input-group-addon">
		<span class="glyphicon glyphicon-calendar"></span>
	</span>
</div>

