package no.ntnu.imt3281.ludo.logic.messages;

public class UserWantsToCreateGame extends Message {

    String[] toInviteDisplayNames;

    public UserWantsToCreateGame(String action) {
        super(action);
    }

    public void setToInviteDisplayNames(String[] toInviteDisplayNames) {
        this.toInviteDisplayNames = toInviteDisplayNames;
    }

    public String[] getToInviteDisplayNames() {
        return toInviteDisplayNames;
    }
}
