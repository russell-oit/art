The files in this directory enable running of the ART demo database
in server mode as opposed to the default standalone(in-process) mode.

Running in standalone mode means that once one process is connected
to the database, no other process can access it.
Running in server mode allows more than one process to access the database,
allowing you to query the database using other tools e.g. Squirrel SQL client
while it's also in use by ART.

To run the databases in server mode, run start-databases.bat
(or ./start-databases.sh on linux)
To stop the databases, run stop-databases.bat (./stop-databases.sh on linux)

When you run ART, change the jdbc url for the ArtRepository datasource
to jdbc:hsqldb:hsql://localhost/artrepository
and for SampleDB to jdbc:hsqldb:hsql://localhost/sampledb
and for OrgChartDB to jdbc:hsqldb:hsql://localhost/orgchartdb


Description of files
---------------------
readme.txt			this file. contains a description of files in this directory
sqltool.jar			utility that allows running of sql statements on hsqldb database
start-databases.bat	start databases for ART demo in server mode
stop-database.bat	stop databases used for ART demo	