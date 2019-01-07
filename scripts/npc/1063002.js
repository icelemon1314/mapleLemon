/* NPC : A pile of white flower
 * Location : Sleepywood, forest of patient
 */

var itemSet = new Array(4020007, 4020008, 4010006);
var rand = Math.floor(Math.random() * itemSet.length);

function action(mode, type, selection) {
    if (mode == 1) {
		if (cm.getQuestStatus(1000902) == 1 && !cm.haveItem(4031026)) {
			cm.gainItem(4031026, 5);
		} else {
			cm.gainItem(itemSet[rand], 2);
		}
		cm.warp(105000000);
    }
    cm.dispose();
}