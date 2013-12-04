perl html2ps\html2ps -t -C hb -U -D -b file:///c:/toa/projects/art/manuals/automated/images/ -f html2ps\config-toc.txt -o manual.ps manual.htm
call ps2pdf manual.ps
del manual.ps