<%-- 
    Document   : adminRightsConfig
    Created on : 19-Apr-2014, 20:02:45
    Author     : Timothy Anyona

Admin rights configuration page
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.adminRightsConfiguration" var="pageTitle"/>

<spring:message code="page.message.errorOccurred" var="errorOccurredText"/>
<spring:message code="page.message.rightsGranted" var="rightsGrantedText"/>
<spring:message code="page.message.rightsRevoked" var="rightsRevokedText"/>
<spring:message code="adminRights.message.selectAdmin" var="selectAdminText"/>
<spring:message code="adminRights.message.selectDatasourceOrReportGroup" var="selectDatasourceOrReportGroupText"/>
<spring:message code="page.text.available" var="availableText"/>
<spring:message code="page.text.selected" var="selectedText"/>
<spring:message code="page.text.search" var="searchText"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-6 col-md-offset-3">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/lou-multi-select-0.9.11/css/multi-select.css">
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/notify-combined-0.3.1.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootbox-4.4.0.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/lou-multi-select-0.9.11/js/jquery.multi-select.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.quicksearch.js"></script>
		
		<script type="text/javascript">
			$(document).ready(function() {
				$('a[id="configure"]').parent().addClass('active');
				$('a[href*="adminRightsConfig"]').parent().addClass('active');

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
										that.$selectableUl.focus();
										return false;
									}
								});

						that.qs2 = $selectionSearch.quicksearch(selectionSearchString)
								.on('keydown', function(e) {
									if (e.which === 40) {
										that.$selectionUl.focus();
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
				}); //end multiselect

				$('#actionsDiv').on('click', '.updateRights', function() {
					var action = $(this).data('action');

					var admins = $('#admins').val();
					var datasources = $('#datasources').val();
					var reportGroups = $('#reportGroups').val();

					if (admins === null) {
						bootbox.alert("${selectAdminText}");
						return;
					}
					if (datasources === null && reportGroups === null) {
						bootbox.alert("${selectDatasourceOrReportGroupText}");
						return;
					}

					var rightsUpdatedMessage;
					if (action === 'grant') {
						rightsUpdatedMessage = "${rightsGrantedText}";
					} else {
						rightsUpdatedMessage = "${rightsRevokedText}";
					}

					$.ajax({
						type: "POST",
						dataType: "json",
						url: "${pageContext.request.contextPath}/updateAdminRight",
						data: {action: action, admins: admins, datasources: datasources,
							reportGroups: reportGroups},
						success: function(response) {
							var reusableAlert = true;
							if (response.success) {
								var recordName = undefined;
								notifyActionSuccess(rightsUpdatedMessage, recordName, reusableAlert);
							} else {
								notifyActionError("${errorOccurredText}", response.errorMessage, ${showErrors}, reusableAlert);
							}
						},
						error: ajaxErrorHandler
					}); //end ajax
				}); //end on click

				//handle select all/deselect all
				addSelectDeselectAllHandler();
				
				$('#ajaxResponseContainer').on("click", ".alert .close", function () {
					$(this).parent().hide();
				});

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

				<div id="ajaxResponseContainer">
					<div id="ajaxResponse">
					</div>
				</div>

				<div class="form-group">
					<label class="control-label col-md-3" for="admins">
						<spring:message code="adminRights.text.admins"/>
					</label>
					<div class="col-md-9">
						<select name="admins" id="admins" multiple="multiple" class="form-control multi-select">
							<c:forEach var="admin" items="${admins}">
								<option value="${admin.userId}-${encode:forHtmlAttribute(admin.username)}">
									<encode:forHtmlContent value="${admin.username}"/>
								</option>
							</c:forEach>
						</select>
						<a href="#" class="select-all" data-item="#admins"><spring:message code="page.text.selectAll"/></a> / 
						<a href="#" class="deselect-all" data-item="#admins"><spring:message code="page.text.deselectAll"/></a>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-3" for="datasources">
						<spring:message code="adminRights.text.datasources"/>
					</label>
					<div class="col-md-9">
						<select name="datasources" id="datasources" multiple="multiple" class="form-control multi-select">
							<c:forEach var="datasource" items="${datasources}">
								<option value="${datasource.datasourceId}">
									<encode:forHtmlContent value="${datasource.name}"/>
								</option>
							</c:forEach>
						</select>
						<a href="#" class="select-all" data-item="#datasources"><spring:message code="page.text.selectAll"/></a> / 
						<a href="#" class="deselect-all" data-item="#datasources"><spring:message code="page.text.deselectAll"/></a>
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
						<a href="#" class="select-all" data-item="#reportGroups"><spring:message code="page.text.selectAll"/></a> / 
						<a href="#" class="deselect-all" data-item="#reportGroups"><spring:message code="page.text.deselectAll"/></a>
					</div>
				</div>
				<div class="form-group">
					<div class="col-md-12">
						<div id="actionsDiv" class="pull-right">
							<a class="btn btn-default" 
							   href="${pageContext.request.contextPath}/adminRights">
								<spring:message code="page.action.show"/>
							</a>
							<button type="button" class="btn btn-default updateRights" data-action="grant">
								<spring:message code="page.action.grant"/>
							</button>
							<button type="button" class="btn btn-default updateRights" data-action="revoke">
								<spring:message code="page.action.revoke"/>
							</button>
						</div>
					</div>
				</div>
			</fieldset>
		</form>
	</jsp:body>
</t:mainPageWithPanel>
