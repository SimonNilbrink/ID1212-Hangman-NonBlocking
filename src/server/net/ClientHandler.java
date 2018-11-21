package server.net;

import common.ObjectConverter;
import common.Request;
import common.Response;
import server.model.Game;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

/**
 * Handles all communication with one specific game client, makes sure to serve the client with what it requests.
 */
public class ClientHandler{

    private Game game = new Game(new ThreadResponse());
    private boolean isConnected;
    private SocketChannel clientChannel;
    private final Queue<Response> responsesToSend = new ArrayDeque<>();
    private ByteBuffer buffer = ByteBuffer.allocate(512);
    private GameServer server;

    ClientHandler(GameServer server,SocketChannel clientChannel) {
        this.server = server;
        isConnected = true;
        this.clientChannel = clientChannel;

    }

    public void readFromClient() throws IOException {
        Response response;
        Request request;

        try {
            int bytesRead = clientChannel.read(buffer);
            if (bytesRead > 0) {
                buffer.flip();
                request = (Request) ObjectConverter.byteArrayToObject(buffer.array());
                buffer.clear();
            }else throw new IOException();

            switch (request.getRequestType()) {
                case QUIT:
                    clientDisconnect();
                    break;
                case NEW_GAME:
                    CompletableFuture.runAsync(()->game.newGame());
                    break;
                case GUESSLETTER:
                    response = game.guessWithLetter(request.getLetterToGuess());
                    addResponseToQueue(response);
                    break;
                case GUESSWORD:
                    response = game.guessWithWord(request.getWordToGuess());
                    addResponseToQueue(response);
                    break;
                default:
                    System.out.println("no ");
            }
        } catch (Exception ex) {
            clientDisconnect();
        }
    }

    /**
     * Responsible to write data to the ByteBuffer, then send through a specific channel.
     */
     void writeToClient()  throws IOException{
         synchronized (responsesToSend) {
             while (responsesToSend.peek() != null) {
                 ByteBuffer tempBuffer = ByteBuffer.wrap(ObjectConverter.calculateAndPrependSizeOfObjectToBeSent(responsesToSend.remove()));
                 while (tempBuffer.hasRemaining()) clientChannel.write(tempBuffer);
             }
         }
    }

    /**
     *
     * @param response
     */
    private synchronized void addResponseToQueue(Response response){
        synchronized (responsesToSend) {
            responsesToSend.add(response);
        }
        clientChannel.keyFor(server.getSelector()).interestOps(SelectionKey.OP_WRITE);
        server.getSelector().wakeup();
    }

    private void clientDisconnect() throws IOException{
       clientChannel.close();
    }

    /**
     * Callback function for the thread that handles the I/O-operation (new game)
     */
    private class ThreadResponse implements Game.Callback {
        public void callback(Response response){
            addResponseToQueue(response);
        }
    }


}
