<%-- 
    Document   : showDataTables
    Created on : 09-Feb-2017, 08:09:00
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText"/>

<div id="dataTablesOutput">
	<table id="tableData" class="table table-bordered table-striped table-condensed">

	</table>
</div>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-3.3.6/css/bootstrap.min.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/DataTables-1.10.13/css/dataTables.bootstrap.min.css"/>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-1.12.4.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/DataTables-1.10.13/js/jquery.dataTables.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/DataTables-1.10.13/js/dataTables.bootstrap.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/art.js"></script>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/css/buttons.dataTables.min.css"/>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/css/buttons.bootstrap.min.css"/>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/js/dataTables.buttons.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/js/buttons.bootstrap.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/JSZip-2.5.0/jszip.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/pdfmake-0.1.18/pdfmake.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/pdfmake-0.1.18/vfs_fonts.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/js/buttons.html5.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/js/buttons.print.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/Buttons-1.2.4/js/buttons.colVis.min.js"></script>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/yadcf-0.9.1/jquery.dataTables.yadcf.css"/>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/yadcf-0.9.1/jquery.dataTables.yadcf.js"></script>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/moment-2.17.1/moment-with-locales.min.js"></script>

<script type="text/javascript">
	//https://stackoverflow.com/questions/27380390/jquery-datatables-format-numbers
	//https://softwareengineering.stackexchange.com/questions/160732/function-declaration-as-var-instead-of-function
	//https://stackoverflow.com/questions/5142286/two-functions-with-the-same-name-in-javascript-how-can-this-work
	//https://stackoverflow.com/questions/336859/javascript-function-declaration-syntax-var-fn-function-vs-function-fn

	var formatAllNumbers = false;
	var formattedNumberColumns = [];
	var customNumberFormats = [[]];

	//https://stackoverflow.com/questions/35450227/how-to-parse-given-date-string-using-moment-js
	//http://momentjs.com/docs/
	var inputDateFormat = 'YYYY-MM-DD';
	var outputDateFormat = 'DD-MMM-YYYY';

	var inputDateTimeFormat = 'YYYY-MM-DD HH:mm:ss.SSS';
	var outputDateTimeFormat = 'DD-MMM-YYYY HH:mm:ss';

	moment.locale('${locale}');

	function dateFormatter(data, type, full, meta) {
		//https://stackoverflow.com/questions/25319193/jquery-datatables-column-rendering-and-sorting
		if (type === "display") {
			var formattedDate;
			if (data === null) {
				formattedDate = '';
			} else {
				formattedDate = moment(data, inputDateFormat).format(outputDateFormat);
			}
			return formattedDate;
		} else {
			return data;
		}
	}

	function timestampFormatter(data, type, full, meta) {
		//https://stackoverflow.com/questions/25319193/jquery-datatables-column-rendering-and-sorting
		if (type === "display") {
			var formattedDate;
			if (data === null) {
				formattedDate = '';
			} else {
				//http://wiki.fasterxml.com/JacksonFAQDateHandling
				//https://egkatzioura.wordpress.com/2013/01/22/spring-jackson-and-date-serialization/
				//https://momentjs.com/docs/#/parsing/string/
				formattedDate = moment(data, inputDateTimeFormat).format(outputDateTimeFormat);
			}
			return formattedDate;
		} else {
			return data;
		}
	}

	function twoDecimals(data, type, full, meta) {
		if (type === "display") {
			if (data === null) {
				return '';
			} else {
				//https://stackoverflow.com/questions/6134039/format-number-to-always-show-2-decimal-places
				//https://developer.mozilla.org/en/docs/Web/JavaScript/Reference/Global_Objects/NumberFormat
				return new Intl.NumberFormat('${languageTag}', {minimumFractionDigits: 2}).format(data);
			}
		} else {
			return data;
		}
	}

	function defaultFormatter(data, type, full, meta) {
		if (type === "display") {
			if (data === null) {
				return '';
			} else {
				return String(data);
			}
		} else {
			return data;
		}
	}

	function numberFormatter(data, type, full, meta) {
		if (type === "display") {
			var formattedNumber;
			if (data === null) {
				formattedNumber = '';
			} else {
				formattedNumber = data.toLocaleString('${languageTag}');
			}
			return formattedNumber;
		} else {
			return data;
		}
	}

	var options = {
		orderClasses: false,
		pagingType: "full_numbers",
		lengthMenu: [[10, 25, 50, 100, -1], [10, 25, 50, 100, "${showAllRowsText}"]],
		pageLength: 50,
		dom: "lBfrtip",
		buttons: [
			{extend: 'colvis', postfixButtons: ['colvisRestore']},
			{extend: 'excel', exportOptions: {columns: ':visible'}},
			{extend: 'pdf', exportOptions: {columns: ':visible'}},
			{extend: 'print', exportOptions: {columns: ':visible'}}
		],
		language: {
			//pageContext.response.locale always returns OS locale when page included with requestdispatcher.include?
			url: "${pageContext.request.contextPath}/js/dataTables/i18n/dataTables_${locale}.json"
		},
		initComplete: function (settings) {
			afterTableInitialization(settings);
		}
	};

	function afterTableInitialization(settings) {
		//https://datatables.net/forums/discussion/34352/passing-datatable-object-to-initcomplete-callback
		$('div.dataTables_filter input').focus();

		var tbl = $('#tableData');
		var headingRow = tbl.find('thead tr:first');
		var colCount = ${columns.size()};
		var cols = '';
		for (var i = 1; i <= colCount; i++) {
			cols += '<th></th>';
		}

		var filterRow = '<tr>' + cols + '</tr>';
		headingRow.after(filterRow);

		var table = settings.oInstance.api();
		var filterColumnDefs = [];
		for (var i = 0; i < colCount; i++) {
			filterColumnDefs.push({
				column_number: i, filter_type: "text", filter_default_label: ""
			});
		}

		yadcf.init(table, filterColumnDefs, {filters_tr_index: 1});
	}
