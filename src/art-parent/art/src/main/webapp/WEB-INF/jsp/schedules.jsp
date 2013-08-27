<%@include file="/WEB-INF/jspf/adminHeader.jspf" %>

<%@page import="art.utils.ArtHelper" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<script type="text/javascript" charset=\"utf-8\">
	var $jQuery = jQuery.noConflict();
	$jQuery(document).ready(function() {
		$jQuery('#schedulesTable').dataTable({
			"bJQueryUI": true,
			"sPaginationType": "full_numbers",
			"aaSorting": [],
			"aLengthMenu": [[10, 25, -1], [10, 25, "All"]],
			"iDisplayLength": 10,
			"oLanguage": {
				"sUrl": "<%= ArtHelper.getDataTablesLanguageUrl(request) %>"
			}
		});
	});
</script>

<div id="container">
	<div id="demo_jui">
		<button id="btnAddNewRow" value="Ok">New</button> 
		<button id="btnDeleteRow" value="cancel">Delete selected</button>
		<table id="schedulesTable" class="display">
			<thead>
				<tr>
					<th>Schedule Name</th>
					<th>Minute</th>
					<th>Hour</th>
					<th>Day (Day of the month)</th>
					<th>Month</th>
					<th>Weekday (Day of the week)</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="schedule" items="${schedules}" varStatus="status">
					<tr>
						<td>${schedule.scheduleName}</td>
						<td>${schedule.minute}</td>
						<td>${schedule.hour}</td>
						<td>${schedule.day}</td>
						<td>${schedule.month}</td>
						<td>${schedule.weekday}</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>


</div>

<jsp:include page="/user/scheduleHelp.html"/>

<%@include file="/WEB-INF/jspf/footer.jspf" %>