@echo off
@title MapleStory Server  Mode：Debug  ver:027
@echo 正在删除历史封包记录
cls
set CLASSPATH=.;dist\*;libs\*;
java -server server.Start -Xmx500m -Xms500m 
pause