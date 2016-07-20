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

/* Robin
	First NPC on Snail Hunting Ground I (40000)
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
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			cm.sendNext("Welcome to DigitalMS my friend! I take it you're as excited as me?");
		} else if (status == 1) {
			cm.sendNextPrev("I guess you know how to play MapleStory at least a bit. But there are some things about DigitalMS you should know.");
		} else if (status == 2) {
			cm.sendNextPrev("We just started this server, which means that there are still a lot of things which don't work.");
		} else if (status == 3) {
			cm.sendNextPrev("So please be a bit understanding and help us by reporting any bugs you may encounter!");
		} else if (status == 4) {
			cm.sendNextPrev("The DigitalMS support E-Mail is xbpm07x@gmail.com, please E-Mail us if you need help/want to report a bug.");
		} else if (status == 5) {
			if (cm.getQuestStatus(100012) ==
				net.sf.odinms.client.MapleQuestStatus.Status.COMPLETED) {
				cm.sendNextPrev("One gift should be enough, right?");
				cm.gainMeso(5000000);
				return;
			}
			cm.startQuest(100012);
			cm.completeQuest(100012);
			if (cm.getQuestStatus(100012) ==
				net.sf.odinms.client.MapleQuestStatus.Status.COMPLETED) {
				cm.sendNextPrev("Since I'm such a fucking nice guy, how about a welcome gift? Check your inventory!");
			}
		} else if (status == 6) {
			cm.sendPrev("Now go, go reach level 200 and pwn!");
		}
	}
}	
