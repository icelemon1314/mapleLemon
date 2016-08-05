/* 
 * This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
/**
-- Odin JavaScript --------------------------------------------------------------------------------
	Boats Between Ellinia and Orbis
-- By ---------------------------------------------------------------------------------------------
	Information
-- Version Info -----------------------------------------------------------------------------------
	1.6 - Fix for infinity looping [Information]
	1.5 - Ship/boat is now showed 
	    - Removed temp message[Information]
	    - Credit to Snow/superraz777 for old source
	    - Credit to Titan/Kool for the ship/boat packet
	1.4 - Fix typo [Information]
	1.3 - Removing some function since is not needed [Information]
	    - Remove register player menthod [Information]
	    - Remove map instance and use reset reactor function [Information]
	1.2 - Should be 2 ship not 1 [Information]
	1.1 - Add timer variable for easy edit [Information]
	1.0 - First Version by Information
---------------------------------------------------------------------------------------------------
**/

//Time Setting is in millisecond
// 15分钟一趟 登船4分钟 等待1分钟 航行10分钟
var closeTime = 240000; //The time to close the gate
var beginTime = 300000; //The time to begin the ride
var rideTime = 600000; //The time that require move to destination
var invasionTime = 60000; //The time that spawn balrog
var Orbis_btf;
var Boat_to_Orbis;
var Orbis_Boat_Cabin;
var Orbis_docked;
var Ellinia_btf;
var Ellinia_Boat_Cabin;
var Ellinia_docked;
var nextTime;

// 服务端启动的时候调用
function init() {
	// 候船开往魔法密林
    Orbis_btf = em.getChannelServer().getMapFactory().getMap(200000112);
	// 候船开往天空之城
    Ellinia_btf = em.getChannelServer().getMapFactory().getMap(101000301);
	// 航海中开往天空之城
    Boat_to_Orbis = em.getChannelServer().getMapFactory().getMap(200090010);
	// 航海中开往魔法密林
    Boat_to_Ellinia = em.getChannelServer().getMapFactory().getMap(200090000);
    Orbis_Boat_Cabin = em.getChannelServer().getMapFactory().getMap(200090011);
    Ellinia_Boat_Cabin = em.getChannelServer().getMapFactory().getMap(200090001);
    Orbis_docked = em.getChannelServer().getMapFactory().getMap(200000100);
    Ellinia_docked = em.getChannelServer().getMapFactory().getMap(101000300);
    Orbis_Station = em.getChannelServer().getMapFactory().getMap(200000111);
    OBoatsetup();
    EBoatsetup();
    //scheduleNew();
	nextTime = getNextTime();
	em.schedule("scheduleNew", nextTime*60*1000);
}

function getNextTime(){
	// 每隔15分钟一班 前4分钟登船，1分钟关门，10分钟航行
	// 秒数的控制不精确
	var curTime = em.getCurentMin();
	if ( 0 <= curTime && curTime <10) {
		return 10-curTime;
	} else if (14<=curTime && curTime <25) {
		return 25-curTime;
	} else if (29<=curTime && curTime <40) {
		return 40-curTime;
	} else if (44<=curTime && curTime <55) {
		return 55-curTime;
	} else if (59 <= curTime) {
		return 1;
	} else {
		return 0;
	}
}

function scheduleNew() {
    Ellinia_docked.setDocked(true);
    Orbis_Station.setDocked(true);
    Ellinia_docked.broadcastMessage(em.sendBoat(true));
    Orbis_Station.broadcastMessage(em.sendBoat(true));
    em.setProperty("docked", "true");
    em.setProperty("entry", "true");
    em.setProperty("haveBalrog","false");
    em.schedule("stopentry", closeTime);
    em.schedule("takeoff", beginTime);
}

function stopentry() {
    em.setProperty("entry","false");
    Orbis_Boat_Cabin.resetReactors();
    Ellinia_Boat_Cabin.resetReactors();
}

