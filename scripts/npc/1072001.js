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

/* Magician Job Instructor
*/

var status;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (mode == 1)
            status++;
        else
            status--;

		if (cm.haveItem(4031009)) {
			if (status == 0)
				cm.sendNext("嗯哼...这封信确实是从#b汉斯#k那里拿过来的。你这么远跑过来就是想参加测试并完成2转么？好吧，我将给你解释怎么完成测试！")
			else if (status == 1)
				cm.sendNextPrev("我会把你传送到一个隐藏地图。你将在那里看到在外面不能见到的怪物。虽然他们的外貌是一样的，但是内涵却是不同的。");
			else if (status == 2)
				cm.sendNextPrev("你需要收集 #b#t4031013##k。只要30个就足够了，最后把它交给我。噢，忘了告诉你，你只需要打到怪物就能掉落这个道具。就是这么简单，不是么？");
			else if (status == 3)
				cm.sendYesNo("你进去后，直到你完成任务，否则不可能出来。如果你不幸挂掉了，经验同样会减少，所以你应该准备好再进去，你现在就想进去么？");
			else if (status == 4)
				cm.sendNext("好吧，记得我需要的东西！");
			else if (status == 5) {
				cm.warp(108000200, 0);
				cm.dispose();
			}
		} else {
			cm.sendOk("你找我有事么？");
			cm.dispose();
		}
    }
}	