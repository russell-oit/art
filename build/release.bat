rem create release folder
md C:\toa\Projects\ART\Release\%1

rem copy nightly to release folder
md C:\toa\Projects\ART\Release\%1\art-%1
xcopy /Y /E C:\toa\Projects\ART\Nightly\art-nightly C:\toa\Projects\ART\Release\%1\art-%1

rem create zip package
7za a C:\toa\Projects\ART\Release\%1\art-%1.zip C:\toa\Projects\ART\Release\%1\art-%1\