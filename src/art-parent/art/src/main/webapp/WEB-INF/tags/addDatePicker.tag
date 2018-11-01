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
<%@attribute name="locale" required="true"%>
<%@attribute name="defaultFormat" %>

<%-- any content can be specified here e.g.: --%>
<c:if test="${empty defaultFormat}">
	<c:set var="defaultFormat" value="YYYY-MM-DD"/>
</c:if>

<script>
	var javaDateFormat = '${encode:forJavaScript(reportParam.parameter.dateFormat)}';
	var finalDateFormat;
	if (javaDateFormat) {
		var momentDateFormat = moment().toMomentFormatString(javaDateFormat);
		finalDateFormat = momentDateFormat;
	} else {
		finalDateFormat = '${defaultFormat}';
	}

	//must use useStrict in addition to keepInvalid if using the format property
	//https://github.com/Eonasdan/bootstrap-datetimepicker/issues/1711
	//https://github.com/Eonasdan/bootstrap-datetimepicker/issues/919
	//https://eonasdan.github.io/bootstrap-datetimepicker/Options/
	$('#div-${encode:forJavaScript(reportParam.htmlElementName)}').datetimepicker({
		locale: '${locale}',
		format: finalDateFormat,
		keepInvalid: true,
		useStrict: true
	});
</script>