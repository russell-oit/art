<%-- 
    Document   : adminConsole
    Created on : 17-Sep-2013, 10:54:42
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:mainPage title="ART - Admin Console">
	<jsp:attribute name="pageJavascript">
		<script type="text/javascript" charset="utf-8">
			$(document).ready(function() {
				$(function() {
					$('a[href*="admin.do"]').parent().addClass('active');
				});
			});
		</script>
	</jsp:attribute>

	<jsp:body>
		HELLO
		<a href="#" class="btn btn-info">Contact Us</a> 
	</jsp:body>

</t:mainPage>
