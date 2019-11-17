package no.ntnu.imt3281.ludo.server;

public class UserInfo {
    private final String userId;
    private String displayName;
    private byte[] avatarImage;
    private int gamesPlayed;
    private int gamesWon;

    public UserInfo(String userId, String displayName, byte[] avatarImage, int gamesPlayed, int gamesWon){
        this.userId = userId;
        this.displayName = displayName;
        this.avatarImage = avatarImage;
        this.gamesPlayed = gamesPlayed;
        this.gamesWon = gamesWon;
    }

    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public byte[] getAvatarImage() {
        return avatarImage;
    }

    public void setAvatarImage(byte[] avatarImage) {
        this.avatarImage = avatarImage;
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
