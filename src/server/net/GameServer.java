package server.net;

import java.io.IOException;
import java.net.InetSocketAddress;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Establishes connection between client and server.
 */
public class GameServer {

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private int portNr = 1337;


    /**
     * Method that look at each key in the key-set that the selector has and do the operation that is wanted.
     */
    public void serve(){
        try {
            setupListeningSocketChannel();
            setupSelector();
            SelectionKey key;

            while(true) {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while(keys.hasNext()){
                    key = keys.next();
                    if(key.isValid()) {
                        if (key.isAcceptable()) {
                            startClientHandler(key);
                        }
                        else if(key.isReadable()){
                            ClientHandler clientHandler = (ClientHandler) key.attachment();
                            try {
                                clientHandler.readFromClient();
                            }catch (IOException e){
                                key.cancel();
                            }
                        }
                        else if(key.isWritable()){
                            try {
                                ClientHandler clientHandler = (ClientHandler) key.attachment();
                                clientHandler.writeToClient();
                                key.interestOps(SelectionKey.OP_READ);
                            } catch (Exception e) {
                                key.cancel();
                            }
                        }
                    }
                    keys.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets up the servers listening channel and make it listen to the given portnumber. Then configure it to be
     * non-blocking.
     * @throws IOException
     */
    private void setupListeningSocketChannel() throws IOException{
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(portNr));
        serverSocketChannel.configureBlocking(false);
    }

    /**
     * Opens a new selector and setts it to be acceptable so the server can accept connections from clients in an
     * non-blocking manner.
     * @throws IOException
     */
    private void setupSelector() throws IOException{
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    /**
     * Is responsible to create a new clientHandler for a specific client.
     *
     */
    private void startClientHandler(SelectionKey key) throws IOException{
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        ClientHandler clientHandler = new ClientHandler(this,socketChannel);
        socketChannel.register(selector,SelectionKey.OP_READ,clientHandler);

    }


    Selector getSelector() {
        return selector;
    }
}
