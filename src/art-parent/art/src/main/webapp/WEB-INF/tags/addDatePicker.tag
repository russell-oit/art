<%-- 
    Document   : addDatePicker
    Created on : 17-Oct-2018, 07:21:25
    Author     : Timothy Anyona
--%>

<%@tag description="Adds a datepicker to a given report parameter input" pageEncoding="UTF-8"%>
<%@tag trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="reportParam" type="art.reportparameter.ReportParameter" required="true"%>
<%@attribute name="locale" type="java.util.Locale" required="true"%>
<%@attribute name="defaultFormat" %>

<%-- any content can be specified here e.g.: --%>
<c:if test="${empty defaultFormat}">
	<c:set var="defaultFormat" value="YYYY-MM-DD"/>
</c:if>

<input type="hidden"
	   name="${encode:forHtmlAttribute(reportParam.hiddenHtmlElementName)}"
	   id="${encode:forHtmlAttribute(reportParam.hiddenHtmlElementName)}"
	   value="${encode:forHtmlAttribute(reportParam.getHtmlValueWithLocale(locale))}">

<script>
	var javaDateFormat = '${encode:forJavaScript(reportParam.parameter.dateFormat)}';
	var momentDateFormat;
	if (javaDateFormat) {
		momentDateFormat = moment().toMomentFormatString(javaDateFormat);
	} else {
		momentDateFormat = '${defaultFormat}';
	}

	//must use useStrict in addition to keepInvalid if using the format property
	//https://github.com/Eonasdan/bootstrap-datetimepicker/issues/1711
	//https://github.com/Eonasdan/bootstrap-datetimepicker/issues/919
	//https://eonasdan.github.io/bootstrap-datetimepicker/Options/
	$('#div-${encode:forJavaScript(reportParam.htmlElementName)}').datetimepicker({
		locale: '${locale}',
		format: momentDateFormat,
		keepInvalid: true,
		useStrict: true
	});
	
	$('#div-${encode:forJavaScript(reportParam.htmlElementName)}').on('dp.change', function(e) {
		var chosenDate = e.date.format(momentDateFormat);
		$('#${encode:forJavaScript(reportParam.hiddenHtmlElementName)}').val(chosenDate);
		$('#${encode:forJavaScript(reportParam.hiddenHtmlElementName)}').change();
	});
</script>
