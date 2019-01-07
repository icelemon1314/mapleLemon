/* 
	任务: 比格斯的物品收集
	author：icelemon1314
*/

var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            qm.sendNext("如果你能给我某些东西，我可以给你一样神器，你想要么？");
        } else if (status == 1) {
            qm.sendNext("你帮我收集10个花蘑菇的盖和30个蓝蜗牛的壳，赶紧去吧，骚年！");
        } else if (status == 2) {
			if (qm.canCompleteQuest()) {
				qm.forceCompleteQuest();
				qm.sendOk("给你的东西收好了么？");
            } else {
				//qm.gainItem(4000001,10);
				//qm.gainItem(4000000,30);
				qm.sendOk("你还没有获得我需要的道具！");
			}
			
			qm.dispose();
        }
    }
}