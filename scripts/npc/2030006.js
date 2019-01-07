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
 
var status = 0;
var qChars = new Array("Q1: 冒险岛中从1级到2级升级所需经验是多少？#10#12#15#20#3",
        "Q1: 根据不同职业为了第一次转职，所要求的能力被正确叙述的是哪一个？#战士 35 力量#飞侠 25 运气#魔法师 25 智力#弓箭手 20 敏捷#1",
        "Q1: 被怪物攻击时特别的异常状态没有被正确说明的是哪一个？#虚弱 - 移动速度降低#封印 - 不能使用技能#黑暗 - 降低命中率#诅咒 - 减少经验值#1",
        "Q1: 蚂蚁洞里没有哪个怪物？#刺蘑菇#僵尸蘑菇#月牙牛魔王#石球 #4",
        "Q1: 第一次转职不可以选择的职业？#战士#巫师#魔法师#飞侠#2");
var qItems = new Array("Q2: 怪物和怪物爆出的物品正确联系的是哪一个？#蓝蜗牛 - 蜗牛肉#蝙蝠 - 蝙蝠翅膀#红螃蟹 - 蟹黄#猪 - 猪脚#2",
        "Q2: 怪物和怪物爆出的物品正确联系的是哪一个？#土龙 - 龙头#火野猪 - 焦炭#僵尸蘑菇 - 符纸#黑食人花 - 黑食人花的花叶#4",
        "Q2: 在迈依普尔物语中登场的药和功效正确连线的是哪一个？#白色药水 - 恢复 250 HP#橙色药水 - 恢复 400 MP#红色药水 - 恢复 100 HP#披萨饼 - 恢复 400 HP#4",
        "Q2: 下面哪个药水恢复 50% Hp 和 Mp?#特殊药水#超级药水#活力神水#橙色药水#1",
        "Q2: 在迈依普尔物语中登场的药和功效正确连线的是哪一个？#蓝色药水 - 恢复 150 MP#活力神水 - 恢复 200 MP#露水 - 恢复 3000 MP#柠檬 - 恢复 50 MP#3");
var qMobs = new Array("Q3: 下列动物中等级最高的是？#绿蘑菇#木妖#蓝水灵#斧木妖#4",
        "Q3: 彩虹岛没有哪个怪物？#黑鳄鱼#绿蜗牛#蓝蜗牛#蘑菇仔#1",
        "Q3: 哪个怪物在去天空之城的船上能看到？#蝙蝠怪#蝙蝠魔#猎犬#黑雪人#2",
        "Q3: 哪个怪物在金银岛看不到？#石头球#蜗牛#斧木妖#猎犬#1",
        "Q3: 神秘岛上没有哪种怪物？#石头球#白狼人#火焰猎犬#猪猪#4",
        "Q3: 哪种怪物可以飞？#蝙蝠 #花蘑菇#小白雪人#白狼人#1",
        "Q3: 那个怪物在金银岛看不到？#三眼章鱼#刺蘑菇#青蛇#蓝独角狮#4",
        "Q3: 哪种怪物在彩虹岛见不到？#蜗牛#白狼人#猪猪#蓝蜗牛#2");
var qQuests = new Array("Q4: 为了进行2次转职收集好30个黑玉后转职教官会给你的物品是什么?#英雄证 #力气项链#智慧项链#魔法石#1",
        "Q4: 哪个任务可以反复执行？#寻找《上古魔书》#约翰的礼物#守卫兵鲁克的决心#艾温的玻璃鞋#4",
        "Q4: 那个不是二转教官？#妮娜#赫丽娜#汉斯#达克鲁#1",
        "Q4: 射手村的玛雅，请求我们拿什么物品给她，来治好自己的病？#中药#奇怪的药#续命丸#超级药水#2",
        "Q4: 在废弃都市能够见到一个离家的少年阿列克斯，他的父亲是谁?#阿尔利#斯坦长老 #阿尔莫斯#阿杜比斯#2",
        "Q4: 要求级别最高的任务是哪一个？#阿尔卡斯特和黑暗水晶#艾温的玻璃鞋#迷宫入口的守卫兵#简和野猪#1");
var qTowns = new Array("Q5: 在神秘岛冰峰雪域看不见的NPC是谁？#巴伯下士#杰夫#伊吉上等兵#保姆#4",
        "Q5: 在金银岛的明珠港不能看到的NPC是谁？#特奥#佩森#赛恩#智慧爷爷#3",
        "Q5: 在金银岛的废弃都市不能见到的NPC是谁？#鲁克#内拉#休咪#废都中巴#1",
        "Q5: 金银岛没有的村落？#魔法密林#射手村#彩虹岛#勇士部落#3",
        "Q5: 在魔法密林见不到的NPC是谁？#汉斯#易德#妖精 艾温#酋长#4",
        "Q5: 在金银岛的勇士部落不能看到的NPC是谁？#易德#武术教练#利伯#酋长#1");
