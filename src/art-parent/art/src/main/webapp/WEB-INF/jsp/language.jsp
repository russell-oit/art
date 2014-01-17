<%-- 
    Document   : language
    Created on : 17-Jan-2014, 11:13:03
    Author     : Timothy Anyona

Display application language selection page
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="localeCode" value="${pageContext.response.locale}"/>

<spring:message code="page.title.language" var="pageTitle"/>

<t:mainPageWithPanel title="${pageTitle}" mainColumnClass="col-md-6 col-md-offset-3">

	<jsp:attribute name="javascript">
		<script type="text/javascript">
			$(document).ready(function() {
				$(function() {
					$('a[href*="language.do"]').parent().addClass('active');
				});
				
				$('#lang').focus();
			});
		</script>
	</jsp:attribute>

	<jsp:body>
		<form class="form-horizontal" method="POST" action="">
			<fieldset>
				<c:if test="${not empty success}">
					<div class="alert alert-success alert-dismissable">
						<button type="button" class="close" data-dismiss="alert" aria-hidden="true">x</button>
						<spring:message code="language.message.languageUpdated"/>
					</div>
				</c:if>
				
				<div class="form-group">
					<label class="control-label col-md-2" for="lang">
						<spring:message code="page.label.language"/>
					</label>
					<div class="col-md-10">
						<select name="lang" id="lang" class="form-control">
							<option value="en">English</option>
							<c:forEach var="language" items="${languages}">
								<option value="${language.key}" ${localeCode == language.key ? "selected" : ""}>
									${language.value}
								</option>
							</c:forEach>
						</select>
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
		</form>
	</jsp:body>
</t:mainPageWithPanel>
