@echo off
@title 转存怪物技能
COLOR 8F

set CLASSPATH=.;..\dist\*;..\libs\*
java -Xmx512m -Dwzpath=..\ -Dpath=..\ tools.wztosql.DumpMobSkills
pause
