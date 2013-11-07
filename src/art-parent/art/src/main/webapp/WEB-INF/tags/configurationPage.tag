<%-- 
    Document   : configurationPage
    Created on : 04-Nov-2013, 08:36:39
    Author     : Timothy Anyona

Template for configuration pages
Includes the elements in a main page, plus datatables css and javascript
--%>

<%@tag description="Configuration Page Template" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="title" required="true" %>
<%@attribute name="css" fragment="true" %>
<%@attribute name="javascript" fragment="true" %>
<%@attribute name="datatablesAllText" required="true" %>

<%-- any content can be specified here e.g.: --%>
<t:mainPage title="${title}">
	<jsp:attribute name="css">
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/datatables-jowin.css">
		
		<jsp:invoke fragment="css"/>
	</jsp:attribute>
		
	<jsp:attribute name="javascript">
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.dataTables-1.9.4.min.js"></script>
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/datatables-jowin.js"></script>
		<script type="text/javascript" charset="utf-8">
			$(document).ready(function() {
				$('.datatable').dataTable({
					"sPaginationType": "bs_full",
					"aaSorting": [],
					"aLengthMenu": [[5, 10, 25, -1], [5, 10, 25, "${datatablesAllText}"]],
					"iDisplayLength": -1,
					"oLanguage": {
						"sUrl": "${pageContext.request.contextPath}/dataTables/dataTables_${pageContext.response.locale}.txt"
					}
				});
				$('.datatable').each(function() {
					var datatable = $(this);
					// SEARCH - Add the placeholder for Search and Turn this into in-line form control
					var search_input = datatable.closest('.dataTables_wrapper').find('div[id$=_filter] input');
					search_input.attr('placeholder', 'Search');
					search_input.addClass('form-control input-sm');
					// LENGTH - Inline-Form control
					var length_sel = datatable.closest('.dataTables_wrapper').find('div[id$=_length] select');
					length_sel.addClass('form-control input-sm');
				});

				$(function() {
					$('a[id="configure"]').parent().addClass('active');
				});

				$(function() {
					//required only if action icons are buttons
					$("a[data-toggle='tooltip']").tooltip({container: 'body'});
				});
			});
		</script>
		
		<jsp:invoke fragment="javascript"/>
	</jsp:attribute>

	<jsp:body>
		<div class="text-center">
			${title}
		</div>
        <jsp:doBody/>
    </jsp:body>
</t:mainPage>