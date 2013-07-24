rem clear archive directory
rd /S /Q build-hg
md build-hg

rem clear package directory
rd /S /Q art-nightly
md art-nightly

rem create archive from hg repo
hg archive --repository ..\hg -t files -- build-hg

rem copy maven source files (before generation of target files)
md art-nightly\src\art-parent
xcopy /Y /E build-hg\src\art-parent art-nightly\src\art-parent

rem copy changelog
copy /Y build-hg\src\changelog.txt art-nightly

rem copy readme
copy /Y build-hg\build\readme.txt art-nightly

rem copy license
copy /Y build-hg\build\LICENSE.txt art-nightly

rem copy database scripts
md art-nightly\database
xcopy /Y /E build-hg\database art-nightly\database

rem copy docs
md art-nightly\docs
copy /Y ..\Manuals\automated\*.pdf art-nightly\docs

rem compile, build and copy war
call mvn -o -f build-hg\src\art-parent\pom.xml clean package
copy build-hg\src\art-parent\art\target\art.war art-nightly

