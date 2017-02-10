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
	var inputDateFormat = 'YYYY-MM-DD'; //moment format
	var outputDateFormat = ''; //moment format e.g. DD-MMM-YYYY

	var inputDateTimeFormat = 'YYYY-MM-DD HH:mm:ss.SSS'; //moment format
	var outputDateTimeFormat = ''; //moment format e.g. DD-MMM-YYYY HH:mm:ss

	var showColumnFilters = ${showColumnFilters};

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

		if (showColumnFilters) {
			var table = settings.oInstance.api();
			createColumnFilters(table);
		}
	}

	function createColumnFilters(table) {
		var tbl = $('#tableData');
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

	//http://papaparse.com/docs#config
	var csvConfig = {
		header: true,
		error: function (e) {
			bootbox.alert(e);
		},
		complete: function (parsed) {
			//https://stackoverflow.com/questions/26597460/displaying-csv-headers-using-papaparse-plugin
			var columns = [];
			if (parsed.meta['fields']) {
				$.each(parsed.meta['fields'], function (i) {
					columns.push({
						data: parsed.meta['fields'][i],
						title: parsed.meta['fields'][i]
					});
				});
			} else {
				var colCount = parsed.data[0].length;
				for (var i = 1; i <= colCount; i++) {
					var columnName = "Column " + i;
					columns.push({
						title: columnName
					});
				}
			}

			$.extend(options, {
				data: parsed.data,
				columns: columns
			});

			var tbl = $('#tableData');
			tbl.dataTable(options);
		}
	};
</script>

<c:if test="${not empty templateFileName}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${templateFileName}"></script>
</c:if>

<c:choose>

	<c:when test="${reportType == 'DataTables'}">
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
</c:choose>


