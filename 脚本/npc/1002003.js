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
/* Author: Xterminator
	NPC Name: 		Mr. Goldstein
	Map(s): 		Victoria Road : Lith Harbour (104000000)
	Description:		Extends Buddy List
*/
var status = -1;

function start() {
    cm.sendYesNo("I hope I can make as much as yesterday... well, hello! Don't you want to extend your buddy list? You look like someone who'd have a whole lot of friends... well, what do you think? With some money I can make it happen for you. Remember, though, it only applies to one character at a time, so it won't affect any of your other characters on your account. Do you want to extend your buddy list?");
}

function action(mode, type, selection) {
    status++;
    if (mode == -1) {
        if(mode == 0)
            cm.sendNext(status == 0 ? "I see... you don't have as many friends as I thought you would. Hahaha, just kidding! Anyway if you feel like changing your mind, please feel free to come back and we'll talk business. If you make a lot of friends, then you know ... hehe ..." : "I see... I don't think you don't have as many friends as I thought you would. If not, you just don't have 250,000 mesos with you right this minute? Anyway, if you ever change your mind, come back and we'll talk business. That is, of course, once you have get some financial relief... hehe ...");
        cm.dispose();
        return;
    }
    if (status == 0)          
        cm.sendYesNo("Alright, good call! It's not that expensive actually. #b250,000 mesos and I'll add 5 more slots to your buddy list#k. And no, I won't be selling them individually. Once you buy it, it's going to be permanently on your buddy list. So if you're one of those that needs more space there, then you might as well do it. What do you think? Will you spend 250,000 mesos for it?");
    else if (status == 1) {
        var capacity = cm.getPlayer().getBuddylist().getCapacity();
        if (capacity >= 50 || cm.getMeso() < 250000) {
            cm.sendNext("Hey... are you sure you have #b250,000 mesos#k? If so, then check and see if you have extended your buddy list to the max. Even if you pay up, the most you can have on your buddy list is #b50#k.");
        } else {
            cm.gainMeso(-250000);
            cm.getPlayer().setBuddyCapacity(capacity + 5);
            cm.sendOk("Alright! Your buddy list will have 5 extra slots by now. Check and see for it yourself. And if you still need more room on your buddy list, you know who to find. Of course, it isn't going to be for free ... well, so long ...");
        }
        cm.dispose();
    }
}