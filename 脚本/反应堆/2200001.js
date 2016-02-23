/* @Author Lerk
 * 
 * 2200001.js: Warps player into one of two Secret Factories
 * 
*/

function act() {
    rm.playerMessage(5, "找到了秘密工厂。");
    if (Math.random() < .5) {
        rm.warp(922000020);
    } else {
        rm.warp(922000021);
    }
}