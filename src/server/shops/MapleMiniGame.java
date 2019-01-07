package server.shops;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleQuestStatus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import server.quest.MapleQuest;
import tools.packet.PlayerShopPacket;

public final class MapleMiniGame extends AbstractPlayerStore {

    private static final int slots = 2;
    private final boolean[] exitAfter;
    private final boolean[] ready;
    private final int[] points;
    private int GameType = 0;
    private int[][] piece = new int[15][15];
    private final List<Integer> matchcards = new ArrayList();
    int loser = 0;
    int turn = 1;
    int piecetype = 0;
    int firstslot = 0;
    int tie = -1;

    public MapleMiniGame(MapleCharacter owner, int itemId, String description, String pass, int GameType) {
        super(owner, itemId, description, pass, 1);
        this.GameType = GameType;
        this.points = new int[2];
        this.exitAfter = new boolean[2];
        this.ready = new boolean[2];
        reset();
    }

    public void reset() {
        for (int i = 0; i < 2; i++) {
            this.points[i] = 0;
            this.exitAfter[i] = false;
            this.ready[i] = false;
        }
    }

    public void setFirstSlot(int type) {
        this.firstslot = type;
    }

    public int getFirstSlot() {
        return this.firstslot;
    }

    public void setPoints(int slot) {
        this.points[slot] += 1;
        checkWin();
    }

    public int getPoints() {
        int ret = 0;
        for (int i = 0; i < 2; i++) {
            ret += this.points[i];
        }
        return ret;
    }

    public void checkWin() {
        if ((getPoints() >= getMatchesToWin()) && (!isOpen())) {
            int x = 0;
            int highest = 0;
            boolean tie = false;
            for (int i = 0; i < 2; i++) {
                if (this.points[i] > highest) {
                    x = i;
                    highest = this.points[i];
                    tie = false;
                } else if (this.points[i] == highest) {
                    tie = true;
                }
                this.points[i] = 0;
            }
            broadcastToVisitors(PlayerShopPacket.getMiniGameResult(this, tie ? 1 : 2, x));
            setOpen(true);
            update();
            checkExitAfterGame();
        }
    }

    public int getOwnerPoints(int slot) {
        return this.points[slot];
    }

    public void setPieceType(int type) {
        this.piecetype = type;
    }

    public int getPieceType() {
        return this.piecetype;
    }

    public void setGameType() {
        if (this.GameType == 2) {
            this.matchcards.clear();
            for (int i = 0; i < getMatchesToWin(); i++) {
                this.matchcards.add(i);
                this.matchcards.add(i);
            }
        }
    }

    public void shuffleList() {
        if (this.GameType == 2) {
            Collections.shuffle(this.matchcards);
        } else {
            this.piece = new int[15][15];
        }
    }

    public int getCardId(int slot) {
        return (this.matchcards.get(slot - 1));
    }

    public int getMatchesToWin() {
        return getPieceType() == 1 ? 10 : getPieceType() == 0 ? 6 : 15;
    }

    public void setLoser(int type) {
        this.loser = type;
    }

    public int getLoser() {
        return this.loser;
    }

    public void send(MapleClient c) {
        if (getMCOwner() == null) {
            closeShop(false, false);
            return;
        }
        c.getSession().write(PlayerShopPacket.getMiniGame(c, this));
    }

    public void setReady(int slot) {
        ready[slot] = !ready[slot];
    }

    public boolean isReady(int slot) {
        return this.ready[slot];
    }

    public void setPiece(int move1, int move2, int type, MapleCharacter chr) {
        if ((this.piece[move1][move2] == 0) && (!isOpen())) {
            this.piece[move1][move2] = type;
            broadcastToVisitors(PlayerShopPacket.getMiniGameMoveOmok(move1, move2, type));
            boolean found = false;
            for (int y = 0; y < 15; y++) {
                for (int x = 0; x < 15; x++) {
                    if ((!found) && (searchCombo(x, y, type))) {
                        broadcastToVisitors(PlayerShopPacket.getMiniGameResult(this, 2, getVisitorSlot(chr)));
                        setOpen(true);
                        update();
                        checkExitAfterGame();
                        found = true;
                    }
                }
            }
            nextLoser();
        }
    }

    public void nextLoser() {
        this.loser += 1;
        if (this.loser > 1) {
            this.loser = 0;
        }
    }

    public void exit(MapleCharacter player) {
        if (player == null) {
            return;
        }
        player.setPlayerShop(null);
        if (isOwner(player)) {
            update();
            removeAllVisitors(3, 1);
        } else {
            removeVisitor(player);
        }
    }

