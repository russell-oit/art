perl html2ps\html2ps -U -D -f html2ps\config-no-toc.txt -o features.ps features.htm
call ps2pdf features.ps
del features.ps