
var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1)
		status++;
    else
		status--;
    if (status == 0) {
		cm.sendNext("欢迎来到冒险岛的世界，时光倒流，让我们一起来怀念昔日的冒险岛！");
    } else if (status == 1) {
		//cm.startQuest(1000);
		cm.sendNext("好吧，话不多说，你懂的，赶紧去前面看看吧！");
    } else if (status == 2) {
		cm.dispose();
    }
}