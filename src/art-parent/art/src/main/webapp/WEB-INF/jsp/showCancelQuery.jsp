<%-- 
    Document   : showCancelQuery
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<spring:message code="page.message.errorOccurred" var="errorOccurredText" javaScriptEscape="true"/>

<div id="cancelQueryDiv-${runId}" class="pull-right" style="display:none">
	<button type="button" id="cancelQuery-${runId}">
		<spring:message code="dialog.button.cancel"/>
	</button>
</div>

<script>
	$("#cancelQuery-${runId}").on("click", function () {
		var runId = "${runId}";
		var url = '${pageContext.request.contextPath}/cancelQuery';
		var data = {runId: runId};

		$.ajax({
			type: 'POST',
			url: url,
			dataType: 'json',
			data: data,
			success: function (response) {
				if (!response.success) {
					if (response.errorMessage) {
						console.log("Error", response.errorMessage);
					} else {
						//statement not found. try again after a short interval
						var retryIntervalMilliseconds = 1000;
						setTimeout(function () {
							$.post(url, data)
						}, retryIntervalMilliseconds);
					}
				}
			},
			error: function (xhr) {
				//https://stackoverflow.com/questions/6186770/ajax-request-returns-200-ok-but-an-error-event-is-fired-instead-of-success
				console.log("Error", xhr);
			}
		});
	});
</script>
