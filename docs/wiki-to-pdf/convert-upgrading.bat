perl html2ps\html2ps -U -D -f html2ps\config-no-toc.txt -o Upgrading.ps Upgrading.html
call ps2pdf Upgrading.ps
del Upgrading.ps