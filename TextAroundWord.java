import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
// In comments below "word" mean set of symbols, that has at least 1 Alphabetic character or number
public interface TextAroundWord {

    /**
     * Represents data structure, that includes two fields:
     * String message and boolean isWord, that true if message has at least 1 Alphabetic character or number.
     */
    class messageCharacteristics{
        private final String message;
        private final boolean isWord;
        messageCharacteristics(String message, boolean isWord){
            this.message = message;
            this.isWord = isWord;
        }
    }
    /**
     * Processing InvalidRadius and InvalidKey exceptions. key must be word, radius must be > 0;
     */
    class InvalidRadiusException extends Exception
    {
        public String toString()
        {
            return "Error. Radius must be > 0.";
        }
    }
    class InvalidKeyException extends Exception
    {
        public String toString()
        {
            return "Error. Key-word must have at least 1 Alphabetic character or number.";
        }
    }
    /**
     * This method checking transferred file, finding all symbolic constructions that contains key-word,
     * for each create .txt file in created directory, named in format "key-word + "_Occurrences"". print
     * in each file:
     * if mode is false, exactly Radius words from key-word on left side, key-word, and
     * Radius words from key-word on right side.
     * if mode is true, only full sentences(words outside sentences are removed) from no more than
     * Radius words from key-word on left side, and no more than Radius words from key-word on right side.
     * !!!WARNING!!! HIGHLY RECOMENDED USE MODE == TRUE WITH BIG RADIUS (MORE THAN 25). OTHER WAY YOU MAY FIND
     * SOME FILES EMPTY OR ONLY WITH ONE SENTENCE, EVEN WITHOUT KEY-WORD.
     */
    static void textAroundWord(Path file, String key, int radius, boolean mode) {
        key = key.replaceAll(" ", "");
        try {
            if(!isValidParams(key,radius)){
                System.out.println("Program cannot run successfully");
                return;
            }
            int keyOccur = 1; // Count keys
            BufferedReader br = new BufferedReader(new FileReader(file.toFile()));
            File resDirectory = createResDirectory(key);
            String line;
            LinkedList<messageCharacteristics> wordsInRadius = new LinkedList<>(); // List with words/symbols
            int i = 1;
            while ((line = br.readLine()) != null) { // Read while file not empty
                if (i > 100) { // Process in portions
                    keyOccur = processLinkedList(wordsInRadius, resDirectory,radius, key, keyOccur, false, mode);
                } else {
                    ++i;
                    for (String word : line.split(" ")) { // add words to List
                        wordsInRadius.add(new messageCharacteristics(word, isWord(word)));
                    }
                }
            }
            // Process list remainder after while loop
            processLinkedList(wordsInRadius, resDirectory,radius, key, keyOccur, true, mode);
            br.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    // This method returns true, if word has at least 1 Alphabetic character or number
    private static boolean isWord(String word){
        word = word.replaceAll(" ", "");
        if(word.isEmpty() || word.contains(" "))
            return false;
        int len = word.length();
        for (int i = 0; i < len; i++) {
            if(Character.isDigit(word.charAt(i)) || Character.isAlphabetic(word.charAt(i)))
                return true;
        }
        return false;
    }
    // Return ArrayList, that contains all positions, that store key in LinkedList.
    private static ArrayList<Integer> keysPositionsInList(LinkedList<messageCharacteristics> wordsInRadius, String key){
        ArrayList<Integer> res = new ArrayList<>();
        for (messageCharacteristics element: wordsInRadius) {
            if(element.message.contains(key))
                res.add(wordsInRadius.indexOf(element));
        }
        return res;
    }

    // Creates file that contains message in special directory.
    private static void createFile(String message, String key, String directoryName, int keyOccur){
        try {
            File file = new File(directoryName + "\\" + key + "Occurrence№" + keyOccur + ".txt");
            PrintWriter pw = new PrintWriter(file);
            pw.print(message);
            pw.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // Returns String, that will be printed in file. If right border of message < radius flag := false.
    // Param Mode: if true, save message only in full sentences(words outside sentences are removed).
    // If false, save message with Exactly Radius words on sides from keyPos.
    private static String messageFromRadius(LinkedList<messageCharacteristics> wordsInRadius,
                                            int radius, int keyPos, boolean[] flag, boolean mode){
        int curLeftRadius = keyPos - 1;
        int curRightRadius = keyPos + 1;
        int counter = 1;
        int SizeWordsList = wordsInRadius.size();
        while(counter < radius ) {
            if (curLeftRadius < 0){
                curLeftRadius = 0;
                break;
            }
            if(wordsInRadius.get(curLeftRadius).isWord){
                ++counter;
            }
            --curLeftRadius;
        } // Finding left border of message
        counter = 1;
        while(counter < radius) {
            if (curRightRadius >= SizeWordsList){
                curRightRadius = SizeWordsList - 1;
                flag[0] = false;
                break;
            }
            if(wordsInRadius.get(curRightRadius).isWord){
                ++counter;
            }
            ++curRightRadius;
        } // Finding right border of message

        return messageByMode(wordsInRadius,curLeftRadius,curRightRadius,mode);
    }
    // If mode == true return message with only full sentences. Else create res exactly by start/end
    private static  String messageByMode(LinkedList<messageCharacteristics> wordsInRadius, int start, int end, boolean mode){
        StringBuilder res = new StringBuilder();
        if(mode) {
            int curEnd = start;
            for (int i = start; i <= end; i++) { // Finding start of message
                if (iesSentenceEnd(wordsInRadius.get(i).message)) {
                    start = i + 1;
                    break;
                }
            }
            for (int i = start; i <= end; i++) { // Finding end of message
                if (iesSentenceEnd(wordsInRadius.get(i).message))
                    curEnd = i;
            }
            for (int i = start; i <= curEnd; i++) { // Creating message
                res.append(wordsInRadius.get(i).message);
                res.append(" ");
            }
        }else {
            for (int i = start; i <= end; i++) { // Creating message
                res.append(wordsInRadius.get(i).message);
                res.append(" ");
            }
        }
        return res.toString();
    }
    // Check if this word end of sentence.
    private static boolean iesSentenceEnd(String word){
        if(word.isEmpty())
            return false;
        char endChar = word.charAt(word.length() - 1);
        return endChar == '.' || endChar == '?' || endChar == '!';
    }
    // Delete in list word, that further than left Radius from startPosition.
    private static void deleteFurtherThanLeftRadius(LinkedList<messageCharacteristics> wordsInRadius,
                                                 int radius, int startPosition){
        while(radius > 0) {
            if (startPosition < 0)
                return;
            if (wordsInRadius.get(startPosition).isWord)
                --radius;
            --startPosition;
        }
        for (int i = 0; i <= startPosition; i++)
            wordsInRadius.removeFirst();
    }
    // Process LinkedList to write messages in files. Param Mode define how function that creates message will work
    private static int processLinkedList(LinkedList<messageCharacteristics> wordsInRadius, File resDirectory,
                                          int radius, String key, int keyOccur, boolean isEnd, boolean mode){
        if(mode && radius<15) //set default value, based on average length of sentences in english
            radius = 15;
        ArrayList<Integer> keysPositionsInList = keysPositionsInList(wordsInRadius,key); // Keys positions in List
        if(keysPositionsInList.isEmpty())
            deleteFurtherThanLeftRadius(wordsInRadius, radius,wordsInRadius.size()-1);
        if(isEnd){
            for (Integer integer : keysPositionsInList) { // Process keys besides last
                boolean[] isFurtherThenRadius = {true};
                String message = messageFromRadius(wordsInRadius, radius,
                        integer, isFurtherThenRadius, mode);
                createFile(message, key, resDirectory.getName(), keyOccur); // Write message in file
                ++keyOccur;
            }
        }else {
            int lastKeyPos = keysPositionsInList.get(keysPositionsInList.size() - 1);
            for (int j = 0; j < keysPositionsInList.size() - 1; j++) { // Process keys besides last
                boolean[] isFurtherThenRadius = {true};
                String message = messageFromRadius(wordsInRadius, radius,
                        keysPositionsInList.get(j), isFurtherThenRadius, mode);
                if (isFurtherThenRadius[0]) {
                    createFile(message, key, resDirectory.getName(),keyOccur); // Write message in file
                    ++keyOccur;
                } else {
                    lastKeyPos = j;
                    break;
                }
            }
            deleteFurtherThanLeftRadius(wordsInRadius, radius, lastKeyPos);
        }
        return keyOccur;
    }
    // Create directory for res files
    private static File createResDirectory(String key){
        File resDirectory = new File(key + "_Occurrences");// Create directory
        if(!resDirectory.exists()){
            if(!resDirectory.mkdir()){
                System.out.println("Directory cannot be created");
            }
            return resDirectory;
        }else{
            for (int i = 2; true; i++) {
                File newResDirectory = new File(key + "_Occurrences" + i);// Create directory
                if(newResDirectory.mkdir())
                    return newResDirectory;
            }
        }
    }
    // Throws exceptions, if function take invalid arguments
    private static boolean isValidParams(String key, int radius){
        try {
            if (!isWord(key))  // Check key
                throw new InvalidKeyException();
        }catch (InvalidKeyException e){
            System.out.println("Exception: " + e);
            return false;
        }
        try {
            if (radius < 1) { // Check radius
                throw new InvalidRadiusException();
            }
        }catch (InvalidRadiusException e){
            System.out.println("Exception: " + e);
            return false;
        }
        return true;
    }
}
