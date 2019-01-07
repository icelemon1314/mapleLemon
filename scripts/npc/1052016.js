//官方版出租车
//射手村中巴

var status = 0;
var maps = Array(104000000, 100000000, 102000000, 101000000);
var cost = Array(800, 1000, 1000, 1200);
var costBeginner = Array(80, 100, 100, 120);
var selectedMap = -1;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (status >= 2 && mode == 0) {
			cm.sendOk("这个村落还有很多漂亮的景点，如果你想去其他地方，欢迎随时使用我们的出租车服务。");
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			cm.sendNext("您好~！我是废都中巴。你想不想又快捷又安全到其它地方去？那么请使用我们的出租车吧。它会马上将你送去你想去的地方，价格很便宜哦!");
		} else if (status == 1) {
			cm.sendNextPrev("为了照顾新手，我可以给他们90%的优惠哦！")
		} else if (status == 2) {
			var selStr = "请你选择目的地吧。按照目的地的不同，车费也有所不同。#b";
			if (cm.isBeginner()) {
				for (var i = 0; i < maps.length; i++) {
					selStr += "\r\n#L" + i + "##m" + maps[i] + "# (" + costBeginner[i] + " 金币)#l";
				}
			} else {
				for (var i = 0; i < maps.length; i++) {
					selStr += "\r\n#L" + i + "##m" + maps[i] + "# (" + cost[i] + " 金币)#l";
				}
			}
			cm.sendSimple(selStr);
		} else if (status == 3) {
			if (cm.isBeginner()) {
				if (cm.getMeso() < costBeginner[selection]) {
					cm.sendOk("请检查您的金币是否足够本次费用。")
					cm.dispose();
				} else {
					cm.sendYesNo("看来这里的事情你已经办完了嘛。你确定要去 #m" + maps[selection] + "#吗？");
					selectedMap = selection;
				}
			}
			else {
				if (cm.getMeso() < cost[selection]) {
					cm.sendOk("请检查您的金币是否足够本次费用。")
					cm.dispose();
				} else {
					cm.sendYesNo("看来这里的事情你已经办完了嘛。你确定要去 #m" + maps[selection] + "#吗?");
					selectedMap = selection;
				}
			}		
		} else if (status == 4) {
			if (cm.isBeginner()) {
				cm.gainMeso(-costBeginner[selectedMap]);
			}
			else {
				cm.gainMeso(-cost[selectedMap]);
			}
			cm.warp(maps[selectedMap], 0);
			cm.dispose();
		}
	}
}	
