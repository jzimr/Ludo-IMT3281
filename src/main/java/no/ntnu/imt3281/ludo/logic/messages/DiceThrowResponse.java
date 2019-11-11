package no.ntnu.imt3281.ludo.logic.messages;

public class DiceThrowResponse extends Message{

    String gameid;
    int dicerolled;

    public DiceThrowResponse(String action) {super(action);}

    public void setGameid(String gameid) {
        this.gameid = gameid;
    }

    public String getGameid() {
        return gameid;
    }

    public void setDicerolled(int dicerolled) {
        this.dicerolled = dicerolled;
    }

    public int getDicerolled() {
        return dicerolled;
    }
}
