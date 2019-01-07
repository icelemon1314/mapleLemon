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

/* 
    弓箭手转职教官
    NpcId: 1072002
    MapId: 106010000
*/

var status;
var mode0_pattern=0;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (mode == 1){
           status++;
           if(mode0_pattern !=0)
              mode0_pattern=0;
        }else if(mode==0){
           if(mode0_pattern == 1){
               mode0_pattern=0;
               cm.sendOk("好吧，那你做好准备后再来找我。");  
               cm.dispose();
               return;
           }else{
               status--;
           }
        }else{
           status--; 
        }
        
        if(cm.getJobId()==300){
            if (cm.haveItem(4031012)){//已经通过测试拿到英雄证书的弓箭手
                //if(status==0){
                   cm.sendOk("你已经顺利通过了我的考验，快拿着得到的#t4031012#去找#p1012100#完成转职吧。");  
                   cm.dispose();
                //}              
            }else if (cm.haveItem(4031010)) {//拥有赫丽娜的信但还未通过测试的弓箭手
			if (status == 0)
				cm.sendNext("噢，这不是#p1012100#写的介绍信吗！是她指示你带这封信来见我的？");
			else if (status == 1)
				cm.sendNext("嗯。。。所以说你是想通过我的测试来获得进入下一个职业阶段的资格，从而进一步提高自己的能力？很好。");
            //cm.sendNextPrev("一会儿开始测试后我会先将你传送到一个隐藏的地图，你将在那里见到许多你在外面无法遇到的怪物。尽管在外表上这些怪物可能会与你曾经遇到过的怪物十分相似，但是他们的实力会和你曾经遇到过的怪物有所不同。");
			else if (status == 2)
				cm.sendNext("好吧如果你真的准备好了，我可以给你这样一个机会。听好，等会进行测试时我会将你传送到一个隐藏的地图，在里面你会见到许多令你感到似曾相识的怪物，然而事实上这些怪物的实力与你平时遇到的怪物是有所不同的。");
			else if (status == 3)
                                cm.sendNext("而若想通过测试你需要清除测试地图中的怪物并收集#b30个#k从怪物身上掉落的#b#t4031013##k，并在收集完后把它们交给我。而如果你真的已经具备了进入下一阶段职业的实力，我想这应该不难做到吧。");
			else if (status == 4){
                                cm.sendYesNo("另外还要提醒你的是，一旦进入测试场地后，除非中途死亡或下线，否则直到测试完成前你都无法中途离开测试场地。因此在决定进入测试场地前请确保你已做好充足的准备。那么，你现在是否已经准备好开始测试了呢？");
                                mode0_pattern=1;
                        }else if (status == 5)
				cm.sendNext("好吧，既然你已经准备好了，我这就把你传送到测试场地，别忘了完成测试需要收集的东西。");
			else if (status == 6) {
				cm.warp(108000100, 0);
				cm.dispose();
			}
		}else{//没有赫丽娜的信件的弓箭手
			cm.sendOk("想转到弓箭手第二阶段职业吗? 先去拜访射手村的#p1012100#吧。");
			cm.dispose();
                }
        }else{//非弓箭手职业
            cm.sendOk("只有通过我的试验的人才能够转职到第二阶段。");
            cm.dispose();
        }
    }
}	