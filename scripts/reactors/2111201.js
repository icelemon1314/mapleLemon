/*
	Zakum Altar - Summons Zakum.
*/

function act() {
    rm.changeMusic("Bgm06/FinalFight");
    rm.getMap().spawnSimpleZakum( - 10, -215);
    rm.mapMessage("弱化扎昆出现了，请在规定时间范围内击败它。");
    if (!rm.getPlayer().isGM()) {
        rm.getMap().startSpeedRun();
    }
}