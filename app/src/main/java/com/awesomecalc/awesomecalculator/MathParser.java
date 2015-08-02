package com.awesomecalc.awesomecalculator;

import java.util.ArrayList;

/**
 * Created by Thilo on 15.06.2015.
 */
public class MathParser extends Object {

    public static class ParseException extends RuntimeException {
        public ParseException(String e)
        {
            super(e);
        }
    };

    public static boolean isOperator(Character c)
    {
        return  c == '+' ||
                c == '-' ||
                c == '/' ||
                c == '%' ||
                c == '*' ;
    }

    public static boolean isToplevelToken(String token)
    {
        return tokenizeForView(token).size() == 1 && token.charAt(0) == '#';
    }

    public static boolean isFurtherTokenizable(String token)
    {
        return tokenizeForView(token).size() > 1 || isToplevelToken(token);
    }

    /**
     * @param originStr the string to look in
     * @param subStr the string to look for
     * @param index index to look at for the substring
     * @return true if originStr has the specified substring at index
     */
    public static boolean hasSubstringAt(String originStr, String subStr, int index)
    {
        if(originStr.length() < subStr.length() + index)
            return false;

        return subStr.equals(originStr.substring(index, index+subStr.length()));
    }

    private static String removeSpaces(String str)
    {
        return str.replaceAll("\\s+","");
    }

    /**
     * @param mathExpr mathematical expression to be tokenized
     * @return returns single token if expression can not be tokenized any further. Tokens can itself contain other tokens
     */
    public static ArrayList<String> tokenizeForView(String mathExpr)
    {
        ArrayList<String> output = new ArrayList<String>();
        int bracketDepth = 0;
        String temp = "";

        mathExpr = removeSpaces(mathExpr);

        //parse ',' delimited tokens
        for (int i = 0; i < mathExpr.length(); i++)
        {
            if(mathExpr.charAt(i) == '(') {
                bracketDepth++;
                temp += '(';
            }
            else if(mathExpr.charAt(i) == ')') {
                bracketDepth--;
                temp += ')';
            }
            else if(mathExpr.charAt(i) == ',' && bracketDepth == 0) {
                output.add(temp);
                temp = "";
            }
            else {
                temp += mathExpr.charAt(i);
            }
        }

        if(bracketDepth != 0 && mathExpr.length() > 1)
            throw new ParseException("can not tokenize: brackets don't add up");

        if(!output.isEmpty()) {
            output.add(temp);
            return output;
        }
        else {
            temp = "";
        }

        //parse normal tokens
        for (int i = 0; i < mathExpr.length();)
        {
            if(isOperator(mathExpr.charAt(i))) //parse operators
            {
                output.add(Character.toString(mathExpr.charAt(i)));
                i++;
            }
            else if (mathExpr.charAt(i) == '#') //parse functions
            {
                temp += '#';
                i++;

                while(i < mathExpr.length()
                        && (bracketDepth > 0 || Character.isLetter(mathExpr.charAt(i)) ||  mathExpr.charAt(i) == '('))
                {
                    temp += mathExpr.charAt(i);

                    if(mathExpr.charAt(i) == '(')
                        bracketDepth++;

                    else if(mathExpr.charAt(i) == ')')
                        bracketDepth--;

                    i++;
                }

                output.add(temp);
                temp = "";
            }
            else if(mathExpr.charAt(i) == '(' || mathExpr.charAt(i) == ')')
            {
                output.add((Character.toString(mathExpr.charAt(i))));
                i++;
            }
            else //parse numbers and variables
            {
                while(i < mathExpr.length()
                        && !(mathExpr.charAt(i) == '(' || mathExpr.charAt(i) == ')' || mathExpr.charAt(i) == '#' || isOperator(mathExpr.charAt(i)) ))
                {
                    temp += mathExpr.charAt(i);
                    i++;
                }

                output.add(temp);
                temp = "";
            }
        }

        return output;
    }

    public static ArrayList<String> tokenizeForEval(String mathExpr)
    {
        ArrayList<String> output = new ArrayList<String>();
        int bracketDepth = 0;
        String temp = "";

        mathExpr = removeSpaces(mathExpr);

        //parse ',' delimited tokens
        for (int i = 0; i < mathExpr.length(); i++)
        {
            if(mathExpr.charAt(i) == '(') {
                bracketDepth++;
                temp += '(';
            }
            else if(mathExpr.charAt(i) == ')') {
                bracketDepth--;
                temp += ')';
            }
            else if(mathExpr.charAt(i) == ',' && bracketDepth == 0) {
                output.add(temp);
                temp = "";
            }
            else {
                temp += mathExpr.charAt(i);
            }
        }

        if(bracketDepth != 0)
            throw new ParseException("can not tokenize: brackets don't add up");

        if(!output.isEmpty()) {
            output.add(temp);
            return output;
        }
        else {
            temp = "";
        }

        //parse normal tokens
        for (int i = 0; i < mathExpr.length();)
        {
            if(isOperator(mathExpr.charAt(i))) //parse operators
            {
                output.add(Character.toString(mathExpr.charAt(i)));
                i++;
            }
            else if (mathExpr.charAt(i) == '#') //parse functions
            {
                temp += '#';
                i++;

                while(i < mathExpr.length()
                        && (bracketDepth > 0 || Character.isLetter(mathExpr.charAt(i)) ||  mathExpr.charAt(i) == '('))
                {
                    temp += mathExpr.charAt(i);

                    if(mathExpr.charAt(i) == '(')
                        bracketDepth++;

                    else if(mathExpr.charAt(i) == ')')
                        bracketDepth--;

                    i++;
                }

                output.add(temp);
                temp = "";
            }
            else if(mathExpr.charAt(i) == '(') //parse bracketed subtokens
            {
                while(i < mathExpr.length()
                        && (bracketDepth > 0 || mathExpr.charAt(i) == '('))
                {
                    temp += mathExpr.charAt(i);

                    if(mathExpr.charAt(i) == '(')
                        bracketDepth++;

                    else if(mathExpr.charAt(i) == ')')
                        bracketDepth--;

                    i++;
                }

                output.add(temp);
                temp = "";
            }
            else //parse numbers and variables
            {
                while(i < mathExpr.length()
                        && !(mathExpr.charAt(i) == '(' || mathExpr.charAt(i) == '#' || isOperator(mathExpr.charAt(i)) ))
                {
                    temp += mathExpr.charAt(i);
                    i++;
                }

                output.add(temp);
                temp = "";
            }
        }

        return output;
    }
}
