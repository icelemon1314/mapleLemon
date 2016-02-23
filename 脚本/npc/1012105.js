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
/* Ms. Tan
	Henesys Skin Change.
*/
var status = 0;
var skin = Array(0, 1, 2, 3, 4);

function start() {
    cm.sendSimple("你好! 欢迎来到射手村护肤中心！你想像我一样拥有光滑、细嫩的肌肤么？如果你有 #b#i4053000##k, 我就可以帮你实现你的愿望！#l\r\n\#L1#我已经有会员卡#l");
}

function action(mode, type, selection) {
    if (mode < 1)
        cm.dispose();
    else {
        status++;
        if (status == 1)
            cm.sendStyle("通过我们的特殊机器，你可以先看到我们护理后的效果，你喜欢哪种效果呢？赶紧来选一个吧！", skin);
        else {
            if (cm.haveItem(4053000)){
                cm.gainItem(4053000, -1);
                cm.setSkin(selection);
                cm.sendOk("对自己的皮肤还满意么？");
            } else {
                cm.sendOk("嗯哼...你没有我们中心的会员卡，我不能给你提供服务，赶紧去商城购买吧！");
			}
            cm.dispose();
        }
    }
}
