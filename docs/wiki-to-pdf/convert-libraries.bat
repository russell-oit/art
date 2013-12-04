perl html2ps\html2ps -U -D -f html2ps\config-no-toc.txt -o libraries.ps libraries.htm
call ps2pdf libraries.ps
del libraries.ps