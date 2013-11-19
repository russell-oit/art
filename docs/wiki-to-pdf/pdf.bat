@echo off

rem get latest wiki contents
java -jar html-to-pdf\html-to-pdf.jar %1

rem wait a few seconds
timeout 5

rem convert wiki html to pdf
call convert-all

rem add table of contents for html files
call add-toc

rem copy pdfs to docs folder
copy /Y *.pdf docs