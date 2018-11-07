<%-- 
    Document   : showDataTables
    Created on : 09-Feb-2017, 08:09:00
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="dataTables.text.showAllRows" var="showAllRowsText"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>

<div id="${outputDivId}">
	<table id="${tableId}" class="table table-bordered table-striped table-condensed">

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

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/yadcf-0.9.2/jquery.dataTables.yadcf.css"/>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/yadcf-0.9.2/jquery.dataTables.yadcf.js"></script>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/moment-2.17.1/moment-with-locales.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/moment-jdateformatparser/moment-jdateformatparser.min.js"></script>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/PapaParse-4.1.4/papaparse.min.js"></script>

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
	var inputDateFormat = 'YYYY-MM-DD'; //moment format e.g. YYYY-MM-DD
	var javaInputDateFormat = '${encode:forJavaScript(options.inputDateFormat)}';
	if (javaInputDateFormat) {
		inputDateFormat = moment().toMomentFormatString(javaInputDateFormat);
	}

	var outputDateFormat = ''; //moment format e.g. DD-MMM-YYYY
	var javaOutputDateFormat = '${encode:forJavaScript(options.outputDateFormat)}';
	if (javaOutputDateFormat) {
		outputDateFormat = moment().toMomentFormatString(javaOutputDateFormat);
	}

	var inputDateTimeFormat = 'YYYY-MM-DD HH:mm:ss.SSS'; //moment format e.g. YYYY-MM-DD HH:mm:ss.SSS
	var javaInputDateTimeFormat = '${encode:forJavaScript(options.inputDateTimeFormat)}';
	if (javaInputDateTimeFormat) {
		inputDateTimeFormat = moment().toMomentFormatString(javaInputDateTimeFormat);
	}

	var outputDateTimeFormat = ''; //moment format e.g. DD-MMM-YYYY HH:mm:ss
	var javaOutputDateTimeFormat = '${encode:forJavaScript(options.outputDateTimeFormat)}';
	if (javaOutputDateTimeFormat) {
		outputDateTimeFormat = moment().toMomentFormatString(javaOutputDateTimeFormat);
	}

	var showColumnFilters = ${options.showColumnFilters};

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

	function datetimeFormatter(data, type, full, meta) {
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
		order: [],
		pagingType: "full_numbers",
		lengthMenu: [[20, 50, 100, -1], [20, 50, 100, "${showAllRowsText}"]],
		pageLength: 50,
		processing: true,
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
		initComplete: afterTableInitialization
	};

	var reportOptionsString = '${encode:forJavaScript(optionsJson)}';
	if (reportOptionsString) {
		var reportOptions = JSON.parse(reportOptionsString);
		if (reportOptions) {
			var dtOptions = reportOptions.dtOptions;
			if (dtOptions) {
				$.extend(options, dtOptions);
			}
		}
	}

	function afterTableInitialization(settings) {
		//https://datatables.net/forums/discussion/34352/passing-datatable-object-to-initcomplete-callback
		$('div.dataTables_filter input').focus();

		if (showColumnFilters) {
			var table = settings.oInstance.api();
			createColumnFilters(table);
		}
	}

	function createColumnFilters(table) {
		var tbl = $('#${tableId}');
		var headingRow = tbl.find('thead tr:first');
		//https://stackoverflow.com/questions/34609173/datatables-1-10-column-count
		var colCount = table.columns().header().length;
		var cols = '';
		for (var i = 1; i <= colCount; i++) {
			cols += '<th></th>';
		}

		var filterRow = '<tr>' + cols + '</tr>';
		headingRow.after(filterRow);
		var filterColumnDefs = [];
		for (var i = 0; i < colCount; i++) {
			filterColumnDefs.push({
				column_number: i, filter_type: "text", filter_default_label: ""
			});
		}

		yadcf.init(table, filterColumnDefs, {filters_tr_index: 1});
	}

	var download;
	var reportType = '${encode:forJavaScript(reportType)}';
	if (reportType === 'DataTablesCsvServer') {
		download = true;
	} else {
		download = false;
	}

	//http://papaparse.com/docs#config
	//https://mwholt.blogspot.co.ke/2014/11/papa-parse-4-fastest-csv-parser.html
	var csvConfig = {
		download: download,
		header: true,
		error: function (e) {
			bootbox.alert({
				title: '${errorOccurredText}',
				message: e
			});
		},
		complete: function (parsed) {
			//https://stackoverflow.com/questions/26597460/displaying-csv-headers-using-papaparse-plugin
			//https://stackoverflow.com/questions/27754135/papaparse-errors-explanation
			var columns = [];
			if (parsed.meta['fields']) {
				//if header: true, we get array of objects and parsed.meta['fields'] has column names
				$.each(parsed.meta['fields'], function (i) {
					columns.push({
						data: parsed.meta['fields'][i],
						title: parsed.meta['fields'][i]
					});
				});
			} else {
				//if header: false, we get array of arrays and parsed.meta['fields'] is undefined
				//https://stackoverflow.com/questions/26416735/initializing-a-jquery-datatable-with-a-local-array-of-arrays-as-datasource
				var colCount = parsed.data[0].length;
				for (var i = 1; i <= colCount; i++) {
					var columnName = "Column " + i;
					columns.push({
						title: columnName
					});
				}
			}

			//https://stackoverflow.com/questions/32911546/how-to-recreate-a-table-with-jquery-datatables
			//https://stackoverflow.com/questions/24452270/how-to-reinitialize-datatable-in-ajax
			//https://datatables.net/manual/tech-notes/3
			var tbl = $('#${tableId}');

			if ($.fn.DataTable.isDataTable("#${tableId}")) {
				$('#${tableId}').DataTable().destroy();
				tbl.empty();
			}

			var finalOptions = $.extend({}, options, {
				data: parsed.data,
				columns: columns
			});

			tbl.DataTable(finalOptions);
		}
	};
