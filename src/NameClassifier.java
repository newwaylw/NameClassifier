import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * a simple NB name classifier using character ngram features
 */

public class NameClassifier {
    private HashMap<String, Integer> femaleNameMap;
    private HashMap<String, Integer> maleNameMap;
    private int totalFemaleNames;
    private int totalMaleNames;
    private int totalMaleNgrams;
    private int totalFemaleNgrams;
    private double femaleClassPriorProb;
    private int arity;

    private static String[] classLabel = {"Female", "Male"};

    /**
     * constructor
     * @param arity arity of the character ngram to generate as features
     */
    public NameClassifier(int arity) {
        this.femaleNameMap = new HashMap<String, Integer>();
        this.maleNameMap = new HashMap<String, Integer>();
        this.totalFemaleNames = 0;
        this.totalMaleNames = 0;
        this.totalFemaleNgrams = 0;
        this.totalMaleNgrams = 0;

        //default prior, will be updated once we read the training data
        this.femaleClassPriorProb = Math.log(0.5);
        this.arity = arity;

    }

    /**
     * train a NB classifier, using hashmap to keep frequency counts
     * of each feature belonging to each class
     * @param trainingFile
     */
    public void fit(String trainingFile) {
        BufferedReader br = null;
        String line = "";
        try {
            br = new BufferedReader(new FileReader(trainingFile));
            String name;
            boolean isFemale;
            boolean isTrain;
            HashMap<String, Integer> targetMap;
            while ((line = br.readLine()) != null) {

                // Tab as separator
                String[] elements = line.split("\t");
                isTrain = elements[3].equals("Train");
                // only use the Training data part of the file
                if (isTrain) {
                    name = elements[1];
                    isFemale = elements[2].equals("Female");
                    ArrayList<String> charNgrams = TextProcessor.ngram(name.toLowerCase(), this.arity);

                    //check which class label it is to update the correct hashmap
                    if (isFemale) {
                        targetMap = this.femaleNameMap;
                        this.totalFemaleNgrams += charNgrams.size();
                        this.totalFemaleNames += 1;
                    } else {
                        targetMap = this.maleNameMap;
                        this.totalMaleNgrams += charNgrams.size();
                        this.totalMaleNames += 1;
                    }

                    for (String ngram : charNgrams) {
                        if (targetMap.containsKey(ngram)) {
                            Integer value = targetMap.get(ngram);
                            targetMap.put(ngram, value + 1);
                        } else {
                            targetMap.put(ngram, 1);
                        }
                    }
                }
            }
            this.femaleClassPriorProb = Math.log(this.totalFemaleNames * 1.0 / (this.totalMaleNames + this.totalFemaleNames));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * predict whether a String name is a Female name or Male name,
     * with NB formula: p(C|W) -> p(W|C) * p(C)
     * @param name
     * @return boolean - prediction if the name is a female name
     */
    public boolean predict(String name) {
        ArrayList<String> charNgrams = TextProcessor.ngram(name, this.arity);
        double logProbFemale = 0.0;
        double logProbMale = 0.0;
        int uniqueFemaleNgrams = this.femaleNameMap.size();
        int uniqueMaleNgrams = this.maleNameMap.size();

        for (String ngram : charNgrams) {
            // count the character ngram frequency in female data
            if (this.femaleNameMap.containsKey(ngram)) {
                int freq = this.femaleNameMap.get(ngram);
                //estimating the p(W|"Female"),
                //Yes, everyone should use Kneser-Ney smoothing,
                //but add one smoothing is gold
                logProbFemale += Math.log((freq + 1.0) / (this.totalFemaleNgrams + uniqueFemaleNgrams));
            }
            // count the character ngram frequency in male data
            if (this.maleNameMap.containsKey(ngram)) {
                int freq = this.maleNameMap.get(ngram);
                //p(W|"Male")
                logProbMale += Math.log((freq + 1.0) / (this.totalMaleNgrams + uniqueMaleNgrams));
            }

        }
        logProbFemale += this.femaleClassPriorProb;
        logProbMale += 1.0 - this.femaleClassPriorProb;

        return logProbFemale > logProbMale;
    }

    /**
     * print classification confusion matrix with p/r/f1 metrics
     * @param confusionMatrix
     */
    public void displayMetric(int[][] confusionMatrix) {
        System.out.format("\t\t%s\t%s\t\t%s\t%s\t%s\n", classLabel[0], classLabel[1], "Precision", "Recall", "F1-score");
        for (int i = 0; i < confusionMatrix.length; i++) {
            double precision = confusionMatrix[i][i] * 1.0 / (confusionMatrix[i][0] + confusionMatrix[i][1]);
            double recall = confusionMatrix[i][i] * 1.0 / (confusionMatrix[0][i] + confusionMatrix[1][i]);
            double f1 = 2 * (precision * recall) / (precision + recall);
            System.out.format("%s\t%s\t%s\t\t%.3f\t%.3f\t%.3f\n",
                    classLabel[i],
                    confusionMatrix[i][0],
                    confusionMatrix[i][1],
                    precision,
                    recall,
                    f1);
        }
    }

    /**
     * usage java NameClassifier <filename>
     * @param args
     */
    public static void main(String[] args) {
        NameClassifier classifier = new NameClassifier(3);
        classifier.fit(args[0]);

        BufferedReader br = null;
        String line = "";
        int[][] confusionMatrix = new int[2][2];

        try {
            br = new BufferedReader(new FileReader(args[0]));
            String name;
            String predictedLabel;
            String trueLabel;
            while ((line = br.readLine()) != null) {
                // Tab as separator
                String[] elements = line.split("\t");
                boolean isTest = (elements[3].equals("Test"));
                if (isTest) {
                    name = elements[1];
                    trueLabel = elements[2];
                    boolean isFemaleName = classifier.predict(name);
                    if (isFemaleName) {
                        predictedLabel = "Female";
                    } else {
                        predictedLabel = "Male";
                    }

                    confusionMatrix[Arrays.asList(classLabel).indexOf(predictedLabel)]
                            [Arrays.asList(classLabel).indexOf(trueLabel)]++;
                }
            }

            classifier.displayMetric(confusionMatrix);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
