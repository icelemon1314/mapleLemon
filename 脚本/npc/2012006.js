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
var status = -1;
var sel;

function start() {
    cm.sendNext("欢迎你的到来, 我是#p2012006#。\r\n你已经买好船票了么？");
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.dispose();
        return;
    }
    status++;
    if (status == 0)
		cm.sendSimple("你想去哪个站台？\r\n\r\n#b#L0#去往魔法密林飞船的站台#k#l");
    else if (status == 1) {
        cm.sendNext("好的, 我将送你到站台上： #m" + 200000110 + "#");
    } else if(status == 2){
        cm.warp(200000110);
        cm.dispose();
    }
}