</script>

<c:if test="${not empty templateFileName}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${encode:forHtmlAttribute(templateFileName)}"></script>
</c:if>

<c:choose>
	<c:when test="${reportType == 'DataTables' || reportType == 'MongoDB'}">
		<script type="text/javascript">
	//https://datatables.net/reference/option/
	//https://stackoverflow.com/questions/1290131/javascript-how-to-create-an-array-of-object-literals-in-a-loop
	//https://stackoverflow.com/questions/14473170/accessing-arraylist-elemnts-in-javascript-from-jsp
	var columns = [];
	var i = 0;
			<c:forEach var="column" items="${columns}">
	i++;
	var columnName = "${encode:forJavaScript(column.name)}";
	var columnDef = {
		data: columnName,
		title: columnName
	};
	var columnType = '${encode:forJavaScript(column.type)}';
	if (columnType === 'Numeric') {
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
	} else if (columnType === 'Date') {
		if (inputDateFormat && outputDateFormat) {
			columnDef["render"] = dateFormatter;
		}
	} else if (columnType === 'Timestamp') {
		if (inputDateTimeFormat && outputDateTimeFormat) {
			columnDef["render"] = datetimeFormatter;
		}
	}
	columns.push(columnDef);
			</c:forEach>

	var dataString = '${encode:forJavaScript(data)}';
	var data = JSON.parse(dataString);

	$.extend(options, {
		data: data,
		columns: columns
	});
		</script>

		<script type="text/javascript">
			$(document).ready(function () {
				var tbl = $('#${tableId}');
				tbl.dataTable(options);
			});
		</script>
	</c:when>
	<c:when test="${reportType == 'DataTablesCsvLocal'}">
		<style>
			#filechooser {
                /* color: #555; */
                text-decoration: underline;
                cursor: pointer; /* "hand" cursor */
            }
		</style>
		<p align="center" style="line-height: 1.5">
			<spring:message code="pivotTableJs.text.dropCsv"/>&nbsp;<spring:message code="pivotTableJs.text.or"/>&nbsp;
			<label id="filechooser">
				<spring:message code="pivotTableJs.text.clickToChoose"/>
				<input id="csv" type="file" style="display:none"/>
			</label>
		</p>
		<script type="text/javascript">
			var showData = function (f) {
				Papa.parse(f, csvConfig);
			};

			$("#csv").bind("change", function (event) {
				showData(event.target.files[0]);
			});

			var dragging = function (evt) {
				evt.stopPropagation();
				evt.preventDefault();
				evt.originalEvent.dataTransfer.dropEffect = 'copy';
				$("body").removeClass("whiteborder").addClass("greyborder");
			};

			var endDrag = function (evt) {
				evt.stopPropagation();
				evt.preventDefault();
				evt.originalEvent.dataTransfer.dropEffect = 'copy';
				$("body").removeClass("greyborder").addClass("whiteborder");
			};

			var dropped = function (evt) {
				evt.stopPropagation();
				evt.preventDefault();
				$("body").removeClass("greyborder").addClass("whiteborder");
				showData(evt.originalEvent.dataTransfer.files[0]);
			};

			$("html")
					.on("dragover", dragging)
					.on("dragend", endDrag)
					.on("dragexit", endDrag)
					.on("dragleave", endDrag)
					.on("drop", dropped);

		</script>
	</c:when>
	<c:when test="${reportType == 'DataTablesCsvServer'}">
		<script type="text/javascript">
			var dataFile = '${pageContext.request.contextPath}/js-templates/${encode:forJavaScript(dataFileName)}';
				Papa.parse(dataFile, csvConfig);
		</script>
	</c:when>
</c:choose>


