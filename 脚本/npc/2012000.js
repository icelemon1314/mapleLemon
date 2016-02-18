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

var status = 0;
var ticket = new Array(4031047);
var cost = new Array(5000); // 囧，单个值会有bug
var mapNames = new Array("开往魔法密林的船票（一般）");
var mapName2 = new Array("魔法密林");
var select;

function start() {
    var where = "你好，我负责出售开往各大陆飞船的船票。你想买哪种船票呢？\r\n#L0##b开往魔法密林的船票（一般）#k#l";
    //for (var i = 0; i < ticket.length; i++) {
        //where += "\r\n#L" + i + "##b" + mapNames[i] + "#k#l";
	//}
    cm.sendSimple(where);
}

function action(mode, type, selection) {
    if(mode < 1) {
        cm.dispose();
    } else {
        status++;
        if (status == 1) {
            cm.sendYesNo("开往" + mapName2[selection] +"的飞船每隔 " + (selection == 0 ? 15 : 10) +"分钟一班,从整点开始发船,你需要支付#b"+5000+"金币#k。你确定要购买#b#t"+4031047+"##k?");
        } else if(status == 2) {
            if (cm.getMeso() < 5000 || !cm.canHold(4031047))
                cm.sendOk("你确定你有 #b"+5000+" 金币#k? 如果是，请确认背包空间是否足够。");
            else {
                cm.gainMeso(-5000);
                cm.gainItem(4031047,1);
            }
            cm.dispose();
        }
    }
}
