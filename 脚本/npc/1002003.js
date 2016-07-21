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
	NPC Name: 		Mr. Goldstein
	Map(s): 		Victoria Road : Lith Harbour (104000000)
	Description:		Extends Buddy List
*/
var status = 0;

function start() {
    cm.sendNext("我希望今天能像昨天那个生意好！你想增加好友上限么？看上去你的好友列表要爆仓了。你给我一点小费的话，我就可以帮你实现愿望。需要提醒的是，这个增加只会对这个角色起作用。你想增加好友上限么？");
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (status == 1 && mode == 0) {
            cm.sendOk("是吗……看来我的预感是错的，你好像没什么朋友啊？哈哈哈～玩笑，玩笑～如果你改变了主意，可以再来找我。等朋友多一点的时候……呵呵……");
			cm.dispose();
			return;
		}
		if (mode == 1)
            status++;
        else
            status--;
		if (status == 1)          
			cm.sendYesNo("好的！明智的决定。价格不贵，因为我下定决心，给你打了个#r大折扣#k。#b好友目录添加5名一共是25万金币#k。当然，绝不零售。只要购买一次，目录就可以永久增加。对好友目录不足的人来说，这个买卖应该不坏。怎么样？你愿意支付25万金币吗？");
		else if (status == 2) {
			var capacity = cm.getPlayer().getBuddylist().getCapacity();
			if (capacity >= 50 || cm.getMeso() < 250000) {
				cm.sendNext("你……确定自己有#b5万金币#k吗？如果有的话，请你确认一下好友目录是否已经增加到最大了。即使钱再多，好友目录的人数也无法增加到#b50个以上#k。");
			} else {
				cm.gainMeso(-250000);
				cm.getPlayer().setBuddyCapacity(capacity + 5);
				cm.sendOk("好的！你的好友目录已经增加了5个。你可以确认一下。如果好友目录还是不够的话，可以随时来找我。我可以随时帮你增加，不管多少次都行。当然不是免费的……那么再见~");
			}
			cm.dispose();
		}
	}
}