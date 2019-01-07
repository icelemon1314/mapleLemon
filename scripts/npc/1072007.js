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
    NpcId:1072007
    MapId:108000400
*/

var status;

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
        }else{
            status--;
        }
        
        if(status==0){
            if(cm.haveItem(4031013,30)){
                cm.sendNext("你已经收集到30个黑珠了么，嗯，很好！作为测试通过的证明我将给你#b#t4031012##k，带着它回去找#p1052001#，#p1052001#会允许你进入下一个职业阶段。");
            }else{
                cm.sendOk("记住你需要收集到#b30个#k从怪物身上掉落的#b#t4031013##k才能通过考核，再去加油吧，祝你好运！")
                cm.dispose();   
           }    
        }else if(status==1){
                cm.removeAll(4031013);
                cm.gainItem(4031011, -1);
                cm.gainItem(4031012,1);  
                cm.warp(102040000, 0);
                cm.dispose();
        }          
    }
          
}


/*
function start() {
    if (cm.haveItem(4031013,30)) {
        cm.sendNext("你已经收集到30个黑珠了？那好吧，你已通过了我的测试，我将给你#t4031012#，拿着它去找汉斯吧。");
    } else {
	cm.gainItem(4031013,30);
        cm.sendOk("为完成测试你需要清除地图中的怪物并收集#b30个#t4031013##k。祝你好运！")
        cm.dispose();
    }
}

function action(mode, type, selection) {
    if (mode == 1) {
                cm.warp(101020000, 0);
                //cm.sendOk("既然你已完成测试，我将拿走你身上所有的#t4031013#以及汉斯给你的信件。")
		cm.removeAll(4031013);
		cm.gainItem(4031009, -1);
		cm.gainItem(4031012,1);
	}
	cm.dispose();
}*/