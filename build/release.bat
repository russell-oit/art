@echo off
rem create release artifacts for an art version
rem example usage, release 2.5

if [%1]==[] goto usage

rem copy nightly to release folder
robocopy art-nightly ..\Releases\%1\art-%1 /mir

rem create zip package
7z a ..\Releases\%1\art-%1.zip ..\Releases\%1\art-%1\

rem copy docs
robocopy ..\Manuals\automated\ ..\Manuals\%1 *.pdf
robocopy ..\Manuals\automated\ ..\Manuals\%1 *.html

goto :eof

:usage
@echo Usage: %0 art-version e.g. %0 2.5.2
exit /B 1