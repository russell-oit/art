<link rel='stylesheet' type='text/css' href='${contextPath}/css/htmlGridOutput.css'>
<script type='text/javascript' src='${contextPath}/js/sorttable.js'></script>
<script type='text/javascript' src='${contextPath}/js/htmlGridOutput.js'></script>

Region ID: ${regionId.displayValues}
<br>

<table class='sortable'>
	<thead>
		<tr>
			<td>ID</td>
			<td>Name</td>
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