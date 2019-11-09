package no.ntnu.imt3281.ludo.logic.messages;

public class UserWantsToCreateGame extends Message {

    String hostid;
    String[] toinvitedisplaynames;

    public UserWantsToCreateGame(String action, String hostid, String[] toinvitedisplaynames) {
        super(action);
        this.hostid = hostid;
        this.toinvitedisplaynames = toinvitedisplaynames;
    }

    public void setHostid(String hostid) {
        this.hostid = hostid;
    }

    public String getHostid() {
        return hostid;
    }

    public void setToinvitedisplaynames(String[] toinvitedisplaynames) {
        this.toinvitedisplaynames = toinvitedisplaynames;
    }

    public String[] getToinvitedisplaynames() {
        return toinvitedisplaynames;
    }
}
