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
	NPC Name: 		Cloy
	Map(s): 		Victoria Road : Henesys Park (100000200)
	Description: 		Pet Master
 */
var status = -1;
var sel;

function start() {
    cm.sendNext("你……是在饲养我的孩子吗？我成功地利用#t5180000#，开发出了给玩偶注入生命的魔法。人们将像这样获得生命的孩子们叫做#b宠物#k。如果你有和宠物有关的问题，可以尽管问我。");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        if(mode == 0 && status >= 2)
            status -= 2;
        else{
            cm.dispose();
            return;
        }
    }
    if (status == 0)
		// 你想知道什么呢？\r\n#b#L0#请为我说明一下宠物。#l\r\n#L1#宠物该怎么饲养呢？#l\r\n#L2#宠物也会死吗？#l\r\n#L3#请告诉我褐色小猫、黑色小猫的命令。#l\r\n#L4#请告诉我褐色小狗的命令。#l\r\n#L5#请告诉我粉红兔子、白色兔子的命令。#l\r\n#L6#请告诉我小怪猫的命令。#l\r\n#L7#请告诉我雪犬的命令。#l\r\n#L8#请告诉我黑小猪的命令。#l\r\n#L9#请告诉我熊猫的命令。#l\r\n#L10#请告诉我恐龙王子、恐龙公主的命令。#l\r\n#L11#请告诉我云豹的命令。#l\r\n#L12#请告诉我圣诞鹿的命令。#l\r\n#L13#请告诉我猴子的命令。#l\r\n#L14#请告诉我小白雪人的命令。#l\r\n#L15#请告诉我蝙蝠怪的命令。#l\r\n#L16#请告诉我齐天大圣的命令。#l\r\n#L17#请告诉我火鸡的命令。#l\r\n#L18#请告诉我鬼灵精怪的命令。#l\r\n#L19#请告诉我刺猬的命令。#l\r\n#L20#请告诉我小蘑菇的命令。#l\r\n#L22#请告诉我小企企的命令。#l\r\n#L23#请告诉我黄金猪猪的命令。#l\r\n#L24#请告诉我啊呜啊呜的命令。#l\r\n#L25#请告诉我潜水玩具鸭的命令。#l\r\n#L26#请告诉我臭鼬的命令。#l\r\n#L27#告诉我米儿（龙龙）的命令。#l\r\n#L28#告诉我乐儿（龙龙）的命令。#l\r\n#L29#告诉我小象宠物的命令。#l\r\n#L30#告诉我威尔士柯基狗宠物的命令。#l\r\n#L31#告诉我波斯猫的命令。#l\r\n#L32##告诉我馅饼宝宝的命令。#l\r\n#L33##告诉我蛋糕宝宝的命令。#l\r\n#L34#请告诉我独角兽埃塞尔、迪埃尔的命令。#l\r\n#L49##告诉我鹦鹉啾啾的命令。#l\r\n#L50##告诉我海獭阿德里亚诺的命令。#l\r\n#L51##告诉我猩猩崩克的命令。#l\r\n#L52##告诉我龙的命令。#l\r\n#L53##告诉我火红小恶魔的命令。#l\r\n#L54##告诉我幽青小恶魔的命令。#l\r\n#L55##告诉我雷黄小恶魔的命令。#l\r\n#L35#请告诉我移动宠物能力值的方法。#l#k"
        // cm.sendSimple("你想知道哪个内容？#b\r\n#L0#告诉我更多和宠物相关的信息。#l\r\n#L1#我怎么饲养宠物？#l\r\n#L2#宠物也会死亡么？#l\r\n#L3#查看褐色和黑色小猫的命令#l\r\n#L4#查看褐色小狗的命令#l\r\n#L5#查看粉红色和白色兔子的命令#l\r\n#L6#What are the commands for Mini Kargo?#l\r\n#L7#What are the commands for Rudolph and Dasher?#l\r\n#L8#What are the commands for Black Pig?#l\r\n#L9#查看熊猫的命令#l\r\n#L10#What are the commands for Husky?#l\r\n#L11#What are the commands for Dino Boy and Dino Girl?#l\r\n#L12#What are the commands for Monkey?#l\r\n#L13#What are the commands for Turkey?#l\r\n#L14#What are the commands for White Tiger?#l\r\n#L15#What are the commands for Penguin?#l\r\n#L16#What are the commands for Golden Pig?#l\r\n#L17#What are the commands for Robot?#l\r\n#L18#What are the commands for Mini Yeti?#l\r\n#L19#What are the commands for Jr. Balrog?#l\r\n#L20#What are the commands for Baby Dragon?#l\r\n#L21#What are the commands for Green/Red/Blue Dragon?#l\r\n#L22#What are the commands for Black Dragon?#l\r\n#L23#What are the commands for Jr. Reaper?#l\r\n#L24#What are the commands for Porcupine?#l\r\n#L25#What are the commands for Snowman?#l\r\n#L26#What are the commands for Skunk?#l\r\n#L27#Please teach me about transferring pet ability points.#l");
		cm.sendSimple("你想知道哪个内容？#b\r\n#L0#告诉我更多和宠物相关的信息。#l\r\n#L1#我怎么饲养宠物？#l\r\n#L2#宠物也会死亡么？#l\r\n#L3#查看褐色和黑色小猫的命令#l\r\n#L4#查看褐色小狗的命令#l\r\n#L5#查看粉红色和白色兔子的命令#l\r\n#L6#查看小怪猫的命令#l\r\n#L7#请告诉我雪犬的命令。#l\r\n#L9#查看熊猫的命令#l\r\n");
    else if (status == 1) {
        sel = selection;
        if (selection == 0) {
            status = 3;
            cm.sendNext("所以你想知道更多宠物的信息。很久以前，我制作了一个娃娃，把它放在生命之水中后，就可以创造一个活着的玩偶，他能够很好的与人相处。");
        } else if (selection == 1) {
            status = 6;
            cm.sendNext("根据你的命令，宠物可能做出喜欢，讨厌等其它反应。如果你给与一个指令，宠物能够很好的完成，那么你们之间的亲密度就会上升。双击宠物你可以查看宠物相关信息。");
        } else if (selection == 2) {
            status = 11;
            cm.sendNext("好吧，它们并不是真正意义上的活着，在魔法失效后，它们会继续变为一个玩偶。它们是用魔力和生命之水维持着。当然，当它们活着的时候，它们就和普通的动物没有区别。");
        } else if (selection == 3)
            cm.sendNext("#r褐色和黑色小猫#k的命令是：括号内的等级代表宠物需要达到这个等级才会响应你的命令。\r\n#b坐#k (Level 1 ~ 30)\r\n#b坏#k (Level 1 ~ 30)\r\n#b笨#k (Level 1 ~ 30)\r\n#b我爱你#k (Level 1~30)\r\n#bpoop#k (Level 1 ~ 30)\r\n#btalk, say, chat#k (Level 10 ~ 30)\r\n#bcutie#k (Level 10 ~ 30)\r\n#bup, stand, rise#k (Level 20 ~ 30)");
        else if (selection == 4)
            cm.sendNext("These are the commands for #rBrown Puppy#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#b坐#k (Level 1 ~ 30)\r\n#b坏#k (Level 1 ~ 30)\r\n#bstupid, ihateyou, baddog, dummy#k (Level 1 ~ 30)\r\n#b我爱你#k (Level 1~30)\r\n#bpee#k (Level 1 ~ 30)\r\n#btalk, say, chat#k (Level 10 ~ 30)\r\n#bdown#k (Level 10 ~ 30)\r\n#bup, stand, rise#k (Level 20 ~ 30)");
        else if (selection == 5)
            cm.sendNext("These are the commands for #rPink Bunny and White Bunny#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#b坐#k (Level 1 ~ 30)\r\n#b坏#k (Level 1 ~ 30)\r\n#bup, stand, rise#k (Level 1 ~ 30)\r\n#b我爱你#k (Level 1~30)\r\n#bpoop#k (Level 1 ~ 30)\r\n#btalk, say, chat#k (Level 10 ~ 30)\r\n#bhug#k (Level 10 ~ 30)\r\n#bsleep, sleepy, gotobed#k (Level 20 ~ 30)");
        else if (selection == 6)
            cm.sendNext("These are the commands for #rMini Kargo#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#b坐#k (Level 1 ~ 30)\r\n#b坏#k (Level 1 ~ 30)\r\n#bup, stand, rise#k (Level 1 ~ 30)\r\n#b我爱你#k (Level 1~30)\r\n#bpee#k (Level 1 ~ 30)\r\n#btalk, say, chat#k (Level 10 ~ 30)\r\n#bthelook, charisma#k (Level 10 ~ 30)\r\n#bdown#k (Level 10 ~ 30)\r\n#bgoodboy, goodgirl#k (Level 20 ~ 30)");
        else if (selection == 7)
            cm.sendNext("These are the commands for #rRudolph and Dasher#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#b坐#k (Level 1 ~ 30)\r\n#b坏#k (Level 1 ~ 30)\r\n#bup, stand#k (Level 1 ~ 30)\r\n#b笨#k (Level 1 ~ 30)\r\n#bmerryxmas, merrychristmas#k (Level 1 ~ 30)\r\n#b我爱你#k (Level 1~30)\r\n#bpoop#k (Level 1 ~ 30)\r\n#btalk, say, chat#k (Level 11 ~ 30)\r\n#blonely, alone#k (Level 11 ~ 30)\r\n#bcutie#k (Level 11 ~ 30)\r\n#bmush, go#k (Level 21 ~ 30)");
        /*else if (selection == 8)
            cm.sendNext("These are the commands for #rBlack Pig#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#b坐#k (Level 1 ~ 30)\r\n#b坏#k (Level 1 ~ 30)\r\n#bpoop#k (Level 1 ~ 30)\r\n#b我爱你#k (Level 1~30)\r\n#bhand#k (Level 1 ~ 30)\r\n#b笨#k (Level 1 ~ 30)\r\n#btalk, chat, say#k (Level 10 ~ 30)\r\n#bsmile#k (Level 10 ~ 30)\r\n#bthelook, charisma#k (Level 20 ~ 30)");
        */else if (selection == 9)
            cm.sendNext("These are the commands for #rPanda#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#b坐#k (Level 1 ~ 30)\r\n#bchill, relax#k (Level 1 ~ 30)\r\n#b坏#k (Level 1 ~ 30)\r\n#bpoop#k (Level 1 ~ 30)\r\n#b我爱你#k (Level 1 ~ 30)\r\n#bup, stand, rise#k (Level 1 ~ 30)\r\n#btalk, chat, say#k (Level 10 ~ 30)\r\n#bletsplay#k (Level 10 ~ 30)\r\n#bmeh, bleh#k (Level 10 ~ 30)\r\n#bsleep#k (Level 20 ~ 30)");
        /*else if (selection == 10)
            cm.sendNext("These are the commands for #rHusky#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#b坐#k (Level 1 ~ 30)\r\n#b坏#k (Level 1 ~ 30)\r\n#bstupid, ihateyou, baddog, dummy#k (Level 1 ~ 30)\r\n#bhand#k (Level 1 ~ 30)\r\n#bpoop#k (Level 1 ~ 30)\r\n#b我爱你#k (Level 1 ~ 30)\r\n#bdown#k (Level 10 ~ 30)\r\n#btalk, chat, say#k (Level 10 ~ 30)\r\n#bup, stand, rise#k (Level 20 ~ 30)");
        else if (selection == 11)
            cm.sendNext("These are the commands for #rDino Boy and Dino Girl#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#b坐#k (Level 1 ~ 30)\r\n#bbad, no, badboy, badgirl#k (Level 1 ~ 30)\r\n#b我爱你#k (Level 1 ~ 30)\r\n#bpoop#k (Level 1 ~ 30)\r\n#bsmile, laugh#k (Level 1 ~ 30)\r\n#b笨#k (Level 1 ~ 30)\r\n#btalk, chat, say#k (Level 10 ~ 30)\r\n#bcutie#k (Level 10 ~ 30)\r\n#bsleep, nap, sleepy#k (Level 20 ~ 30)");
        else if (selection == 12)
            cm.sendNext("These are the commands for #rMonkey#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#b坐#k (Level 1 ~ 30)\r\n#brest#k (Level 1 ~ 30)\r\n#bbad, no, badboy, badgirl#k (Level 1 ~ 30)\r\n#bpee#k (Level 1 ~ 30)\r\n#b我爱你#k (Level 1 ~ 30)\r\n#bup, stand#k (Level 1 ~ 30)\r\n#btalk, chat, say#k (Level 10 ~ 30)\r\n#bplay#k (Level 10 ~ 30)\r\n#bmelong#k (Level 10 ~ 30)\r\n#bsleep, gotobed, sleepy#k (Level 20 ~ 30)");
        else if (selection == 13)
            cm.sendNext("These are the commands for #rTurkey#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#b坐#k (Level 1 ~ 30)\r\n#bno, rudeboy, mischief#k (Level 1 ~ 30)\r\n#bstupid#k (Level 1 ~ 30)\r\n#b我爱你#k (Level 1 ~ 30)\r\n#bup, stand#k (Level 1 ~ 30)\r\n#btalk, chat, gobble#k (Level 10 ~ 30)\r\n#byes, goodboy#k (Level 10 ~ 30)\r\n#bsleepy, birdnap, doze#k (Level 20 ~ 30)\r\n#bbirdeye, thanksgiving, fly, friedbird, imhungry#k (Level 30)");
        else if (selection == 14)
            cm.sendNext("These are the commands for #rWhite Tiger#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#b坐#k (Level 1 ~ 30)\r\n#bbad, no, badboy, badgirl#k (Level 1 ~ 30)\r\n#b我爱你#k (Level 1 ~ 30)\r\n#bpoop#k (Level 1 ~ 30)\r\n#brest, chill#k (Level 1 ~ 30)\r\n#b笨#k (Level 1 ~ 30)\r\n#btalk, chat, say#k (Level 10 ~ 30)\r\n#bactsad, sadlook#k (Level 10 ~ 30)\r\n#bwait#k (Level 20 ~ 30)");
        else if (selection == 15)
            cm.sendNext("These are the commands for #rPenguin#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#b坐#k (Level 1 ~ 30)\r\n#bbad, no, badboy, badgirl#k (Level 1 ~ 30)\r\n#bpoop#k (Level 1 ~ 30)\r\n#bup, stand, rise#k (Level 1 ~ 30)\r\n#b我爱你#k (Level 1 ~ 30)\r\n#btalk, chat, say#k (Level 10 ~ 30)\r\n#bhug, hugme#k (Level 10 ~ 30)\r\n#bwing, hand#k (Level 10 ~ 30)\r\n#bsleep#k (Level 20 ~ 30)\r\n#bkiss, smooch, muah#k (Level 20 ~ 30)\r\n#bfly#k (Level 20 ~ 30)\r\n#bcute, adorable#k (Level 20 ~ 30)");
        else if (selection == 16)
            cm.sendNext("These are the commands for #rGolden Pig#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#b坐#k (Level 1 ~ 30)\r\n#bbad, no, badboy, badgirl#k (Level 1 ~ 30)\r\n#bpoop#k (Level 1 ~ 30)\r\n#b我爱你#k (Level 1 ~ 30)\r\n#btalk, chat, say#k (Level 11 ~ 30)\r\n#bloveme, hugme#k (Level 11 ~ 30)\r\n#bsleep, sleepy, gotobed#k (Level 21 ~ 30)\r\n#bignore / impressed / outofhere#k (Level 21 ~ 30)\r\n#broll, showmethemoney#k (Level 21 ~ 30)");
        else if (selection == 17)
            cm.sendNext("These are the commands for #rRobot#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#b坐#k (Level 1 ~ 30)\r\n#bup, stand, rise#k (Level 1 ~ 30)\r\n#b笨#k (Level 1 ~ 30)\r\n#b坏#k (Level 1 ~ 30)\r\n#battack, charge#k (Level 1 ~ 30)\r\n#b我爱你#k (Level 1 ~ 30)\r\n#bgood, thelook, charisma#k (Level 11 ~ 30)\r\n#bspeack, talk, chat, say#k (Level 11 ~ 30)\r\n#bdisguise, change, transform#k (Level 11 ~ 30)");
        else if (selection == 18)
            cm.sendNext("These are the commands for #rMini Yeti#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#b坐#k (Level 1 ~ 30)\r\n#bbad, no, badboy, badgirl#k (Level 1 ~ 30)\r\n#bpoop#k (Level 1 ~ 30)\r\n#bdance, boogie, shakeit#k (Level 1 ~ 30)\r\n#bcute, cutie, pretty, adorable#k (Level 1 ~ 30)\r\n#b我爱你, likeyou, mylove#k (Level 1 ~ 30)\r\n#btalk, chat, say#k (Level 11 ~ 30)\r\n#bsleep, nap, sleepy, gotobed#k (Level 11 ~ 30)");
        else if (selection == 19)
            cm.sendNext("These are the commands for #rJr. Balrog#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#bliedown#k (Level 1 ~ 30)\r\n#bno|bad|badgirl|badboy#k (Level 1 ~ 30)\r\n#b我爱你|mylove|likeyou#k (Level 1 ~ 30)\r\n#bcute|cutie|pretty|adorable#k (Level 1 ~ 30)\r\n#bpoop#k (Level 1 ~ 30)\r\n#bsmirk|crooked|laugh#k (Level 1 ~ 30)\r\n#bmelong#k (Level 11 ~ 30)\r\n#bgood|thelook|charisma#k (Level 11 ~ 30)\r\n#bspeak|talk|chat|say#k (Level 11 ~ 30)\r\n#bsleep|nap|sleepy#k (Level 11 ~ 30)\r\n#bgas#k (Level 21 ~ 30)");
        else if (selection == 20)
            cm.sendNext("These are the commands for #rBaby Dragon#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#b坐#k (Level 1 ~ 30)\r\n#bno|bad|badgirl|badboy#k (Level 1 ~ 30)\r\n#b我爱你|loveyou#k (Level 1 ~ 30)\r\n#bpoop#k (Level 1 ~ 30)\r\n#bstupid|ihateyou|dummy#k (Level 1 ~ 30)\r\n#bcutie#k (Level 11 ~ 30)\r\n#btalk|chat|say#k (Level 11 ~ 30)\r\n#bsleep|sleepy|gotobed#k (Level 11 ~ 30)");
        else if (selection == 21)
            cm.sendNext("These are the commands for #rGreen/Red/Blue Dragon#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#b坐#k (Level 15 ~ 30)\r\n#bno|bad|badgirl|badboy#k (Level 15 ~ 30)\r\n#b我爱你|loveyou#k (Level 15 ~ 30)\r\n#bpoop#k (Level 15 ~ 30)\r\n#bstupid|ihateyou|dummy#k (Level 15 ~ 30)\r\n#btalk|chat|say#k (Level 15 ~ 30)\r\n#bsleep|sleepy|gotobed#k (Level 15 ~ 30)\r\n#bchange#k (Level 21 ~ 30)");
        else if (selection == 22)
            cm.sendNext("These are the commands for #rBlack Dragon#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#b坐#k (Level 15 ~ 30)\r\n#bno|bad|badgirl|badboy#k (Level 15 ~ 30)\r\n#b我爱你|loveyou#k (Level 15 ~ 30)\r\n#bpoop#k (Level 15 ~ 30)\r\n#bstupid|ihateyou|dummy#k (Level 15 ~ 30)\r\n#btalk|chat|say#k (Level 15 ~ 30)\r\n#bsleep|sleepy|gotobed#k (Level 15 ~ 30)\r\n#bcutie, change#k (Level 21 ~ 30)");
        else if (selection == 23)
            cm.sendNext("These are the commands for #rJr. Reaper#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#b坐#k (Level 1 ~ 30)\r\n#bno|bad|badgirl|badboy#k (Level 1 ~ 30)\r\n#bplaydead, poop#k (Level 1 ~ 30)\r\n#btalk|chat|say#k (Level 1 ~ 30)\r\n#b我爱你, hug#k (Level 1 ~ 30)\r\n#bsmellmyfeet, rockout, boo#k (Level 1 ~ 30)\r\n#btrickortreat#k (Level 1 ~ 30)\r\n#bmonstermash#k (Level 1 ~ 30)");
        else if (selection == 24)
            cm.sendNext("These are the commands for #rPorcupine#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#b坐#k (Level 1 ~ 30)\r\n#bno|bad|badgirl|badboy#k (Level 1 ~ 30)\r\n#b我爱你|hug|goodboy#k (Level 1 ~ 30)\r\n#btalk|chat|say#k (Level 1 ~ 30)\r\n#bcushion|sleep|knit|poop#k (Level 1 ~ 30)\r\n#bcomb|beach#k (Level 10 ~ 30)\r\n#btreeninja#k (Level 20 ~ 30)\r\n#bdart#k (Level 20 ~ 30)");
        else if (selection == 25)
            cm.sendNext("These are the commands for #rSnowman#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#b坐#k (Level 1 ~ 30)\r\n#b笨#k (Level 1 ~ 30)\r\n#bloveyou, mylove, ilikeyou#k (Level 1 ~ 30)\r\n#bmerrychristmas#k (Level 1 ~ 30)\r\n#bcutie, adorable, cute, pretty#k (Level 1 ~ 30)\r\n#bcomb, beach/坏#k (Level 1 ~ 30)\r\n#btalk, chat, say/sleep, sleepy, gotobed#k (Level 10 ~ 30)\r\n#bchang#k (Level 20 ~ 30)");
        else if (selection == 26)
            cm.sendNext("These are the commands for #rSkunk#k. The level mentioned next to the command shows the pet level required for it to respond.\r\n#b坐#k (Level 1 ~ 30)\r\n#bbad/no/badgirl/badboy#k (Level 1 ~ 30)\r\n#brestandrelax, poop#k (Level 1 ~ 30)\r\n#btalk/chat/say, 我爱你#k (Level 1 ~ 30)\r\n#bsnuggle/hug, sleep, goodboy#k (Level 1 ~ 30)\r\n#bfatty, blind, badbreath#k (Level 10 ~ 30)\r\n#bsuitup, bringthefunk#k (Level 20 ~ 30)");
        else if (selection == 27) {
            status = 14;
            cm.sendNext("In order to transfer the pet ability points, closeness and level, Pet AP Reset Scroll is required. If you take this\r\nscroll to Mar the Fairy in Ellinia, she will transfer the level and closeness of the pet to another one. I am especially giving it to you because I can feel your heart for your pet. However, I can't give this out for free. I can give you this book for 250,000 mesos. Oh, I almost forgot! Even if you have this book, it is no use if you do not have a new pet to transfer the Ability points.");
        }*/
        if(selection > 2)
            cm.dispose();
    } else if (status == 2) {
        if(sel == 0)
            cm.sendNextPrev("But Water of Life only comes out little at the very bottom of the World Tree, so I can't give him too much time in life... I know, it's very unfortunate... but even if it becomes a doll again I can always bring life back into it so be good to it while you're with it.");
        else if (sel == 1)
            cm.sendNextPrev("Talk to the pet, pay attention to it and its intimacy level will go up and eventually his overall level will go up too. As the intimacy level rises, the pet's overall level will rise soon after. As the overall level rises, one day the pet may even talk like a person a little bit, so try hard raising it. Of course it won't be easy doing so...");
        else if (sel == 2)
            cm.sendNextPrev("After some time... that's correct, they stop moving. They just turn back to being a doll, after the effect of magic dies down and Water of Life dries out. But that doesn't mean it's stopped forever, because once you pour Water of Life over, it's going to be back alive.");
        else if (sel == 27)
            cm.sendYesNo("250,000 mesos will be deducted. Do you really want to buy?");
    } else if (status == 3) {
        if (sel == 0)
            cm.sendNextPrev("哦，是的，当你给它们特殊指令它们会做出反应。你可以骂他们，爱它们...这一切都是你想如何照顾他们。它们害怕离开自己的主人，所以你一定要好好照顾它们哦！");
        else if (sel == 1){
            cm.sendNextPrev("It may be a live doll but they also have life so they can feel the hunger too. #bFullness#k shows the level of hunger the pet's in. 100 is the max, and the lower it gets, it means that the pet is getting hungrier. After a while, it won't even follow your command and be on the offensive, so watch out over that.");
            return;
        }else if (sel == 2)
            cm.sendNextPrev("Even if it someday moves again, it's sad to see them stop altogether. Please be nice to them while they are alive and moving. Feed them well, too. Isn't it nice to know that there's something alive that follows and listens to only you?");
        else if (sel == 27){
            if (cm.getMeso() < 250000 || !cm.canHold(4160011))
                cm.sendOk("Please check if your inventory has empty slot or you don't have enough mesos.");
            else {
                cm.gainMeso(-250000);
                cm.gainItem(4160011, 1);
            }
            cm.dispose();
        }
    } else if (status == 4){
        if(sel != 1)
            cm.dispose();
        cm.sendNextPrev("Oh yes! Pets can't eat the normal human food. Instead my disciple #bDoofus#k sells #bPet Food#k at the Henesys Market so if you need food for your pet, find Henesys. It'll be a good idea to buy the food in advance and feed the pet before it gets really hungry.");
    } else if (status == 5)
        cm.sendNextPrev("Oh, and if you don't feed the pet for a long period of time, it goes back home by itself. You can take it out of its home and feed it but it's not really good for the pet's health, so try feeding him on a regular basis so it doesn't go down to that level, alright? I think this will do.");
    else if (status == 6)
        cm.dispose();
}