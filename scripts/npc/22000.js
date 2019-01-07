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

/* Shanks
	Warp NPC to Lith Harbor (104000000)
	located in Southperry (60000)
*/

var status = 0;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1 || status == 4) {
		cm.dispose();
	} else {
		if (status == 2 && mode == 0) {
			cm.sendOk("好吧，我会在这里等着你的！");
			status = 4;
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			cm.sendNext("看上去你很强大了！（PS:点击NPC会自动接任务，可以做的任务在任务列表中，达到完成条件后，找NPC领奖，周知！！！）");
		} else if (status == 1) {
			cm.sendNextPrev("如果你达到了10级，那么我就可以送你去#m104000000#，在那里你可以遇见更强大的冒险家！")
		} else if (status == 2) {
			cm.sendYesNo("你准备好了么？");
		} else if (status == 3) {
			cm.warp(104000000,0);
			cm.dispose();
		}
	}
}	