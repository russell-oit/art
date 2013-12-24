<%-- 
    Document   : configurationPage
    Created on : 04-Nov-2013, 08:36:39
    Author     : Timothy Anyona

Template for configuration pages
Includes the elements in a main page, plus highlighting of the configure menu
--%>

<%@tag description="Configuration Page Template" pageEncoding="UTF-8"%>
<%@tag trimDirectiveWhitespaces="true" %>

<%@taglib tagdir="/WEB-INF/tags" prefix="t" %>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="title" required="true" %>
<%@attribute name="css" fragment="true" %>
<%@attribute name="javascript" fragment="true" %>

<%-- any content can be specified here e.g.: --%>
<t:mainPage title="${title}">
	<jsp:attribute name="javascript">
		<script type="text/javascript" charset="utf-8">
			$(document).ready(function() {
				$(function() {
					$('a[id="configure"]').parent().addClass('active');
				});

				$(function() {
					$("[data-toggle='tooltip']").tooltip({container: 'body'});
				});
			});
		</script>
		
		<jsp:invoke fragment="javascript"/>
	</jsp:attribute>

	<jsp:body>
        <jsp:doBody/>
    </jsp:body>
</t:mainPage>