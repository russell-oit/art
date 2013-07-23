rem clear archive directory
rd /S /Q hg
md hg

rem clear package directory
rd /S /Q art-nightly
md art-nightly

rem create archive from hg repo
hg archive --repository ..\hg -t files -- hg

rem copy maven source files (before generation of target files)
md art-nightly\src\art-parent
xcopy /Y /E hg\src\art-parent art-nightly\src\art-parent

rem copy changelog
copy /Y hg\src\changelog.txt art-nightly

rem copy readme
copy /Y hg\build\readme.txt art-nightly

rem copy docs
md art-nightly\docs
copy /Y ..\Manuals\automated\*.pdf art-nightly\docs

rem copy database scripts
md art-nightly\database
xcopy /Y /E hg\database art-nightly\database

rem compile, build and copy war
call mvn -o -f hg\src\art-parent\pom.xml clean package
copy hg\src\art-parent\art\target\art.war art-nightly

