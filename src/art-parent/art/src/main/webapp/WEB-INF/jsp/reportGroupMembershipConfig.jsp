<%-- 
    Document   : reportGroupMembershipConfig
    Created on : 12-Nov-2017, 12:47:48
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.reportGroupMembershipConfiguration" var="pageTitle"/>

<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="page.message.membershipAdded" var="membershipAddedText"/>
<spring:message code="page.message.membershipRemoved" var="membershipRemovedText"/>
<spring:message code="reportGroupMembership.message.selectReport" var="selectReportText"/>
<spring:message code="reportGroupMembership.message.selectReportGroup" var="selectReportGroupText"/>
<spring:message code="page.text.available" var="availableText"/>
<spring:message code="page.text.selected" var="selectedText"/>
<spring:message code="page.text.search" var="searchText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-6 col-md-offset-3"
					 hasNotify="true">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/lou-multi-select-0.9.11/css/multi-select.css">
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/lou-multi-select-0.9.11/js/jquery.multi-select.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.quicksearch.js"></script>
		
		<script type="text/javascript">
			$(document).ready(function() {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="reportGroupMembershipConfig"]').parent().addClass('active');

				$('.multi-select').multiSelect({
					selectableHeader: "<div>${availableText}</div>\n\
					<input type='text' class='form-control input-sm' autocomplete='off' placeholder='${searchText}'>",
					selectionHeader: "<div>${selectedText}</div>\n\
					<input type='text' class='form-control input-sm' autocomplete='off' placeholder='${searchText}'>",
					afterInit: function(ms) {
						var that = this,
								$selectableSearch = that.$selectableUl.prev(),
								$selectionSearch = that.$selectionUl.prev(),
								selectableSearchString = '#' + that.$container.attr('id') + ' .ms-elem-selectable:not(.ms-selected)',
								selectionSearchString = '#' + that.$container.attr('id') + ' .ms-elem-selection.ms-selected';

						that.qs1 = $selectableSearch.quicksearch(selectableSearchString)
								.on('keydown', function(e) {
									if (e.which === 40) {
										that.$selectableUl.trigger("focus");
										return false;
									}
								});

						that.qs2 = $selectionSearch.quicksearch(selectionSearchString)
								.on('keydown', function(e) {
									if (e.which === 40) {
										that.$selectionUl.trigger("focus");
										return false;
									}
								});
					},
					afterSelect: function() {
						this.qs1.cache();
						this.qs2.cache();
					},
					afterDeselect: function() {
						this.qs1.cache();
						this.qs2.cache();
					}
				});
				
				$('#ajaxResponseContainer').on("click", ".alert .close", function () {
					$(this).parent().hide();
				});

			});

			function updateMembership(action) {
				var reports = $('#reports').val();
				var reportGroups = $('#reportGroups').val();

				if (reports === null) {
					bootbox.alert("${selectReportText}");
					return;
				}
				if (reportGroups === null) {
					bootbox.alert("${selectReportGroupText}");
					return;
				}

				var recordsUpdatedMessage;
				if (action === 'ADD') {
					recordsUpdatedMessage = "${membershipAddedText}";
				} else {
					recordsUpdatedMessage = "${membershipRemovedText}";
				}

				$.ajax({
					type: "POST",
					dataType: "json",
					url: "${pageContext.request.contextPath}/updateReportGroupMembership",
					data: {action: action, reports: reports, reportGroups: reportGroups},
					success: function(response) {
						if (response.success) {
							notifyActionSuccessReusable(recordsUpdatedMessage);
						} else {
							notifyActionErrorReusable("${errorOccurredText}", response.errorMessage, ${showErrors});
						}
					},
					error: function(xhr) {
						ajaxErrorHandler(xhr);
					}
				}); //end ajax
			}

			$('#select-all-reports').click(function() {
				$('#reports').multiSelect('select_all');
				return false;
			});
			$('#deselect-all-reports').click(function() {
				$('#reports').multiSelect('deselect_all');
				return false;
			});

			$('#select-all-reportGroups').click(function() {
				$('#reportGroups').multiSelect('select_all');
				return false;
			});
			$('#deselect-all-reportGroups').click(function() {
				$('#reportGroups').multiSelect('deselect_all');
				return false;
			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<form class="form-horizontal" method="POST" action="">
			<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
			<fieldset>
				<c:if test="${error != null}">
					<div class="alert alert-danger alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
						<p><spring:message code="page.message.errorOccurred"/></p>
						<c:if test="${showErrors}">
							<p>${encode:forHtmlContent(error)}</p>
						</c:if>
					</div>
				</c:if>

				<div id="ajaxResponseContainer">
					<div id="ajaxResponse">
					</div>
				</div>

				<div class="form-group">
					<label class="control-label col-md-3" for="reports">
						<spring:message code="page.text.reports"/>
					</label>
					<div class="col-md-9">
						<select name="reports" id="reports" multiple="multiple" class="form-control multi-select">
							<c:forEach var="report" items="${reports}">
								<option value="${report.reportId}">
									<encode:forHtmlContent value="${report.name}"/>
								</option>
							</c:forEach>
						</select>
						<a href='#' id='select-all-reports'><spring:message code="page.text.selectAll"/></a> / 
						<a href='#' id='deselect-all-reports'><spring:message code="page.text.deselectAll"/></a>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-3" for="reportGroups">
						<spring:message code="page.text.reportGroups"/>
					</label>
					<div class="col-md-9">
						<select name="reportGroups" id="reportGroups" multiple="multiple" class="form-control multi-select">
							<c:forEach var="reportGroup" items="${reportGroups}">
								<option value="${reportGroup.reportGroupId}">
									<encode:forHtmlContent value="${reportGroup.name}"/>
								</option>
							</c:forEach>
						</select>
						<a href='#' id='select-all-reportGroups'><spring:message code="page.text.selectAll"/></a> / 
						<a href='#' id='deselect-all-reportGroups'><spring:message code="page.text.deselectAll"/></a>
					</div>
				</div>
				<div class="form-group">
					<div class="col-md-12">
						<div class="pull-right">
							<a class="btn btn-default" 
							   href="${pageContext.request.contextPath}/reportGroupMembership">
								<spring:message code="page.action.show"/>
							</a>
							<button type="button" class="btn btn-default" onclick="updateMembership('ADD');">
								<spring:message code="page.action.add"/>
							</button>
							<button type="button" class="btn btn-default" onclick="updateMembership('REMOVE');">
								<spring:message code="page.action.remove"/>
							</button>
						</div>
					</div>
				</div>
			</fieldset>
		</form>
	</jsp:body>
</t:mainPageWithPanel>
