rem add table of contents for html files
copy /Y *.htm docs
java -jar html-to-pdf\html-to-pdf.jar addtoc