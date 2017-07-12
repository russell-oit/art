rem add table of contents for html files
copy /Y *.html docs
java -jar art-wiki-to-html\artwikitohtml-1.0.2-SNAPSHOT.jar addtoc