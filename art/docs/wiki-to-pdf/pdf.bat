@echo off

rem get latest wiki contents
java -jar "C:\toa\Projects\ART\Manuals\automated\html-to-pdf\html-to-pdf.jar" %1

rem convert wiki html to pdf
rem wait a few seconds
timeout 5

call convert-all