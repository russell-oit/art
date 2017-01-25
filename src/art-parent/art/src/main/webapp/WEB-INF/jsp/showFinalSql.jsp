<%-- 
    Document   : showFinalSql
    Created on : 08-Jan-2015, 12:46:13
    Author     : Timothy Anyona

Display the final sql used to generate a report
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/highlight-9.9.0/styles/magula.css">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/highlight-9.9.0/highlight.pack.js"></script>

<pre><code class="sql">${encode:forHtmlContent(finalSql)}</code></pre>

<script>
	$("pre code").each(function (i, e) {
		hljs.highlightBlock(e);
	});
</script>

