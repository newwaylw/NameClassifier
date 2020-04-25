import java.util.ArrayList;

/**
 * Utility class
 */
public class TextProcessor {

    /**
     * tokenize a string into an array of tokens
     * @param in
     * @param lowercase
     * @return
     */
    public static String[] tokenize(String in, boolean lowercase){
        String pattern = "\\s+";
        if (lowercase){
            return in.toLowerCase().split(pattern);
        }else{
            return in.split(pattern);
        }

    }

    /**
     * generate character ngram from a string
     * @param word input string
     * @param n arity
     * @return
     */
    public static ArrayList<String> ngram(String word, int n){
        int stringLen = word.length();
        ArrayList<String> ngramsList = new ArrayList<String>();

        //limit the arity to the length of the input string
        for(int i=0; i< Math.min(n, stringLen); i++){
            for (int offset=0; offset < stringLen - i; offset++) {
                ngramsList.add(word.substring(offset, offset + i + 1));
            }
        }
            return ngramsList;
    }

    /**
     * only for local testing
     * @param argv
     */
    public static void main(String[] argv){
        String test = "I am a test      sentence.";
        String[] out = TextProcessor.tokenize(test, true);
        for ( String word: out) {
            System.out.println(word);
        }

        System.out.println(TextProcessor.ngram("James", 6));
    }

}
