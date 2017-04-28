<%-- 
    Document   : textInput
    Created on : 08-Mar-2016, 17:04:08
    Author     : Timothy Anyona

Display report parameter that uses text input
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<input type='text' class="form-control"
	   name="${encode:forHtmlAttribute(reportParam.htmlElementName)}"
	   id="${encode:forHtmlAttribute(reportParam.htmlElementName)}"
	   value="${encode:forHtmlAttribute(reportParam.htmlValue)}">

