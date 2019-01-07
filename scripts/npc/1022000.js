

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
        武术教练
        NPCId:1022000
	MapId:102000003
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
                       cm.sendOk("好吧，我尊重你的选择。");
                       cm.dispose();   
                       return;
                }else if(mode0_pattern==2){//点击“不是”按钮或sendSimple对话框的结束对话后的另一种处理   
                       mode0_pattern=0;
                       cm.dispose();   
                       return;
                }else{//点击对话框上一步时返回上一状态
                       status--;
                }                 
            }else{//其他mode取值时
                status--;
            }	
        
        if (cm.getJobId()==0) { // 新手
                if (status == 0) {       
                        cm.sendNext("你的身上似乎有一种潜质，让我看看...");                             
                } else if (status == 1) {
                        if (cm.getPlayerStat("LVL") >= 10 && cm.getPlayerStat("STR") >= 35) {
                                cm.sendYesNo("嗯，看上去你已经拥有一定的力量了，虽然还不够强大，不过我可以帮助你变得更强，你是否想成为一名战士？");
                                mode0_pattern=1;
                        } else {
                                cm.sendOk("你目前的力量还不够，继续修炼到#b等级10级以上，力量35点以上#k后再来吧。");	
                                cm.dispose();
                        }
                }else if(status == 2){
                    cm.changeJob(100);
                    cm.getPlayer().gainSP(1);
                    cm.sendOk("嗯...从现在起你就是一名#r战士#k了，那么去冒险中努力磨练自己吧，只有刻苦修行你才能够拥有强健的体魄与过人的力量，相信终有一天你会变得十分强大！");
                    cm.dispose();
                }
        } else if (cm.getJobId()==100) { // 二转
                if (cm.getPlayerStat("LVL") >= 30) {
                        if (cm.haveItem(4031012)) { // 完成试练任务了
                                if (status == 0) 
                                        cm.sendNext("嗯..你果然没让我失望。");
                                else if(status==1)
                                        cm.sendNext("既然你通过了测试，那么就让我来帮你获得更多力量吧。接下来你需要在#b剑客#k、#b准骑士#k与#b枪战士#k这三种职业中选择一个作为你下一阶段的职业方向，其中每种职业都有各自擅长的武器与特殊技能，具体关于这些职业你有什么想知道的在最终决定前都可以问我。");
                                else if (status == 2){
                                    cm.sendSimple("关于下一阶段的职业你还想了解什么\r\n#b#L0#我想知道有关剑客的介绍#l\r\n#L1#我想知道有关准骑士的介绍#l\r\n#L2#我想知道有关枪战士的介绍#l\r\n#L3#我已经决定好了#l");
                                    mode0_pattern=2;
                                }                                        
                                else if (status == 3) {
                                        if (selection == 0) 
                                                cm.sendPrev("剑客介绍");                                          						
                                        else if (selection == 1) 
                                                cm.sendPrev("准骑士介绍");
                                        else if (selection == 2) 
                                                cm.sendPrev("枪战士介绍");
                                        else if (selection == 3){
                                               status++;
                                               mode0_pattern=2;
                                               cm.sendSimple("那么你想选择的2转职业是：\r\r#b#L0#剑客#l\r\n#L1#准骑士#l\r\r#L2#枪战士#l");
                                        } 
                                } else if (status == 4){            
                                        cm.sendSimple("那么你想选择的2转职业是：\r\r#b#L0#剑客#l\r\n#L1#准骑士#l\r\r#L2#枪战士#l");
                                        mode0_pattern=2;
                                } else if (status == 5) {
                                        if (selection == 0) {
                                                jobName = "剑客";
                                                jobId = 110;
                                        } else if (selection == 1) {
                                                jobName = "准骑士";
                                                jobId = 120;					
                                        } else if (selection == 2) {
                                                jobName = "枪战士";
                                                jobId = 130;
                                        }	
                                        cm.sendYesNo("你想成为一名#b"+jobName+"#k吗？转职完成后职业是无法更改的...你是否真的想好了？");
                                } else if (status == 6) {
                                        cm.gainItem(4031012, -1);
                                        cm.changeJobById(jobId);
                                        cm.getPlayer().gainSP(1);
                                        cm.sendNext("好，你已经是一名#b"+jobName+"#k了，现在起我将给予你更多力量，请继续在冒险之路上刻苦修行吧，我会在你达到#r70级以上#k的时候在这里等你凯旋。");
                                        cm.dispose();
                                }		
                        } else if (!cm.haveItem(4031008)) { // 可以开始2转		
                                if (status == 0) {
                                        cm.sendNext("你看上去比以前强大了不少。")
                                } else if (status == 1) {
                                        cm.sendNext("看来可以考虑给予你更多的力量了，不过在此之前我需要考验一下你的实力来判断你是否有这个资格。");
                                } else if (status == 2) {                                   
                                        cm.sendOk("去把这封介绍信交给#b部落西面附近#k的#r#p1072000##k吧，他会指引你进入下一阶段的测试。");
                                        cm.gainItem(4031008,1);
                                        cm.dispose();
                                }	
                        } else { // 2转进行中
                                cm.sendOk("还没见到#r#p1072000##k吗？他平时都待在#b勇士部落西部#k的某个地方。");
                                cm.dispose();
                        }
                }else{
                     cm.sendOk("第二阶段转职需要#r等级30级以上#k，目前你的修行还不够。");
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
				cm.sendOk("好吧，赶紧拿着这个去找#b泰勒斯#k。");
			} else {
				cm.sendOk("你还没有找到我需要的 #r#t4031059##k，赶紧去寻找异界之门吧！");
			}
			cm.dispose();
		}else {
            cm.sendOk("想成为战士的都到这里来…");
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
