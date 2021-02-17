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

	var dpOptions;
	var paramOptionsString = '${encode:forJavaScript(reportParam.parameter.options)}';
	if (paramOptionsString) {
		var paramOptions = JSON.parse(paramOptionsString);
		var dpOptions = paramOptions.dp;
		if (dpOptions) {
			//https://stackoverflow.com/questions/858181/how-to-check-a-not-defined-variable-in-javascript
			//var datepickerOptions will be defined in external js/template file
			if (typeof datepickerOptions !== 'undefined') {
				$.extend(dpOptions, datepickerOptions);
			}
		}
	}

	if (!dpOptions && typeof datepickerOptions !== 'undefined') {
		dpOptions = datepickerOptions;
	}

	//must use useStrict in addition to keepInvalid if using the format property
	//https://github.com/Eonasdan/bootstrap-datetimepicker/issues/1711
	//https://github.com/Eonasdan/bootstrap-datetimepicker/issues/919
	//https://eonasdan.github.io/bootstrap-datetimepicker/Options/
	var options = {
		locale: '${locale}',
		format: momentDateFormat,
		keepInvalid: true,
		useStrict: true
	};

	if (dpOptions) {
		$.extend(options, dpOptions);
	}
	
	$('#div-${encode:forJavaScript(reportParam.htmlElementName)}').datetimepicker(options);

	$('#div-${encode:forJavaScript(reportParam.htmlElementName)}').on('dp.change', function (e) {
		var chosenDate = e.date.format(momentDateFormat);
		$('#${encode:forJavaScript(reportParam.hiddenHtmlElementName)}').val(chosenDate);
		$('#${encode:forJavaScript(reportParam.hiddenHtmlElementName)}').change();
	});
</script>
