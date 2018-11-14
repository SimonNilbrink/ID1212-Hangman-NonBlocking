package common;

import java.io.Serializable;

public class Response implements Serializable {


    private boolean isDone = false;
    private char theWordSoFar[];
    private int totalScore;
    private int attemptsLeft;

    public Response(char[] theWordSoFar, int totalScore, int attemptsLeft) {
        this.theWordSoFar = theWordSoFar;
        this.totalScore = totalScore;
        this.attemptsLeft = attemptsLeft;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public char[] getTheWordSoFar() {
        return theWordSoFar;
    }

    public void setTheWordSoFar(char[] theWordSoFar) {
        this.theWordSoFar = theWordSoFar;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public int getAttemptsLeft() {
        return attemptsLeft;
    }

    public void setAttemptsLeft(int attemptsLeft) {
        this.attemptsLeft = attemptsLeft;
    }
}
