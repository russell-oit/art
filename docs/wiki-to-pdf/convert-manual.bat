perl html2ps\html2ps -t -C hb -U -D -b file:///c:/toa/projects/art/manuals/automated/images/ -f html2ps\config-toc.txt -o Manual.ps Manual.htm
call ps2pdf Manual.ps
del Manual.ps