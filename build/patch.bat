rem create a patch for an art version
rem example usage, patch 2.5.2 patch2 "login.jsp Art*.java"

rem create art patch
robocopy latest-art %1\latest-patch /s web.xml %~3

rem create zip package
7z a %1\art-%1-%2.zip .\%1\latest-patch\*

