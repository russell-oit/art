<%-- 
    Document   : datetimeInput
    Created on : 09-Mar-2016, 16:59:59
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<input type="text" size="20" class="datetimepicker" 
	   name="${reportParam.htmlElementName}"
	   value="${reportParam.htmlValue}">
