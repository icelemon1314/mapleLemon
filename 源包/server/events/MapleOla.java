package server.events;

import client.MapleCharacter;
import server.Randomizer;

public class MapleOla extends MapleSurvival {

    private int[] stages = new int[3];

    public MapleOla(int channel, MapleEventType type) {
        super(channel, type);
    }

    @Override
    public void finished(MapleCharacter chr) {
        givePrize(chr);
    }

    @Override
    public void reset() {
        super.reset();
        this.stages = new int[]{0, 0, 0};
    }

    @Override
    public void unreset() {
        super.unreset();
        this.stages = new int[]{Randomizer.nextInt(5), Randomizer.nextInt(8), Randomizer.nextInt(15)};
        if (this.stages[0] == 2) {
            this.stages[0] = 3;
        }
    }

    public boolean isCharCorrect(String portalName, int mapid) {
        int st = this.stages[(mapid % 10 - 1)];
        return portalName.equals(new StringBuilder().append("ch").append(st < 10 ? "0" : "").append(st).toString());
    }
}
