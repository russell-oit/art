A R T                          http://art.sourceforge.net
R eporting
T ool

* WHAT IS ART?
 ART is a lightweight, multiplatform web based query tool 
 and reporting environment. Scalable and easy to use, SQL 
 query results can be published in a few minutes.
 Supports tabular, crosstab/pivot, charts, scheduling,  
 alerts, Jasper Reports, Mondrian OLAP, LDAP, SSO, 
 data exportable to spreadsheet & pdf.
 100% Java, works on Apache Tomcat and other J2EE engines
 (Min. req. Servlet2.5/Jsp2.1 / Java 1.6)
 
 The name ART stands for A Reporting Tool.

* INSTALL
	Check out the Installing manual in the docs folder
	
  1. ART embeds a ready-to-use hsqldb database and it can
      be used out of the box as the ART Repository without 
      any additional database setup.
     OR      
      (optional) Create some tables in a database by
      executing (1) the art_tables.sql script and (2) the
      proper quartz sql script for your database
      (check in database/quartz folder)
     
  2. Deploy the art.war archive in a servlet engine, go
     to  http://server_name:port/art and  follow on screen
     instructions (make  sure the jdbc drivers are in the
     classpath) to connect to your ART Repository

  To know  more, read the admin manual in the docs folder  
  or from the ART wiki:  
  http://sourceforge.net/p/art/wiki/
  
* UPGRADE
	Check out the Upgrading manual in the docs folder
	
  0. Backup your ART Repository
  1. ART Repository Upgrade  
     . Run the scripts in the database/upgrade folder, in the order of versions                
  2. ART Web Application:
     deploy  the  new  art.war archive, log in  and set ART
     properties as they were before.
     As usual ART is backward compatible.
         
     Check changelog file for changes that might impact
     existing objects.
    
* Compile
	Check out the Admin manual in the docs folder
	
  Source code is in the art.war file. Unzip art.war read
  the build.xml file and run ant
    
  
      ... check for updates on http://art.sourceforge.net,
          use the Discussion forum to ask questions and enjoy ART!

