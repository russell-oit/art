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

<script type="text/javascript">
	var numericFormatter = function (data, type, full, meta) {
		var formattedNumber;
		if (data === null) {
			formattedNumber = '';
		} else {
			formattedNumber = data.toLocaleString('${languageTag}');
		}
		return formattedNumber;
	};

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
//		var columnFilterRow = createColumnFilters2(tbl);
//		//move column filter row after heading row
//		columnFilterRow.insertAfter(columnFilterRow.next());
//		var table = settings.oInstance.api();
//		// Apply the column filter
//		applyColumnFilters(tbl, table);

		var headingRow = tbl.find('thead tr:first');
		var colcount = ${columns.size()};
		var cols = '';
		for (var i = 1; i <= colcount; i++) {
			cols += '<th></th>';
		}
		;
		var newrow='<tr>' + cols + '</tr>';
		
		var thead = tbl.find('thead');
		headingRow.after(newrow);
//		var thead = document.getElementById('tableData').tHead;
//		thead.insertRow(0);

//		var tab = document.getElementById('tableData');
//		tab.insertRow(2);
		
//console.log(headingRow);
//		var columnFilterRow = headingRow.clone();
//		//insert cloned row as first row because datatables will put heading styling on the last thead row
////		columnFilterRow.insertBefore(headingRow);
//		columnFilterRow.insertAfter(headingRow);

		var table = settings.oInstance.api();
		yadcf.init(table, [
			{column_number: 0, filter_type: "text", filter_default_label: ""},
			{column_number: 1, filter_type: "text"}
		]
, {filters_tr_index: 1}
				);

//		test();
	}
	;

	function createColumnFilters2(tbl) {
		//add row to thead to enable column filtering
		//use clone so that plugins work properly? e.g. colvis
		var headingRow = tbl.find('thead tr:first');
		var columnFilterRow = headingRow.clone();
		//insert cloned row as first row because datatables will put heading styling on the last thead row
		columnFilterRow.insertBefore(headingRow);
		//put search fields into cloned row
		columnFilterRow.find('th').each(function () {
			if ($(this).hasClass('noFilter')) {
				$(this).html('');
			} else {
				var title = $(this).text();
				$(this).html('<input type="text" class="form-control input-sm" placeholder="' + title + '">');
			}
		});

		return columnFilterRow;
	}

	function test() {
		$('#tableData thead td').each(function () {
			var title = $('#mytable thead th').eq($(this).index()).text();
			$(this).html('<input type="text" placeholder="Search ' + title + '" />');
		});
		$("#tableData thead input").on('keyup change', function () {
			table
					.column($(this).parent().index() + ':visible')
					.search(this.value)
					.draw();
		});
	}

</script>

<c:if test="${not empty templateFileName}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${templateFileName}"></script>
</c:if>

<script type="text/javascript">
	//https://datatables.net/reference/option/
	var data = [{"col1": 1000.0}];

//	return '';

	//https://stackoverflow.com/questions/1290131/javascript-how-to-create-an-array-of-object-literals-in-a-loop
	//https://stackoverflow.com/questions/14473170/accessing-arraylist-elemnts-in-javascript-from-jsp
	var columns = [];
	<c:forEach var="column" items="${columns}">
	columns.push({
		data: "${column.name}",
		title: "${column.name}"
//		, render : function (data, type, full, meta) { return numericFormatter(data);}
		<c:if test="${column.type == 'numeric'}">
		, render: numericFormatter
//	, render : function (data, type, full, meta) { return numericFormatter(data); }
		</c:if>
	});
	</c:forEach>
	$.extend(options, {
		data: ${data},
//	data: data,
//			columns: [{"title": "one", "data": "col1", render: function (data, type, full, meta) {return data.toLocaleString('en-GB'); }}],
//		columns: [{"title": "one", "data": "col1", render: $.fn.dataTable.render.number( ',', '.')}],
//				columns: [{"data": "col1", type: "num"}],
//		columns: [{"data": "col1"}],
		columns: columns

	});
</script>

<script type="text/javascript">
	$(document).ready(function () {
		var tbl = $('#tableData');



		var oTable = tbl.dataTable(options);
		

	});
</script>
