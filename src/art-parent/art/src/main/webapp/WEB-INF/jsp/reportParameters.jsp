<%-- 
    Document   : parameters
    Created on : 09-Mar-2016, 11:17:18
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%-- moment.js needs to be loaded before eonasdan datepicker --%>
<c:if test="${hasDateParam || hasDateRangeParam}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/moment-2.22.2/moment-with-locales.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/moment-jdateformatparser-1.2.1/moment-jdateformatparser.min.js"></script>
</c:if>

<c:if test="${hasDateParam}">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-datetimepicker-4.17.47/css/bootstrap-datetimepicker.min.css">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-datetimepicker-4.17.47/js/bootstrap-datetimepicker.min.js"></script>
</c:if>

<c:if test="${hasDateRangeParam}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/daterangepicker-3.0.3/daterangepicker.js"></script>
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/daterangepicker-3.0.3/daterangepicker.css">
</c:if>

<c:if test="${hasLovParam}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-select-1.12.4/js/bootstrap-select.min.js"></script>
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/bootstrap-select-1.12.4/css/bootstrap-select.min.css">
</c:if>

<c:if test="${hasChainedParam}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/appelsiini-chained-selects-1.0.1/jquery.chained.remote.js"></script>
</c:if>

<c:if test="${hasRobinHerbotsMask}">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/Inputmask-4.0.3/min/jquery.inputmask.bundle.min.js"></script>
</c:if>

<c:if test="${hasFileParam}">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/jasny-bootstrap-4.0.0/css/jasny-bootstrap.min.css">
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/jasny-bootstrap-4.0.0/js/jasny-bootstrap.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootbox-4.4.0.min.js"></script>
</c:if>

<c:forEach var="reportParameter" items="${reportParams}">
	<c:set var="reportParam" value="${reportParameter.value}" scope="request"/>

	<c:if test="${reportParam.parameter.hidden}">
		<input type="hidden"
			   name="${encode:forHtmlAttribute(reportParam.htmlElementName)}"
			   id="${encode:forHtmlAttribute(reportParam.htmlElementName)}"
			   value="${encode:forHtmlAttribute(reportParam.getHtmlValueWithLocale(locale))}">
	</c:if>

	<c:if test="${reportParam.parameter.forDisplay}">
		<div class="form-group">
			<label class="control-label ${labelColClass}" for="${encode:forHtmlAttribute(reportParam.htmlElementName)}">
				${encode:forHtmlContent(reportParam.parameter.getLocalizedLabel(locale))}
			</label>
			<div class="${inputColClass}">
				<c:set var="help" value="${reportParam.parameter.getLocalizedHelpText(locale)}"/>
				<c:if test="${not empty fn:trim(help)}">
					<div class="input-group">
					</c:if>

					<c:choose>
						<c:when test="${reportParam.parameter.useLov}">
							<c:set var="lovValues" value="${reportParam.lovValuesAsString}" scope="request"/>
							<c:choose>
								<c:when test="${reportParam.chained}">
									<jsp:include page="chainedInput.jsp" />
								</c:when>
								<c:otherwise>
									<jsp:include page="dropdownInput.jsp" />
								</c:otherwise>
							</c:choose>
						</c:when>
						<c:otherwise>
							<c:choose>
								<c:when test="${reportParam.parameter.parameterType == 'MultiValue'
												|| reportParam.parameter.dataType == 'Text'}">
									<jsp:include page="textareaInput.jsp" />
								</c:when>
								<c:when test="${reportParam.parameter.dataType == 'Date'}">
									<jsp:include page="dateInput.jsp" />
								</c:when>
								<c:when test="${reportParam.parameter.dataType == 'DateTime'}">
									<jsp:include page="datetimeInput.jsp" />
								</c:when>
								<c:when test="${reportParam.parameter.dataType == 'DateRange'}">
									<jsp:include page="daterangeInput.jsp" />
								</c:when>
								<c:when test="${reportParam.parameter.dataType == 'File'}">
									<jsp:include page="fileInput.jsp" />
								</c:when>
								<c:when test="${reportParam.parameter.dataType.numeric}">
									<jsp:include page="numberInput.jsp" />
								</c:when>
								<c:otherwise>
									<jsp:include page="textInput.jsp"/>
								</c:otherwise>
							</c:choose>
						</c:otherwise>
					</c:choose>

					<c:if test="${not empty fn:trim(help)}">
						<span class="input-group-btn" >
							<button class="btn btn-default" type="button"
									data-toggle="tooltip" title="${help}">
								<i class="fa fa-info"></i>
							</button>
						</span>
					</c:if>

					<c:if test="${not empty fn:trim(help)}">
					</div>
				</c:if>
			</div>
		</div>
	</c:if>
</c:forEach>

<c:forEach var="reportParameter" items="${reportParams}">
	<c:set var="reportParam" value="${reportParameter.value}" scope="request"/>

	<c:if test="${reportParam.chainedParent}">
		<script type="text/javascript">
			$("#${reportParam.htmlElementName}").change();
			$("#${reportParam.hiddenHtmlElementName}").change();
		</script>
	</c:if>
</c:forEach>

