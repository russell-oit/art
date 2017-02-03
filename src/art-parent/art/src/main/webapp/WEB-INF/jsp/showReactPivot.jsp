<%-- 
    Document   : showReactPivot
    Created on : 02-Feb-2017, 17:47:19
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<div id="reactPivotOutput">

</div>
<br>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/react-pivot-1.18.3/react-pivot-standalone-1.18.2.min.js"></script>

<script type="text/javascript">
	//set default values. can be overridden in template file
	//https://github.com/davidguttman/react-pivot
	//https://github.com/davidguttman/react-pivot/blob/master/index.jsx
	var rows = ${rows};
	var dimensions = [];
	var reduce = function () {};
	var calculations = [];
	var compact = false;
	var csvDownloadFileName = 'table.csv';
	var csvTemplateFormat = false;
	var defaultStyles = true;
	var hiddenColumns = [];
	var nPaginateRows = 25;
	var solo = null;
	var sortBy = null;
	var sortDir = 'asc';
	var tableClassName = '';
	var hideDimensionFilter = false;
	var activeDimensions = [];
	var excludeSummaryFromExport = false;
</script>

<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${templateFileName}"></script>

<script type="text/javascript">
	ReactPivot(document.getElementById('reactPivotOutput'), {
		rows: rows,
		dimensions: dimensions,
		reduce: reduce,
		calculations: calculations,
		compact: compact,
		csvDownloadFileName: csvDownloadFileName,
		csvTemplateFormat: csvTemplateFormat,
		defaultStyles: defaultStyles,
		hiddenColumns: hiddenColumns,
		nPaginateRows: nPaginateRows,
		solo: solo,
		sortBy: sortBy,
		sortDir: sortDir,
		tableClassName: tableClassName,
		hideDimensionFilter: hideDimensionFilter,
		activeDimensions: activeDimensions,
		excludeSummaryFromExport: excludeSummaryFromExport
	});
</script>
