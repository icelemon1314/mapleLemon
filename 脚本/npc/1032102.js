var status = 0;
var pet = null;
var theitems = Array();

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0) {
            cm.sendOk("好的，下次再见。");
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            cm.sendSimple("初次见面～我是在#m101000000#研究多种魔法的#p1032102#，我特别感兴趣的是有关生命的魔法。生命多么神秘呀...我正在研究怎么创造那样生命。#b\r\n#L0#我要把我的宠物复活#l#k");
        } else if (status == 1) {
            if (selection == 0) { //复活宠物	
                var inv = cm.getInventory(5);
                var pets = cm.getPlayer().getPets(); //includes non-summon
                for (var i = 0; i <= inv.getSlotLimit(); i++) {
                    var it = inv.getItem(i);
                    if (it != null && it.getItemId() >= 5000000 && it.getItemId() < 5010000 && it.getExpiration() > 0 && it.getExpiration() < cm.getCurrentTime()) {
                        theitems.push(it);
                    }
                }
                if (theitems.length <= 0) {
                    cm.sendOk("没有可需要复活的宠物.");
                    cm.dispose();
                } else {
                    var selStr = "请选择需要复活的宠物，注意：必须要有#b#i4070000# #t4070000##k我才能帮您复活宠物。#b\r\n";
                    for (var i = 0; i < theitems.length; i++) {
                        selStr += "\r\n#L" + i + "##i" + theitems[i].getItemId() + "##t" + theitems[i].getItemId() + "##l";
                    }
                    cm.sendSimple(selStr);
                }
            }
        } else if (status == 2) {
            if (theitems.length <= 0) {
                cm.sendOk("没有可需要复活的宠物.");
            } else if (!cm.haveItem(4070000, 1)) {
                cm.sendOk("您好像还没有 #b#i4070000# #t4070000##k 吧.");
            } else {
				// 1463241600907
				cm.gainItem(4070000, -1);
                theitems[selection].setExpiration(cm.getCurrentTime() + (45 * 24 * 60 * 60 * 1000));
                cm.getPlayer().refreshItem(theitems[selection]);
                cm.sendOk("恭喜您复活宠物成功，宠物使用时间延长45天。");
            }
            cm.dispose();
        }
    }
}