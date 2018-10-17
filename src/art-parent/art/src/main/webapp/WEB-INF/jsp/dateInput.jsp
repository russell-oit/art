<%-- 
    Document   : dateInput
    Created on : 08-Mar-2016, 17:28:14
    Author     : Timothy Anyona

Display input for date and datetime parameters
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<div id="div-${encode:forHtmlAttribute(reportParam.htmlElementName)}" class='input-group date'>
	<input type='text' class="form-control"
		   placeholder="${encode:forHtmlAttribute(reportParam.parameter.getLocalizedPlaceholderText(requestContext.locale))}"
		   name="${encode:forHtmlAttribute(reportParam.htmlElementName)}"
		   id="${encode:forHtmlAttribute(reportParam.htmlElementName)}"
		   value="${encode:forHtmlAttribute(reportParam.getHtmlValueWithLocale(requestContext.locale))}">
	<span class="input-group-addon">
		<span class="glyphicon glyphicon-calendar"></span>
	</span>
</div>

<script>
	var javaDateFormat = '${encode:forJavaScript(reportParam.parameter.dateFormat)}';
	var finalDateFormat;
	if (javaDateFormat) {
		var momentDateFormat = moment().toMomentFormatString(javaDateFormat);
		finalDateFormat = momentDateFormat;
	} else {
		finalDateFormat = 'YYYY-MM-DD';
	}

	//must use useStrict in addition to keepInvalid if using the format property
	//https://github.com/Eonasdan/bootstrap-datetimepicker/issues/1711
	//https://github.com/Eonasdan/bootstrap-datetimepicker/issues/919
	//https://eonasdan.github.io/bootstrap-datetimepicker/Options/
	$('#div-${encode:forJavaScript(reportParam.htmlElementName)}').datetimepicker({
		locale: '${requestContext.locale}',
		format: finalDateFormat,
		keepInvalid: true,
		useStrict: true
	});
</script>

<t:addRobinHerbotsMask reportParam="${reportParam}"/>

