@echo off
@title MapleStory Server  Mode£ºDebug  ver:027 dev By:icelemon1314
cls
set CLASSPATH=.;dist\*;libs\*;
java -server server.Start -Xmx500m -Xms500m 
pause