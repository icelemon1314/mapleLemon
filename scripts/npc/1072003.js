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
    飞侠转职教官
    NpcId: 1072003
    MapId: 102040000
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
               cm.sendOk("那请做好准备后再来找我。");  
               cm.dispose();
               return;
           }else if(mode0_pattern == 2){
               mode0_pattern=0;
               cm.sendOk("还有什么问题吗？");  
               cm.dispose();
               return;
           }else{
               status--;
           }
        }else{
           status--; 
        }
        
        if(cm.getJobId()==400){//如果玩家当前职业为飞侠
            if (cm.haveItem(4031012)){//已经通过测试拿到英雄证书的飞侠
                //if(status==0){
                   cm.sendOk("你已经通过了我的考核，快拿着我给你的#t4031012#找#p1052001#完成转职吧。");  
                   cm.dispose();
                //}              
            }else if (cm.haveItem(4031011)) {//持有达鲁克的信但还未通过测试的飞侠
                    if (status == 0)
                            cm.sendNext("嗯，是#p1052001#叫你来见我的？");
                    else if (status == 1){
                            cm.sendYesNo("想要获得我的认可得先通过我的测试才行，你准备好了吗？");
                            mode0_pattern=1;
                    }else if (status == 2)
                            cm.sendNext("好，听着，等会进行测试时我会把你传送到一个隐藏的测试场地，在里面你将见到许多外表上与你平时所见怪物相似，但实力不同于一般怪物的怪物。而你要做的是清除这些怪物并收集从它们身上掉落的#b#t4031013##k，收集到#b30个#k交给我就算你通过考核，怎么样，很简单吧？");
                    else if (status == 3){
                            //cm.sendNext("而若想通过测试你需要清除测试场地中的怪物并收集#b30个#k从怪物身上掉落的#b#t4031013##k，并在收集完后把它们交给我。而如果你真的已经具备了进入下一阶段职业的实力，我想这应该不难做到吧。");
                            cm.sendNext("另外要提醒你的是，一旦开始测试，除非中途死亡或掉线，你将不能中途离开测试场地，而若在测试中死亡，经验值依旧会减少并且死亡后将需要重新接受测试。因此你应该在测试前就做好充分的准备。");
                    }else if (status == 4){
                            cm.sendYesNo("关于测试要说的就这么多，那么，你是否想现在就开始测试？");
                            mode0_pattern=2;
                    }else if (status == 5)
                            cm.sendNext("好，如果没有别的问题了，接下来我就把你送到测试场地开始测试，记住完成测试需要收集的东西。");
                    else if (status == 6) {
                            cm.warp(108000400, 0);
                            cm.dispose();
                    }
		}else{//没有达鲁克的信件的飞侠
			cm.sendOk("想转到飞侠第二阶段职业吗? 先去拜访废弃之都的‘达克鲁’先生吧。");
			cm.dispose();
                }
        }else{//非飞侠职业
            cm.sendOk("只有通过我的试验的人才能够转职到第二阶段。");
            cm.dispose();
        }
    }
}	