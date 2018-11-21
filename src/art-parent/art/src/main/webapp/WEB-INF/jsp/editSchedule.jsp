<%-- 
    Document   : editSchedule
    Created on : 01-Apr-2014, 11:23:24
    Author     : Timothy Anyona

Edit schedule page
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:choose>
	<c:when test="${action == 'add'}">
		<spring:message code="page.title.addSchedule" var="pageTitle"/>
		<c:set var="panelTitle" value="${pageTitle}"/>
	</c:when>
	<c:when test="${action == 'copy'}">
		<spring:message code="page.title.copySchedule" var="pageTitle"/>
		<c:set var="panelTitle" value="${pageTitle}"/>
	</c:when>
	<c:when test="${action == 'edit'}">
		<spring:message code="page.title.editSchedule" var="panelTitle"/>
		<c:set var="pageTitle">
			${panelTitle} - ${schedule.name}
		</c:set>
	</c:when>
</c:choose>

<spring:message code="select.text.nothingSelected" var="nothingSelectedText"/>
<spring:message code="select.text.noResultsMatch" var="noResultsMatchText"/>
<spring:message code="select.text.selectedCount" var="selectedCountText"/>
<spring:message code="select.text.selectAll" var="selectAllText"/>
<spring:message code="select.text.deselectAll" var="deselectAllText"/>
<spring:message code="jobs.text.nextRunDate" var="nextRunDateText"/>
<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>

