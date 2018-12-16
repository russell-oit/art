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
<spring:message code="page.button.apply" var="applyText"/>
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
<spring:message code="daterangepicker.text.thisWeek" var="thisWeekText"/>
<spring:message code="daterangepicker.text.lastWeek" var="lastWeekText"/>
<spring:message code="daterangepicker.text.yearToDate" var="yearToDateText"/>
<spring:message code="daterangepicker.text.monthToDate" var="monthToDateText"/>
<spring:message code="daterangepicker.text.quarterToDate" var="quarterToDateText"/>
<spring:message code="daterangepicker.text.weekToDate" var="weekToDateText"/>

<input type="text" class="form-control"
	   name="${encode:forHtmlAttribute(reportParam.htmlElementName)}"
	   id="${encode:forHtmlAttribute(reportParam.htmlElementName)}"
	   value="">

<script>
	var locale = '${locale}';
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

	var paramOptionsString = '${encode:forJavaScript(reportParam.parameter.options)}';
	var paramOptions;
	var dateRangeOptions;
	var rangesOption;

	if (paramOptionsString) {
		paramOptions = JSON.parse(paramOptionsString);
		if (paramOptions) {
			dateRangeOptions = paramOptions.dateRange;
			if (dateRangeOptions) {
				rangesOption = dateRangeOptions.ranges;
			}
		}
	}

	var ranges = {};
	if (rangesOption !== null) {
		if (rangesOption === undefined) {
			rangesOption = ['default'];
		}

		//https://stackoverflow.com/questions/6116474/how-to-find-if-an-array-contains-a-specific-string-in-javascript-jquery
		if ($.inArray('default', rangesOption) > -1) {
			rangesOption = ["today", "yesterday", "last7Days", "last30Days", "thisMonth", "lastMonth"];
		}

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
		var thisWeekText = '${thisWeekText}';
		var lastWeekText = '${lastWeekText}';
		var yearToDateText = '${yearToDateText}';
		var monthToDateText = '${monthToDateText}';
		var quarterToDateText = '${quarterToDateText}';
		var weekToDateText = '${weekToDateText}';

		$.each(rangesOption, function (index, value) {
			//https://stackoverflow.com/questions/14910760/switch-case-as-string
			//https://api.jquery.com/jquery.each/
			//https://stackoverflow.com/questions/9840512/get-dates-for-last-quarter-and-this-quarter-through-javascript
			switch (value) {
				case "today":
					ranges[todayText] = [moment(), moment()];
					break;
				case "yesterday":
					ranges[yesterdayText] = [moment().subtract(1, 'days'), moment().subtract(1, 'days')];
					break;
				case "last7Days":
					ranges[last7DaysText] = [moment().subtract(6, 'days'), moment()];
					break;
				case "last30Days":
					ranges[last30DaysText] = [moment().subtract(29, 'days'), moment()];
					break;
				case "thisMonth":
					ranges[thisMonthText] = [moment().startOf('month'), moment().endOf('month')];
					break;
				case "lastMonth":
					ranges[lastMonthText] = [moment().subtract(1, 'month').startOf('month'), moment().subtract(1, 'month').endOf('month')];
					break;
				case "thisQuarter":
					ranges[thisQuarterText] = [moment().startOf('quarter'), moment().endOf('quarter')];
					break;
				case "lastQuarter":
					ranges[lastQuarterText] = [moment().subtract(1, 'quarter').startOf('quarter'), moment().subtract(1, 'quarter').endOf('quarter')];
					break;
				case "thisYear":
					ranges[thisYearText] = [moment().startOf('year'), moment().endOf('year')];
					break;
				case "lastYear":
					ranges[lastYearText] = [moment().subtract(1, 'year').startOf('year'), moment().subtract(1, 'year').endOf('year')];
					break;
				case "thisWeek":
					ranges[thisWeekText] = [moment().startOf('week'), moment().endOf('week')];
					break;
				case "lastWeek":
					ranges[lastWeekText] = [moment().subtract(1, 'week').startOf('week'), moment().subtract(1, 'week').endOf('week')];
					break;
				case "yearToDate":
					ranges[yearToDateText] = [moment().startOf('year'), moment()];
					break;
				case "monthToDate":
					ranges[monthToDateText] = [moment().startOf('month'), moment()];
					break;
				case "quarterToDate":
					ranges[quarterToDateText] = [moment().startOf('quarter'), moment()];
					break;
				case "weekToDate":
					ranges[weekToDateText] = [moment().startOf('week'), moment()];
					break;
				default:
					break;
			}
		});
	}

	var defaultJavaDateFormat = 'MMMM dd, yyyy';
	var momentDateFormat = moment().toMomentFormatString(defaultJavaDateFormat);

	//set default options
	var options = {
		ranges: ranges,
		locale: {
			format: momentDateFormat,
			applyLabel: '${applyText}',
			cancelLabel: '${cancelText}',
			customRangeLabel: '${customRangeText}',
			separator: ' - '
		},
		autoUpdateInput: false
	};

	if (dateRangeOptions) {
		//https://stackoverflow.com/questions/208105/how-do-i-remove-a-property-from-a-javascript-object
		delete dateRangeOptions.ranges;
		$.extend(options, dateRangeOptions);
	}

	momentDateFormat = options.locale.format;
	var separator = options.locale.separator;

	var selector = '#${encode:forJavaScript(reportParam.htmlElementName)}';

	$(selector).on('apply.daterangepicker', function (ev, picker) {
		$(this).val(picker.startDate.format(momentDateFormat) + separator + picker.endDate.format(momentDateFormat));
	});
</script>

<c:if test="${not empty reportParam.parameter.template}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${encode:forHtmlAttribute(reportParam.parameter.template)}" charset="utf-8"></script>
</c:if>

<script>
	$(selector).daterangepicker(options, rangeUpdated);
</script>
