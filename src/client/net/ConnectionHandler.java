package client.net;

import common.ObjectConverter;
import common.Request;
import common.Response;

import static common.RequestType.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;


/**
 * Class that handle the connection from client to server.
 */
public class ConnectionHandler implements Runnable{

    private IGameObserver gameObserver;
    private SocketChannel socketChannel;
    private Selector selector;
    private boolean isConnected;
    private ByteBuffer buffer = ByteBuffer.allocate(512);
    private final Queue<Request> requestsToSend = new ArrayDeque<>();
    private boolean timeToSend;

    public ConnectionHandler(IGameObserver gameObserver) {
        this.gameObserver = gameObserver;
    }

    /**
     * Sets up the socketchannel to be non-blocking and connects it to given host and ip
     * @param host
     * @param port
     * @throws IOException
     */

    public void connect(String host, int port) throws IOException{
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(host,port));
        isConnected = true;
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            setUpSelectorForConnection();
            while(isConnected){
                if (timeToSend) {
                    socketChannel.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
                    timeToSend = false;
                }
                selector.select();
                for(SelectionKey key : selector.selectedKeys()){
                    selector.selectedKeys().remove(key);
                    if(key.isValid()) {
                        if (key.isConnectable()) {
                            socketChannel.finishConnect();
                        }
                        else if(key.isReadable()){
                            readFromServer();
                        }
                        else if(key.isWritable()){
                            writeToServer(key);
                        }
                    }
                }
            }
            disconnectClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens a new selector and setts it to be connectable so it can connect to the server
     * @throws IOException
     */
    private void setUpSelectorForConnection()throws IOException{
        selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
    }


    /**
     * Reads from the server channel to get the response from server
     * @throws IOException
     */
    private void readFromServer()throws IOException{
        int bytesRead = socketChannel.read(buffer);
        if(bytesRead > 0) {
            buffer.flip();
            gameObserver.gameChanges((Response) ObjectConverter.byteArrayToObject(buffer.array()));
            buffer.clear();
        }else throw new IOException();
    }

    /**
     * Write to the server channel, takes all the messages in the queue and send them.
     * @param key
     * @throws IOException
     */
    private void writeToServer(SelectionKey key)throws IOException{
        synchronized (requestsToSend) {
            while (requestsToSend.peek() != null) {
                ByteBuffer tempBuffer = ByteBuffer.wrap(ObjectConverter.calculateAndPrependSizeOfObjectToBeSent(requestsToSend.remove()));
                while (tempBuffer.hasRemaining()) socketChannel.write(tempBuffer);
            }
        }
        key.interestOps(SelectionKey.OP_READ);
        selector.wakeup();
    }

    private void disconnectClient()throws IOException{
        socketChannel.close();
        socketChannel.keyFor(selector).cancel();
    }

    /**
     * Request the server to set up a new game
     */
    public void newGame(){
        sendGuess(new Request(NEW_GAME));
    }

    /**
     * Calls the function sendGuess with a guess formated after the decided protocol
     * @param letterToGuess the letter that the user want to guess
     */
    public void sendLetterToGuess(char letterToGuess){
        Request request = new Request(GUESSLETTER);
        request.setLetterToGuess(letterToGuess);
        sendGuess(request);
    }

    /**
     * Calls the function sendGuess with a guess formated after the decided protocol
     * @param wordToGuess if the user guesses a whole word
     */
    public void sendWordToGuess(String wordToGuess){
        Request request = new Request(GUESSWORD);
        request.setWordToGuess(wordToGuess);
        sendGuess(request);
    }

    /**
     *Send an Request with the type QUIT to tell the server to close its connection to client
     */
    public void quitGame(){
        sendGuess(new Request(QUIT));
        isConnected = false;
    }

    /**
     *
     * Takes the Request created in the public functions and sends it to the server,
     * sets the variable timeToSend to true, so that the connection thread can set the key
     * for the client channel to write operation.
     * @param request the protocol used for requests
     */
    private void sendGuess(Request request){
        synchronized (requestsToSend) {
            requestsToSend.add(request);
        }
        timeToSend = true;
        selector.wakeup();
    }




}

