<%-- 
    Document   : numberInput
    Created on : 18-Dec-2018, 17:21:59
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<input type='number' class="form-control"
	   ${reportParam.parameter.dataType == 'Double' ? 'step="any"' : ""}
	   placeholder="${encode:forHtmlAttribute(reportParam.parameter.getLocalizedPlaceholderText(locale))}"
	   name="${encode:forHtmlAttribute(reportParam.htmlElementName)}"
	   id="${encode:forHtmlAttribute(reportParam.htmlElementName)}"
	   value="${encode:forHtmlAttribute(reportParam.getHtmlValueWithLocale(locale))}">
