

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
        达克鲁
        NPCId:1052001
	MapId:103000003
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
                       cm.sendOk("你应该知道，在这里已经没有别的选择!");
                       cm.dispose();   
                       return;
                }else if(mode0_pattern==2){//点击“不是”按钮或sendSimple对话框的结束对话后的另一种处理   
                       mode0_pattern=0;       
                       cm.sendOk("那你还是真正下定决心后再来找我吧");
                       cm.dispose();   
                       return;
                }else if(mode0_pattern==3){//点击“不是”按钮或sendSimple对话框的结束对话后的另一种处理   
                       mode0_pattern=0;       
                       //cm.sendOk("好吧那还是等你准备好了再来吧。");
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
                        cm.sendYesNo("那么你已经决定成为一名#r飞侠#k了么?"); 
                        mode0_pattern=1;
                } else if (status == 1) {
                        if (cm.getPlayerStat("LVL") >= 10 && cm.getPlayerStat("LUK") >= 25) {
                                cm.sendNext("这将是你最终做出的一个非常重要的选择，一旦你选择了这个职业，你将永远不能改变。"); 
                               
                        } else {
                                cm.sendOk("你还需要加强修炼我才能给你指明怎样成为一名出色的#r飞侠#k,要成为飞侠你必须达到#b等级10级以上，运气25点以上#k。");	
                                cm.dispose();
                        }
                }else if(status == 2){
                    cm.sendYesNo("请谨慎决定，你是否真的要成为一名#r飞侠#k？");
                    mode0_pattern=2;
                }else if(status == 3){    
                    cm.changeJob(400);
                    cm.getPlayer().gainSP(1);
                    cm.sendOk("嗯...从现在起你已经是一名#r飞侠#k了，为了变得更强今后你需要继续在冒险中不断地修炼自己，那么现在开始去为了更高的境界而奋斗吧。");
                    cm.dispose();
                }
        } else if (cm.getJobId()==400) { // 若当前玩家职业是飞侠
                if (cm.getPlayerStat("LVL") >= 30) {//达到二转等级条件
                        if (cm.haveItem(4031012)) { // 若已完成转职考试任务
                                if (status == 0){ 
                                        cm.sendNext("嗯你顺利通过了测试，很好，看来的确应该给你进入飞侠下一职业阶段的资格。");
                                }else if (status == 1){
                                        cm.sendNext("在飞侠的第二个阶段，你将有#b刺客#k与#b侠客#k两种职业方向可以选择，其中刺客擅长使用#b拳套#k与#b飞镖#k，侠客更擅长使用#b短刀#k类武器，并且两种职业的技能与属性也有所不同。而在做出职业选择前，你应该进行充分的考虑，对于下一阶段职业有什么问题的话可以问我。");
                                }else if (status == 2){
                                        cm.sendSimple("有关下一阶段的职业你还想了解些什么\r\n#b#L0#我想知道有关刺客的介绍#l\r\n#L1#我想知道有关侠客的介绍#l\r\n#L2#我想我已经决定好了#l");   
                                        mode0_pattern=3;
                                }                                        
                                else if (status == 3) {
                                        if (selection == 0) 
                                                cm.sendPrev("刺客介绍");                                          						
                                        else if (selection == 1) 
                                                cm.sendPrev("侠客介绍");     
                                        else if (selection == 2){
                                               status++;
                                               cm.sendSimple("那么第二阶段你想选择的职业是：\r\r#b#L0#刺客#l\r\n#L1#侠客#l");
                                               mode0_pattern=3;
                                        } 
                                } else if (status == 4){            
                                        cm.sendSimple("那么第二阶段你想选择的职业是：\r\r#b#L0#刺客#l\r\n#L1#侠客#l");
                                        mode0_pattern=3;
                                } else if (status == 5) {
                                        if (selection == 0) {
                                                jobName = "刺客";
                                                jobId = 410;
                                        } else if (selection == 1) {
                                                jobName = "侠客";
                                                jobId = 420;					
                                        } 
                                        cm.sendYesNo("你确定要成为#b"+jobName+"#k吗？这个选择将影响到你未来的职业发展方向，且转职后无法更改，你是否真的决定好了？");
                                } else if (status == 6) {
                                        cm.gainItem(4031012, -1);
                                        cm.changeJobById(jobId);
                                        cm.getPlayer().gainSP(1);
                                        cm.sendOk("嗯，你已经是一名#b"+jobName+"#k了，希望你将不会后悔你的选择。继续去冒险修炼吧，时间会给你新的成长与领悟，等你达到#r70级以上#k后再来找我。");
                                        cm.dispose();
                                }		
                        } else if (!cm.haveItem(4031011)) { // 可以开始2转		
                                if (status == 0) {
                                        cm.sendNext("你取得的进步真令人意外。")
                                } else if (status == 1) {
                                        cm.sendNext("看来是时候让你进入职业的下一阶段了，不过在那之前还是需要先考核一下你的能力。");
                                        mode0_pattern=3;
                                } else if (status == 2) {                                      
                                        cm.sendOk("来，带着这封信去找#r#p1072003##k,他应该就待在#b废都周边工地#k的某处。通过转职官的考验然后把#t4031012#带回来给我，你就可以进入飞侠的下一个阶段。");
                                        cm.gainItem(4031011,1);
                                        cm.dispose();
                                }	
                        } else { // 2转进行中
                                cm.sendOk("快去见#r#p1072003##k，他平时一般都在工地区域附近活动，相信你会找到他的。");
                                cm.dispose();
                        }
                }else{
                     cm.sendOk("想进行第二阶段转职必须达到#r等级30级以上#k，#r30级#k后再来吧。");
                     cm.dispose();
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
				cm.sendOk("好吧，赶紧拿着这个去找#b艾瑞克#k。");
			} else {
				cm.sendOk("你还没有找到我需要的 #r#t4031059##k，赶紧去寻找异界之门吧！");
			}
			cm.dispose();
		}else {
            cm.sendOk("想成为飞侠的到这里来。。。");
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
