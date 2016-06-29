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
	VIP Cab - Victoria Road : Ellinia (101000000)
-- By ---------------------------------------------------------------------------------------------
	Xterminator
-- Version Info -----------------------------------------------------------------------------------
    1.0 - First Version by Xterminator
    2.0 - Second Version by Moogra
    2.1 - Clean up By Moogra
---------------------------------------------------------------------------------------------------
**/

var status = 0;
var cost = 10000;

function start() {
    cm.sendNext("你好，我提供VIP专属服务。我不像其它出租车一样，我可以提供给你更好的服务，带你去更遥远的地方，当然，费用也是不菲的，如果你有10000金币，我会安全的把你送到：\r\n\r\n#b蚂蚁广场#k");
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
			var string="";
			if (cm.getJobId() == 0) {
				string="我们给新手打九折";
				cost = 1000;
			} else {
				string="看上去你很强壮了";
			}
            string += "，蚂蚁广场坐落在丛林的深处，那里有24小时营业的外卖车，你确定要去那里么？";
			cm.sendYesNo(string);
        } else if (status == 2) {
            if (cm.getMeso() < cost)
                cm.sendNext("看上去你没有足够的金币，所以我不能给你提供服务！");
            else {
                cm.gainMeso(-cost);
                cm.warp(105070001, 0);
            }
            cm.dispose();
        }
    }
}