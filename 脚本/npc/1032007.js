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
	Joel  - Ellinia Station(101000300)
-- By ---------------------------------------------------------------------------------------------
	Information
-- Version Info -----------------------------------------------------------------------------------
	1.2 - Price as GMS [Sadiq]
	1.1 - Changed Price as GMS by Shogi
	1.0 - First Version by Information
---------------------------------------------------------------------------------------------------
**/

var cost = 5000;
var status = 0;

function start() {
	status = -1;
    cm.sendYesNo("你好, 我负责出售开往天空之城的船票。开往天空之城的飞船每15分钟一趟，船票的价格为：#b"+cost+" 金币#k. 你确定需要购买 #b#t4031045##k？");
}

function action(mode, type, selection) {
    if(mode == -1)
        cm.dispose();
    else {
        if(mode == 0) {
            cm.sendNext("看来你在这里还有其它事情要做？");
            cm.dispose();
            return;
        }
        status++;
        if(status == 0) {
            if (cm.getMeso() >= cost && cm.canHold(4031045)) {
                cm.gainItem(4031045,1);
                cm.gainMeso(-cost);
                cm.dispose();
            } else {
                cm.sendOk("你确定你有 #b"+cost+" 金币#k？ 如果你确定你有的话，那么检查下背包是否有空位！");
                cm.dispose();
            }
        }
    }
}
