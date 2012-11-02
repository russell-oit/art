perl html2ps\html2ps -t -C hb -U -D -b file:///c:/toa/projects/art/manuals/automated/images/ -f html2ps\config.txt -o AdminManual.ps manual.htm
call ps2pdf AdminManual.ps
del AdminManual.ps