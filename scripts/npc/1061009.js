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
/* Door of Dimension
	Enter 3rd job event
*/

function start() {
    if (cm.isQuestStarted(100101) && !cm.haveItem(4031059)) {
        var em = cm.getEventManager("3rdjob");
        if (em == null) {
            cm.sendOk("事件出错了，赶紧找Alex看看！");
			cm.dispose();
        } else {
            em.newInstance(cm.getPlayer().getName()).registerPlayer(cm.getPlayer());
		}
    } else {
		cm.sendOk("你找我有事么？");
	}
    cm.dispose();
}