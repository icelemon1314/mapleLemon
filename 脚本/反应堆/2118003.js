function act() {
    rm.getReactor().forceTrigger();
    rm.getReactor().delayedDestroyReactor(1000);
    rm.mapMessage("莱格斯出现了。");
    rm.spawnMonster(9300281);
}