/*
	Zakum Altar - Summons Zakum.
*/

function act() {
    rm.changeMusic("Bgm06/FinalFight");
    rm.getMap().spawnChaosZakum( - 10, -215);
    rm.mapMessage("进阶扎昆出现了，请在规定时间范围内击败它。");
    if (!rm.getPlayer().isGM()) {
        rm.getMap().startSpeedRun();
    }
}