package no.ntnu.imt3281.ludo.logic.messages;

public class UsersListResponse extends Message {

    String[] displaynames;

    public UsersListResponse(String action) {super(action);}

    public UsersListResponse(String action, String[] displaynames) {
        super(action);
        this.displaynames = displaynames;
    }

    public void setDisplaynames(String[] displaynames) {
        this.displaynames = displaynames;
    }

    public String[] getDisplaynames() {
        return displaynames;
    }
}
