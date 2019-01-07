package server.maps;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import tools.Pair;

public class MapleReactorStats {

    private Point tl;
    private Point br;
    private final Map<Byte, StateData> stateInfo;

    public MapleReactorStats() {
        this.stateInfo = new HashMap();
    }

    public void setTL(Point tl) {
        this.tl = tl;
    }

    public void setBR(Point br) {
        this.br = br;
    }

    public Point getTL() {
        return this.tl;
    }

    public Point getBR() {
        return this.br;
    }

    public void addState(byte state, int type, Pair<Integer, Integer> reactItem, byte nextState, int timeOut, byte canTouch) {
        stateInfo.put(state, new StateData(type, reactItem, nextState, timeOut, canTouch));
    }

    public byte getNextState(byte state) {
        StateData nextState = (StateData) this.stateInfo.get(Byte.valueOf(state));
        if (nextState != null) {
            return nextState.getNextState();
        }
        return -1;
    }

    public int getType(byte state) {
        StateData nextState = (StateData) this.stateInfo.get(Byte.valueOf(state));
        if (nextState != null) {
            return nextState.getType();
        }
        return -1;
    }

    public Pair<Integer, Integer> getReactItem(byte state) {
        StateData nextState = (StateData) this.stateInfo.get(Byte.valueOf(state));
        if (nextState != null) {
            return nextState.getReactItem();
        }
        return null;
    }

    public int getTimeOut(byte state) {
        StateData nextState = (StateData) this.stateInfo.get(Byte.valueOf(state));
        if (nextState != null) {
            return nextState.getTimeOut();
        }
        return -1;
    }

    public byte canTouch(byte state) {
        StateData nextState = (StateData) this.stateInfo.get(Byte.valueOf(state));
        if (nextState != null) {
            return nextState.canTouch();
        }
        return 0;
    }

    private static class StateData {

        private final int type;
        private final int timeOut;
        private final Pair<Integer, Integer> reactItem;
        private final byte nextState;
        private final byte canTouch;

        private StateData(int type, Pair<Integer, Integer> reactItem, byte nextState, int timeOut, byte canTouch) {
            this.type = type;
            this.reactItem = reactItem;
            this.nextState = nextState;
            this.timeOut = timeOut;
            this.canTouch = canTouch;
        }

        private int getType() {
            return this.type;
        }

        private byte getNextState() {
            return this.nextState;
        }

        private Pair<Integer, Integer> getReactItem() {
            return this.reactItem;
        }

        private int getTimeOut() {
            return this.timeOut;
        }

        private byte canTouch() {
            return this.canTouch;
        }
    }
}
