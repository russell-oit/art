<%-- 
    Document   : displayStackTrace
    Created on : 09-Nov-2014, 12:01:15
    Author     : Timothy Anyona
--%>

<%@tag description="Display error stacktrace" pageEncoding="UTF-8"%>
<%@tag trimDirectiveWhitespaces="true" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="encode" %>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="error" type="Throwable" required="true" %>

<%-- any content can be specified here e.g.: --%>
<pre>
	<%
		java.io.PrintWriter pOut = new java.io.PrintWriter(out);
		try {
			Throwable err = (Throwable) request.getAttribute("error");

			if (err != null) {
				if (err instanceof ServletException) {
					// It's a ServletException: we should extract the root cause
					ServletException se = (ServletException) err;
					Throwable rootCause = se.getRootCause();
					if (rootCause == null) {
						rootCause = se;
					}
					rootCause.printStackTrace(pOut);
				} else {
					// It's not a ServletException, so we'll just show it
					err.printStackTrace(pOut);
				}
			} else {
				out.println("No error information available");
			}
		} catch (Exception ex) {
			ex.printStackTrace(pOut);
		}
	%>
</pre>