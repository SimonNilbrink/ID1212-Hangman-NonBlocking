package common;


import java.io.Serializable;

/**
 * Object that are sent to the server
 */
public class Request implements Serializable {

    private RequestType requestType;
    private char letterToGuess;
    private String wordToGuess;

    public Request(RequestType requestType) {
        this.requestType = requestType;
    }

    public void setLetterToGuess(char letterToGuess) {
        this.letterToGuess = letterToGuess;
    }

    public void setWordToGuess(String wordToGuess) {
        this.wordToGuess = wordToGuess;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public char getLetterToGuess() {
        return letterToGuess;
    }

    public String getWordToGuess() {
        return wordToGuess;
    }
}
