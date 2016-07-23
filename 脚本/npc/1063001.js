/* NPC : A pile of blue flower
 * Location : Sleepywood, forest of patient
 */

var status = 0;
var itemSet = new Array(4020005, 4020006, 4020004, 4020001, 4020003, 4020000, 4020002);
var rand = Math.floor(Math.random() * itemSet.length);


function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
		return;
    }
	if (status == 1 && mode == 0) {
		cm.sendOk("小小奖励！");
		cm.dispose();
		return;
	}
    if (mode == 1) {
		status++;
    } else {
		status--;
    }
    if (status == 0) {
		if (cm.getQuestStatus(1000901) == 1 && !cm.haveItem(4031026)) {
			cm.gainItem(4031026, 5);
		} else {
			cm.gainItem(itemSet[rand], 2);
		}
		cm.warp(105000000);
		cm.dispose();
    }
}