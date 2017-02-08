<%-- 
    Document   : showDygraphs
    Created on : 07-Feb-2017, 20:12:38
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<%-- must use table to center chart --%>
<%-- http://dygraphs.com/tutorial.html --%>
<table align="center">
	<tr>
		<td>
			<div id="dygraphsOutput">

			</div>
		</td>
	</tr>
</table>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dygraphs-2.0.0/dygraph.min.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/dygraphs-2.0.0/dygraph.min.js"></script>

<script type="text/javascript">
	//http://dygraphs.com/options.html
	//http://dygraphs.com/tutorial.html
	var options = {};
</script>

<c:if test="${not empty templateFileName}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${templateFileName}"></script>
</c:if>

<c:choose>
	<c:when test="${reportType == 'Dygraphs'}">
		<script type="text/javascript">
	new Dygraph(document.getElementById("dygraphsOutput"), '${csvData}', options);
		</script>
	</c:when>
	<c:when test="${reportType == 'DygraphsCsvLocal'}">
		<style>
			#filechooser {
				/* color: #555; */
				text-decoration: underline;
				cursor: pointer; /* "hand" cursor */
			}
		</style>
		<br>
		<p align="center" style="line-height: 1.5">
			<spring:message code="pivotTableJs.text.dropCsv"/>&nbsp;<spring:message code="pivotTableJs.text.or"/>&nbsp;
			<label id="filechooser">
				<spring:message code="pivotTableJs.text.clickToChoose"/>
				<input id="csv" type="file" style="display:none"/>
			</label>
		</p>
		<script type="text/javascript">
			var showDygraphs = function (file) {
				$("#dygraphsOutput").html("<p align='center' style='color:grey;'>(processing...)</p>");
				//https://www.html5rocks.com/en/tutorials/file/dndfiles/
				//https://www.nczonline.net/blog/2012/05/15/working-with-files-in-javascript-part-2/
				//https://www.sitepoint.com/html5-file-drag-drop-read-analyze-upload-progress-bars/
				var reader = new FileReader();
				reader.onload = function (event) {
					var contents = event.target.result;
					//csv file should not have quotes - which means column names should not have commas
					//https://stackoverflow.com/questions/14708486/csv-cannot-display-two-axis-in-dygraphs-without-hardcoding
					new Dygraph(document.getElementById("dygraphsOutput"), contents, options);
				};

				reader.onerror = function (event) {
					bootbox.alert("File could not be read. Error code: " + event.target.error.code);
				};

				reader.readAsText(file);
			};

			$("#csv").bind("change", function (event) {
				showDygraphs(event.target.files[0]);
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
				showDygraphs(evt.originalEvent.dataTransfer.files[0]);
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

