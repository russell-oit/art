<%-- 
    Document   : addRobinHerbotsMask
    Created on : 16-Oct-2018, 21:01:55
    Author     : Timothy Anyona
--%>

<%@tag description="Adds a RobinHerbots mask to a given report parameter input" pageEncoding="UTF-8"%>
<%@tag trimDirectiveWhitespaces="true" %>

<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="reportParam" type="art.reportparameter.ReportParameter" required="true"%>

<%-- any content can be specified here e.g.: --%>
<script type="text/javascript">
	var paramOptionsString = '${encode:forJavaScript(reportParam.parameter.options)}';
	var paramOptions = JSON.parse(paramOptionsString);
	var robinHerbotsOptions = paramOptions.mask1;
	if (robinHerbotsOptions) {
		$('#${encode:forJavaScript(reportParam.htmlElementName)}').inputmask(robinHerbotsOptions);
	}
</script>