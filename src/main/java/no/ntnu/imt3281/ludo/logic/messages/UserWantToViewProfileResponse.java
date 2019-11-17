package no.ntnu.imt3281.ludo.logic.messages;

public class UserWantToViewProfileResponse extends Message{

    private String userId;
    private String displayName;
    private byte[] imageString;
    private int gamesPlayed;
    private int gamesWon;

    public UserWantToViewProfileResponse(String action){super(action);}

    public UserWantToViewProfileResponse(String action, String userId, String displayName, byte[] imageString, int gamesPlayed, int gamesWon){
        super(action);
        this.userId = userId;
        this.displayName = displayName;
        this.imageString = imageString;
        this.gamesPlayed = gamesPlayed;
        this.gamesWon = gamesWon;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public byte[] getImageString() {
        return imageString;
    }

    public void setImageString(byte[] imageString) {
        this.imageString = imageString;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
    }

}
