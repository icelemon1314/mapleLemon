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
/**
-- Odin JavaScript --------------------------------------------------------------------------------
	VIP Cab - Victoria Road : Lith Harbor (104000000)
-- By ---------------------------------------------------------------------------------------------
	Xterminator
-- Version Info -----------------------------------------------------------------------------------
	1.0 - First Version by Xterminator
---------------------------------------------------------------------------------------------------
**/

var status = 0;
var cost = 1000;

function start() {
    cm.sendNext("Hi，你好。我是专门为VIP客户服务的专车。我们不像普通的出租车一样送你们穿梭在各个城市之间，我们可以送你去很偏远又美丽的地方。过去#b蚂蚁广场#k需要花费10000金币。虽然有点小贵，但总体来说还是很超值。");
}

function action(mode, type, selection) {
	if (mode == -1) {
        cm.dispose();
    } else {
		if (status == 1 && mode == 0) {
            cm.sendOk("这个小镇还有很多有趣的地方，如果你想去蚂蚁广场就来找我吧！");
            cm.dispose();
            return;
        }
		if (mode == 1)
            status++;
        else
            status--;
		if (status == 1) {
			cm.sendYesNo(cm.getJobId() == 0 ? "我们给新手打1折。蚂蚁广场位于林中之城的深处。那里有24小时便利店。你想花费#b1,000 金币#k去那里么？" : "蚂蚁广场位于林中之城的深处。那里有24小时便利店。你想花费 #b10,000 金币#k去到哪里么?");
			cost *= cm.getJobId() == 0 ? 10 : 1;
		} else if (status == 2) {
			if (cm.getMeso() < cost)
				cm.sendNext("看上去你没有这么多金币。所以我不能送你过去。")
			else {
				cm.gainMeso(-cost);
				cm.warp(105070001);
			}
			cm.dispose();
		}
	}
}