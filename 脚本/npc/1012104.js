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
/* Brittany
	Henesys Random Hair/Hair Color Change.
*/
var status = 0;
var beauty = 0;
var mhair = Array(30310, 30330, 30060, 30150, 30210, 30140, 30120, 30200);
var fhair = Array(31150, 31310, 31300, 31160, 31100, 31030, 31080, 31070);
var hairnew = Array();
var hairCard = 4050000;
var hairColorCard = 4051000;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            cm.sendSimple("我是总监的助理。如果你有普通会员卡我可以帮你随机弄一个头发，你想现在就打理头发么？\r\n#L0#发型设计: #i"+hairCard+"##t"+hairCard+"##l\r\n#L1#头发染色: #i"+hairColorCard+"##t"+hairColorCard+"##l");
        } else if (status == 1) {
            if (selection == 0) {
                beauty = 0;
                hairnew = Array();
                if (cm.getPlayer().getGender() == 0) {
                    for(var i = 0; i < mhair.length; i++)
                        hairnew.push(mhair[i] + parseInt(cm.getPlayer().getHair()% 10));
                }
                if (cm.getPlayer().getGender() == 1) {
                    for(var i = 0; i < fhair.length; i++)
                        hairnew.push(fhair[i] + parseInt(cm.getPlayer().getHair() % 10));
                }
                cm.sendYesNo("如果你有普通会员卡，我可以随机帮你做一个发型，你确定你有#b#t"+hairCard+"##k 么？");
            } else if (selection == 1) {
                beauty = 1;
                haircolor = Array();
                var current = parseInt(cm.getPlayer().getHair()/10)*10;
                for(var i = 0; i < 8; i++)
                    haircolor.push(current + i);
                cm.sendYesNo("如果你使用普通会员卡，那么你的头发颜色将会随机变化。你确定你有 #b#t"+hairColorCard+"##k ？");
            }
        }
        else if (status == 2){
            if (beauty == 0){
                if (cm.haveItem(hairCard)){
                    cm.gainItem(hairCard, -1);
                    cm.setHair(hairnew[Math.floor(Math.random() * hairnew.length)]);
                    cm.sendOk("是不是对新发型比较满意呢？");
                } else {
                    cm.sendOk("嗯哼...看上去你没有普通会员卡...我没有办法为你提供服务. 你可以去商城中购买...我能帮到你的就这些了...");
                }
            }
            if (beauty == 1){
                if (cm.haveItem(hairColorCard)){
                    cm.gainItem(hairColorCard, -1);
                    cm.setHair(haircolor[Math.floor(Math.random() * haircolor.length)]);
                    cm.sendOk("是不是对新颜色比较满意呢？");
                } else {
                    cm.sendOk("嗯哼...看上去你没有普通会员卡...我没有办法为你提供服务. 你可以去商城中购买...我能帮到你的就这些了...");
                }
            }
			cm.dispose();

        }
    }
}
