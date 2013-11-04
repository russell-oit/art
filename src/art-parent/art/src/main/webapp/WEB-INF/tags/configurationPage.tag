<%-- 
    Document   : configurationPage
    Created on : 04-Nov-2013, 08:36:39
    Author     : Timothy Anyona

Template for configuration pages
Includes the elements in a main page
--%>

<%@tag description="Configuration Page Template" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="title" required="true"%>

<%-- any content can be specified here e.g.: --%>
<t:mainPage title="${title}">
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
		${title} <br>
        <jsp:doBody/>
    </jsp:body>
</t:mainPage>