var correctAnswer = 0;

function start() {
    if (cm.haveItem(4031058, 1)) {
        cm.sendOk("你已经有了 #t4031058# 不要再浪费我的时间了。");
        cm.dispose();
    } else if (cm.isQuestStarted(100102)) { 
		cm.sendNext("哇奥看看这是谁来了 ！\r\n我处在这么偏远的地方你也能找到？");
	}else {
        cm.sendOk("你找我有事么？");
		cm.dispose();
    }
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (mode == 0) {
            cm.sendOk("下次再见。");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 1)
            cm.sendNextPrev(" 如果你能给我1个 #v4005004##b黑暗水晶#k 那么我将给你一次答题的机会，如果5道题都能答对，我会奖励你#v4031058# #b智慧项链#k。");
        else if (status == 2) {
            if (!cm.haveItem(4005004)) {
                cm.sendOk("你没有 #b黑暗水晶？#k");
                cm.dispose();
            } else {
                cm.gainItem(4005004, -1);
                cm.sendSimple("黑暗水晶我收下了，测验马上就开始了，准备好 #b接受挑战了么？#k.\r\n\r\n" + getQuestion(qChars[Math.floor(Math.random() * qChars.length)]));
                status = 2;
            }
        } else if (status == 3) {
            if (selection == correctAnswer)
                cm.sendOk(" 你答对了，赶紧来看下一题吧！");
            else {
                cm.sendOk("你答错了。\r\n你必须再给我一个 #b黑暗水晶#k 才可以再挑战!");
                cm.dispose();
            }
        } else if (status == 4)
            cm.sendSimple("问题是这样的：\r\n\r\n" + getQuestion(qItems[Math.floor(Math.random() * qItems.length)]));
        else if (status == 5) {
            if (selection == correctAnswer)
                cm.sendOk(" 你答对了，赶紧来看下一题吧！");
            else {
                cm.sendOk("你答错了。\r\n你必须再给我一个 #b黑暗水晶#k 才可以再挑战!");
                cm.dispose();
            }
        } else if (status == 6) {
            cm.sendSimple("问题是这样的：\r\n\r\n" + getQuestion(qMobs[Math.floor(Math.random() * qMobs.length)]));
            status = 6;
        } else if (status == 7) {
            if (selection == correctAnswer)
                cm.sendOk(" 你答对了，赶紧来看下一题吧！");
            else {
                cm.sendOk("你答错了。\r\n你必须再给我一个 #b黑暗水晶#k 才可以再挑战!");
                cm.dispose();
            }
        } else if (status == 8)
            cm.sendSimple("问题是这样的： \r\n\r\n" + getQuestion(qQuests[Math.floor(Math.random() * qQuests.length)]));
        else if (status == 9) {
            if (selection == correctAnswer) {
                cm.sendOk(" 你答对了，赶紧来看下一题吧！");
                status = 9;
            } else {
                cm.sendOk("你答错了。\r\n你必须再给我一个 #b黑暗水晶#k 才可以再挑战!");
                cm.dispose();
            }
        } else if (status == 10) {
            cm.sendSimple("最后一个问题：\r\n\r\n" + getQuestion(qTowns[Math.floor(Math.random() * qTowns.length)]));
            status = 10;
        } else if (status == 11) {
            if (selection == correctAnswer) {
                cm.gainItem(4031058, 1);
				cm.completeQuest(100102);
                cm.sendOk("恭喜 , 你太强大了.\r\n拿着这个 #v4031058# 去找你的转职教官吧!.");
            } else {
                cm.sendOk("太可惜了，只差一题就可以爆机了！！ 下次再努力吧><.\r\n当然了 #b黑暗水晶#k 同样记得带哦!");
                cm.dispose();
            }
        } else if(status == 12) {
			cm.warp(211000001, 0);
			cm.dispose();
		}
    }
}
function getQuestion(qSet) {
    var q = qSet.split("#");
    var qLine = q[0] + "\r\n\r\n#L0#" + q[1] + "#l\r\n#L1#" + q[2] + "#l\r\n#L2#" + q[3] + "#l\r\n#L3#" + q[4] + "#l";
    correctAnswer = parseInt(q[5], 10);
    correctAnswer--;
    return qLine;
}