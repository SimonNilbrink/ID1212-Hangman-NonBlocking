package client.view;

import client.net.ConnectionHandler;

import javax.swing.*;
import java.io.IOException;

/**
 * The class UserInterface takes care of all the input from the user.
 */
public class UserInterface implements Runnable{

    private ConnectionHandler connectionHandler;

    public UserInterface(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    /**
     * The user interface runs in a separate thread.
     */
    @Override
    public void run() {
        boolean running = true;
        boolean gameStarted = false;
        java.util.Scanner input = new java.util.Scanner(System.in);
        String guess;
        connect();
        System.out.println("'*quit' to exit game, '*new game' to start a new game.");
        while(running){
            System.out.print("");
            guess = input.nextLine();
            if(guess.equals(""))
                continue;
            else if(guess.charAt(0)=='*') {
                switch (guess) {
                    case "*quit":
                        connectionHandler.quitGame();
                        running = false;
                        break;
                    case "*new game":
                        connectionHandler.newGame();
                        gameStarted = true;
                        break;
                    default:
                        System.out.println("Possible command is *quit and *new game");
                        break;
                }
            }
            else if(guess.length()==1 && gameStarted)
                connectionHandler.sendLetterToGuess(guess.charAt(0));
            else if(gameStarted)
                connectionHandler.sendWordToGuess(guess);
            else
                System.out.println("You need to start a new game before guessing.");
        }
    }


    /**
     * Open an simple option panel to insert IP and PORT number to the Game server.
     * Rest of the game is in console.
     */
    private void connect(){
        boolean notConnected = true;
        while(notConnected) {
            JTextField ip = new JTextField();
            JTextField port = new JTextField();
            Object[] fields = {
                    "IP", ip,
                    "PORT", port,
            };
            JOptionPane.showConfirmDialog(null, fields, "Connect to Server", JOptionPane.OK_CANCEL_OPTION);
            try {
                connectionHandler.connect("localhost",1337);
                notConnected = false;
            }catch (IOException e) {
                System.out.println("No connection could be established");
            }
        }
    }
}
