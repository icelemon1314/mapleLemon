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

/* Grendel the Really Old
	Magician Job Advancement
	Victoria Road : Magic Library (101000003)

	Custom Quest 100006, 100008, 100100, 100101
*/

var status = 0;
var job1 = 200;
var job2 = 0;
var job3 = 0;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0 && status == 2) {
			cm.sendOk("下定决心,再来找我!");
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			if (cm.isBeginner()) {
				if (cm.getPlayerStat("LVL") >= 8 && cm.getPlayerStat("INT") >= 20)
					cm.sendNext("你希望成为一名#r魔法师#k？");
				else {
					cm.sendOk("成为#r魔法师#k需要满足等级大于8级，智力大于20点，看来你还需要再去锻炼！")
					cm.dispose();
				}
			} else {
				if (cm.getPlayerStat("LVL") >= 30 
					&& cm.getJobId() == job1) {
					if (cm.getQuestStatus(100006).getId() >=
						client.MapleQuestStatus.Status.STARTED.getId()) {
						cm.completeQuest(100008);
						if (cm.getQuestStatus(100008) ==
						 client.MapleQuestStatus.Status.COMPLETED) {
							status = 20;
							cm.sendNext("I see you have done well. I will allow you to take the next step on your long road.");
						} else {
							cm.sendOk("Go and see the #rJob Instructor#k.")
							cm.dispose();
						}
					} else {
						status = 10;
						cm.sendNext("The progress you have made is astonishing.");
					}
				} else if (cm.getQuestStatus(100100).equals(MapleQuestStatus.Status.STARTED)) {
					cm.completeQuest(100101);
					if (cm.getQuestStatus(100101).equals(MapleQuestStatus.Status.COMPLETED)) {
						cm.sendOk("Alright, now take this to #bRobeira#k.");
					} else {
						cm.sendOk("Hey, " + cm.getChar().getName() + "! I need a #bBlack Charm#k. Go and find the Door of Dimension.");
						cm.startQuest(100101);
					}
					cm.dispose();
				} else {
					cm.sendOk("You have chosen wisely.");
					cm.dispose();
				}
			}
		} else if (status == 1) {
			cm.sendNextPrev("你需要慎重考虑，一旦决定将不能更改，你可想好了？");
		} else if (status == 2) {
			cm.sendYesNo("你确定要成为一名#r魔法师#k？");
		} else if (status == 3) {
			if (cm.getJobId() == 0)
				cm.changeJob(job1);
			cm.sendOk("恭喜你！现在你已经是一名#r魔法师#k了，努力修炼吧！终有一天你会很强大！\r\n第二次转职是在 #r30级 #k后！\r\n#r那时你再来找我吧！");
			cm.dispose();
		} else if (status == 11) {
			cm.sendNextPrev("You may be ready to take the next step as a #rFire/Poison Wizard#k, #rIce/Lightning Wizard#k or #rCleric#k.");
		} else if (status == 12) {
			cm.sendAcceptDecline("But first I must test your skills. Are you ready?");
		} else if (status == 13) {
			if (cm.haveItem(4031009)) {
				cm.sendOk("Please report this bug at http://odinms.de/forum/.\r\nstatus = 13");
			} else {
				cm.startQuest(100006);
				cm.sendOk("Take this letter to the #bJob Instructor#k near Ellinia. He will show you the way.");
			}
		} else if (status == 21) {
			cm.sendSimple("What do you want to become?#b\r\n#L0#Fire/Poison Wizard#l\r\n#L1#Ice/Lightning Wizard#l\r\n#L2#Cleric#l#k");
		} else if (status == 22) {
			var jobName;
			if (selection == 0) {
				jobName = "Fire/Poison Wizard";
				job = net.sf.odinms.client.MapleJob.FP_WIZARD;
			} else if (selection == 1) {
				jobName = "Ice/Lightning Wizard";
				job = net.sf.odinms.client.MapleJob.IL_WIZARD;
			} else {
				jobName = "Cleric";
				job = net.sf.odinms.client.MapleJob.CLERIC;
			}
			cm.sendYesNo("Do you want to become a #r" + jobName + "#k?");
		} else if (status == 23) {
			cm.changeJob(job);
			cm.sendOk("So be it! Now go, and go with pride.");
		}
	}
}	
