rem remove table of contents from html files
copy /Y *.html docs
java -jar art-wiki-to-html\artwikitohtml-1.0.2-SNAPSHOT.jar removetoc