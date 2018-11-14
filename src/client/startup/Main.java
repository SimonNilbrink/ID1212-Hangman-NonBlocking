package client.startup;

import client.controller.Controller;
import client.net.ConnectionHandler;
import client.view.GameView;
import client.view.UserInterface;

public class Main {


    public static void main(String[] args){
        GameView gameView = new GameView();
        ConnectionHandler connectionHandler = new ConnectionHandler(gameView);
        Controller controller = new Controller(connectionHandler);
        UserInterface userInterface = new UserInterface(controller);
        new Thread(userInterface).start();

    }
}
