java -cp ../../lib/hsqldb-2.3.4.jar;./sqltool.jar org.hsqldb.cmdline.SqlTool --driver=org.hsqldb.jdbc.JDBCDriver --inlineRC=URL=jdbc:hsqldb:hsql://localhost/SampleDB,User=SAMPLE --sql "shutdown;"
java -cp ../../lib/hsqldb-2.3.4.jar;./sqltool.jar org.hsqldb.cmdline.SqlTool --driver=org.hsqldb.jdbc.JDBCDriver --inlineRC=URL=jdbc:hsqldb:hsql://localhost/ArtRepository,User=ART --sql "shutdown;"
