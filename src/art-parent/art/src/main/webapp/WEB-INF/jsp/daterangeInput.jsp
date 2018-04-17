<%-- 
    Document   : daterangeInput
    Created on : 17-Apr-2018, 12:02:02
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<spring:message code="dialog.button.cancel" var="cancelText"/>
<spring:message code="daterangepicker.button.apply" var="applyText"/>
<spring:message code="daterangepicker.button.customRange" var="customRangeText"/>
<spring:message code="daterangepicker.text.today" var="todayText"/>
<spring:message code="daterangepicker.text.yesterday" var="yesterdayText"/>
<spring:message code="daterangepicker.text.lastXDays" arguments="7" var="last7DaysText"/>
<spring:message code="daterangepicker.text.lastXDays" arguments="30" var="last30DaysText"/>
<spring:message code="daterangepicker.text.thisMonth" var="thisMonthText"/>
<spring:message code="daterangepicker.text.lastMonth" var="lastMonthText"/>
<spring:message code="daterangepicker.text.thisQuarter" var="thisQuarterText"/>
<spring:message code="daterangepicker.text.lastQuarter" var="lastQuarterText"/>
<spring:message code="daterangepicker.text.thisYear" var="thisYearText"/>
<spring:message code="daterangepicker.text.lastYear" var="lastYearText"/>
<spring:message code="daterangepicker.text.to" var="toText"/>

<input type="text" class="form-control"
	   name="${encode:forHtmlAttribute(reportParam.htmlElementName)}"
	   id="${encode:forHtmlAttribute(reportParam.htmlElementName)}"
	   value="">

<script>
	var locale = '${requestContext.locale}';
	moment.locale(locale);

	function rangeUpdated(start, end) {
		var fromParameterJson = '${encode:forJavaScript(reportParam.parameter.dateRangeFromParameterJson)}';
		if (fromParameterJson) {
			var fromParameter = JSON.parse(fromParameterJson);
			var fromParameterName = fromParameter.name;
			var fromParameterJavaFormat = fromParameter.format;
			var fromParameterMomentFormat = moment().toMomentFormatString(fromParameterJavaFormat);
			$('#p-' + fromParameterName).val(start.format(fromParameterMomentFormat));
		}

		var toParameterJson = '${encode:forJavaScript(reportParam.parameter.dateRangeToParameterJson)}';
		if (toParameterJson) {
			var toParameter = JSON.parse(toParameterJson);
			var toParameterName = toParameter.name;
			var toParameterJavaFormat = toParameter.format;
			var toParameterMomentFormat = moment().toMomentFormatString(toParameterJavaFormat);
			$('#p-' + toParameterName).val(end.format(toParameterMomentFormat));
		}
	}

	var javaDateFormat = '${encode:forJavaScript(reportParam.parameter.parameterOptions.dateRange.format)}';
	var momentDateFormat = moment().toMomentFormatString(javaDateFormat);

	var localeOptions = {
		format: momentDateFormat,
		applyLabel: '${applyText}',
		cancelLabel: '${cancelText}',
		customRangeLabel: '${customRangeText}',
		direction: '${encode:forJavaScript(reportParam.parameter.parameterOptions.dateRange.direction)}',
		separator: '${encode:forJavaScript(reportParam.parameter.parameterOptions.dateRange.separator)}'
		
	};

	var todayText = '${todayText}';
	var yesterdayText = '${yesterdayText}';
	var last7DaysText = '${last7DaysText}';
	var last30DaysText = '${last30DaysText}';
	var thisMonthText = '${thisMonthText}';
	var lastMonthText = '${lastMonthText}';
	var thisQuarterText = '${thisQuarterText}';
	var lastQuarterText = '${lastQuarterText}';
	var thisYearText = '${thisYearText}';
	var lastYearText = '${lastYearText}';
	var toText = '${toText}';

	var ranges = {};
	if (${reportParam.parameter.parameterOptions.dateRange.showRanges}) {
		$.extend(ranges, {
			'${todayText}': [moment(), moment()],
			'${yesterdayText}': [moment().subtract(1, 'days'), moment().subtract(1, 'days')],
			'${last7DaysText}': [moment().subtract(6, 'days'), moment()],
			'${last30DaysText}': [moment().subtract(29, 'days'), moment()],
			'${thisMonthText}': [moment().startOf('month'), moment().endOf('month')],
			'${lastMonthText}': [moment().subtract(1, 'month').startOf('month'), moment().subtract(1, 'month').endOf('month')]
		});
	}

	var overallOptions = {
		locale: localeOptions,
		ranges: ranges,
		autoApply: ${reportParam.parameter.parameterOptions.dateRange.autoApply},
		showDropdowns: ${reportParam.parameter.parameterOptions.dateRange.showDropdowns},
		showWeekNumbers: ${reportParam.parameter.parameterOptions.dateRange.showWeekNumbers},
		showISOWeekNumbers: ${reportParam.parameter.parameterOptions.dateRange.showISOWeekNumbers},
		showCustomRangeLabel: ${reportParam.parameter.parameterOptions.dateRange.showCustomRangeLabel},
		timePicker: ${reportParam.parameter.parameterOptions.dateRange.timePicker},
		timePicker24Hour: ${reportParam.parameter.parameterOptions.dateRange.timePicker24Hour},
		timePickerIncrement: ${reportParam.parameter.parameterOptions.dateRange.timePickerIncrement},
		timePickerSeconds: ${reportParam.parameter.parameterOptions.dateRange.timePickerSeconds},
		linkedCalendars: ${reportParam.parameter.parameterOptions.dateRange.linkedCalendars},
		autoUpdateInput: ${reportParam.parameter.parameterOptions.dateRange.autoUpdateInput},
		alwaysShowCalendars: ${reportParam.parameter.parameterOptions.dateRange.alwaysShowCalendars},
		opens: '${encode:forJavaScript(reportParam.parameter.parameterOptions.dateRange.opens)}',
		drops: '${encode:forJavaScript(reportParam.parameter.parameterOptions.dateRange.drops)}',
		buttonClasses: '${encode:forJavaScript(reportParam.parameter.parameterOptions.dateRange.buttonClasses)}',
		applyClass: '${encode:forJavaScript(reportParam.parameter.parameterOptions.dateRange.applyClass)}',
		cancelClass: '${encode:forJavaScript(reportParam.parameter.parameterOptions.dateRange.cancelClass)}'
	};
</script>

<c:if test="${not empty reportParam.parameter.template}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${encode:forHtmlAttribute(reportParam.parameter.template)}" charset="utf-8"></script>
</c:if>

<script>
	$('#${encode:forJavaScript(reportParam.htmlElementName)}').daterangepicker(overallOptions, rangeUpdated);
</script>
