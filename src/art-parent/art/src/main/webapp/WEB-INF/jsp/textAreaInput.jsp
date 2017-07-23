<%-- 
    Document   : textareaInput
    Created on : 08-Mar-2016, 17:19:51
    Author     : Timothy Anyona

Display report parameter that uses textarea input
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<%-- https://stackoverflow.com/questions/18106503/bootstrap-textarea-adding-space-characters-inside-text-field --%>
<%-- https://stackoverflow.com/questions/2202999/why-is-textarea-filled-with-mysterious-white-spaces --%>
<textarea rows="5" class="form-control"
		  placeholder="${encode:forHtmlAttribute(reportParam.parameter.getLocalizedPlaceholderText(requestContext.locale))}"
		  name="${encode:forHtmlAttribute(reportParam.htmlElementName)}"
		  id="${encode:forHtmlAttribute(reportParam.htmlElementName)}">${encode:forHtmlContent(reportParam.htmlValue)}</textarea>
