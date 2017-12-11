<link rel='stylesheet' type='text/css' href='${contextPath}/css/htmlGridOutput.css'>
<script type='text/javascript' src='${contextPath}/js/sorttable.js'></script>
<script type='text/javascript' src='${contextPath}/js/htmlGridOutput.js'></script>

Region ID: ${regionId.displayValues}
<br>

<table id='htmlGridTable' class='sortable'>
	<thead>
		<tr>
			<th>ID</th>
			<th>Name</th>
		</tr>
	</thead>
	<tbody>
		<#list results as result>
			<tr>
				<td>${result.CITY_ID}</td>
				<td>${result.NAME}</td>			
			</tr>
		</#list>
	</tbody>
</table>

<script>
	$(document).ready(function() {
		var table = document.getElementById('htmlGridTable');
		sorttable.DATE_RE = /^(\\d\\d?)[\\/\\.-](\\d\\d?)[\\/\\.-]((\\d\\d)?\\d\\d)$/;
		sorttable.makeSortable(table);
	});
</script>