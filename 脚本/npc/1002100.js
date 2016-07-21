/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
// Jane the Alchemist
var status = -1;
var amount = -1;
var items = [[2000002,310],[2022003,1060],[2022000,1600],[2001000,3120]];
var item;

function start() {
    if (cm.isQuestCompleted(2013))
        cm.sendNext("是你呀，多谢你上次帮我。我现在正在做很多东西。如果你需要什么就和我说吧！");
    else {
        if (cm.isQuestCompleted(2010))
            cm.sendNext("你似乎还不够强大，所以还不能购买我的药水......");
        else
            cm.sendOk("我的梦想是环游世界，就像你一样。但是我的父亲并不允许我这么做，因为他认为外面太危险。如果我能证明我不是一个弱小的女孩，或者他会答应我的。");
        cm.dispose();
    }
}

function action(mode, type, selection) {
    status++;
    if (mode != 1){
        if(mode == 0 && type == 1)
            cm.sendNext("我还有很多上次你给我的材料。所有的道具都在这里了，你可以慢慢挑选。");
        cm.dispose();
        return;
    }
    if (status == 0){
        var selStr = "你想买哪个道具？#b";
        for (var i = 0; i < items.length; i++)
            selStr += "\r\n#L" + i + "##i" + items[i][0] + "# (价格 : " + items[i][1] + " 金币)#l";
        cm.sendSimple(selStr);
    } else if (status == 1) {
        item = items[selection];
        var recHpMp = ["300 HP.","1000 HP.","800 MP","1000 HP 和 MP."];
        cm.sendGetNumber("你想要购买 #b#t" + item[0] + "##k? #t" + item[0] + "# 帮助你恢复 " + recHpMp[selection] + " 你想买几个？", 1, 1, 100);
    } else if (status == 2) {
        cm.sendYesNo("Will you purchase #r" + selection + "#k #b#t" + item[0] + "#(s)#k? #t" + item[0] + "# costs " + item[1] + " mesos for one, so the total comes out to be #r" + (item[1] * selection) + "#k mesos.");
        amount = selection;
    } else if (status == 3) {
        if (cm.getMeso() < item[1] * amount)
            cm.sendNext("看上去你的金币不够。或者你的背包满了。你最少需要 #r" + (item[1] * selectedItem) + "#k 金币！");
        else {
            if (cm.canHold(item[0])) {
                cm.gainMeso(-item[1] * amount);
                cm.gainItem(item[0], amount);
                cm.sendNext("感谢你的光临。欢迎下次光临！");
            } else
                cm.sendNext("请确认你的背包有足够的位置！");
        }
        cm.dispose();
    }
}