/*
	Maple Administrator - Displays Battler Info
*/

var status = -1;
var sel = 0;
var sec = 0;

function action(mode, type, selection) {
    var battlers = cm.getPlayer().getBoxed();
    if (mode != 1) {
        cm.dispose();
    } else {
        status++;
        if (status == 0) {
            if (battlers.size() <= 0) {
                cm.sendOk("You have no monsters stored away.");
                cm.dispose();
                return;
            }
            var selStr = "Check the stats of which?\r\n\r\n#b";
            for (var i = 0; i < battlers.size(); i++) {
                if (battlers.get(i) != null) {
                    selStr += "#L" + i + "#" + battlers.get(i).getName() + " (#o" + battlers.get(i).getMonsterId() + "#) Level " + battlers.get(i).getLevel() + " " + battlers.get(i).getGenderString() + "#l\r\n";
                }
            }
            cm.sendSimple(selStr);
        } else if (status == 1) {
            if (selection < 0 || selection >= battlers.size() || battlers.get(selection) == null) {
                cm.dispose();
                return;
            }
            sel = selection;
            var info = "#e" + battlers.get(selection).getName() + "#n (#o" + battlers.get(selection).getMonsterId() + "#)\r\n";
            info += "Level " + battlers.get(selection).getLevel() + " " + battlers.get(selection).getGenderString() + "\r\n";
            info += "EXP " + battlers.get(selection).getExp() + "/" + battlers.get(selection).getNextExp() + "\r\n";
            info += "HP " + battlers.get(selection).calcHP() + "\r\n";
            info += "ATK: " + battlers.get(selection).getATK(0) + ", DEF: " + battlers.get(selection).getDEF() + "%\r\n";
            info += "Sp.ATK: " + battlers.get(selection).getSpATK(0) + ", Sp.DEF: " + battlers.get(selection).getSpDEF() + "%\r\n";
            info += "Speed: " + battlers.get(selection).getSpeed() + ", Evasion: " + battlers.get(selection).getEVA() + ", Accuracy: " + battlers.get(selection).getACC() + "\r\n";
            info += "Element: " + battlers.get(selection).getElementString() + "\r\n";
            info += "Nature: " + battlers.get(selection).getNatureString() + "\r\n";
            info += "Item: " + battlers.get(selection).getItemString() + "\r\n";
            info += "Ability: " + battlers.get(selection).getAbilityString() + "\r\n";
            info += "\r\n#b";
            info += "#L0#How do I evolve this?#l\r\n";
            info += "#L1#Release this monster.#l\r\n";
            info += "#L3#Rename this monster.#l\r\n";
            info += "#L5#Take this monster out.#l\r\n";
            info += "#L6#Give/take item.#l\r\n";
            info += "#L7#Rate this monster.#l\r\n";
            cm.sendSimple(info);
        } else if (status == 2) {
            sec = selection;
            if (selection == 0) { //how i evolve
                var evo = battlers.get(sel).getEvolutionType().value;
                if (evo == 0) {
                    cm.sendNext("Congratulations, for you have reached the final stage in the evolution.");
                    cm.dispose();
                } else if (evo == 1) {
                    cm.sendNext("You still have a a long way to go, for you must level up some more.");
                    cm.dispose();
                } else if (evo == 2) {
                    var selStr = "You can only evolve by using a certain item. I can evolve it for you. Let's see here...\r\n\r\n";
                    if (cm.haveItem(battlers.get(sel).getFamily().evoItem.id)) {
                        cm.sendSimple(selStr + "#L0##v" + battlers.get(sel).getFamily().evoItem.id + "##z" + battlers.get(sel).getFamily().evoItem.id + "##l");
                    } else {
                        cm.sendNext(selStr + "You don't have the evolution item needed. Required: #v" + battlers.get(sel).getFamily().evoItem.id + "##z" + battlers.get(sel).getFamily().evoItem.id + "#");
                        cm.dispose();
                    }
                }

            } else if (selection == 1) {
                cm.sendYesNo("Are you sure you want to release the monster " + battlers.get(sel).getName() + " (#o" + battlers.get(sel).getMonsterId() + "#)?");
            } else if (selection == 3) {
                cm.sendGetText("Please enter the new name for your monster. (Min: 2 characters, Max: 20 characters)");
            } else if (selection == 5) {
                if (cm.getPlayer().countBattlers() >= 6) {
                    cm.sendOk("You already have six monsters.");
                    cm.dispose();
                    return;
                }
                var battt = cm.getPlayer().getBattlers();
                for (var i = 0; i < battt.length; i++) {
                    if (battt[i] != null && battlers.get(sel).getMonsterId() == battt[i].getMonsterId()) {
                        cm.sendOk("You already have one of this monster.");
                        cm.dispose();
                        return;
                    }
                }
                cm.getPlayer().getBattlers()[cm.getPlayer().countBattlers()] = battlers.get(sel);
                battlers.remove(sel);
                cm.getPlayer().changedBattler();
                cm.sendOk("The monster has been taken.");
            } else if (selection == 6) {
                if (battlers.get(sel).getItem() != null) {
                    if (cm.canHold(battlers.get(sel).getItem().id, 1)) {
                        cm.gainItem(battlers.get(sel).getItem().id, 1);
                        cm.sendOk("You have taken the item from this monster.");
                        battlers.get(sel).setItem(0);
                    } else {
                        cm.sendOk("Please make inventory space.");
                    }
                    cm.dispose();
                    return;
                }
                var selStr = "Which item would you like to give to this monster?#b\r\n";
                var hi = cm.getAllHoldItems();
                var pass = false;
                for (var i = 0; i < hi.length; i++) {
                    if (cm.haveItem(hi[i].id, 1)) {
                        pass = true;
                        selStr += "#L" + i + "##i" + hi[i].id + "#" + hi[i].customName + "#l\r\n";
                    }
                }
                if (!pass) {
                    cm.sendNext("You have no hold items.");
                    cm.dispose();
                } else {
                    cm.sendSimple(selStr);
                }
            } else if (selection == 7) {
                cm.sendNext(battlers.get(sel).getIVString());
                cm.dispose();
            }
        } else if (status == 3) {
            if (sec == 0) {
                if (cm.haveItem(battlers.get(sel).getFamily().evoItem.id)) {
                    cm.gainItem(battlers.get(sel).getFamily().evoItem.id, -1);
                    battlers.get(sel).evolve(true, cm.getPlayer());
                    cm.getPlayer().changedBattler();
                    cm.playSound(false, "5th_Maple/gaga");
                    cm.sendNext("Your monster has evolved!!!");
                }
            } else if (sec == 1) {
                battlers.remove(sel);
                cm.getPlayer().changedBattler();
                cm.sendNext("It has been released!");
            } else if (sec == 3) {
                if (cm.getText().length() < 2 || cm.getText().length() > 20) {
                    cm.sendOk(cm.getText() + " cannot be accepted.");
                } else {
                    cm.getPlayer().changedBattler();
                    battlers.get(sel).setName(cm.getText());
                }
            } else if (sec == 6) {
                var hi = cm.getAllHoldItems()[selection];
                if (cm.haveItem(hi.id, 1)) {
                    cm.gainItem(hi.id, -1);
                    battlers.get(sel).setItem(hi.id);
                    cm.sendOk("The item has been set on to the monster.");
                }
            }
            cm.dispose();
        }
    }
}