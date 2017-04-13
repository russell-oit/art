<%-- 
    Document   : datasourceNotes
    Created on : 30-Jan-2017, 12:52:53
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<ul>
	<li>
		If using <b>Oracle</b>, please note that ART doesn't come with the Oracle JDBC driver.
		The driver (<b>ojdbcX.jar</b>) needs to be manually
		downloaded and copied to the <b>WEB-INF\lib</b> directory.
		The application will then need to be restarted. The driver can be downloaded from 
		<a href="http://www.oracle.com/technetwork/database/features/jdbc/index-091264.html">
			<b>http://www.oracle.com/technetwork/database/features/jdbc/index-091264.html</b>
		</a>
	</li>
	<li>
		The <b>SQL Server (Microsoft Driver)</b> included with ART 
		(Microsoft JDBC Driver 6) supports SQL Server 2005 and above.
		If you need to connect to SQL Server 2000, use the jTDS driver.
		See <a href="https://msdn.microsoft.com/en-us/library/mt679183(v=sql.110).aspx">
			<b>https://msdn.microsoft.com/en-us/library/mt679183(v=sql.110).aspx</b> 
		</a> for a support matrix for the Microsoft driver.
	</li>
</ul>

