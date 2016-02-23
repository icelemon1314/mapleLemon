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
/* Denma the Owner
	Henesys VIP Eye Change.
*/
var status = 0;
var beauty = 0;
var mface = Array(20000, 20001, 20002, 20003, 20004, 20005, 20006, 20007, 20008);
var fface = Array(21000, 21001, 21002, 21003, 21004, 21005, 21006, 21007, 21008);
var facenew = Array();
var faceCard = 4052001;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            cm.sendSimple("你好，欢迎来到射手村整容院！你想让你的脸型变成其它样子么？如果你有#b#i"+faceCard+"##k, 我们可以把你的脸弄成你想要的样子！#L1#我已经有会员卡#l");
        } else if (status == 1) {
            if (selection == 1) {
                facenew = Array();
                if (cm.getPlayer().getGender() == 0) {
                    for(var i = 0; i < mface.length; i++)
                        facenew.push(mface[i] + cm.getPlayer().getFace()% 1000 - (cm.getPlayer().getFace()% 100));
                }
                if (cm.getPlayer().getGender() == 1) {
                    for(var i = 0; i < fface.length; i++)
                        facenew.push(fface[i] + cm.getPlayer().getFace()% 1000 - (cm.getPlayer().getFace()% 100));
                }
                cm.sendStyle("让我看看...我可以改变你的脸型，你想要试试么？赶紧来选一个你喜欢的脸型吧！", facenew);
            }
        }
        else if (status == 2){
            if (cm.haveItem(faceCard)){
                cm.gainItem(faceCard, -1);
                cm.setFace(facenew[selection]);
                cm.sendOk("嗯，看来我的手艺依旧独领风骚！");
            } else {
                cm.sendOk("嗯哼...看上去你没有高级会员卡...我没有办法为你提供服务. 你可以去商城中购买...我能帮到你的就这些了...");
			}
			cm.dispose();
        }
    }
}
