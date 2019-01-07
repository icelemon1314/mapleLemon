/* Crumbling Statue
	1061007
*/

var status = 0;
var zones = 0;
var selectedMap = -1;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (status >= 1 && mode == 0) {
        cm.dispose();
        return;
    }
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {
        cm.sendNext("摇摇欲坠的雕像令你伤心:(");
    } else if (status == 1) {
        cm.sendYesNo("你想离开这个伤心的地方么？");
    } else if (status == 2) {
        cm.warp(105000000);
        cm.dispose();
    }
}