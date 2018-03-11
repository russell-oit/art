<%-- 
    Document   : showOrgChart
    Created on : 11-Dec-2017, 18:16:01
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


<div id="${containerId}" style="text-align: center"></div>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/font-awesome-4.5.0/css/font-awesome.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/js/OrgChart-2.0.10/css/jquery.orgchart.min.css">

<script src="${pageContext.request.contextPath}/js/jquery-1.12.4.min.js"></script>
<script src="${pageContext.request.contextPath}/js/OrgChart-2.0.10/js/jquery.orgchart.min.js"></script>

<c:if test="${options.exportButton}">
	<script src="${pageContext.request.contextPath}/js/html2canvas-0.5.0-beta4/html2canvas.min.js"></script>
	<%-- es6-promise.auto only needed for IE. https://caniuse.com/#search=promise --%>
	<script src="${pageContext.request.contextPath}/js/es6-promise-4.1.1/es6-promise.auto.min.js"></script>
	<script src="${pageContext.request.contextPath}/js/jsPDF-1.3.5/jspdf.min.js"></script>
</c:if>

<c:if test="${reportType == 'OrgChartDatabase'}">
	<script src="${pageContext.request.contextPath}/js/JSONLoop.js"></script>
</c:if>

<c:if test="${not empty options.cssFile}">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js-templates/${options.cssFile}">
</c:if>

<script>
	var datasource;
	var dataString = '${data}';
	var reportType = '${reportType}';

	if (reportType === 'OrgChartDatabase') {
		//http://www.orgchartcomponent.com/walkthrough/Article1/
		//https://stackoverflow.com/questions/15862976/what-is-the-best-way-to-model-an-organization-using-people-positions-and-teams
		//http://mikehillyer.com/articles/managing-hierarchical-data-in-mysql/
		//http://www.orgchartcomponent.com/walkthrough/VeryLargeOrgCharts/
		//https://github.com/dabeng/OrgChart/issues/154
		//https://codepen.io/dabeng/pen/mRZpLK
		//https://github.com/dabeng/JSON-Loop
		//https://support.office.com/en-ie/article/Create-an-organization-chart-automatically-from-employee-data-8f2e693e-25fc-410e-8264-9084eb0b9360
		//https://www.smartdraw.com/organizational-chart/organizational-chart-tips.htm
		//https://sharepointorgchart.com/how-to/creating-org-chart-sql-database

		var dataset = JSON.parse(dataString);

		datasource = {};
		dataset.forEach(function (item, index) {
			if (!item.parent_id) {
				delete item.parent_id;
				Object.assign(datasource, item);
			} else {
				var jsonloop = new JSONLoop(datasource, 'id', 'children');
				jsonloop.findNodeById(datasource, item.parent_id, function (err, node) {
					if (err) {
						console.error(err);
					} else {
						delete item.parent_id;
						if (node.children) {
							node.children.push(item);
							var b = 2;
						} else {
							node.children = [item];
							var a = 1;
						}
					}
				});
			}
		});
	} else if (reportType === 'OrgChartJson') {
		//https://www.w3schools.com/js/js_json_parse.asp
		//https://api.jquery.com/jquery.parsejson/
		//https://stackoverflow.com/questions/23311182/convert-json-string-to-object-jquery
		datasource = JSON.parse(dataString);
	} else if (reportType === 'OrgChartList') {
		//https://stackoverflow.com/questions/11047670/creating-a-jquery-object-from-a-big-html-string
		//https://stackoverflow.com/questions/19443345/convert-html-string-into-jquery-object
		//https://rawgit.com/dabeng/OrgChart/master/demo/ul-datasource.html
		datasource = $($.parseHTML(dataString));
	} else if (reportType === 'OrgChartAjax') {
		datasource = '${data}';
	}

	var orgChartSettings = {
		data: datasource,
		initCompleted: function () {
			//show tooltips for title text. may be too long and therefore not shown completely
			$('.orgchart div.title').each(function () {
				var text = $(this).text();
				$(this).attr('title', text);
			});
		}
	};

	var orgChartOptions = JSON.parse('${optionsJson}');
	$.extend(orgChartSettings, orgChartOptions);

	var nodeContent = orgChartSettings.nodeContent;
	if (!nodeContent) {
		//https://stackoverflow.com/questions/5601681/removing-one-property-of-an-object
		//https://stackoverflow.com/questions/208105/how-do-i-remove-a-property-from-a-javascript-object
		//https://stackoverflow.com/questions/6485127/how-to-delete-unset-the-properties-of-a-javascript-object
		delete orgChartSettings.nodeContent;
	}
</script>

<c:if test="${not empty templateFileName}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js-templates/${templateFileName}"></script>
</c:if>

<script>
	$('#${containerId}').orgchart(orgChartSettings);
</script>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/tooltipster/css/tooltipster.bundle.min.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/tooltipster/js/tooltipster.bundle.min.js"></script>

<script type="text/javascript">
	$(document).ready(function () {
		$('.orgchart div.title').tooltipster({
			delay: 0,
			animationDuration: 0
		});
	});
</script>