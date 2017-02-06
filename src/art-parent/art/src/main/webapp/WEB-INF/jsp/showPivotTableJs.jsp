<%-- 
    Document   : showPivotTableJs
    Created on : 05-Feb-2017, 21:38:29
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<div id="pivotTableJsOutput">

</div>


<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-1.12.4.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-ui-1.11.4-all-smoothness/jquery-ui.min.js"></script>

<%-- c3 0.4 doesn't work with d3 4.x --%>
<%-- https://github.com/nicolaskruchten/pivottable/issues/579 --%>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/d3-3.5.17/d3.min.js"></script>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/c3-0.4.11/c3.min.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/c3-0.4.11/c3.min.js"></script>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/pivottable-2.7.0/pivot.min.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/pivottable-2.7.0/pivot.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/pivottable-2.7.0/c3_renderers.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/pivottable-2.7.0/export_renderers.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.ui.touch-punch-0.2.3.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/PapaParse-4.1.4/papaparse.min.js"></script>

<script type="text/javascript">
	//set default values. can be overridden in template file
	//https://github.com/nicolaskruchten/pivottable/wiki/Parameters
	//https://stackoverflow.com/questions/4528744/how-does-extend-work-in-jquery
	//https://stackoverflow.com/questions/10130908/jquery-merge-two-objects
	var renderers = $.extend(
			$.pivotUtilities.renderers,
			$.pivotUtilities.c3_renderers,
			$.pivotUtilities.export_renderers
			);

	var options = {renderers: renderers};
	var overwrite = false;
	var locale = 'en';

	var download;
	var reportType = '${reportType}';
	if (reportType === 'PivotTableJsCsvServer') {
		download = true;
	} else {
		download = false;
	}

	var csvConfig = {
		download: download,
		skipEmptyLines: true,
		error: function (e) {
			bootbox.alert(e);
		},
		complete: function (parsed) {
			$("#pivotTableJsOutput").pivotUI(parsed.data, options, overwrite, locale);
		}
	};
</script>

<c:if test="${not empty templateFileName}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${templateFileName}"></script>
</c:if>

<c:if test="${not empty locale}">
	<script type="text/javascript">
	locale = '${locale}';
	</script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/pivottable-2.7.0/pivot.${locale}.js"></script>
</c:if>

<c:choose>
	<c:when test="${reportType == 'PivotTableJs'}">
		<script type="text/javascript">
	$("#pivotTableJsOutput").pivotUI(${input}, options, overwrite, locale);
		</script>
	</c:when>
	<c:when test="${reportType == 'PivotTableJsCsvLocal'}">
		<%-- http://nicolas.kruchten.com/pivottable/examples/local.html --%>
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
//			$('#csv').parse({
//				config: csvConfig
//			});
			var parseAndPivot = function (f) {
				$("#pivotTableJsOutput").html("<p align='center' style='color:grey;'>(processing...)</p>");
				Papa.parse(f, csvConfig);
			};

			$("#csv").bind("change", function (event) {
				parseAndPivot(event.target.files[0]);
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
				parseAndPivot(evt.originalEvent.dataTransfer.files[0]);
			};

			$("html")
					.on("dragover", dragging)
					.on("dragend", endDrag)
					.on("dragexit", endDrag)
					.on("dragleave", endDrag)
					.on("drop", dropped);

		</script>
	</c:when>
	<c:when test="${reportType == 'PivotTableJsCsvServer'}">
		<script type="text/javascript">
			//http://nicolas.kruchten.com/pivottable/examples/mps_csv.html
			var dataFile = '${pageContext.request.contextPath}/js-templates/${dataFileName}';
				Papa.parse(dataFile, csvConfig);
		</script>
	</c:when>
</c:choose>
