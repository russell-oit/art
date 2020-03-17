<%-- 
    Document   : showCancelQuery
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<spring:message code="page.message.errorOccurred" var="errorOccurredText" javaScriptEscape="true"/>

<div id="cancelQueryDiv" class="pull-right" style="display:none">
	<button type="button" id="cancelQuery" data-statement-id="${statementId}">
		<spring:message code="dialog.button.cancel"/>
	</button>
</div>

<script>
	$("#cancelQuery").on("click", function () {
		var statementId = $(this).attr("data-statement-id");

		$.ajax({
			type: 'POST',
			url: '${pageContext.request.contextPath}/cancelQuery',
			data: {statementId: statementId},
			error: function (xhr) {
				//https://stackoverflow.com/questions/6186770/ajax-request-returns-200-ok-but-an-error-event-is-fired-instead-of-success
				console.log("Error", xhr);
			}
		});
	});
</script>
