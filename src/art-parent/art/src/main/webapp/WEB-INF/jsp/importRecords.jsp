<%-- 
    Document   : importRecords
    Created on : 21-Jan-2018, 21:17:22
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<spring:message code="page.title.importRecords" var="importRecordsText"/>
<c:set var="panelTitle">
	${importRecordsText} - <spring:message code="${importRecords.recordType.localizedDescription}"/>
</c:set>
<c:set var="pageTitle">
	${panelTitle}
</c:set>

<spring:message code="switch.text.yes" var="yesText" javaScriptEscape="true"/>
<spring:message code="switch.text.no" var="noText" javaScriptEscape="true"/>

<t:mainPageWithPanel title="${pageTitle}" panelTitle="${panelTitle}"
					 mainColumnClass="col-md-6 col-md-offset-3">

	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jasny-bootstrap-4.0.0/css/jasny-bootstrap.min.css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-switch/css/bootstrap3/bootstrap-switch.min.css">
	</jsp:attribute>

	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jasny-bootstrap-4.0.0/js/jasny-bootstrap.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-switch/js/bootstrap-switch.min.js"></script>

		<script type="text/javascript">
			$(document).ready(function () {
				//enable bootstrap-switch
				$('.switch-yes-no').bootstrapSwitch({
					onText: '${yesText}',
					offText: '${noText}'
				});
			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<spring:url var="formUrl" value="/importRecords"/>
		<form:form class="form-horizontal" method="POST" action="${formUrl}" modelAttribute="importRecords" enctype="multipart/form-data">
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
							<p><encode:forHtmlContent value="${error}"/></p>
						</c:if>
					</div>
				</c:if>

				<form:hidden path="recordType"/>

				<div class="form-group">
					<label class="control-label col-md-4" for="filePath">
						<spring:message code="importRecords.label.file"/>
					</label>
					<div class="col-md-8">
						<div class="fileinput fileinput-new" data-provides="fileinput">
							<span class="btn btn-default btn-file">
								<span class="fileinput-new">
									<spring:message code="reports.text.selectFile"/>
								</span>
								<span class="fileinput-exists">
									<spring:message code="reports.text.change"/>
								</span>
								<input type="file" name="importFile" accept=".json, .csv, .zip">
							</span>
							<span class="fileinput-filename"></span>
							<a href="#" class="close fileinput-exists" data-dismiss="fileinput" style="float: none">&times;</a>
						</div>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4">
						<spring:message code="reports.label.format"/>
					</label>
					<div class="col-md-8">
						<c:forEach var="fileFormat" items="${fileFormats}">
							<label class="radio-inline">
								<form:radiobutton path="fileFormat"
												  value="${fileFormat}"/>
								${fileFormat.description}
							</label>
						</c:forEach>
						<form:errors path="fileFormat" cssClass="error"/>
					</div>
				</div>
				<div class="form-group">
					<label class="control-label col-md-4" for="overwrite">
						<spring:message code="reports.text.overwrite"/>
					</label>
					<div class="col-md-8">
						<div class="checkbox">
							<form:checkbox path="overwrite" id="overwrite" class="switch-yes-no"/>
						</div>
					</div>
				</div>
				<div class="form-group">
					<div class="col-md-12">
						<button type="submit" class="btn btn-primary pull-right">
							<spring:message code="page.text.import"/>
						</button>
					</div>
				</div>
			</fieldset>
		</form:form>
	</jsp:body>
</t:mainPageWithPanel>
