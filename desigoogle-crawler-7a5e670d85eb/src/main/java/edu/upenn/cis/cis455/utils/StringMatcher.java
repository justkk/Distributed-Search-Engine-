package edu.upenn.cis.cis455.utils;

public class StringMatcher {


    public static boolean wildCardMatcher(String inputString, String pattern) {

        if(pattern.charAt(pattern.length()-1)!='$') {
            pattern = pattern + "*";
        }

        if (pattern.charAt(pattern.length() - 1) == '$') {
            inputString = inputString + '$';
        }

        boolean state[][] = new boolean[inputString.length() + 1][pattern.length() + 1];

        for (int i = 0; i <= inputString.length(); i++) {
            state[i][0] = false;
        }

        for (int i = 1; i <= pattern.length(); i++) {
            if (pattern.charAt(i - 1) != '*')
                state[0][i] = false;
            else
                state[0][i] = state[0][i - 1];
        }

        state[0][0] = true;

        for (int i = 1; i <= inputString.length(); i++) {

            for (int j = 1; j <= pattern.length(); j++) {

                if (inputString.charAt(i - 1) == pattern.charAt(j - 1)) {
                    state[i][j] = state[i - 1][j - 1];
                } else if (pattern.charAt(j - 1) == '*') {
                    state[i][j] = state[i - 1][j - 1] || state[i - 1][j] || state[i][j - 1];
                }
            }
        }
        return state[inputString.length()][pattern.length()];
    }

    public static void main(String[] args) {

        //System.out.println(StringMatcher.wildCardMatcher("/intl/b/c/about", "/intl/*/about"));

    }


}
