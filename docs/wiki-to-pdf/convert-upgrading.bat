perl html2ps\html2ps -t -C hb -U -D -f html2ps\config-toc.txt -o Upgrading.ps Upgrading.html
call ps2pdf Upgrading.ps
del Upgrading.ps