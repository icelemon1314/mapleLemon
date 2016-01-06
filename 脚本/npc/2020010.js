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
/* 
	弓箭手三转
	Bowman 3rd job advancement
	El Nath: Chief's Residence (211000001)

	Custom Quest 100100, 100102
*/

var status = 0;
var job;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 1) {
            cm.sendOk("等你想好了再来找我吧！");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
		
        if (status == 0) {
            if (!(cm.getJobId()==310 ||cm.getJobId()==320)) { 
                cm.sendOk("我没有什么可以教你的！");
                cm.dispose();
                return;
            }
            if (cm.isQuestCompleted(100102)){ // 三转问答完成
				cm.gainItem(4031058, -1);
                cm.sendNext("哇哦，看上你也挺聪明的嘛！");
            } else if (cm.isQuestStarted(100102)) { // 三转问答没完成
                cm.sendOk("去找到隐藏在冰封雪域某处圣地中的#r黑圣石#k吧！");
                cm.dispose();
            } else if (cm.isQuestCompleted(100101)){ // 完成了第一阶段挑战
                cm.sendNext("我的预感是对的，你证明了你的力量！");
            } else if (cm.isQuestStarted(100100)) { // 没完成镜像挑战
                cm.sendOk("赶紧去找一转教官吧，他会教你怎么做的！");
                cm.dispose();
            } else if ((cm.getJobId()==310 ||cm.getJobId()==320) &&cm.getLevel() >= 70){ // 准备三转
                cm.sendNext("#b恭喜你达到了70级以上！\r\n#k#r你现在已经可以进行第三次转职了！\r\n#k如果你已经准备好请点击下一步！");
            }else {
                cm.sendOk("你还不够强大的哦！");
                cm.dispose();
            }
        } else if (status == 1) {
            if (cm.isQuestCompleted(100102)) { // 转职任务都完成了
                if (cm.getJobId()==310) {
                    cm.changeJobById(311);
                    cm.getPlayer().gainAp(5);
                    cm.sendOk("恭喜你，成功转职为了#r射手#k！");
                    cm.dispose();
                } else if (cm.getJobId()==320) {
                    cm.changeJobById(321);
                    cm.getPlayer().gainAp(5);
                    cm.sendOk("恭喜你，成功转职为了#r游侠#k！");
                    cm.dispose();
                }
            } else if (cm.isQuestCompleted(100101)) // 完成第一阶段
                cm.sendYesNo("你准备好接受最后的挑战了么？");
            else {
				// 准备开始转职
                cm.sendYesNo("我可以让你变得更加的强大，在此之前你需要证明你拥有强大的力量和睿智的头脑，准备好接受挑战了么？");
			}
        } else if (status == 2) {
            if (cm.isQuestCompleted(100101)) { // 开始第二阶段挑战
                cm.startQuest(100102);
				cm.gainItem(4031057,-1);
                cm.sendOk("去找到隐藏在冰封雪域某处圣地中的#r黑圣石#k吧！别忘记带一个黑暗水晶成品过去！！！");
                cm.dispose();
            } else { // 开始第一阶段任务
                cm.startQuest(100100);
                cm.sendOk("赶紧去找一转教官吧，他会教你怎么做的！");
                cm.dispose();
            }
        }
    }
}