function takeoff() {
    em.setProperty("docked","false");
    var temp1 = Orbis_btf.getCharacters().iterator();
    while(temp1.hasNext()) {
        temp1.next().changeMap(Boat_to_Ellinia, Boat_to_Ellinia.getPortal(0));
    }
    var temp2 = Ellinia_btf.getCharacters().iterator();
    while(temp2.hasNext()) {
        temp2.next().changeMap(Boat_to_Orbis, Boat_to_Orbis.getPortal(0));
    }
    Ellinia_docked.setDocked(false);
    Orbis_Station.setDocked(false);
    Ellinia_docked.broadcastMessage(em.sendBoat(false));
    Orbis_Station.broadcastMessage(em.sendBoat(false));
    em.schedule("invasion", invasionTime);
    em.schedule("arrived", rideTime);
}

function arrived() {
    var temp1 = Boat_to_Orbis.getCharacters().iterator();
    while(temp1.hasNext()) {
        temp1.next().changeMap(Orbis_docked, Orbis_docked.getPortal(0));
    }
    var temp2 = Orbis_Boat_Cabin.getCharacters().iterator();
    while(temp2.hasNext()) {
        temp2.next().changeMap(Orbis_docked, Orbis_docked.getPortal(0));
    }
    var temp3 = Boat_to_Ellinia.getCharacters().iterator();
    while(temp3.hasNext()) {
        temp3.next().changeMap(Ellinia_docked, Ellinia_docked.getPortal(0));
    }
    var temp4 = Ellinia_Boat_Cabin.getCharacters().iterator();
    while(temp4.hasNext()) {
        temp4.next().changeMap(Ellinia_docked, Ellinia_docked.getPortal(0));
    }
    Boat_to_Orbis.killAllMonsters();
    Boat_to_Ellinia.killAllMonsters();
    scheduleNew();
}

function invasion() {
    var numspawn;
    var chance = Math.floor(Math.random() * 10);
    //if(chance <= 2)
    //    numspawn = 0;
    //else
        numspawn = 2;
    if(numspawn > 0) {
        for(var i=0; i < numspawn; i++) {
            Boat_to_Orbis.spawnMonsterOnGroundBelow(em.getMonster(8150000), new java.awt.Point(485, -221));
            Boat_to_Ellinia.spawnMonsterOnGroundBelow(em.getMonster(8150000), new java.awt.Point(-590, -221));
        }
        //Boat_to_Orbis.setDocked(true);
        //Boat_to_Ellinia.setDocked(true);
        Boat_to_Orbis.broadcastMessage(em.sendMonsterBoat(true));
        Boat_to_Ellinia.broadcastMessage(em.sendMonsterBoat(true));
        Boat_to_Orbis.broadcastMessage(em.musicChange("Bgm04/ArabPirate"));
        Boat_to_Ellinia.broadcastMessage(em.musicChange("Bgm04/ArabPirate"));
        em.setProperty("haveBalrog","true");
    }
}

function OBoatsetup() {
    em.getChannelServer().getMapFactory().getMap(200090011).getPortal("out00").setScriptName("OBoat1");
    em.getChannelServer().getMapFactory().getMap(200090011).getPortal("out01").setScriptName("OBoat2");
}

function EBoatsetup() {
    em.getChannelServer().getMapFactory().getMap(200090001).getPortal("out00").setScriptName("EBoat1");
    em.getChannelServer().getMapFactory().getMap(200090001).getPortal("out01").setScriptName("EBoat2");
}

function cancelSchedule() {
}

function allMonstersDead(){
	Boat_to_Orbis.broadcastMessage(em.sendMonsterBoat(false));
    Boat_to_Ellinia.broadcastMessage(em.sendMonsterBoat(false));
    Boat_to_Orbis.broadcastMessage(em.musicChange("Bgm04/UponTheSky"));
    Boat_to_Ellinia.broadcastMessage(em.musicChange("Bgm04/UponTheSky"));
}