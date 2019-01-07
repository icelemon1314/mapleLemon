@echo off
@title MapleStory Server  Mode£ºDebug  ver:027
cls
set CLASSPATH=.;dist\*;libs\*;
java -server gui.ServerUI -Xmx500m -Xms500m 
pause