rem clear archive directory
rd /S /Q C:\toa\Projects\ART\Nightly\hg
md C:\toa\Projects\ART\Nightly\hg

rem clear package directory
rd /S /Q C:\toa\Projects\ART\Nightly\art-nightly
md C:\toa\Projects\ART\Nightly\art-nightly

rem create archive from hg repo
hg archive --repository C:\toa\Projects\ART\hg -t files -- C:\toa\Projects\ART\Nightly\hg

rem copy maven source files (before generation of target files)
md C:\toa\Projects\ART\Nightly\art-nightly\maven\art-parent
xcopy /Y /E C:\toa\Projects\ART\Nightly\hg\src\art-parent C:\toa\Projects\ART\Nightly\art-nightly\maven\art-parent

rem copy changelog
copy /Y C:\toa\Projects\ART\Nightly\hg\src\changelog.txt C:\toa\Projects\ART\Nightly\art-nightly

rem copy readme
copy /Y C:\toa\Projects\ART\Nightly\hg\build\readme.txt C:\toa\Projects\ART\Nightly\art-nightly

rem copy docs
md C:\toa\Projects\ART\Nightly\art-nightly\docs
copy /Y C:\toa\Projects\ART\Manuals\automated\*.pdf C:\toa\Projects\ART\Nightly\art-nightly\docs

rem copy database scripts
md C:\toa\Projects\ART\Nightly\art-nightly\database
xcopy /Y /E C:\toa\Projects\ART\Nightly\hg\database C:\toa\Projects\ART\Nightly\art-nightly\database

rem compile, build and copy war
cd C:\toa\Projects\ART\Nightly\hg\src\art-parent
call mvn -o clean package
copy C:\toa\Projects\ART\Nightly\hg\src\art-parent\art\target\art.war C:\toa\Projects\ART\Nightly\art-nightly


cd C:\toa\Projects\ART\Nightly
