<%-- 
    Document   : fileInput
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<div class="fileinput fileinput-new" data-provides="fileinput">
	<span class="btn btn-default btn-file">
		<span class="fileinput-new">
			<spring:message code="reports.text.selectFile"/>
		</span>
		<span class="fileinput-exists">
			<spring:message code="reports.text.change"/>
		</span>
		<input type="file"
			   name="${encode:forHtmlAttribute(reportParam.htmlElementName)}"
			   accept=".json, .csv, .zip" multiple>
	</span>
	<span class="fileinput-filename"></span>
	<a href="#" class="close fileinput-exists" data-dismiss="fileinput" style="float: none">&times;</a>
</div>
