package test;

import edu.stanford.nlp.simple.*;

import java.util.*;


public class BasicPipelineExample {

    public static String text = "Joe Smith was born in California. " +
            "In 2017, he went to Paris, France in the summer. " +
            "His flight left at 3:00pm on July 10th, 2017. " +
            "After eating some escargot for the first time, Joe said, \"That was delicious!\" " +
            "He sent a postcard to his sister Jane Smith. " +
            "After hearing about Joe's trip, Jane decided she might go to France one day. parser";

    public static void main(String[] args) {

        Document document = new Document(text);
        for (Sentence s : document.sentences())
            System.out.println(s.lemmas());

    }

}

