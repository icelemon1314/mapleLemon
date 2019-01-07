package server.events;

import client.MapleCharacter;
import client.MapleDisease;
import java.util.concurrent.ScheduledFuture;
import server.Timer.EventTimer;
import server.life.MobSkillFactory;
import server.maps.MapleMap;
import tools.MaplePacketCreator;

public class MapleSnowball extends MapleEvent {

    private final MapleSnowballs[] balls = new MapleSnowballs[2];

    public MapleSnowball(int channel, MapleEventType type) {
        super(channel, type);
    }

    @Override
    public void finished(MapleCharacter chr) {
    }

    @Override
    public void unreset() {
        super.unreset();
        for (int i = 0; i < 2; i++) {
            getSnowBall(i).resetSchedule();
            resetSnowBall(i);
        }
    }

    @Override
    public void reset() {
        super.reset();
        makeSnowBall(0);
        makeSnowBall(1);
    }

    @Override
    public void startEvent() {
        for (int i = 0; i < 2; i++) {
            MapleSnowballs ball = getSnowBall(i);
            ball.broadcast(getMap(0), 0);
            ball.setInvis(false);
            ball.broadcast(getMap(0), 5);
            getMap(0).broadcastMessage(MaplePacketCreator.enterSnowBall());
        }
    }

    public void resetSnowBall(int teamz) {
        this.balls[teamz] = null;
    }

    public void makeSnowBall(int teamz) {
        resetSnowBall(teamz);
        this.balls[teamz] = new MapleSnowballs(teamz);
    }

    public MapleSnowballs getSnowBall(int teamz) {
        return this.balls[teamz];
    }

    public static class MapleSnowballs {

        private int position = 0;
        private final int team;
        private int startPoint = 0;
        private boolean invis = true;
        private boolean hittable = true;
        private int snowmanhp = 7500;
        private ScheduledFuture<?> snowmanSchedule = null;

        public MapleSnowballs(int team_) {
            this.team = team_;
        }

        public void resetSchedule() {
            if (this.snowmanSchedule != null) {
                this.snowmanSchedule.cancel(false);
                this.snowmanSchedule = null;
            }
        }

        public int getTeam() {
            return this.team;
        }

        public int getPosition() {
            return this.position;
        }

        public void setPositionX(int pos) {
            this.position = pos;
        }

        public void setStartPoint(MapleMap map) {
            this.startPoint += 1;
            broadcast(map, this.startPoint);
        }

        public boolean isInvis() {
            return this.invis;
        }

        public void setInvis(boolean i) {
            this.invis = i;
        }

        public boolean isHittable() {
            return (this.hittable) && (!this.invis);
        }

        public void setHittable(boolean b) {
            this.hittable = b;
        }

        public int getSnowmanHP() {
            return this.snowmanhp;
        }

        public void setSnowmanHP(int shp) {
            this.snowmanhp = shp;
        }

        public void broadcast(MapleMap map, int message) {
            for (MapleCharacter chr : map.getCharactersThreadsafe()) {
                chr.getClient().getSession().write(MaplePacketCreator.snowballMessage(this.team, message));
            }
        }

        public int getLeftX() {
            return this.position * 3 + 175;
        }

        public int getRightX() {
            return getLeftX() + 275;
        }

        public static void hitSnowball(MapleCharacter chr) {
            int team = chr.getTruePosition().y > -80 ? 0 : 1;
            MapleSnowball sb = (MapleSnowball) chr.getClient().getChannelServer().getEvent(MapleEventType.Snowball);
            MapleSnowballs ball = sb.getSnowBall(team);
            if ((ball != null) && (!ball.isInvis())) {
                boolean snowman = (chr.getTruePosition().x < -360) && (chr.getTruePosition().x > -560);
                if (!snowman) {
                    int damage = ((Math.random() < 0.01D) || ((chr.getTruePosition().x > ball.getLeftX()) && (chr.getTruePosition().x < ball.getRightX()))) && (ball.isHittable()) ? 10 : 0;
                    chr.getMap().broadcastMessage(MaplePacketCreator.hitSnowBall(team, damage, 0, 1));
                    if (damage == 0) {
                        if (Math.random() < 0.2D) {
                            chr.getClient().getSession().write(MaplePacketCreator.leftKnockBack());
                            chr.getClient().getSession().write(MaplePacketCreator.enableActions());
                        }
                    } else {
                        ball.setPositionX(ball.getPosition() + 1);

                        if ((ball.getPosition() == 255) || (ball.getPosition() == 511) || (ball.getPosition() == 767)) {
                            ball.setStartPoint(chr.getMap());
                            chr.getMap().broadcastMessage(MaplePacketCreator.rollSnowball(4, sb.getSnowBall(0), sb.getSnowBall(1)));
                        } else if (ball.getPosition() == 899) {
                            MapleMap map = chr.getMap();
                            for (int i = 0; i < 2; i++) {
                                sb.getSnowBall(i).setInvis(true);
                                map.broadcastMessage(MaplePacketCreator.rollSnowball(i + 2, sb.getSnowBall(0), sb.getSnowBall(1)));
                            }
                            chr.getMap().broadcastMessage(MaplePacketCreator.serverMessageNotice( new StringBuilder().append("Congratulations! Team ").append(team == 0 ? "Story" : "Maple").append(" has won the Snowball Event!").toString()));
                            for (MapleCharacter chrz : chr.getMap().getCharactersThreadsafe()) {
                                if (((team == 0) && (chrz.getTruePosition().y > -80)) || ((team == 1) && (chrz.getTruePosition().y <= -80))) {
                                    MapleSnowball.givePrize(chrz);
                                }
                                sb.warpBack(chrz);
                            }
                            sb.unreset();
                        } else if (ball.getPosition() < 899) {
                            chr.getMap().broadcastMessage(MaplePacketCreator.rollSnowball(4, sb.getSnowBall(0), sb.getSnowBall(1)));
                            ball.setInvis(false);
                        }
                    }
                } else if (ball.getPosition() < 899) {
                    int damage = 15;
                    if (Math.random() < 0.3D) {
                        damage = 0;
                    }
                    if (Math.random() < 0.05D) {
                        damage = 45;
                    }
                    chr.getMap().broadcastMessage(MaplePacketCreator.hitSnowBall(team + 2, damage, 0, 0));
                    ball.setSnowmanHP(ball.getSnowmanHP() - damage);
                    if (damage > 0) {
                        chr.getMap().broadcastMessage(MaplePacketCreator.rollSnowball(0, sb.getSnowBall(0), sb.getSnowBall(1)));
                        if (ball.getSnowmanHP() <= 0) {
                            ball.setSnowmanHP(7500);
                            final MapleSnowballs oBall = sb.getSnowBall(team == 0 ? 1 : 0);
                            oBall.setHittable(false);
                            final MapleMap map = chr.getMap();
                            oBall.broadcast(map, 4);
                            oBall.snowmanSchedule = EventTimer.getInstance().schedule(new Runnable() {
                                @Override
                                public void run() {
                                    oBall.setHittable(true);
                                    oBall.broadcast(map, 5);
                                }
                            }, 10000L);

                        }
                    }
                }
            }
        }
    }
}
