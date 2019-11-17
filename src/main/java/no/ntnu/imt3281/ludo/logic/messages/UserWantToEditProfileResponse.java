package no.ntnu.imt3281.ludo.logic.messages;

public class UserWantToEditProfileResponse extends Message {

    boolean changed;
    String response;

    public UserWantToEditProfileResponse(String action){super(action);}

    public UserWantToEditProfileResponse(String action, boolean changed, String response){
        super(action);
        this.changed = changed;
        this.response = response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public String getResponse() {
        return response;
    }

    public boolean isChanged() {
        return changed;
    }

}
