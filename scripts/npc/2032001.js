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
/* Spiruna
Orbis : Old Man's House (200050001)

Refining NPC:
 * Dark Crystal - Half Price compared to Vogen, but must complete quest
 */

var status = 0;

function start() {
    //if (cm.isQuestCompleted(3034))
        cm.sendYesNo("你以前帮助了我很多... 如果你有足够的#r黑暗水晶母矿#k，我可以帮你提炼母矿，手续费5折优惠，只要500000金币/个！");
    //else {
    //    cm.sendOk("走开，不要打扰我沉思！");
    //   cm.dispose();
    //}
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.dispose();
        return;
    }
    status++;
    if (status == 1)
        cm.sendGetNumber("好吧, 你想要做几个呢？", 1, 1, 100);
    else if (status == 2) {
        var complete = true;
        if (cm.getMeso() < 500000 * selection){
            cm.sendOk("貌似你的金币不够，虽然你和我关系好，但也不能免费的哈！");
            cm.dispose();
            return;
        } else if (!cm.haveItem(4004004, 10 * selection))
            complete = false;
        if (!complete)
            cm.sendOk("看上去你没有足够的母矿，10个母矿才能提炼一个成品！");
        else {
            cm.gainItem(4004004, -10 * selection);
            cm.gainMeso(-500000 * selection);
            cm.gainItem(4005004, selection);
            cm.sendOk("看来我的手艺还是挺不错的！");
        }
        cm.dispose();
    }
}