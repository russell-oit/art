<%-- 
    Document   : runReportPageFooter
    Created on : 30-May-2014, 16:16:56
    Author     : Timothy Anyona

Html page footer fragment when displaying report output in a new page
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:if test="${allowSelectParameters}">
</div>
</div>
</div>
</c:if>

</div>
</div>
<div id="push"></div>
</div>

<jsp:include page="/WEB-INF/jsp/footer.jsp"/>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap-dropdown-hover-4.2.0/jquery.bootstrap-dropdown-hover.min.js"></script>
<script>
	$(function () {
		$('[data-hover="dropdown"]').bootstrapDropdownHover({
			hideTimeout: 100
		});
	});
</script>

</body>
</html>
