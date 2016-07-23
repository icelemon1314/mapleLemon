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

/* Mr. Smith
	Victoria Road: Perion (102000000)
	
	Refining NPC: 
	* Warrior Gloves - 10-60 + upgrades
	* Processed Wood/Screws
*/

var status = 0;
var selectedType = -1;
var selectedItem = -1;
var item;
var mats;
var matQty;
var cost;
var qty;
var equip;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == 1)
		status++;
	else
		cm.dispose();
	if (status == 0 && mode == 1) {
		var selStr = "Hi，我是斯密斯。辛德的徒弟。我师傅在这边做了很久了，所以复杂又重要的工作就是我师傅做，我只负责做一些简单的工作，你想在我这里做啥呢？"
		var options = new Array("制作手套","升级手套","制造材料");
		for (var i = 0; i < options.length; i++){
			selStr += "\r\n#L" + i + "# " + options[i] + "#l";
		}
			
		cm.sendSimple(selStr);
	}
	else if (status == 1 && mode == 1) {
		selectedType = selection;
		if (selectedType == 0){ //glove refine
			var selStr = "好吧，你想做哪个手套呢？#b";
			// 1082003,1082000,1082004,1082001,1082007,1082008,1082023,1082009,1082059
			var items = new Array ("腕甲#k - 战士 Lv. 10#b","钢制短手套#k - 战士 Lv. 15#b","皮手套#k - 战士 Lv. 20#b","白纹短手套#k - 战士 Lv. 25#b",
				"青铜机器手套#k - 战士 Lv. 30#b","铁制轻便手套#k - 战士 Lv. 35#b","钢铁指节手套#k - 战士 Lv. 40#b","钢铁合金手套#k - 战士 Lv. 50#b","青铜战斗手套#k - 战士 Lv. 60#b");
			for (var i = 0; i < items.length; i++){
				selStr += "\r\n#L" + i + "# " + items[i] + "#l";
			}
			cm.sendSimple(selStr);
			equip = true;
		}
		else if (selectedType == 1){ //glove upgrade
			var selStr = "升级手套？这个不是很难，旧手套的属性不会继承到新手套上。你想制作哪一个？#b";
			// 1082005,1082006,1082035,1082036,1082024,1082025,1082010,1082011,1082060,1082061
			var crystals = new Array ("钢制机器手套#k - 战士 Lv. 30#b","紫矿机器手套#k - 战士 Lv. 30#b","黄轻便手套#k - 战士 Lv. 35#b","黑轻便手套#k - 战士 Lv. 35#b",
				"朱矿指节手套#k - 战士 Lv. 40#b","黑指节手套#k - 战士 Lv. 40#b","锂矿合金手套#k - 战士 Lv. 50#b","黄金合金手套#k - 战士 Lv. 50#b",
				"蓝战斗手套#k - 战士 Lv. 60#b","黑战斗手套#k - 战士 Lv. 60#b");
			for (var i = 0; i < crystals.length; i++){
				selStr += "\r\n#L" + i + "# " + crystals[i] + "#l";
			}
			cm.sendSimple(selStr);
			equip = true;
		}
		else if (selectedType == 2){ //material refine
			var selStr = "制作材料？我知道几种材料的制作方式...#b";
			var materials = new Array ("用树枝制作木板","用木块制作木板","制作螺丝钉 (10个)");
			for (var i = 0; i < materials.length; i++){
				selStr += "\r\n#L" + i + "# " + materials[i] + "#l";
			}
			cm.sendSimple(selStr);
			equip = false;
		}
		if (equip)
			status++;
	}
	else if (status == 2 && mode == 1) {
		selectedItem = selection;
		if (selectedType == 2){ //material refine
			var itemSet = new Array (4003001,4003001,4003000);
			var matSet = new Array(4000003,4000018,new Array (4011000,4011001));
			var matQtySet = new Array (10,5,new Array (1,1));
			var costSet = new Array (0,0,0)
			item = itemSet[selectedItem];
			mats = matSet[selectedItem];
			matQty = matQtySet[selectedItem];
			cost = costSet[selectedItem];
		}
		
		var prompt = "你想制作 #t" + item + "#s? 你想制作几个？";
		
		cm.sendGetNumber(prompt,1,1,100)
	}
	else if (status == 3 && mode == 1) {
		if (equip)
		{
			selectedItem = selection;
			qty = 1;
		}
		else
			qty = selection;

		if (selectedType == 0){ //glove refine
			var itemSet = new Array(1082003,1082000,1082004,1082001,1082007,1082008,1082023,1082009,1082059);
			var matSet = new Array(new Array(4000021,4011001),4011001,new Array(4000021,4011000),4011001,new Array(4011000,4011001,4003000),new Array(4000021,4011001,4003000),new Array(4000021,4011001,4003000),
				new Array(4011001,4021007,4000030,4003000),new Array(4011007,4011000,4011006,4000030,4003000));
			var matQtySet = new Array(new Array(15,1),2,new Array(40,2),2,new Array(3,2,15),new Array(30,4,15),new Array(50,5,40),new Array(3,2,30,45),new Array(1,8,2,50,50));
			var costSet = new Array(1000,2000,5000,10000,20000,30000,40000,50000,70000);
			item = itemSet[selectedItem];
			mats = matSet[selectedItem];
			matQty = matQtySet[selectedItem];
			cost = costSet[selectedItem];
		}
		else if (selectedType == 1){ //glove upgrade
			var itemSet = new Array(1082005,1082006,1082035,1082036,1082024,1082025,1082010,1082011,1082060,1082061);
			var matSet = new Array(new Array(1082007,4011001),new Array(1082007,4011005),new Array(1082008,4021006),new Array(1082008,4021008),new Array(1082023,4011003),new Array(1082023,4021008),
				new Array(1082009,4011002),new Array(1082009,4011006),new Array(1082059,4011002,4021005),new Array(1082059,4021007,4021008));
			var matQtySet = new Array (new Array(1,1),new Array(1,2),new Array(1,3),new Array(1,1),new Array(1,4),new Array(1,2),new Array(1,5),new Array(1,4),new Array(1,3,5),new Array(1,2,2));
			var costSet = new Array (20000,25000,30000,40000,45000,50000,55000,60000,70000,80000);
			item = itemSet[selectedItem];
			mats = matSet[selectedItem];
			matQty = matQtySet[selectedItem];
			cost = costSet[selectedItem];
		}
		
		var prompt = "你想制作 ";
		if (qty == 1)
			prompt += "1个 #t" + item + "#?";
		else
			prompt += qty + " #t" + item + "#?";
			
		prompt += " 制作这个道具，我需要一些特殊的材料，确保你有足够的背包空间！#b";
		
		if (mats instanceof Array){
			for(var i = 0; i < mats.length; i++){
				prompt += "\r\n#i"+mats[i]+"# " + matQty[i] * qty + " #t" + mats[i] + "#";
			}
		}
		else {
			prompt += "\r\n#i"+mats+"# " + matQty * qty + " #t" + mats + "#";
		}
		
		if (cost > 0)
			prompt += "\r\n" + cost * qty + " 金币";
		
		cm.sendYesNo(prompt);
	}
	else if (status == 4 && mode == 1) {
		var complete = true;
		
		if (cm.getMeso() < cost * qty)
			{
				cm.sendOk("你没有足够的金币！")
			}
			else
			{
				if (mats instanceof Array) {
					for(var i = 0; complete && i < mats.length; i++)
					{
						if (matQty[i] * qty == 1)	{
							if (!cm.haveItem(mats[i]))
							{
								complete = false;
							}
						}
						else {
							var count = 0;
							var iter = cm.getChar().getInventory(MapleInventoryType.ETC).listById(mats[i]).iterator();
							while (iter.hasNext()) {
								count += iter.next().getQuantity();
							}
							if (count < matQty[i] * qty)
								complete = false;
						}					
					}
				}
				else {
					var count = 0;
					var iter = cm.getChar().getInventory(MapleInventoryType.ETC).listById(mats).iterator();
					while (iter.hasNext()) {
						count += iter.next().getQuantity();
					}
					if (count < matQty * qty)
						complete = false;
				}
			}
			
			if (!complete) 
				cm.sendOk("看来你没有带足够的材料，赶紧再去收集吧！");
			else {
				if (mats instanceof Array) {
					for (var i = 0; i < mats.length; i++){
						cm.gainItem(mats[i], -matQty[i] * qty);
					}
				}
				else
					cm.gainItem(mats, -matQty * qty);
					
				if (cost > 0)
					cm.gainMeso(-cost * qty);
				
				if (item == 4003000)//screws
					cm.gainItem(4003000, 10 * qty);
				else
					cm.gainItem(item, qty);
				cm.sendOk("看上去手艺不错，欢迎下次光临！");
			}
		cm.dispose();
	}
}