<t:mainPageWithPanel title="${pageTitle}" mainPanelTitle="${panelTitle}"
					 mainColumnClass="col-md-6 col-md-offset-3" hasNotify="true">

	<jsp:attribute name="headContent">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/moment-2.17.1/moment-with-locales.min.js"></script>

		<script>
			//put obtaining of server offset in head to reduce difference between server and client time
			//https://stackoverflow.com/questions/19629561/moment-js-set-the-base-time-from-the-server
			var serverDate = '${serverDateString}';
			var serverOffset = moment(serverDate, 'YYYY-MM-DD HH:mm:ss.SSS').diff(new Date());

			function currentServerDate()
			{
				return moment().add(serverOffset, 'milliseconds');
			}

			function updateClock()
			{
				var currentTimeString = currentServerDate().format("YYYY-MM-DD HH:mm:ss");
				currentTimeString += '   ${encode:forJavaScript(serverTimeZoneDescription)}';
				$("#clock").val(currentTimeString);
			}
		</script>
	</jsp:attribute>

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/css/bootstrap-select.min.css">
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/js/bootstrap-select.min.js"></script>

		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="schedules"]').parent().addClass('active');

				//display current time. updates every 1000 milliseconds
				setInterval('updateClock()', 1000);

				//{container: 'body'} needed if tooltips shown on input-group element or button
				$("[data-toggle='tooltip']").tooltip({container: 'body'});

				//Enable Bootstrap-Select
				$('.selectpicker').selectpicker({
					liveSearch: true,
					noneSelectedText: '${nothingSelectedText}',
					noneResultsText: '${noResultsMatchText}',
					countSelectedText: '${selectedCountText}',
					selectAllText: '${selectAllText}',
					deselectAllText: '${deselectAllText}'
				});

				//activate dropdown-hover. to make bootstrap-select open on hover
				//must come after bootstrap-select initialization
				$('button.dropdown-toggle').bootstrapDropdownHover({
					hideTimeout: 100
				});

				$('#name').trigger("focus");

				$('#describeSchedule').on("click", function () {
					var second = $('#second').val();
					var minute = $('#minute').val();
					var hour = $('#hour').val();
					var day = $('#day').val();
					var month = $('#month').val();
					var weekday = $('#weekday').val();
					var year = $('#year').val();

					$.ajax({
						type: 'POST',
						url: '${pageContext.request.contextPath}/describeSchedule',
						dataType: 'json',
						data: {second: second, minute: minute, hour: hour, day: day,
							month: month, weekday: weekday, year: year},
						success: function (response)
						{
							if (response.success) {
								var scheduleDescription = response.data;
								var finalString = "<p><pre>" + escapeHtmlContent(scheduleDescription.description)
										+ "</pre><b>${nextRunDateText}:</b> <pre>"
										+ escapeHtmlContent(scheduleDescription.nextRunDateString)
										+ "</pre></p>";
								$("#mainScheduleDescriptionDiv").html(finalString);
							} else {
								notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
							}
						},
						error: function (xhr) {
							ajaxErrorHandler(xhr);
						}
					});
				});

				$('#ajaxResponseContainer').on("click", ".alert .close", function () {
					$(this).parent().hide();
				});

			});
		</script>
	</jsp:attribute>

	<jsp:attribute name="aboveMainPanel">
		<div class="text-right">
			<a href="${pageContext.request.contextPath}/docs/Manual.html#saved-schedules">
				<spring:message code="page.link.help"/>
			</a>
		</div>
	</jsp:attribute>

	<jsp:attribute name="belowMainPanel">
		<div class="row">
			<div class="col-md-6 col-md-offset-3">
				<div class="alert alert-info">
					<jsp:include page="/WEB-INF/jsp/scheduleNotes.jsp" />
				</div>
			</div>
		</div>
	</jsp:attribute>

	<jsp:body>
		<spring:url var="formUrl" value="/saveSchedule"/>
		<form:form class="form-horizontal" method="POST" action="${formUrl}" modelAttribute="schedule">
			<fieldset>
				<c:if test="${formErrors != null}">
					<div class="alert alert-danger alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
						<spring:message code="page.message.formErrors"/>
					</div>
				</c:if>
				<c:if test="${error != null}">
					<div class="alert alert-danger alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
						<p><spring:message code="page.message.errorOccurred"/></p>
						<c:if test="${showErrors}">
							<p>${encode:forHtmlContent(error)}</p>
						</c:if>
					</div>
				</c:if>

				<c:if test="${not empty message}">
					<div class="alert alert-danger alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
						<spring:message code="${message}"/>
					</div>
				</c:if>

				<div id="ajaxResponseContainer">
					<div id="ajaxResponse">
					</div>
				</div>

				<input type="hidden" name="action" value="${action}">
				<div class="form-group">
					<label class="control-label col-md-4">
						<spring:message code="page.label.id"/>
					</label>
					<div class="col-md-8">
						<c:choose>
							<c:when test="${action == 'edit'}">
								<form:input path="scheduleId" readonly="true" class="form-control"/>
							</c:when>
							<c:when test="${action == 'copy'}">
								<form:hidden path="scheduleId"/>
							</c:when>
						</c:choose>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="name">
						<spring:message code="page.text.name"/>
					</label>
					<div class="col-md-8">
						<form:input path="name" maxlength="50" class="form-control"/>
						<form:errors path="name" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="description">
						<spring:message code="page.text.description"/>
					</label>
					<div class="col-md-8">
						<form:textarea path="description" rows="2" cols="40" class="form-control" maxlength="200"/>
						<form:errors path="description" cssClass="error"/>
					</div>
				</div>

				<hr>
				<div class="form-group">
					<label class="control-label col-md-4" for="clock">
					</label>
					<div class="col-md-8">
						<input type="text" id="clock" readonly class="form-control"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="second">
						<spring:message code="schedules.label.second"/>
					</label>
					<div class="col-md-8">
						<form:input path="second" maxlength="100" class="form-control"/>
						<form:errors path="second" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="minute">
						<spring:message code="schedules.label.minute"/>
					</label>
					<div class="col-md-8">
						<form:input path="minute" maxlength="100" class="form-control"/>
						<form:errors path="minute" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="hour">
						<spring:message code="schedules.label.hour"/>
					</label>
					<div class="col-md-8">
						<form:input path="hour" maxlength="100" class="form-control"/>
						<form:errors path="hour" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="day">
						<spring:message code="schedules.label.day"/>
					</label>
					<div class="col-md-8">
						<form:input path="day" maxlength="100" class="form-control"/>
						<form:errors path="day" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="month">
						<spring:message code="schedules.label.month"/>
					</label>
					<div class="col-md-8">
						<form:input path="month" maxlength="100" class="form-control"/>
						<form:errors path="month" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="weekday">
						<spring:message code="schedules.label.weekday"/>
					</label>
					<div class="col-md-8">
						<form:input path="weekday" maxlength="100" class="form-control"/>
						<form:errors path="weekday" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="year">
						<spring:message code="schedules.label.year"/>
					</label>
					<div class="col-md-8">
						<form:input path="year" maxlength="100" class="form-control"/>
						<form:errors path="year" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="timeZone">
						<spring:message code="page.label.timeZone"/>
					</label>
					<div class="col-md-8">
						<form:select path="timeZone" class="form-control selectpicker">
							<form:option value="${encode:forHtmlAttribute(serverTimeZone)}">${encode:forHtmlContent(serverTimeZoneDescription)}</form:option>
								<option data-divider="true"></option>
							<form:options items="${timeZones}"/>
						</form:select>
						<form:errors path="timeZone" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<div class="col-md-8 col-md-offset-4">
						<button type="button" id="describeSchedule" class="btn btn-default">
							<spring:message code="schedules.button.describe"/>
						</button>
						<div id="mainScheduleDescriptionDiv">
							<p>
								<c:if test="${not empty mainScheduleDescription}">
								<pre>${encode:forHtmlContent(mainScheduleDescription)}</pre>
								<b><spring:message code="jobs.text.nextRunDate"/>:</b> <pre><fmt:formatDate value="${nextRunDate}" pattern="${dateDisplayPattern}"/></pre>
							</c:if>
							</p>
						</div>
					</div>
				</div>

				<hr>
				<div class="form-group">
					<label class="col-md-4 control-label " for="extraSchedules">
						<spring:message code="jobs.label.extraSchedules"/>
					</label>
					<div class="col-md-8">
						<form:textarea path="extraSchedules" rows="3" cols="40" class="form-control"/>
						<form:errors path="extraSchedules" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="col-md-4 control-label " for="holidays">
						<spring:message code="schedules.label.holidays"/>
					</label>
					<div class="col-md-8">
						<form:textarea path="holidays" rows="3" cols="40" class="form-control"/>
						<form:errors path="holidays" cssClass="error"/>
					</div>
				</div>

				<hr>
				<div class="form-group">
					<label class="col-md-4 control-label " for="sharedHolidays">
						<spring:message code="schedules.label.sharedHolidays"/>
					</label>
					<div class="col-md-8">
						<form:select path="sharedHolidays" items="${holidays}" multiple="true" 
									 itemLabel="name" itemValue="holidayId" 
									 class="form-control selectpicker"
									 data-actions-box="true"
									 />
						<form:errors path="sharedHolidays" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<div class="col-md-12">
						<button type="submit" class="btn btn-primary pull-right">
							<spring:message code="page.button.save"/>
						</button>
					</div>
				</div>
			</fieldset>
		</form:form>
	</jsp:body>
</t:mainPageWithPanel>
