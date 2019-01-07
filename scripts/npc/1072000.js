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
    战士转职教官
    NpcId:1072000
    MapId:102020300
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
             cm.sendOk("嗯..那就等你准备好后再来找我吧。");  
             cm.dispose();
             return;
           }else{
              status--;
           }
        }else{
           status--; 
        }
        
        if(cm.getJobId()==100){
            if(cm.haveItem(4031012)){//已通过转职官测试的战士
                cm.sendOk("恭喜你通过了我的考验，现在你可以拿着#t4031012#回部落找#p1022000#完成转职了。");  
                cm.dispose();                 
            }else if (cm.haveItem(4031008)) {//拥有武术教练的信件而没有通过转职官测试的战士
                if (status == 0)
                        cm.sendNext("嗯...这么说是#b#p1022000##k叫你来的？好吧，如果你能通过我的考验，我就给你进入下一职业阶段的资格证明。")
                else if (status == 1)
                        cm.sendNext("先来告诉你该怎么做，一会儿测试时你会被传送到一个隐藏的考验场，在那里你将会见到许多外表类似于平常怪物、但实力却有所不同的特殊怪物。");
                else if (status == 2)
                        cm.sendNext("而要通过测试你需要清除考验场中的特殊怪物，并从这些怪物身上收集#b30个#k的#b#t4031013##k，收集完后交给我，我就可以给你测试通过的证明。我想以你的实力，这应该不算太难吧？");
                else if (status == 3){
                        cm.sendYesNo("要提醒你的是，一旦进入考验场你将无法中途离开，除非中途死亡或掉线。而如果在测试中途死亡，经验值同样会减少，所以你应该在进考验场前做好准备。那么现在你是否已准备好进考验场测试了？");   
                        mode0_pattern=1;
                }else if (status == 4)
                        cm.sendNext("好，如果你认为已经准备好了，那我们就进入考验场开始测试吧，别忘了完成测试你要收集的东西。");
                else if (status == 5) {
                        cm.warp(108000300, 0);
                        cm.dispose();
                }
            }else{//没有武术教练的信件也没有通过转职官测试的战士
                    cm.sendOk("想转到战士第二阶段职业吗? 首先去拜访勇士部落的武术教练。");
                    cm.dispose();
            }
        }
		else{
                    cm.sendOk("只有通过我的考试的人才能转职到第二阶段。");
		    cm.dispose();
                }
    }
}	