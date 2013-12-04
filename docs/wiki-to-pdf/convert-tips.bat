perl html2ps\html2ps -t -C hb -U -D  -f html2ps\config-toc.txt -o tips.ps tips.htm
call ps2pdf tips.ps
del tips.ps