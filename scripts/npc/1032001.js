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
/* Dances with Balrog
	Warrior Job Advancement
	Victoria Road : Warriors' Sanctuary (102000003)
*/

var status = 0;
var jobName;
var jobId;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 2) {
            cm.sendNext("你还需要时间再考虑下么？好吧，你慢慢去想吧。 做这个决定并不轻松，等你想好了再来找我吧！ ");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
			
		if (cm.getJobId()==0) { // 新手
			if (status == 0) {
				cm.sendNext("你想成为一名魔法师么？在这之前你需要达到一定的条件，你至少需要达到#b等级8级，智力大于20点#k，你确定要成为一名#r魔法师#k？");                
			} else if (status == 1) {
				if (cm.getPlayerStat("LVL") >= 8 && cm.getPlayerStat("INT") >= 20) {
					cm.changeJob(200);
					cm.sendOk("恭喜你！现在你已经是一名#r魔法师#k了，努力修炼吧！终有一天你会很强大！\r\n第二次转职是在 #r30级 #k后！\r\n#r那时你再来找我吧！");
					cm.dispose();
				} else {
					cm.sendOk("看来你还没有达到条件！");	
					cm.dispose();
				}
			}
		} else if (cm.getJobId()==200) { // 二转
			if (cm.getPlayer().getLevel() >= 30) {
				if (cm.haveItem(4031012)) { // 完成试练任务了
					if (status == 0) 
						cm.sendNext("噢，很高兴能看到你安全回来，我知道你肯定能通过测试。那就让我来让你变得更加强大吧，在这之前你需要选择一个职业，如果你有问题，尽管来问题吧！");
					else if (status == 1) 
						cm.sendSimple("好吧，你决定好了，就点击[我已经想好了]\r\n#b#L0#请告诉我火毒法师的介绍#l\r\n#L1#请告诉我冰雷法师的介绍#l\r\n#L2#请告诉我牧师的介绍#l\r\n#L3#我已经想好了！！！#l");
					else if (status == 2) {
						if (selection == 0) 
							cm.sendNext("火毒法师介绍补充");
						else if (selection == 1) 
							cm.sendNext("冰雷法师介绍补充");
						else if (selection == 2) 
							cm.sendNext("牧师介绍补充");
						else if (selection == 3) 
							cm.sendSimple("你已经想好了么？那赶紧来选择你的2转职业吧：\r\r#b#L0#火毒法师#l\r\n#L1#冰雷法师#l\r\r#L2#牧师#l");				
					} else if (status == 3) {
						if (selection == 0) {
							jobName = "火毒法师";
							jobId = 210;
						} else if (selection == 1) {
							jobName = "冰雷法师";
							jobId = 220;					
						} else if (selection == 2) {
							jobName = "牧师";
							jobId = 230;
						}	
						cm.sendYesNo("你确定2转职业为：#b" + jobName + "#k？转职后不能反悔的哦...你想好了么？");
					} else if (status == 4) {
						cm.gainItem(4031012, -1);
						cm.changeJobById(jobId);
						cm.sendNext("恭喜你转职成功，下次转职是70级，赶紧去升级吧！");
						cm.dispose();
					}		
				} else if (!cm.haveItem(4031009)) { // 可以开始2转		
					if (status == 0) {
						cm.sendNext("看来你变强大了！！！")
					} else if (status == 1) {
						cm.sendNext("但是我需要先测试下你的能力，看你是否货真价实。当然这个测试不会很难，赶紧拿着这封信件去找#r#p1072001##k他将会给你提供帮助！");
					} else if (status == 2) {
						cm.gainItem(4031009,1);
						cm.sendOk("#r#p1072001##k很有可能在#b魔法密林北部#k的某个地方!");
						cm.dispose();
					}	
				} else { // 2转进行中
					cm.sendOk("赶紧去找#b#p1072001##k 。他会告诉你怎么做。");
					cm.dispose();
				}
			}
		} else if (cm.isQuestStarted(100100)){
			cm.sendOk("Hey, 我需要一个#r#t4031059##k，赶紧去寻找异界之门吧！");
			cm.startQuest(100101);
			cm.completeQuest(100100);
			cm.dispose();
		} else if (cm.isQuestStarted(100101)) {
			if (cm.haveItem(4031059)) {
				cm.gainItem(4031059,-1);
				cm.gainItem(4031057,1);
				cm.completeQuest(100101);
				cm.sendOk("好吧，赶紧拿着这个去找#b鲁碧#k。");
			} else {
				cm.sendOk("你还没有找到我需要的 #r#t4031059##k，赶紧去寻找异界之门吧！");
			}
			cm.dispose();
		}else {
			cm.sendOk("魔法是很神奇的一种事物！");
			cm.dispose();
		}
		
	}
}


/*
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
			if (cm.isBeginner()) { // 一转处理
				if (cm.getPlayerStat("LVL") >= 8 && cm.getPlayerStat("INT") >= 20)
					cm.sendNext("你希望成为一名#r魔法师#k？");
				else {
					cm.sendOk("成为#r魔法师#k需要满足等级大于8级，智力大于20点，看来你还需要再去锻炼！")
					cm.dispose();
				}
			} else {
				if (cm.getPlayerStat("LVL") >= 30 && cm.getJobId() == job1) { // 二转
					if (cm.getQuestStatus(100008) >= 1) { // 完成了考验
						status = 20;
						cm.sendNext("看看来你已经通过考验了么？");
					} else if (cm.getQuestStatus(100006) >= 1) { // 接受了送信
						cm.sendOk("赶紧去找 #r#p1072001##k，他将会给你提供帮助！")
						cm.dispose();
					} else {
						status = 10;
						cm.sendNext("看上去你已经足够强大了哦！");
					}
				} else if (cm.getQuestStatus(100100) == 1) {// 三转
					cm.completeQuest(100101);
					if (cm.getQuestStatus(100101)==2) {
						cm.sendOk("Alright, now take this to #bRobeira#k.");
					} else {
						cm.sendOk("Hey, " + cm.getChar().getName() + "! 我需要一个#t4031059##k，赶紧去寻找异界之门吧！");
						cm.startQuest(100101);
					}
					cm.dispose();
				} else {
					cm.sendOk("你的选择是明智的");
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
			cm.sendNextPrev("你想成为一名#r火毒法师#k, #r冰雷法师#k 或者 #r牧师#k吗？");
		} else if (status == 12) {
			cm.sendYesNo("但是必须先经过我的考验，你准备好了么？");
		} else if (status == 13) { // 二转任务
			cm.startQuest(100006);
			cm.gainItem(4031009,1)
			cm.sendOk("拿着这封信件去找#b#p1072001##k 。他会告诉你怎么做。");
			cm.dispose();
		} else if (status == 21) { // 二转进行
			cm.sendSimple("你想成为哪个职业呢？#b\r\n#L0#火毒法师#l\r\n#L1#冰雷法师#l\r\n#L2#牧师#l#k");
		} else if (status == 22) {
			var jobName;
			if (selection == 0) {
				jobName = "火毒法师";
				job = 210;
			} else if (selection == 1) {
				jobName = "冰雷法师";
				job = 220;
			} else {
				jobName = "牧师";
				job = 230;
			}
			cm.sendYesNo("你真的想成为#r" + jobName + "#k？");
		} else if (status == 23) {
			cm.changeJob(job);
			cm.sendOk("恭喜你转职成功，下次转职是70级！");
			cm.dispose();
		}
	}
}	*/
