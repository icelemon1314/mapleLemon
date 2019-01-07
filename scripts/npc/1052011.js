/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/* Exit
	Warp NPC to Subway Ticketing Booth (103000100)
	located in B1 <Area 1> (103000900)
	located in B1 <Area 2> (103000901)
	located in B2 <Area 1> (103000903)
	located in B2 <Area 2> (103000904)
	located in B3 <Area 1> (103000906)
	located in B3 <Area 2> (103000907)
	located in B3 <Area 3> (103000908)
*/

var status = 0;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (status >= 1 && mode == 0) {
			cm.sendOk("下次再见！");
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			cm.sendNext("这里是通向外部的出口。");
		} else if (status == 1) {
			cm.sendYesNo("你想放弃这次挑战并离开这里的么？下次再进来你得重新开始了！")
		} else if (status == 2) {
			cm.warp(103000100, 0);
			cm.dispose();
		}
	}
}