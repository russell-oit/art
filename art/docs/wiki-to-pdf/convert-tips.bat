perl html2ps\html2ps -t -C hb -U -D  -f html2ps\config.txt -o Tips.ps tips.htm
call ps2pdf Tips.ps
del Tips.ps