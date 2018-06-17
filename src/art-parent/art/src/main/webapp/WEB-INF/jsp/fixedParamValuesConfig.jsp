<%-- 
    Document   : fixedParamValuesConfig
    Created on : 02-Apr-2018, 22:22:16
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.fixedParamValuesConfiguration" var="pageTitle"/>

<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="page.message.valuesRemoved" var="valuesRemovedText"/>
<spring:message code="page.message.valueAdded" var="valueAddedText"/>
<spring:message code="page.message.selectUserOrUserGroup" var="selectUserOrUserGroupText"/>
<spring:message code="page.text.available" var="availableText"/>
<spring:message code="page.text.selected" var="selectedText"/>
<spring:message code="page.text.search" var="searchText"/>
<spring:message code="select.text.noResultsMatch" var="noResultsMatchText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-6 col-md-offset-3">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/lou-multi-select-0.9.11/css/multi-select.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/css/bootstrap-select.min.css">
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-select-1.10.0/js/bootstrap-select.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/notify-combined-0.3.1.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootbox-4.4.0.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/lou-multi-select-0.9.11/js/jquery.multi-select.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.quicksearch.js"></script>

		<script type="text/javascript">
			$(document).ready(function () {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="fixedParamValuesConfig"]').parent().addClass('active');

				$('.multi-select').multiSelect({
					selectableHeader: "<div>${availableText}</div>\n\
					<input type='text' class='form-control input-sm' autocomplete='off' placeholder='${searchText}'>",
					selectionHeader: "<div>${selectedText}</div>\n\
					<input type='text' class='form-control input-sm' autocomplete='off' placeholder='${searchText}'>",
					afterInit: function (ms) {
						var that = this,
								$selectableSearch = that.$selectableUl.prev(),
								$selectionSearch = that.$selectionUl.prev(),
								selectableSearchString = '#' + that.$container.attr('id') + ' .ms-elem-selectable:not(.ms-selected)',
								selectionSearchString = '#' + that.$container.attr('id') + ' .ms-elem-selection.ms-selected';
						that.qs1 = $selectableSearch.quicksearch(selectableSearchString)
								.on('keydown', function (e) {
									if (e.which === 40) {
										that.$selectableUl.focus();
										return false;
									}
								});
						that.qs2 = $selectionSearch.quicksearch(selectionSearchString)
								.on('keydown', function (e) {
									if (e.which === 40) {
										that.$selectionUl.focus();
										return false;
									}
								});
					},
					afterSelect: function () {
						this.qs1.cache();
						this.qs2.cache();
					},
					afterDeselect: function () {
						this.qs1.cache();
						this.qs2.cache();
					}
				}); //end multiselect

				//Enable Bootstrap-Select
				$('.selectpicker').selectpicker({
					liveSearch: true,
					noneResultsText: '${noResultsMatchText}'
				});

				//activate dropdown-hover. to make bootstrap-select open on hover
				//must come after bootstrap-select initialization
				$('button.dropdown-toggle').dropdownHover({
					delay: 100
				});

				$('#actionsDiv').on('click', '.updateValues', function () {
					var action = $(this).data('action');

					var users = $('#users').val();
					var userGroups = $('#userGroups').val();
					var parameter = $('#parameter').val();
					var value = $('#value').val();

					if (users === null && userGroups === null) {
						bootbox.alert("${selectUserOrUserGroupText}");
						return;
					}

					var valuesUpdatedMessage;
					if (action === 'add') {
						valuesUpdatedMessage = "${valueAddedText}";
					} else {
						valuesUpdatedMessage = "${valuesRemovedText}";
					}

					$.ajax({
						type: "POST",
						dataType: "json",
						url: "${pageContext.request.contextPath}/updateFixedParamValue",
						data: {action: action, users: users, userGroups: userGroups,
							parameter: parameter, value: value},
						success: function (response) {
							if (response.success) {
								notifyActionSuccess(valuesUpdatedMessage);
							} else {
								notifyActionError("${errorOccurredText}", response.errorMessage, ${showErrors});
							}
						},
						error: ajaxErrorHandler
					}); //end ajax
				}); //end on click

				//handle select all/deselect all
				addSelectDeselectAllHandler();

			}); //end document ready
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
							<p><encode:forHtmlContent value="${error}"/></p>
						</c:if>
					</div>
				</c:if>

				<div id="ajaxResponse">
				</div>

				<div class="form-group">
					<label class="control-label col-md-3" for="users">
						<spring:message code="page.text.users"/>
					</label>
					<div class="col-md-9">
						<select name="users" id="users" multiple="multiple" class="form-control multi-select">
							<c:forEach var="user" items="${users}">
								<option value="${user.userId}">
									<encode:forHtmlContent value="${user.username}"/>
								</option>
							</c:forEach>
						</select>
						<a href="#" class="select-all" data-item="#users"><spring:message code="page.text.selectAll"/></a> / 
						<a href="#" class="deselect-all" data-item="#users"><spring:message code="page.text.deselectAll"/></a>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-3" for="userGroups">
						<spring:message code="page.text.userGroups"/>
					</label>
					<div class="col-md-9">
						<select name="userGroups" id="userGroups" multiple="multiple" class="form-control multi-select">
							<c:forEach var="userGroup" items="${userGroups}">
								<option value="${userGroup.userGroupId}">
									<encode:forHtmlContent value="${userGroup.name}"/>
								</option>
							</c:forEach>
						</select>
						<a href="#" class="select-all" data-item="#userGroups"><spring:message code="page.text.selectAll"/></a> / 
						<a href="#" class="deselect-all" data-item="#userGroups"><spring:message code="page.text.deselectAll"/></a>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-3" for="parameter">
						<spring:message code="page.text.parameter"/>
					</label>
					<div class="col-md-9">
						<select name="parameter" id="parameter" class="form-control selectpicker">
							<c:forEach var="parameter" items="${parameters}">
								<option value="${parameter.parameterId}">
									${encode:forHtmlContent(parameter.name)} (${parameter.parameterId})
								</option>
							</c:forEach>
						</select>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-3" for="value">
						<spring:message code="page.text.value"/>
					</label>
					<div class="col-md-9">
						<textarea rows="4" class="form-control"
								  name="value" id="value"></textarea>
					</div>
				</div>
				<div class="form-group">
					<div class="col-md-12">
						<div id="actionsDiv" class="pull-right">
							<a class="btn btn-default" 
							   href="${pageContext.request.contextPath}/fixedParamValues">
								<spring:message code="page.action.show"/>
							</a>
							<button type="button" class="btn btn-default updateValues" data-action="add">
								<spring:message code="page.action.add"/>
							</button>
							<button type="button" class="btn btn-default updateValues" data-action="removeAll">
								<spring:message code="page.action.removeAll"/>
							</button>
						</div>
					</div>
				</div>
			</fieldset>
		</form>
	</jsp:body>
</t:mainPageWithPanel>
