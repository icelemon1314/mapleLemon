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

/**
-- Odin JavaScript --------------------------------------------------------------------------------
	Jeff - El Nath : El Nath : Ice Valley II (211040200)
-- By ---------------------------------------------------------------------------------------------
	Xterminator
-- Version Info -----------------------------------------------------------------------------------
	1.0 - First Version by Xterminator
---------------------------------------------------------------------------------------------------
**/

var status = 0;

function start() {
    cm.sendNext("Hi，看来你想去更深的地方，里面全是各种凶狠的怪物。即使你做好了准备，但我劝你还是再考虑下是否进去。很久以前，我们村庄有几个勇士进去了，但是到现在都还没有回来，唉...");
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (status == 1 && mode == 0 && cm.getLevel() > 49) {
            cm.sendNext("如果你改变主意了就过来找我吧，总之我需要保护好这边土地！");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 1) {
            if (cm.getLevel() > 49)
                cm.sendYesNo("如果你想进去，我建议你最好改变主意。但是如果你执意要进去的话...我也只能让强壮的人进去，否则你在里面不能存活下来。我不希望看到你升天。让我看看...你确实很强壮，你决定要进去了么？");
             else 
                cm.sendPrev("如果你想进去，我建议你最好改变主意。但是如果你执意要进去的话...我也只能让强壮的人进去，否则你在里面不能存活下来。我不希望看到你升天。让我看看...你的等级不足50级，我还不能让你进去，忘记我刚才说的话吧！");
        } else if (status == 2) {
            if (cm.getLevel() >= 50) 
                cm.warp(211040300, 5);
            cm.dispose();
        }
    }
}