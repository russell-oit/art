<%-- 
    Document   : test2
    Created on : 17-Mar-2016, 12:58:35
    Author     : Timothy Anyona
--%>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/prototype.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/scriptaculous/scriptaculous.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/overlib.js"></script>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/ajaxtags.js"></script>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/ajaxtags-art.css" /> 

<div align="left">
	<table class="plain">
		<tr>
			<td>
				<div id="div0">
					<ajax:portlet
						source="portlet0"
						baseUrl="/art/test/test.jsp"
						classNamePrefix="portletAUTO"
						title="iko"
						imageMaximize="${pageContext.request.contextPath}/images/maximize.png"
						imageMinimize="${pageContext.request.contextPath}/images/minimize.png"
						imageRefresh="${pageContext.request.contextPath}/images/refresh.png"             
						executeOnLoad= "true"
						/>
				</div>
			</td>
		</tr>
	</table>
</div> 
