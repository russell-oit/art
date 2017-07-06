<%-- 
    Document   : datetimeInput
    Created on : 09-Mar-2016, 16:59:59
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<div class='input-group date datetimepicker'>
	<input type='text' class="form-control" data-date-format="YYYY-MM-DD HH:mm"
		   placeholder="${encode:forHtmlAttribute(reportParam.parameter.getLocalizedPlaceholderText(localeString))}"
		   name="${encode:forHtmlAttribute(reportParam.htmlElementName)}"
		   id="${encode:forHtmlAttribute(reportParam.htmlElementName)}"
		   value="${encode:forHtmlAttribute(reportParam.htmlValue)}">
	<span class="input-group-addon">
		<span class="glyphicon glyphicon-calendar"></span>
	</span>
</div>

<script>
	var javaDateFormat = '${reportParam.parameter.dateFormat}';
	if (javaDateFormat) {
		var momentDateFormat = moment().toMomentFormatString(javaDateFormat);
		$('#${reportParam.htmlElementName}').data('date-format', momentDateFormat);
	}
</script>
