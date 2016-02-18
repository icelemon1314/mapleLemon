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
 Rini (Orbis Boat Loader) 2012001
**/

function start() {
	if(cm.haveItem(4031047)){
        var em = cm.getEventManager("Boats");
		if (em.getProperty("entry") == "true")
			cm.sendYesNo("飞船还剩几个座位，购买了船票的乘客赶紧上船吧。你想现在就登船的么？");
		else {
			if (em.getProperty("entry") == "false" && em.getProperty("docked") == "true") {
				cm.sendOk("开船前1分钟停止检票，请耐心等候下一趟飞船！");
				} else {
					cm.sendOk("飞船已经起飞了，请耐心等候下一趟！");
				}
			cm.dispose();
		}
    }else{
        cm.sendOk("你得先去买张去魔法密林的船票！");
        cm.dispose();
    }
}

function action(mode, type, selection){
    cm.warp(200000112);
    cm.dispose();
}