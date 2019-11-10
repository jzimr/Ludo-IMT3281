package no.ntnu.imt3281.ludo.logic.messages;

public class SendGameInvitationsResponse extends Message {

    String gameid;
    String hostdisplayname;

    public SendGameInvitationsResponse(String action){super(action);}

    public SendGameInvitationsResponse(String action, String gameid, String hostdisplayname){
        super(action);
        this.gameid = gameid;
        this.hostdisplayname = hostdisplayname;
    }

    public void setGameid(String gameid) {
        this.gameid = gameid;
    }

    public String getGameid() {
        return gameid;
    }

    public void setHostdisplayname(String hostdisplayname) {
        this.hostdisplayname = hostdisplayname;
    }

    public String getHostdisplayname() {
        return hostdisplayname;
    }
}
