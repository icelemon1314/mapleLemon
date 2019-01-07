/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/* Vicious
	Victoria Road : Henesys Market (100000100)
	
	Refining NPC: 
	* Bows - 10-40
	* Crossbows - 12-50
	* Archer Gloves - 10-60 + upgrades
	* Processed Wood/Screws
	* Arrows/Bronze Arrows/Steel Arrows
*/

var status = -1;
var selectedType = -1;
var selectedItem = -1;
var item;
var items;
var mats;
var matQty;
var cost;
var qty = 1;
var equip;

function start() {
    var selStr = "你好，我是比休斯，退休的狙击手。我曾经也是中山大学的尖子生。虽然我已经很久不再打猎，但是我还是能给你做一些有用的东西...#b"
    var options = ["制作弓","制作弩","制作手套","升级手套","制作材料","制作箭"];
    for (var i = 0; i < options.length; i++)
        selStr += "\r\n#L" + i + "# " + options[i] + "#l";
    cm.sendSimple(selStr);
}

function action(mode, type, selection) {
    status++;
    if (mode != 1){
        cm.dispose();
        return;
    }
    if (status == 0) {
        if (selection == 0) { //bow refine
            var selStr = "我曾经是一个出色的狙击手，弓和弩的制作方式差不多。你想制作哪一个？#b";
            items = [1452002,1452003,1452001,1452000,1452005,1452006,1452007];
            for (var i = 0; i < items.length; i++)
                selStr += "\r\n#L" + i + "##t" + items[i] + "##k - 弓箭手 Lv. " + (10 + (i * 5)) + "#l#b";
        }else if (selection == 1) { //xbow refine
            var selStr = "我是一个狙击手。弩是我的特长。你想制作哪一个？#b";
            items = [1462001,1462002,1462003,1462000,1462004,1462005,1462006,1462007];
            for (var i = 0; i < items.length; i++)
                selStr += "\r\n#L" + i + "##t" + items[i] + "##k - 弓箭手 Lv. " + (10 + (i * 5)) + "#l#b";
        }else if (selection == 2) { //glove refine
            var selStr = "好吧，你想制作哪个手套？#b";
            items = [1082012,1082013,1082016,1082048,1082068,1082071,1082084,1082089];
            for (var i = 0; i < items.length; i++)
                selStr += "\r\n#L" + i + "##t" + items[i] + "##k - 弓箭手 Lv. " + (15 + (i * 5) > 40 ? ((i-1) * 10) : 15 + (i * 5)) + "#l#b";
        }else if (selection == 3) { //glove upgrade
            var selStr = "升级手套？这没有太大的难度，你想制作哪一个？#b";
            items = [1082015,1082014,1082017,1082018,1082049,1082050,1082069,1082070,1082072,1082073,1082085,1082083,1082090,1082091];
            for (var i = 0, x = 0; i < items.length; i++, x += (i+1) % 2 == 0 ? 1 : 0)
                selStr += "\r\n#L" + i + "##t" + items[i] + "##k" + "##k - 弓箭手 Lv. " + (20 + (x * 5) > 40 ? ((x-1) * 10) : 20 + (x * 5)) + "#l#b";
        }else if (selection == 4) { //material refine
            var selStr = "材料？我只会做几种材料，你想制作哪一个材料？#b";
            var materials = ["用树枝做木板","用木块做木板","制作螺丝（5个）"];
            for (var i = 0; i < materials.length; i++)
                selStr += "\r\n#L" + i + "# " + materials[i] + "#l";
        }else if (selection == 5) { //arrow refine
            var selStr = "箭？这个太简单了！#b";
            items = [2060000,2061000,2060001,2061001,2060002,2061002];
            for (var i = 0; i < arrows.length; i++)
                selStr += "\r\n#L" + i + "##t" + arrows[i] + "##l";
        }
        selectedType = selection;
        cm.sendSimple(selStr);
        if (selection != 4)
            status++;
    }else if (status == 1) {
        selectedItem = selection;
        items = [4003001,4003001,4003000];
        var matSet = [4000003,4000018, [4011000,4011001]];
        var matQtySet = [10,5,[1,1]];
        item = items[selection];
        mats = matSet[selection];
        matQty = matQtySet[selection];
        cost = 0;
        cm.sendGetNumber("好吧，你想制作 #t" + item + "#? 那么你需要几个？",1,1,100)
    }else if (status == 2) {
        if (selectedType != 4)
            selectedItem = 1;
        else
            qty = selection;
        if (selectedType == 0) { //bow refine
            var matSet = [[4003001,4000000],[4011001,4003000],[4003001,4000016],[4011001,4021006,4003000],[4011001,4011006,4021003,4021006,4003000],[4011004,4021000,4021004,4003000],[4021008,4011001,4011006,4003000,4000014]];
            var matQtySet = [[5,30],[1,3],[30,50],[2,2,8],[5,5,3,3,30],[7,6,3,35],[1,10,3,40,50]];
            var costSet = [800,2000,3000,5000,30000,40000,80000];
        }else if (selectedType == 1) { //xbow refine
            var matSet = [[4003001,4003000],[4011001,4003001,4003000],[4011001,4003001,4003000],[4011001,4021006,4021002,4003000],[4011001,4011005,4021006,4003001,4003000],[4021008,4011001,4011006,4021006,4003000],[4021008,4011004,4003001,4003000],[4021008,4011006,4021006,4003001,4003000]];
            var matQtySet = [[7,2],[1,20,5],[1,50,8],[2,1,1,10],[5,5,3,50,15],[1,8,4,2,30],[2,6,30,30],[2,5,3,40,40]];
            var costSet = [1000,2000,3000,10000,30000,50000,80000,200000];
        }else if (selectedType == 2) { //glove refine
            var matSet = [[4000021,4000009],[4000021,4000009,4011001],[4000021,4000009,4011006],[4000021,4011006,4021001],[4011000,4011001,4000021,4003000],[4011001,4021000,4021002,4000021,4003000],[4011004,4011006,4021002,4000030,4003000],[4011006,4011007,4021006,4000030,4003000]];
            var matQtySet = [[15,20],[20,20,2],[40,50,2],[50,2,1],[1,3,60,15],[3,1,3,80,25],[3,1,2,40,35],[2,1,8,50,50]];
            var costSet = [5000,10000,15000,20000,30000,40000,50000,70000];
        }else if (selectedType == 3) { //glove upgrade
            var matSet = [[1082013,4021003],[1082013,4021000],[1082016,4021000],[1082016,4021008],[1082048,4021003],[1082048,4021008],[1082068,4011002],[1082068,4011006],[1082071,4011006],[1082071,4021008],[1082084,4011000,4021000],[1082084,4011006,4021008],[1082089,4021000,4021007],[1082089,4021007,4021008]];
            var matQtySet = [[1,2],[1,1],[1,3],[1,1],[1,3],[1,1],[1,4],[1,2],[1,4],[1,2],[1,1,5],[1,2,2],[1,5,1],[1,2,2]];
            var costSet = [7000,7000,10000,12000,15000,20000,22000,25000,30000,40000,55000,60000,70000,80000];
        }else if (selectedType == 5) { //arrow refine
            var matSet = [[4003001,4003004],[4003001,4003004],[4011000,4003001,4003004],[4011000,4003001,4003004],[4011001,4003001,4003005],[4011001,4003001,4003005]];
            var matQtySet = [[1,1],[1,1],[1,3,10],[1,3,10],[1,5,15],[1,5,15]];
            var costSet = [0,0,0,0,0,0]
        }
        if(selectedType != 4){
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        }
        var prompt = "你想制作";
        if (qty == 1)
            prompt += "1个 #t" + item + "#?";
        else
            prompt += qty + " #t" + item + "#?";
        prompt += " 制作这个道具需要一些特殊的材料。确保你背包有足够的空间！#b";
        if (mats instanceof Array)
            for(var i = 0; i < mats.length; i++)
                prompt += "\r\n#i" + mats[i] + "# " + (matQty[i] * qty) + " #t" + mats[i] + "#";
        else
            prompt += "\r\n#i" + mats + "# " + (matQty * qty) + " #t" + mats + "#";
        if (cost > 0)
            prompt += "\r\n" + (cost * qty) + " 金币";
        cm.sendYesNo(prompt);
    }else if (status == 3) {
        var complete = true;
        if (cm.getMeso() < (cost * qty))
            cm.sendOk("你没有足够的金币！")
        else{
            if (mats instanceof Array) {
                for(var i = 0; complete && i < mats.length; i++)
                    if (!cm.haveItem(mats[i], matQty[i]))
                        complete = false;
            }else if (!cm.haveItem(mats, matQty))
                complete = false;
        }	
        if (!complete)
            cm.sendOk("你的材料不够！");
        else {
            if (cm.canHold(item)) {
                if (mats instanceof Array) {
                    for (var i = 0; i < mats.length; i++)
                        cm.gainItem(mats[i], -(matQty[i] * qty));
                }else
                    cm.gainItem(mats, -(matQty * qty));
                cm.gainMeso(-(cost * qty));
                if (item >= 2060000 && item <= 2060002) //bow arrows
                    cm.gainItem(item, 1000 - (item - 2060000) * 100);
                else if (item >= 2061000 && item <= 2061002) //xbow arrows
                    cm.gainItem(item, 1000 - (item - 2061000) * 100);
                else if (item == 4003000)//screws
                    cm.gainItem(4003000, 15 * qty);
                else
                    cm.gainItem(item, qty);
                cm.sendOk("哦，看看我的手艺，还是挺不错的！");
            }else {
                cm.sendOk("请检查你的背包空间是否足够！");
            }
        }
        cm.dispose();
    }
}