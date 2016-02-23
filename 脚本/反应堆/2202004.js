/*@author Jvlaple
 *Reactor : LudiPQ Bonus Reactor - 2202004.js
 * Drops all the Bonus Items
 */

function act() {
    rand = Math.floor(Math.random() * 3);
    if (rand < 1) rand = 1;
    //We'll make it drop a lot of crap :D
    for (var i = 0; i < rand; i++) {
        rm.dropItems(true, 1, 30, 60, 15);
    }
}