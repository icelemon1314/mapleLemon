/*
	Papulatus Reactor: Performs the Papulatus commands
*/

function act() {
    try {
        rm.mapMessage(5, "时间裂缝已经被<裂缝碎块>填充了");
        rm.changeMusic("Bgm09/TimeAttack");
        rm.spawnMonster(8500000, -410, -400);
        rm.getMap(220080000).setReactorState();
    } catch(e) {
        rm.mapMessage(5, "错误: " + e);
    }
}