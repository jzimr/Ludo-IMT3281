package no.ntnu.imt3281.ludo.logic.messages;

public class UserWantsUsersList extends Message {
    String userid;
    String searchquery;

    public UserWantsUsersList(String action, String userid, String searchquery) {
        super(action); 
        this.userid = userid;
        this.searchquery = searchquery;
    }


    public String getSearchquery() {
        return searchquery;
    }

    public void setSearchquery(String searchquery) {
        this.searchquery = searchquery;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
