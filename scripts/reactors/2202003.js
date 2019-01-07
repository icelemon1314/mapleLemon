function act() {
    rm.dropItems();
    var eim = rm.getPlayer().getEventInstance();
    if (eim != null) {
        var newp = parseInt(eim.getProperty("stage2")) + 1;
        if (newp <= 10) {
            eim.setProperty("stage2", newp);
            rm.getMap().startSimpleMapEffect("You have collected " + newp + " passes.", 5120018);
        }
    }
}