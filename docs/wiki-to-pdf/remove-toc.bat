rem remove table of contents from html files
copy /Y *.html docs
java -jar html-to-pdf\html-to-pdf.jar removetoc