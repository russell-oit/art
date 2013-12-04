perl html2ps\html2ps -t -C hb -U -D  -f html2ps\config-toc.txt -o installing.ps installing.htm
call ps2pdf installing.ps
del installing.ps