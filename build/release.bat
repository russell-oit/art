rem create release folder
rd /S /Q ..\Release\%1
md ..\Release\%1

rem copy nightly to release folder
md ..\Release\%1\art-%1
xcopy /Y /E art-nightly ..\Release\%1\art-%1

rem create zip package
7za a ..\Release\%1\art-%1.zip ..\Release\%1\art-%1\