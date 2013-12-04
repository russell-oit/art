perl html2ps\html2ps -U -D -f html2ps\config-no-toc.txt -o upgrading.ps upgrading.htm
call ps2pdf upgrading.ps
del upgrading.ps