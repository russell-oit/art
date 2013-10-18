rem create release artifacts for an art version
rem example usage, release 2.5

rem copy nightly to release folder
robocopy art-nightly ..\Releases\%1\art-%1 /mir

rem create zip package
7z a ..\Releases\%1\art-%1.zip ..\Releases\%1\art-%1\