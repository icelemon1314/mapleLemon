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
	Hotel Receptionist - Sleepywood Hotel(105040400)
-- By ---------------------------------------------------------------------------------------------
	Unknown
-- Version Info -----------------------------------------------------------------------------------
        1.3 - More Cleanup by Moogra (12/17/09)
        1.2 - Cleanup and Statement fix by Moogra
	1.1 - Statement fix [Information]
	1.0 - First Version by Unknown
---------------------------------------------------------------------------------------------------
**/

var status = 0;
var regcost = 499;
var vipcost = 999;
var iwantreg = 0;
var iwantvip = 0;

function start() {
    cm.sendNext("欢迎光临。我门这里是森林宾馆。我们的宾馆给你提供了最好的服务，如果你打猎疲劳了，为什么不来我们旅游放松一下呢？");
}

function action(mode, type, selection) {
    if (mode == -1 || (mode == 0 && status == 1))
        cm.dispose();
    else {
        if (mode == 0 && status == 2) {
            cm.sendNext("我们也提供其它的各种服务，想好了就再来找我吧。");
            cm.dispose();
            return;
        }
        status++;
        if (status == 1) {
            cm.sendSimple("我们提供2中类型的房间。选一种你喜欢的吧：\r\n#b#L0#普通桑拿房 (" + regcost + " 金币)#l\r\n#L1#VIP桑拿房 (" + vipcost + " 金币)#l");
        } else if (status == 2) {
            if (selection == 0) {
				cm.sendYesNo("你选择了普通的桑拿房。你的HP和MP将会快速恢复，当然你也可以在里面购物，你确定你想进去了？");
				iwantreg = 1;
			} else if (selection == 1) {
                cm.sendYesNo("你选择了VIP桑拿房。你的HP和MP将会比普通的桑拿房恢复速度还要快，同时你可能还会发现一些特殊的东西，你确定要进去了？");
                iwantvip = 1;
            }
        } else if (status == 3) {
            if (iwantreg == 1) {
                if (cm.getMeso() >= regcost) {
                    cm.warp(105040401);
                    cm.gainMeso(-regcost);
                } else
                    cm.sendNext("对不起，看上去你的钱不够。普通桑拿房需要" + regcost + "金币。");
            } else if (iwantvip == 1) {
                if (cm.getMeso() >= vipcost) {
                    cm.warp(105040402);
                    cm.gainMeso(-vipcost);
                } else
                    cm.sendNext("对不起，看上去你的钱不够。VIP桑拿房需要" + vipcost + "金币。");
            }
            cm.dispose();
        }
    }
}