</script>

<c:if test="${not empty templateFileName}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${templateFileName}"></script>
</c:if>

<script type="text/javascript">
	//https://datatables.net/reference/option/
	//https://stackoverflow.com/questions/1290131/javascript-how-to-create-an-array-of-object-literals-in-a-loop
	//https://stackoverflow.com/questions/14473170/accessing-arraylist-elemnts-in-javascript-from-jsp
	var columns = [];
	var i = 0;
	<c:forEach var="column" items="${columns}">
	i++;
	var columnDef = {
		data: "${column.name}",
		title: "${column.name}"
	};
	var columnType = '${column.type}';
	if (columnType === 'numeric') {
		if (formatAllNumbers || formattedNumberColumns.indexOf(i) !== -1) {
			//https://stackoverflow.com/questions/1184123/is-it-possible-to-add-dynamically-named-properties-to-javascript-object
			columnDef["render"] = numberFormatter;
		} else {
			//https://stackoverflow.com/questions/7106410/looping-through-arrays-of-arrays
			for (var j = 0; j < customNumberFormats.length; j++) {
				var customFormat = customNumberFormats[j];
				var columnIndex = customFormat[0];
				var formatter = customFormat[1];
				if (columnIndex === i) {
					columnDef["render"] = formatter;
					break;
				}
			}
		}
	} else if (columnType === 'date') {
		if (inputDateFormat && outputDateFormat) {
			columnDef["render"] = dateFormatter;
		}
	} else if (columnType === 'timestamp') {
		if (inputDateTimeFormat && outputDateTimeFormat) {
			columnDef["render"] = timestampFormatter;
		}
	}

	columns.push(columnDef);
	</c:forEach>

	$.extend(options, {
		data: ${data},
		columns: columns
	});
</script>

<script type="text/javascript">
	$(document).ready(function () {
		var tbl = $('#tableData');
		tbl.dataTable(options);
	});
</script>
