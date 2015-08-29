package handling.world.party;

import handling.world.WrodlPartyService;
import java.util.concurrent.ScheduledFuture;
import server.Timer.EtcTimer;

public class PartySearch {

    private final String name;
    private final int partyId;
    private final PartySearchType pst;
    private ScheduledFuture<?> removal;

    public PartySearch(String name, int partyId, PartySearchType pst) {
        this.name = name;
        this.partyId = partyId;
        this.pst = pst;
        scheduleRemoval();
    }

    public PartySearchType getType() {
        return this.pst;
    }

    public int getId() {
        return this.partyId;
    }

    public String getName() {
        return this.name;
    }

    public final void scheduleRemoval() {
        cancelRemoval();
        this.removal = EtcTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                String msg = "超出限制时间，组队广告已被删除。";
                if (PartySearch.this.pst.exped) {
                    msg = "超出限制时间，远征队广告已被删除。";
                }
                WrodlPartyService.getInstance().removeSearch(PartySearch.this, msg);
            }
        }, this.pst.timeLimit * 60 * 1000);
    }

    public void cancelRemoval() {
        if (this.removal != null) {
            this.removal.cancel(false);
            this.removal = null;
        }
    }
}
