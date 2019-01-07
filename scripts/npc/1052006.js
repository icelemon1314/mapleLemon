/**
	Jake - Victoria Road : Subway Ticketing Booth (103000100)
**/

var meso = new Array(500, 1200, 2000);
var item = new Array(4031036, 4031037, 4031038);
var selector;
var menu = "";

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (status == 0 && mode == 0) {
        cm.dispose();
        return;
    } else if (status == 1 && mode == 0) {
        cm.sendNext("只要有票，你随时都能进去。虽然里面有危险的装置，但也有珍贵的物品。以后你想进去的时候再来吧。");
        cm.dispose();
        return;
    }
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {
        if (cm.getPlayerStat("LVL") <= 19) {
            cm.sendNext("你买了票才能进去，但有时候有票也不能进去。你还太小了，坐地铁会很危险的，赶紧去升级到19级再过来吧！");
            cm.dispose();
        } else {
            for (var x = 1; x <= 3; x++) {
                if (cm.getPlayerStat("LVL") >= 20 && cm.getPlayerStat("LVL") <= 29) {
                    menu += "\r\n#L" + x + "##b工地B" + x + "#k#l";
                    break;
                } else if (cm.getPlayerStat("LVL") >= 30 && cm.getPlayerStat("LVL") <= 39 && x < 2) {
                    menu += "\r\n#L" + x + "##b工地B" + x + "#k#l";
                } else {
                    menu += "\r\n#L" + x + "##b工地B" + x + "#k#l";
                }
            }
            cm.sendSimple("你想进去就要买票。买票后你通过右边的#p1052007#可以进去。买什么票？" + menu);
        }
    } else if (status == 1) {
        selector = selection;
		selector -= 1;
        cm.sendYesNo("你要买#b工地B" + selection + "的票#k吗? 票价是" + meso[selector] + "金币。购买前你先确认背包的其它窗有没有空间。");
    } else if (status == 2) {
        if (cm.getMeso() < meso[selector]) {
            cm.sendNext("钱不够？或者背包空间不够？赶紧检查下，列车即将出发了！");
            cm.dispose();
        } else {
            if (selector == 0) {
                cm.sendNext("把票投入那边儿的#p1052007#就行。我听说在第一地区里可以得到珍贵的物品，但是里面陷阱太多，大多数人都放弃了。你千万小心。");
            } else if (selector == 1) {
                cm.sendNext("把票投入那边儿的#p1052007#就行。我听说在第二地区里可以得到珍贵的物品，但是里面陷阱太多，大多数人都放弃了。你千万小心。");
            } else {
                cm.sendNext("把票投入那边儿的#p1052007#就行。我听说在第三地区里可以得到珍贵的物品，但是里面陷阱太多，大多数人都放弃了。你千万小心。");
            }
            cm.gainMeso( - meso[selector]);
            cm.gainItem(item[selector], 1);
            cm.dispose();
        }
    }
}