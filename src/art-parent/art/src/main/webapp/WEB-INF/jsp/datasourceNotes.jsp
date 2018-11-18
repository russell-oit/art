<%-- 
    Document   : datasourceNotes
    Created on : 30-Jan-2017, 12:52:53
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<ul>
	<li>
		If using <b>Oracle</b>, note that ART doesn't come with the Oracle JDBC driver.
		The driver (<b>ojdbc.jar</b>) needs to be manually
		downloaded and copied to the <b>WEB-INF\lib</b> directory.
		The application will then need to be restarted. The driver can be downloaded from 
		<a href="http://www.oracle.com/technetwork/database/features/jdbc/index-091264.html">
			http://www.oracle.com/technetwork/database/features/jdbc/index-091264.html
		</a>
	</li>
	<li>
		The <b>SQL Server (Microsoft driver)</b> included with ART 
		(Microsoft JDBC Driver 7) supports SQL Server 2008R2 and above.
		If you need to connect to SQL Server 2000 or 2005, use the jTDS driver.
		See <a href="https://msdn.microsoft.com/en-us/library/mt679183(v=sql.110).aspx">
			https://msdn.microsoft.com/en-us/library/mt679183(v=sql.110).aspx 
		</a> for a support matrix for the Microsoft driver.
	</li>
	<li>
		For <b>Firebird</b>, you must have the setting <b>WireCrypt = Enabled</b>
		in the <b>firebird.conf</b> file in the firebird installation location.
		See <a href="https://github.com/FirebirdSQL/jaybird/wiki/Jaybird-and-Firebird-3">
			https://github.com/FirebirdSQL/jaybird/wiki/Jaybird-and-Firebird-3
		</a>
	</li>
</ul>

