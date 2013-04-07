rem clear archive directory
rd /S /Q C:\toa\Projects\ART\Nightly\hg
md C:\toa\Projects\ART\Nightly\hg

rem create archive from hg repo
hg archive --repository C:\toa\Projects\ART\hg -t files -- C:\toa\Projects\ART\Nightly\hg

rem compile and build war
cd C:\toa\Projects\ART\Nightly\hg\art\src\art
call ant clean
call ant compile
call ant full
move C:\toa\Projects\ART\Nightly\hg\art\src\art\art.war C:\toa\Projects\ART\Nightly\art-nightly

rem update changelog
copy /Y C:\toa\Projects\ART\Nightly\hg\art\src\changelog.txt C:\toa\Projects\ART\Nightly\art-nightly

rem copy release package readme
copy /Y C:\toa\Projects\ART\Nightly\hg\art\build\art-nightly\readme.txt C:\toa\Projects\ART\Nightly\art-nightly

rem update docs
mkdir C:\toa\Projects\ART\Nightly\art-nightly\docs
copy /Y C:\toa\Projects\ART\Nightly\hg\art\docs\*.pdf C:\toa\Projects\ART\Nightly\art-nightly\docs

rem update database scripts
mkdir C:\toa\Projects\ART\Nightly\art-nightly\database
xcopy /Y /E C:\toa\Projects\ART\Nightly\hg\art\database C:\toa\Projects\ART\Nightly\art-nightly\database