    public boolean isExitAfter(MapleCharacter player) {
        if (getVisitorSlot(player) > -1) {
            return this.exitAfter[getVisitorSlot(player)];
        }
        return false;
    }

    public void setExitAfter(MapleCharacter player) {
        if (getVisitorSlot(player) > -1) {
            exitAfter[getVisitorSlot(player)] = !exitAfter[getVisitorSlot(player)];
        }
    }

    public void checkExitAfterGame() {
        for (int i = 0; i < 2; i++) {
            if (this.exitAfter[i]) {
                this.exitAfter[i] = false;
                exit(i == 0 ? getMCOwner() : (MapleCharacter) this.chrs[(i - 1)].get());
            }
        }
    }

    public boolean searchCombo(int x, int y, int type) {
        boolean ret = false;
        if ((!ret) && (x < 11)) {
            ret = true;
            for (int i = 0; i < 5; i++) {
                if (this.piece[(x + i)][y] != type) {
                    ret = false;
                    break;
                }
            }
        }
        if ((!ret) && (y < 11)) {
            ret = true;
            for (int i = 0; i < 5; i++) {
                if (this.piece[x][(y + i)] != type) {
                    ret = false;
                    break;
                }
            }
        }
        if ((!ret) && (x < 11) && (y < 11)) {
            ret = true;
            for (int i = 0; i < 5; i++) {
                if (this.piece[(x + i)][(y + i)] != type) {
                    ret = false;
                    break;
                }
            }
        }
        if ((!ret) && (x > 3) && (y < 11)) {
            ret = true;
            for (int i = 0; i < 5; i++) {
                if (this.piece[(x - i)][(y + i)] != type) {
                    ret = false;
                    break;
                }
            }
        }
        return ret;
    }

    public int getScore(MapleCharacter chr) {
        int score = 2000;
        int wins = getWins(chr);
        int ties = getTies(chr);
        int losses = getLosses(chr);
        if (wins + ties + losses > 0) {
            score += wins * 2;
            score += ties;
            score -= losses * 2;
        }
        return score;
    }

    @Override
    public byte getShopType() {
        return GameType == 1 ? IMaplePlayerShop.OMOK : IMaplePlayerShop.MATCH_CARD;
    }

    public int getWins(MapleCharacter chr) {
        return Integer.parseInt(getData(chr).split(",")[2]);
    }

    public int getTies(MapleCharacter chr) {
        return Integer.parseInt(getData(chr).split(",")[1]);
    }

    public int getLosses(MapleCharacter chr) {
        return Integer.parseInt(getData(chr).split(",")[0]);
    }

    public void setPoints(int i, int type) {
        MapleCharacter z;
        if (i == 0) {
            z = getMCOwner();
        } else {
            z = getVisitor(i - 1);
        }
        if (z != null) {
            String[] data = getData(z).split(",");
            data[type] = String.valueOf(Integer.parseInt(data[type]) + 1);
            StringBuilder newData = new StringBuilder();
            for (String data1 : data) {
                newData.append(data1);
                newData.append(",");
            }
            String newDat = newData.toString();
            z.getQuestNAdd(MapleQuest.getInstance(this.GameType == 1 ? 122200 : 122210)).setCustomData(newDat.substring(0, newDat.length() - 1));
        }
    }

    public String getData(MapleCharacter chr) {
        MapleQuest quest = MapleQuest.getInstance(this.GameType == 1 ? 122200 : 122210);
        MapleQuestStatus record;
        if (chr.getQuestNoAdd(quest) == null) {
            record = chr.getQuestNAdd(quest);
            record.setCustomData("0,0,0");
        } else {
            record = chr.getQuestNoAdd(quest);
            if ((record.getCustomData() == null) || (record.getCustomData().length() < 5) || (!record.getCustomData().contains(","))) {
                record.setCustomData("0,0,0");
            }
        }
        return record.getCustomData();
    }

    public int getRequestedTie() {
        return this.tie;
    }

    public void setRequestedTie(int t) {
        this.tie = t;
    }

    public int getTurn() {
        return this.turn;
    }

    public void setTurn(int t) {
        this.turn = t;
    }

    @Override
    public void closeShop(boolean s, boolean z) {
        removeAllVisitors(3, 1);
        if (getMCOwner() != null) {
            getMCOwner().setPlayerShop(null);
        }
        update();
        getMap().removeMapObject(this);
    }

    @Override
    public void buy(MapleClient c, int z, short i) {
    }
}
