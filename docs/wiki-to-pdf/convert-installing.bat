perl html2ps\html2ps -t -C hb -U -D  -f html2ps\config-toc.txt -o Installing.ps Installing.html
call ps2pdf Installing.ps
del Installing.ps