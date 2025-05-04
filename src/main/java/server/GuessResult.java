package server;

import java.io.Serializable;

public class GuessResult implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status {
        TOO_SMALL, TOO_BIG, CORRECT, ERROR
    }

    private Status status;
    private int scoreChange;
    private int currentScore;
    private int remainingTrials;
    private String message;

    public GuessResult(Status status, int scoreChange, int currentScore, int remainingTrials, String message) {
        this.status = status;
        this.scoreChange = scoreChange;
        this.currentScore = currentScore;
        this.remainingTrials = remainingTrials;
        this.message = message;
    }

    public Status getStatus() {
        return status;
    }

    public int getScoreChange() {
        return scoreChange;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public int getRemainingTrials() {
        return remainingTrials;
    }

    public String getMessage() {
        return message;
    }
}