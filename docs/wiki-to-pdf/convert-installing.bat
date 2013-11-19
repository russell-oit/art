perl html2ps\html2ps -t -C hb -U -D  -f html2ps\config.txt -o Installing.ps Installing.htm
call ps2pdf Installing.ps
del Installing.ps