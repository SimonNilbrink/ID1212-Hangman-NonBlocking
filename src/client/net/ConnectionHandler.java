package client.net;

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
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;


/**
 * Class that handle the connection from client to server.
 */
public class ConnectionHandler implements Runnable{

    private IGameObserver gameObserver;
    private SocketChannel socketChannel;
    private Selector selector;
    private boolean isConnected;
    private ByteBuffer buffer = ByteBuffer.allocate(512);
    private Queue<Request> messagesToSend = new ArrayDeque<>();

    public ConnectionHandler(IGameObserver gameObserver) {
        this.gameObserver = gameObserver;
    }

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
            SelectionKey key;
            setUpSelectorForConnection();
            while(isConnected){
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while(keys.hasNext()){
                    key = keys.next();
                    if(key.isValid()) {
                        if (key.isConnectable()) {
                            socketChannel.finishConnect();
                            key.interestOps(SelectionKey.OP_WRITE);
                        }
                        else if(key.isReadable()){
                            readFromServer();
                            key.interestOps(SelectionKey.OP_WRITE);
                        }
                        else if(key.isWritable()){
                            writeToServer();
                            key.interestOps(SelectionKey.OP_READ);
                        }
                    }
                    keys.remove();
                }
            }
            disconnectClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setUpSelectorForConnection()throws IOException{
        selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_WRITE);
    }

    private void readFromServer()throws IOException{
        int bytesRead = socketChannel.read(buffer);
        if(bytesRead > 0) {
            buffer.flip();
            CompletableFuture.runAsync(()-> {
                        gameObserver.gameChanges(byteArrayToResponseObject(buffer.array()));
                    });
            buffer.clear();
        }
    }
    private void writeToServer()throws IOException{
        ByteBuffer tempBuffer = ByteBuffer.wrap(calculateAndPrependSizeOfObjectToBeSent(messagesToSend.remove()));
        while(tempBuffer.hasRemaining()) socketChannel.write(tempBuffer);
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
     * Takes the Request created in the public functions and sends it to the server.
     * @param request the protocol used for requests
     */
    private void sendGuess(Request request){
       messagesToSend.add(request);
       selector.wakeup();
    }

    private Response byteArrayToResponseObject(byte[] objectByteArray){
        Response response = null;
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(objectByteArray);
            ObjectInputStream in = new ObjectInputStream(byteArrayInputStream);
            response = (Response) in.readObject();
        } catch (IOException e){
            e.printStackTrace();
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        return response;
    }

    /**
     *
     * This function calculate the size of the Request object and prepend it to an byte array
     * that contains the object itself.
     *
     * @param request the object to be sent
     * @return an array with the object and the length to be sent
     **/
    private byte[] calculateAndPrependSizeOfObjectToBeSent(Request request){
        byte[] objectArray;
        byte[] objectAndLengthArray = null;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

            objectOutputStream.writeObject(request);
            objectOutputStream.flush();
            objectOutputStream.close();

            objectArray = byteArrayOutputStream.toByteArray();

            ByteBuffer byteBuffer = ByteBuffer.allocate(4);
            byteBuffer.putInt(objectArray.length);

            byte[] byteBufferArray = byteBuffer.array();
            objectAndLengthArray = new byte[byteBufferArray.length+objectArray.length];

            System.arraycopy(byteBufferArray, 0, objectAndLengthArray, 0, byteBufferArray.length);
            System.arraycopy(objectArray,0,objectAndLengthArray,byteBufferArray.length,objectArray.length);

        }catch (IOException e){
            e.printStackTrace();
        }

        return objectAndLengthArray;
    }
}

