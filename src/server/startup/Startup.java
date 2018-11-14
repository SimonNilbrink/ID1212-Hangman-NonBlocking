package server.startup;
import server.net.GameServer;

public class Startup {

    public static void main(String [] args){
        GameServer gameServer = new GameServer();
        gameServer.serve();
    }
}
