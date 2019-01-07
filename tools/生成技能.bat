@echo off
@title ×ª´æÎïÆ·
COLOR 8F

set CLASSPATH=.;..\dist\*;..\libs\*
java -Xmx512m -Dwzpath=..\ -Dpath=..\ tools.wztosql.DumpSkillInfo
pause
