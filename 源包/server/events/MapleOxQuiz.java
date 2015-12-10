package server.events;

import client.MapleCharacter;
import client.MapleStat;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import server.Timer.EventTimer;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.Pair;

public class MapleOxQuiz extends MapleEvent {

    private ScheduledFuture<?> oxSchedule;
    private ScheduledFuture<?> oxSchedule2;
    private int timesAsked = 0;
    private boolean finished = false;

    public MapleOxQuiz(int channel, MapleEventType type) {
        super(channel, type);
    }

    @Override
    public void finished(MapleCharacter chr) {
    }

    private void resetSchedule() {
        if (this.oxSchedule != null) {
            this.oxSchedule.cancel(false);
            this.oxSchedule = null;
        }
        if (this.oxSchedule2 != null) {
            this.oxSchedule2.cancel(false);
            this.oxSchedule2 = null;
        }
    }

    @Override
    public void onMapLoad(MapleCharacter chr) {
        super.onMapLoad(chr);
        if ((chr.getMapId() == this.type.mapids[0]) && (!chr.isGM())) {
            chr.canTalk(false);
        }
    }

    @Override
    public void reset() {
        super.reset();
        getMap(0).getPortal("join00").setPortalState(false);
        resetSchedule();
        this.timesAsked = 0;
    }

    @Override
    public void unreset() {
        super.unreset();
        getMap(0).getPortal("join00").setPortalState(true);
        resetSchedule();
    }

    @Override
    public void startEvent() {
        sendQuestion();
        this.finished = false;
    }

    public void sendQuestion() {
        sendQuestion(getMap(0));
    }

    public void sendQuestion(final MapleMap toSend) {
        final Map.Entry question = MapleOxQuizFactory.getInstance().grabRandomQuestion();
        if (this.oxSchedule2 != null) {
            this.oxSchedule2.cancel(false);
        }
        this.oxSchedule2 = EventTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                int number = 0;
                for (MapleCharacter mc : toSend.getCharactersThreadsafe()) {
                    if ((mc.isGM()) || (!mc.isAlive())) {
                        number++;
                    }
                }
                if ((toSend.getCharactersSize() - number <= 1) || (MapleOxQuiz.this.timesAsked == 10)) {
                    toSend.broadcastMessage(MaplePacketCreator.serverMessageNotice("The event has ended"));
                    MapleOxQuiz.this.unreset();
                    for (MapleCharacter chr : toSend.getCharactersThreadsafe()) {
                        if ((chr != null) && (!chr.isGM()) && (chr.isAlive())) {
                            chr.canTalk(true);
                            MapleEvent.givePrize(chr);
                            MapleOxQuiz.this.warpBack(chr);
                        }
                    }

                    MapleOxQuiz.this.finished = true;
                    return;
                }
                toSend.broadcastMessage(MaplePacketCreator.showOXQuiz(((Integer) ((Pair) question.getKey()).left), ((Integer) ((Pair) question.getKey()).right), true));
                toSend.broadcastMessage(MaplePacketCreator.getClock(10));
            }
        }, 10000L);

        if (this.oxSchedule != null) {
            this.oxSchedule.cancel(false);
        }
        this.oxSchedule = EventTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                if (MapleOxQuiz.this.finished) {
                    return;
                }
                toSend.broadcastMessage(MaplePacketCreator.showOXQuiz(((Integer) ((Pair) question.getKey()).left), ((Integer) ((Pair) question.getKey()).right), false));
                timesAsked++;
                for (MapleCharacter chr : toSend.getCharactersThreadsafe()) {
                    if ((chr != null) && (!chr.isGM()) && (chr.isAlive())) {
                        if (!MapleOxQuiz.this.isCorrectAnswer(chr, ((MapleOxQuizFactory.MapleOxQuizEntry) question.getValue()).getAnswer())) {
                            chr.getStat().setHp(0);
                            chr.updateSingleStat(MapleStat.HP, 0L);
                        } else {
                            chr.gainExp(3000, true, true, false);
                        }
                    }
                }
                MapleOxQuiz.this.sendQuestion();
            }
        }, 20000L);
    }

    private boolean isCorrectAnswer(MapleCharacter chr, int answer) {
        double x = chr.getTruePosition().getX();
        double y = chr.getTruePosition().getY();
        if (((x > -234.0D) && (y > -26.0D) && (answer == 0)) || ((x < -234.0D) && (y > -26.0D) && (answer == 1))) {
            chr.dropMessage(6, "[Ox Quiz] Correct!");
            return true;
        }
        chr.dropMessage(6, "[Ox Quiz] Incorrect!");
        return false;
    }
}
