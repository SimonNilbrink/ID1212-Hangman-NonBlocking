package server.model;
import common.Response;
import server.util.WordGenerator;

/**
 * This is the Game class that handles all the game logic for the Hangman game
 */
public class Game {

    private String word;
    private int attemptsLeft;
    private int rightGuesses = 0;
    private char[] theWordSoFar;
    private int totalScore = 0;
    private boolean isDone;
    private String guessedLetters;
    private Callback callback;

    public Game(Callback callback){
        this.callback = callback;
    }

    public void newGame(){
        pickAWord();
        this.attemptsLeft = word.length();
        theWordSoFar = new char[word.length()];
        rightGuesses = 0;
        guessedLetters = "";
        isDone = false;
        callback.callback(new Response(theWordSoFar,totalScore,attemptsLeft));
    }

    public interface Callback {
        void callback(Response response);
    }

    private void pickAWord() {
        this.word = WordGenerator.getWord();
    }

    public Response guessWithLetter(char guess) {
        boolean isWrongGuess = true;

        if (guessedLetters.indexOf(guess)<0 && !isDone) {
            for (int i = 0; i < this.word.length(); i++) {
                if(guess == word.charAt(i)) {
                   theWordSoFar[i] = guess;
                   isWrongGuess = false;
                   rightGuesses++;
                }
            }
            if (isWrongGuess) {
                attemptsLeft--;
                if (attemptsLeft == 0) {
                    totalScore--;
                    isDone = true;
                }
            }else if(rightGuesses==word.length()){
                isDone = true;
                attemptsLeft = -1;
                totalScore++;
            }
            guessedLetters = guessedLetters + guess;
        }
        Response response = new Response(theWordSoFar,totalScore,attemptsLeft);
        response.setDone(isDone);

        return response;
    }

    public Response guessWithWord(String guess) {
        if (!isDone) {
            if(this.word.equals(guess)) {
                totalScore++;
                isDone = true;
                attemptsLeft = -1;
            } else {
                attemptsLeft--;
                if (attemptsLeft == 0) {
                    totalScore--;
                    isDone = true;
                }
            }
        }
        Response response = new Response(theWordSoFar,totalScore,attemptsLeft);
        response.setDone(isDone);
        return response;
    }
}
