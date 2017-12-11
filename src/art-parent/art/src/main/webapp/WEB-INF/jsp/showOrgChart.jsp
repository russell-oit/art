<%-- 
    Document   : showOrgChart
    Created on : 11-Dec-2017, 18:16:01
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


<div id="chart-container" style="text-align: center"></div>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/font-awesome-4.5.0/css/font-awesome.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/js/OrgChart-2.0.10/css/jquery.orgchart.min.css">

<script src="${pageContext.request.contextPath}/js/jquery-1.12.4.min.js"></script>
<script src="${pageContext.request.contextPath}/js/OrgChart-2.0.10/js/jquery.orgchart.min.js"></script>

<c:if test="${reportType == 'OrgChartDatabase'}">
	<script src="${pageContext.request.contextPath}/js/JSONLoop.js"></script>
</c:if>

<c:if test="${not empty options.cssFile}">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js-templates/${options.cssFile}">
</c:if>

<script>
	var orgChartOptions = {
		'nodeContent': 'title'
	};

	var dataString = '${data}';
	var datasource = {};
	var reportType = '${reportType}';

	if (reportType === 'OrgChartDatabase') {
		dataString = '[' + dataString + ']';
		var dataset = JSON.parse(dataString);

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

		$.extend(orgChartOptions, {
			data: datasource
		});
	} else if (reportType === 'OrgChartJson') {
		//https://www.w3schools.com/js/js_json_parse.asp
		//https://api.jquery.com/jquery.parsejson/
		//https://stackoverflow.com/questions/23311182/convert-json-string-to-object-jquery
		datasource = JSON.parse(dataString);
		$.extend(orgChartOptions, {
			data: datasource
		});
	} else if (reportType === 'OrgChartList') {
		//https://stackoverflow.com/questions/11047670/creating-a-jquery-object-from-a-big-html-string
		//https://stackoverflow.com/questions/19443345/convert-html-string-into-jquery-object
		var ul = $($.parseHTML(dataString));

		//https://rawgit.com/dabeng/OrgChart/master/demo/ul-datasource.html
		$.extend(orgChartOptions, {
			data: ul
		});
	}

	if (reportType === 'OrgChartDatabase') {
		$.extend(orgChartOptions, {
			initCompleted: function () {
				$('.orgchart div.title').each(function () {
					var text = $(this).text();
					$(this).attr('title', text);
				});
			}
		});
	}

	$('#chart-container').orgchart(orgChartOptions);
</script>