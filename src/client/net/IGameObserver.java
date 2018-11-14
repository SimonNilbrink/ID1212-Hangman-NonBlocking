package client.net;

import common.Request;
import common.Response;


/**
 * Interface for the Observer pattern. Tells the view if changes to the game, or if connection is lost to the server.
 */
public interface IGameObserver {

    void gameChanges(Response response);
    void connectionLost();

}
