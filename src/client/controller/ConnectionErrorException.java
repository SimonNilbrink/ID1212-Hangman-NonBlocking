package client.controller;

public class ConnectionErrorException extends Exception {

    public ConnectionErrorException(String message, Exception cause){
        super(message,cause);
    }

}
