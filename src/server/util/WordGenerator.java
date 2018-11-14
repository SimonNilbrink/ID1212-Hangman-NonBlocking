package server.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

public class WordGenerator {

    public static String getWord(){
        String word = null;
        int pickWordNr;
        int nrOfWordsInList = 51528;
        Random random = new Random();

        try {
            FileReader fileReader = new FileReader("words.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            pickWordNr = random.nextInt(nrOfWordsInList);
            for (int i = 0; i<=pickWordNr; i++){
                word = bufferedReader.readLine();
                word = word.toLowerCase();
            }
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            ex.printStackTrace();
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
        return word;
    }
}
