

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
        赫丽娜
        NPCId:1012100
	MapId:100000201
*/

var status = 0;
var jobName;
var jobId;
var mode0_pattern=0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
         if(mode == 1){ 
                 status++;
                 if(mode0_pattern !=0)
                     mode0_pattern=0;
            }else if(mode==0){                           
                 if(mode0_pattern==1){//点击“不是”按钮或sendSimple对话框的结束对话后的处理
                       mode0_pattern=0;
                       cm.dispose();   
                       return;
                }else if(mode0_pattern==2){//点击“不是”按钮或sendSimple对话框的结束对话后的另一种处理   
                       mode0_pattern=0;       
                       cm.sendOk("那么请决定好了再来找我。");
                       cm.dispose();   
                       return;
                }else if(mode0_pattern==3){//点击“不是”按钮或sendSimple对话框的结束对话后的另一种处理   
                       mode0_pattern=0;       
                       cm.sendOk("好吧，那还是等你准备好了再来吧。");
                       cm.dispose();   
                       return;
                }else{//点击对话框上一步时返回上一状态
                       status--;
                }                 
            }else{//其他mode取值时
                status--;
            }	
        
        if (cm.getJobId()==0) { // 若当前玩家职业是新手
                if (status == 0) {       
                        cm.sendYesNo("你的理想是成为一名#r弓箭手#k吗?"); 
                        mode0_pattern=1;
                } else if (status == 1) {
                        if (cm.getPlayerStat("LVL") >= 10 && cm.getPlayerStat("DEX") >= 25) {
                                cm.sendYesNo("嗯，你的眼神里似乎透露着一种能够洞察一切的敏锐，相信你应该具有成为一名出色弓箭手的实力与潜质。那么你决定好成为一名弓箭手了吗？"); 
                                mode0_pattern=2;
                        } else {
                                cm.sendOk("想成为一名出色的弓箭手你还需要更多修炼才行，成为弓箭手至少需要#b等级10级，敏捷25点以上#k。");	
                                cm.dispose();
                        }
                }else if(status == 2){
                    cm.changeJob(300);
                    cm.getPlayer().gainSP(1);
                    cm.sendOk("好了，从现在开始你就是一名#r弓箭手#k了。弓箭手最擅长远程攻击，打猎时应注意与怪物适当保持一定距离。而如果想要变得更强你需要不断地修炼提高自己，相信总有一天你会成为一名出色的弓箭手！");
                    cm.dispose();
                }
        } else if (cm.getJobId()==300) { // 若当前玩家职业是弓箭手
                if (cm.getPlayerStat("LVL") >= 30) {//达到二转等级条件
                        if (cm.haveItem(4031012)) { // 若已完成转职考试任务
                                if (status == 0){ 
                                        cm.sendNext("看来你顺利通过了转职官的测试，嗯很好，那么我将批准你进入弓箭手下一职业阶段的资格。");
                                }else if (status == 1){
                                        cm.sendNext("在弓箭手的第二个阶段，你将会有#b猎人#k与#b弩弓手#k两条职业道路可供选择，其中猎人主要使用#b弓#k作为武器，而弩弓手主要使用#b弩#k，并且这两个职业都有各自的强项与特殊的技能。当然，在做出下一阶段的职业选择之前，你有什么与转职相关的问题都可以问我。");
                                }else if (status == 2){
                                        cm.sendSimple("有关转职的问题你还想了解些什么\r\n#b#L0#我想知道有关猎人的介绍#l\r\n#L1#我想知道有关弩弓手的介绍#l\r\n#L2#我想我已经决定好了#l");   
                                        mode0_pattern=1;
                                }                                        
                                else if (status == 3) {
                                        if (selection == 0) 
                                                cm.sendPrev("猎人介绍");                                          						
                                        else if (selection == 1) 
                                                cm.sendPrev("弩弓手介绍");     
                                        else if (selection == 2){
                                               status++;
                                               cm.sendSimple("那么第二阶段你想选择的职业是：\r\r#b#L0#猎人#l\r\n#L1#弩弓手#l");
                                               mode0_pattern=1;
                                        } 
                                } else if (status == 4){            
                                        cm.sendSimple("那么第二阶段你想选择的职业是：\r\r#b#L0#猎人#l\r\n#L1#弩弓手#l");
                                        mode0_pattern=1;
                                } else if (status == 5) {
                                        if (selection == 0) {
                                                jobName = "猎人";
                                                jobId = 310;
                                        } else if (selection == 1) {
                                                jobName = "弩弓手";
                                                jobId = 320;					
                                        } 
                                        cm.sendYesNo("你确定要成为一名#b"+jobName+"#k吗？转职后你的选择将无法更改，你是否真的已经决定好了？");
                                } else if (status == 6) {
                                        cm.gainItem(4031012, -1);
                                        cm.changeJobById(jobId);
                                        cm.getPlayer().gainSP(1);
                                        cm.sendOk("嗯好了，从现在起你就是一名#b"+jobName+"#k了，希望在接下来的一个阶段里你能继续努力修炼与提高自己，从而使自己变得更强。当你等级达到#r70级#k之后可以再来找我。");
                                        cm.dispose();
                                }		
                        } else if (!cm.haveItem(4031010)) { // 可以开始2转		
                                if (status == 0) {
                                        cm.sendNext("你的成长速度真令人惊讶，你看起来比以前强了不少。")
                                } else if (status == 1) {
                                        cm.sendYesNo("或许你已经具备进入弓箭手职业下一个阶段的实力了，但在此之前我还是需要测试一下你的能力，你是否已准备好了？");
                                        mode0_pattern=3;
                                } else if (status == 2) {
                                        cm.gainItem(4031010,1);
                                        cm.sendOk("去把这封信交给射手村附近的#r#p1072002##k，她会指示你接下来该怎么做。");
                                        cm.dispose();
                                }	
                        } else { // 2转进行中
                                cm.sendOk("赶快去找#r#p1072002##k吧，她平时一般都待在射手村周边附近的地段，她会告诉你接下来该怎么做的。");
                                cm.dispose();
                        }
                }else{
                     cm.sendOk("第二阶段转职需要达到#r等级30级#k，请到#r30级#k之后再来找我。");
                     cm.dispose();
                }         
        }else if (cm.isQuestStarted(100100)){
			cm.sendOk("Hey, 我需要一个#r#t4031059##k，赶紧去寻找异界之门吧！");
			cm.startQuest(100101);
			cm.completeQuest(100100);
			cm.dispose();
		} else if (cm.isQuestStarted(100101)) {
			if (cm.haveItem(4031059)) {
				cm.gainItem(4031059,-1);
				cm.gainItem(4031057,1);
				cm.completeQuest(100101);
				cm.sendOk("好吧，赶紧拿着这个去找#b蕾妮#k。");
			} else {
				cm.sendOk("你还没有找到我需要的 #r#t4031059##k，赶紧去寻找异界之门吧！");
			}
			cm.dispose();
		}else {
            cm.sendOk("你想成为弓箭手吗?");
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
