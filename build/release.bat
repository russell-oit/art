rem create release artifacts for an art version
rem example usage, release 2.5

rem copy nightly to release folder
robocopy art-nightly ..\Release\%1\art-%1 /mir

rem create zip package
7z a ..\Release\%1\art-%1.zip ..\Release\%1\art-%1\