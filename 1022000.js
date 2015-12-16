/* Dances with Balrog
 Warrior Job Advancement
 Victoria Road : Warriors' Sanctuary (102000003)
 
 Custom Quest 100003, 100005
 */

var status = 0;
var job;


function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 0 && status == 2) {
        cm.sendOk("请重试。");
        cm.dispose();
        return;
    }
    if (mode == 1)
        status++;
    else
        status--;
    if (status == 0) {
        if (cm.getJob() == 0) {
            if (cm.getPlayerStat("LVL") >= 10 && cm.getJob() == 0) {
                cm.sendNext("你要转职成为一位#r战士#k吗?");
            } else {
                cm.sendOk("你还达不到转职成#r战士#k的要求。");
                cm.dispose();
            }
        } else {
            if (cm.getPlayerStat("LVL") >= 30 && cm.getJob() == 100) { // WARROPR
                if (cm.getQuestStatus(100003) >= 1) {
                    //cm.forceCompleteQuest(100005);
                    if (cm.getQuestStatus(100005) == 2) {
                        status = 20;
                        cm.sendNext("我已经知道你完成了转职任务。");
                    } else {
                        if (!cm.haveItem(4031008)) {
                            cm.gainItem(4031008, 1);
                        }
                        cm.sendOk("现在去找#r#p1072000##k，他将会给你提供帮助。");
                        cm.dispose();
                    }
                } else {
                    status = 10;
                    cm.sendNext("你已经可以转职。");
                }
            } else if (cm.haveItem(4031059)) {
                cm.forceCompleteQuest(100101);
                if (cm.getQuestStatus(100101) == 2) {
                    cm.gainItem(4031059, -1);
                    if (!cm.haveItem(4031057)) {
                        cm.gainItem(4031057, 1);
                    }
                    cm.sendOk("你完成了考验，现在去找#r#p2020008##k。");
                    cm.startQuest(100102);
                }
                cm.dispose();
            } else if (cm.getQuestStatus(100100) == 1) {
                cm.forceCompleteQuest(100100);
                cm.sendOk("嗨, #b#h0##k!我需要一个#b黑符#k，去#p1061009#找找看。");
                cm.startQuest(100101);
                cm.dispose();
            } else if (cm.haveItem(4031057) || cm.getQuestStatus(100102) == 1) {
                cm.sendOk("你完成了考验，现在去找#r#p2020008##k。");
                cm.dispose();
            } else {
                cm.sendOk("你的选择是明智的。");
                cm.dispose();
            }
        }
    } else if (status == 1) {
        cm.sendNextPrev("一旦转职就不能反悔. 如果不想转职请结束对话.");
    } else if (status == 2) {
        cm.sendYesNo("你真的要成为#r战士#k吗?");
    } else if (status == 3) {
        if (cm.getJob() == 0) {
            cm.resetStats(35, 4, 4, 4);
            cm.expandInventory(1, 4);
            cm.expandInventory(4, 4);
            cm.changeJob(100); // WARRIOR
            cm.gainSp(3);
        }
        cm.gainItem(1402001, 1);
        cm.sendOk("转职成功!开始你的冒险之旅吧.");
        cm.dispose();
    } else if (status == 11) {
        cm.sendNextPrev("你想要成为一名#r剑客#k, #r准骑士#k or #r枪战士#k.");
    } else if (status == 12) {
        cm.askAcceptDecline("但是我必须考验你，你准备好了吗？");
    } else if (status == 13) {
        cm.gainItem(4031008, 1);
        cm.startQuest(100003);
        cm.sendOk("请去找#b#p1072000##k，他将会帮助你。");
        cm.dispose();
    } else if (status == 21) {
        cm.sendSimple("你想要成为什么?#b\r\n#L0#剑客#l\r\n#L1#准骑士#l\r\n#L2#枪战士#l#k");
    } else if (status == 22) {
        var jobName;
        if (selection == 0) {
            jobName = "剑客";
            job = 110; // FIGHTER
        } else if (selection == 1) {
            jobName = "准骑士";
            job = 120; // PAGE
        } else {
            jobName = "枪战士";
            job = 130; // SPEARMAN
        }
        cm.sendYesNo("你真的要成为一位 #r" + jobName + "#k?");
    } else if (status == 23) {
        cm.changeJob(job);
        cm.gainItem(4031012, -1);
        cm.sendOk("转职成功!开始你的冒险之旅吧。");
        cm.dispose();
    }
}
