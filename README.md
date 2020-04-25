## Description

This is an implementation of Naïve Bayes classifier 
in the context of name classification (whether a name is a female name or male name).

Features used in the NB classifier are simply the character ngram of the names.
I used arity=3 that achieves the best performance.


## Results
The classifier is evaluated using the test set in `allnames.tsv`.
In the test set there are 9947 female names and 10,060 male names.
My NB classifier metrics (using arity=3) are below:

|   | Female |  Male | Precision  | Recall  | F1 score|
|---|---|---|---|---|---|
| Female  | 7550 | 1306  | 0.853  | 0.759  | 0.803 |
| Male  | 2397  | 8754  | 0.785  | 0.870  | 0.825|

## How to Run
```
java NameClassifier allnames.tsv
```
## Thoughts
Name classification using features from the string itself requires 
more work on the feature selection. Based on my previous experiences:
* It is very important to work from native names. In many languages it is easy to infer
gender, but the information is lost when transliterated (into English).
* Modelling name parts (first name, last name, middle name etc) usually helps. 
* It is a language dependant task, For example in many language family names
gives NO signal towards gender.
