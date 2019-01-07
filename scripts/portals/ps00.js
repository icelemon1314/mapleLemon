/*
    Zakum Entrance
	author:icelemon1314
*/

function enter(pi) {
	
	if (pi.getQuestStatus(100200) != 2) {
		pi.playerMessage(5, "没有完成任务不能进去。");
		return false;
    } else if (!pi.haveItem(4001017)) {
		pi.playerMessage(5, "没有火焰的眼不能进去。");
		return false;
    }

    pi.warp(280030000);
    return true;
}