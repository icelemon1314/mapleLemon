/*
	Computer - Premium road : Kerning City Internet Cafe
*/

var maps = Array(103000000, 190000000, 191000000, 192000000, 195000000);

function start() {
    var selStr = "你想去哪个地图？#b";
    for (var i = 0; i < maps.length; i++) {
        selStr += "\r\n#L" + i + "##m" + maps[i] + "# " + (i >= 1 ? (i == (maps.length - 1) ? ("(Level " + (i * 5 - 4) + "+)") : ("(Levels " + (i * 5 - 4) + " to " + (i * 5) + ")")) : "") + "#l";
    }
    cm.sendSimple(selStr);
}

function action(mode, type, selection) {
    if (mode == 1) {
        cm.warp(maps[selection], 0);
    }
    cm.dispose();
}