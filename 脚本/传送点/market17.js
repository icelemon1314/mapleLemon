//SPECIAL: TODO LEGENDS
function enter(pi) {
    if (pi.getMapId() == 120000200 && pi.getPortal().getName().equals("bi01")) {
        pi.warp(pi.isQuestFinished(2568) ? 912060500 : 120000202, 0);
    } else {
        pi.inFreeMarket();
    }
}