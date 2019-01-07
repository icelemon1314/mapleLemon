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
	Treasure Chest - Line 3 Construction Site: B1 <Subway Depot> (103000902)
-- By ---------------------------------------------------------------------------------------------
	Unknown
-- Version Info -----------------------------------------------------------------------------------
	1.1 - Statement fix [Information]
	1.0 - First Version by Unknown
---------------------------------------------------------------------------------------------------
**/

var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (status >= 2 && mode == 0) {
		cm.sendOk("我们下次再见！");
		cm.dispose();
		return;
    }
    if (mode == 1) {
		status++;
    } else {
		status--;
    }
    if (status == 0) {
		if (cm.getQuestStatus(1001200) == 1 && !cm.haveItem(4031039)) {
			cm.gainItem(4031039, 1); // Shumi's Coin
			cm.warp(103000000, 0);
		} else {
			
		}
	} else {
	    var rand = 1 + Math.floor(Math.random() * 6);
	    if (rand == 1) {
		cm.gainItem(4010003, 2); // Adamantium Ore
	    }
	    else if (rand == 2) {
		cm.gainItem(4010000, 2); // Bronze Ore
	    }
	    else if (rand == 3) {
		cm.gainItem(4010002, 2); // Mithril Ore
	    }
	    else if (rand == 4) {
		cm.gainItem(4010005, 2); // Orihalcon Ore
	    }
	    else if (rand == 5) {
		cm.gainItem(4010004, 2); // Silver Ore
	    }
	    else if (rand == 6) {
		cm.gainItem(4010001, 2); // Steel Ore
	    }
	    cm.warp(103000000, 0);
	}
	cm.dispose();
}	

