<%-- 
    Document   : datasourceNotes
    Created on : 30-Jan-2017, 12:52:53
    Author     : Timothy Anyona
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true" %>

<ul>
	<li>
		Note that some JDBC drivers are not included with ART.
		Where a driver is not included, it needs to be manually
		downloaded and copied to the <b>WEB-INF\lib</b> directory.
		The application will then need to be restarted.
	</li>
	<li>
		The <b>SQL Server (Microsoft driver)</b> included with ART 
		(Microsoft JDBC Driver 7.4) supports SQL Server 2012 and above.
		If you need to connect to SQL Server 2000, 2005 or 2008, use the jTDS driver.
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

