perl html2ps\html2ps -t -C hb -U -D  -f html2ps\config-toc.txt -o Tips.ps Tips.html
call ps2pdf Tips.ps
del Tips.ps