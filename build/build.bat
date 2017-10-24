rem create a build for art from the current source
rem usage, build

rem clear archive directory
rd /S /Q build-hg
md build-hg

rem clear package directory
rd /S /Q art-nightly
md art-nightly

rem create archive from hg repo
hg archive --rev stable --repository ..\hg -t files -- build-hg

rem copy changelog
copy /Y build-hg\src\art-parent\art\changelog.txt art-nightly

rem copy readme
copy /Y build-hg\build\readme.txt art-nightly

rem copy ART license
copy /Y build-hg\src\art-parent\art\LICENSE.txt art-nightly

rem copy license related files for third-party libraries
robocopy build-hg\build\third-party art-nightly\third-party /mir

rem copy database scripts
robocopy build-hg\database art-nightly\database /mir

rem copy docs
robocopy ..\Manuals\automated\ art-nightly\docs *.pdf
robocopy ..\Manuals\automated\ build-hg\src\art-parent\art\src\main\webapp\docs *.pdf

rem copy source files (excluding any target directories)
robocopy build-hg\src\art-parent art-nightly\src\art-parent /mir /xd target

rem compile, build and copy war
call mvn -o -f build-hg\src\art-parent\pom.xml clean package
copy /Y build-hg\src\art-parent\art\target\art.war art-nightly

