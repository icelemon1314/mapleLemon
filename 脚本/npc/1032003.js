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
	Shane - Ellinia (101000000)
-- By ---------------------------------------------------------------------------------------------
	Unknown
-- Version Info -----------------------------------------------------------------------------------
	1.1 - Statement fix [Information]
	1.0 - First Version by Unknown
---------------------------------------------------------------------------------------------------
**/

var status = 0;
var check = 0;

function start() {
    if (cm.getLevel() < 25) {
        cm.sendOk("你的等级还不够进入忍苦森林！至少需要25级以上！");
        cm.dispose();
        check = 1;
    }
    else
        cm.sendYesNo("你好，我是赛恩。我可以让你进入到忍苦森林。你想花费#b5000#k金币，现在就进去么？");
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0) {
            cm.sendOk("好吧，那我们下次再见！");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 1) {
            if (check != 1) {
                if (cm.getPlayer().getMeso() < 5000) {
                    cm.sendOk("看上去你没有足够的金币！")
                    cm.dispose();
                }
                else {
					cm.gainMeso(-5000);
                    if (cm.isQuestStarted(2050))
                        cm.warp(101000100, 0);
                    else if (cm.isQuestStarted(2051))
                        cm.warp(101000102, 0);
                    else if (cm.getLevel() >= 25 && cm.getLevel() < 50)
                        cm.warp(101000100, 0);
                    else if (cm.getLevel() >= 50)
                        cm.warp(101000102, 0);
                    cm.dispose();
                }
            }
        }
    }
}