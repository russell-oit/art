<%-- 
    Document   : parameters
    Created on : 09-Mar-2016, 11:17:18
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<c:forEach var="reportParameter" items="${reportParams}">
	<c:set var="reportParam" value="${reportParameter.value}" scope="request"/>

	<c:if test="${!reportParam.parameter.hidden}">
		<div class="form-group">
			<label class="control-label ${labelColClass}" for="${reportParam.htmlElementName}">
				${reportParam.parameter.label}
			</label>
			<div class="${inputColClass}">
				<div class="input-group">
					<c:choose>
						<c:when test="${reportParam.parameter.useLov}">
							<c:set var="lovValues" value="${reportParam.lovValuesAsString}" scope="request"/>
							<c:choose>
								<c:when test="${not empty reportParam.chainedParents}">
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
								<c:otherwise>
									<jsp:include page="textInput.jsp"/>
								</c:otherwise>
							</c:choose>
						</c:otherwise>
					</c:choose>

					<c:set var="help" value="${reportParam.parameter.helpText}"/>
					<c:if test="${not empty fn:trim(help)}">
						<span class="input-group-btn" >
							<button class="btn btn-default" type="button"
									data-toggle="tooltip" title="${help}">
								<i class="fa fa-info"></i>
							</button>
						</span>
					</c:if>
				</div>
			</div>
		</div>
	</c:if>
</c:forEach>

