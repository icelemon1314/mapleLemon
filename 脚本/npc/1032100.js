/**
	Arwen the Fairy - Victoria Road : Ellinia (101000000)
**/

var status = 0;
var item;
var selected;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (status == 1 && mode == 0) {
        cm.dispose();
        return;
    } else if (status == 2 && mode == 0) {
        cm.sendNext(item + "是很难做的，你先去准备材料吧。");
        cm.dispose();
        return;
    }
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {
        cm.sendNext("对！我是妖精族中的炼金术专家... 我们妖精族自古以来就被禁止跟人类接触，但是像你这么强壮的人没关系。你给我材料，我帮你做特别的东西。");
    } else if (status == 1) {
        cm.sendSimple("你想做什么样的东西呢？\r\n#b#L0##t4011007##l\r\n#L1##t4021009##l\r\n#L2##t4031042##l");
    } else if (status == 2) {
        selected = selection;
        if (selection == 0) {
            item = "#t4011007#";
            cm.sendYesNo("你想做#t4011007#？做那个东西需要冶炼的#b#t4011000#, #t4011001#, #t4011002#, #t4011003#, #t4011004#, #t4011005#, #t4011006##k各一个。还需要10000金币。");
        } else if (selection == 1) {
            item = "#t4021009#";
            cm.sendYesNo("你想做#t4021009#？做那个东西，需要各1个#b#t4021000#, #t4021001#, #t4021002#, #t4021003#, #t4021004#, #t4021005#, #t4021006#, #t4021007#, #t4021008##k，还需要15000金币。");
        } else if (selection == 2) {
            item = "#t4031042#";
            cm.sendYesNo("你想做#t4031042#？做那个东西，需要1个#b#t4001006#和1个#t4011007#和1个#t4021008##k，还需要30000金币。啊！这个翼毛是非常特别的，如果它掉在地上，它就会消失，就不能给别人。");
        }
    } else if (status == 3) {
        if (selected == 0) {
            if (cm.haveItem(4011000) && cm.haveItem(4011001) && cm.haveItem(4011002) && cm.haveItem(4011003) && cm.haveItem(4011004) && cm.haveItem(4011005) && cm.haveItem(4011006) && cm.getMeso() > 10000) {
                cm.gainMeso( - 10000);
                cm.gainItem(4011000, -1);
                cm.gainItem(4011001, -1);
                cm.gainItem(4011002, -1);
                cm.gainItem(4011003, -1);
                cm.gainItem(4011004, -1);
                cm.gainItem(4011005, -1);
                cm.gainItem(4011006, -1);
                cm.gainItem(4011007, 1);
                cm.sendNext("拿着" + item + "。 看上去非常的棒。看来你的材料还是挺不错的。欢迎下次光临。");
            } else {
                cm.sendNext("你是不是钱不够？还是你没有冶炼的#b#t4011000#, #t4011001#, #t4011002#, #t4011003#, #t4011004#, #t4011005#, #t4011006##k各一个？");
            }
        } else if (selected == 1) {
            if (cm.haveItem(4021000) && cm.haveItem(4021001) && cm.haveItem(4021002) && cm.haveItem(4021003) && cm.haveItem(4021004) && cm.haveItem(4021005) && cm.haveItem(4021006) && cm.haveItem(4021007) && cm.haveItem(4021008) && cm.getMeso() > 15000) {
                cm.gainMeso( - 15000);
                cm.gainItem(4021000, -1);
                cm.gainItem(4021001, -1);
                cm.gainItem(4021002, -1);
                cm.gainItem(4021003, -1);
                cm.gainItem(4021004, -1);
                cm.gainItem(4021005, -1);
                cm.gainItem(4021006, -1);
                cm.gainItem(4021007, -1);
                cm.gainItem(4021008, -1);
                cm.gainItem(4021009, 1);
                cm.sendNext("拿着" + item + "。 看上去非常的棒。看来你的材料还是挺不错的。欢迎下次光临。");
            } else {
                cm.sendNext("你是不是钱不够？还是你没有冶炼的各1个#b#t4021000#, #t4021001#, #t4021002#, #t4021003#, #t4021004#, #t4021005#, #t4021006#, #t4021007#, #t4021008##k？");
            }
        } else if (selected == 2) {
            if (cm.haveItem(4001006) && cm.haveItem(4011007) && cm.haveItem(4021008) && cm.getMeso() > 30000) {
                cm.gainMeso( - 30000);
                cm.gainItem(4001006, -1);
                cm.gainItem(4011007, -1);
                cm.gainItem(4021008, -1);
                cm.gainItem(4031042, 1);
                cm.sendNext("拿着" + item + "。 看上去非常的棒。看来你的材料还是挺不错的。欢迎下次光临。");
            } else {
                cm.sendNext("你是不是钱不够？还是你没有1个#b#t4001006#和1个#t4021007##k和1个#t4021008##k吗？");
            }
        }
        cm.dispose();
    }
}