package common;

import java.io.*;
import java.nio.ByteBuffer;

public class ObjectConverter {

    /**
     *
     * This function calculate the size of the Request object and prepend it to an byte array
     * that contains the object itself.
     *
     * @param object the object to be sent
     * @return an array with the object and the length to be sent
     **/
    public static byte[] calculateAndPrependSizeOfObjectToBeSent(Object object){
        byte[] objectArray = null;
        byte[] objectAndLengthArray = null;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

            objectOutputStream.writeObject(object);
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

        return objectArray;
    }

    /**
     * Takes the receive byte array from the client and output it as the object.
     *
     * @param objectByteArray the recieved byte array from client
     * @return the object that the client sent
     */
    public static Object byteArrayToObject(byte[] objectByteArray){
        Object object = null;
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(objectByteArray);
            ObjectInputStream in = new ObjectInputStream(byteArrayInputStream);
            object =  in.readObject();
        } catch (IOException e){
            e.printStackTrace();
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        return object;
    }
}
