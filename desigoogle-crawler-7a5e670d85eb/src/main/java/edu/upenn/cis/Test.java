package edu.upenn.cis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {


    public static void main(String[] args) {
        String s = "//a/b/c[contains   (  text   ( ) , \"some     Substring\")]";

        String[] list = s.split("/(?![^\"]*\"(?:(?:[^\"]*\"){2})*[^\"]*$)");
        System.out.println(list);


        String c = list[4];

        System.out.println(c);

        String[] answer = c.split("(\\[|\\])(?![^\"]*\"(?:(?:[^\"]*\"){2})*[^\"]*$)");
        System.out.println(answer);

        String attr = answer[1];

        String matchRegex1="contains *?\\( *?text *?\\( *?\\) *?, *?\"(.*)\" *?\\)";

        Pattern pattern = Pattern.compile(matchRegex1);
        Matcher matcher = pattern.matcher(attr);
        matcher.matches();
        System.out.println(matcher.group(1));

    }


}
