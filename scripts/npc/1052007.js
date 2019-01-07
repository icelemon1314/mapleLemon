var itemid = new Array(4031036,4031037,4031038);
var mapid = new Array(103000900,103000903,103000906);
var menu;
var status=0;
var sw;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
		cm.dispose();
    } else {
		if (mode == 0 && status == 1) {
			cm.sendNext("你在这里还有事的么？");
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else 
			status--;
		if (status == 1) {
			menu = "这里是检票口，你想要被送到哪里？\r\n";
			for (i=0; i < itemid.length; i++) {
				menu += "#L"+i+"##b#m"+mapid[i]+"##k#l\r\n";
			}
			
			cm.sendSimple(menu);
		} if (status == 2) {
			if(cm.haveItem(itemid[selection],1)) {
				cm.gainItem(itemid[selection],-1);
				cm.warp(mapid[selection]);
			} else {
				cm.sendOk("你没有购买车票！");
			}
			cm.dispose();	
		}
	}
}