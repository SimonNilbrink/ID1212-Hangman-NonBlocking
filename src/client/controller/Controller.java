package client.controller;

import client.net.ConnectionHandler;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class Controller {

    private final ConnectionHandler connectionHandler;

    public Controller(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    public void connect(String ip, int port)throws ConnectionErrorException {
        try {
            connectionHandler.connect(ip, port);
        } catch (IOException e) {
            throw new ConnectionErrorException("Connection couldt not be established to the server", e);
        }
    }
    public void quitGame(){
        connectionHandler.quitGame();
    }

    public void guessLetter(char guessedLetter){
        CompletableFuture.runAsync(() -> {
            connectionHandler.sendLetterToGuess(guessedLetter);
           });

    }
    public void guessWord(String wordToGuess){
        CompletableFuture.runAsync(() -> {
                connectionHandler.sendWordToGuess(wordToGuess);
            });
    }
    public void  newGame(){
        CompletableFuture.runAsync(()-> {
            connectionHandler.newGame();
        });
    }
}
