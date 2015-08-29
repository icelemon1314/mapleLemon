@echo off
@title ×ª´æÈÎÎñ
COLOR 8F

set CLASSPATH=.;..\dist\*;..\libs\*
java -Xmx512m -Dwzpath=..\ -Dpath=..\ tools.wztosql.DumpQuests
pause
