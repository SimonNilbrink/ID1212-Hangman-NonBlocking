package client.startup;

import client.net.ConnectionHandler;
import client.view.GameView;
import client.view.UserInterface;

public class Main {


    public static void main(String[] args){
        GameView gameView = new GameView();
        ConnectionHandler connectionHandler = new ConnectionHandler(gameView);
        UserInterface userInterface = new UserInterface(connectionHandler);
        new Thread(userInterface).start();

    }
}
