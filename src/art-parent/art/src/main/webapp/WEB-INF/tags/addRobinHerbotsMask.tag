<%-- 
    Document   : addRobinHerbotsMask
    Created on : 16-Oct-2018, 21:01:55
    Author     : Timothy Anyona
--%>

<%@tag description="Adds a RobinHerbots mask to a given report parameter input" pageEncoding="UTF-8"%>
<%@tag trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="reportParam" type="art.reportparameter.ReportParameter" required="true"%>

<%-- any content can be specified here e.g.: --%>
<c:if test="${reportParam.parameter.hasRobinHerbotsMask()}">
	<script type="text/javascript">
		var paramOptionsString = '${encode:forJavaScript(reportParam.parameter.options)}';
		var paramOptions = JSON.parse(paramOptionsString);
		var robinHerbotsOptions = paramOptions.mask1;
		if (robinHerbotsOptions) {
			//https://stackoverflow.com/questions/858181/how-to-check-a-not-defined-variable-in-javascript
			//var mask1Options will be defined in external js/template file
			if (typeof mask1Options !== 'undefined') {
				$.extend(robinHerbotsOptions, mask1Options);
			}
			$('#${encode:forJavaScript(reportParam.htmlElementName)}').inputmask(robinHerbotsOptions);
		}
	</script>
</c:if>