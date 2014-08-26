A R T                          http://art.sourceforge.net
R eporting
T ool

* WHAT IS ART?
 ART is a lightweight, multi-platform, web based query tool 
 and reporting environment. Scalable and easy to use, SQL 
 query results can be published in a few minutes.
 Supports tabular, crosstab/pivot, charts, scheduling,  
 alerts, JasperReports, Mondrian OLAP, LDAP, SSO, 
 data exportable to spreadsheet & pdf.
 
 100% Java, works on Apache Tomcat and other Java EE engines
 (Minimum requirements: Servlet 3.0, Java 1.7)
 
 The name ART stands for A Reporting Tool.

* INSTALL
	See the Installing manual in the docs folder
	
  1. ART embeds a default/sample hsqldb database which can
      be used as the ART Repository without 
      any additional database setup.
     OR      
      (optional) Create some tables in a database by
      executing (1) the art_tables.sql script and (2) the
      proper quartz sql script for your database
      (check in the database/quartz folder)
     
  2. Deploy the art.war archive in a java application server, go
     to  http://server_name:port/art and follow on screen
     instructions to connect to your ART Database      
  
* UPGRADE
	See the Upgrading manual in the docs folder
	
  1. Backup your ART Repository
  2. ART Repository Upgrade  
     . Run the scripts in the database/upgrade folder, in the order of versions                
  3. ART Web Application:
     Deploy the new art.war archive, log in and save ART
     settings as they were before.
	               
     Check the changelog file for changes that might impact existing objects.     
    
* COMPILE
	See the Admin manual in the docs folder
	
  Source code is contained in the art.war file. Unzip art.war, read
  the build.xml file and run ant.
  
  Mavenized source code is in the src folder.
    
  
      ... check for updates on http://art.sourceforge.net,
          use the Discussion forum to ask questions, and enjoy ART!

