
package edu.upenn.cis;

import org.apache.commons.lang3.tuple.Pair;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Grammar:
 * op    := + | -
 * num   := [0-9]+
 * pexpr := ( expr )
 * aexpr := num | pexpr
 * expr  := aexpr (op aexpr)*
 */
public class ParserTest {

    public enum Op {
        PLUS, MINUS
    }

    public class Expr {
        // An expression is either a number
        public int d_num;

        // Or a binary operation
        public Expr d_lhs;
        public Expr d_rhs;
        public Op d_op;

        public String description() {
            if (d_lhs == null) {
                return Integer.toString(d_num);
            } else {
                String op = (d_op == Op.PLUS) ? "+" : "-";

                return
                        "(" + d_lhs.description()
                                + " " + op + " " +
                                d_rhs.description() + ")";
            }
        }
    }

    // Holds the rest of the input.
    private String d_input;
    // Current offset into the original input.
    private int d_currOffset;

    private Pattern d_numberPattern;

    {
        try {
            d_numberPattern = Pattern.compile("\\d+");
        } catch (PatternSyntaxException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    // Drops numChar characters from d_input, sets d_input to the
    // remaining string.
    private void consume(int numChar) {
        d_input = d_input.substring(numChar);
        d_currOffset += numChar;
    }

    // Skips all the whitespace.
    private void consumeWhitespace() {
        while (d_input.length() > 0
                && Character.isWhitespace(d_input.charAt(0))) {
            consume(1);
        }
    }

    /*******************************************************************
     * Note: All of the parse***() functions should not consume any of *
     * the input if they fail to parse due to recoverable conditions.  *
     *******************************************************************/

    // Precondition: d_input does NOT start with whitespace.
    private int parseNum() throws ParseException {
        Matcher matcher = d_numberPattern.matcher(d_input);
        boolean foundNum = matcher.find();

        if (!foundNum) {
            throw new ParseException("Expected number", d_currOffset);
        }

        int startIdx = matcher.start();
        int endIdx = matcher.end();

        // Since input doesn't start with whitespace, we expect the
        // first character to be the starting character of a number.
        if (startIdx != 0) {
            throw new ParseException("Expected number", d_currOffset);
        }

        // Parse and return the number
        String numStr = d_input.substring(startIdx, endIdx);
        int num = Integer.parseInt(numStr);
        consume(numStr.length());
        return num;
    }

    // Precondition: d_input does NOT start with whitespace.
    private Op parseOp() throws ParseException {
        if (d_input.length() == 0) {
            throw new ParseException("Excepted operator, got EOL", d_currOffset);
        }

        if (d_input.charAt(0) == '+') {
            consume(1);
            return Op.PLUS;
        } else if (d_input.charAt(0) == '-') {
            consume(1);
            return Op.MINUS;
        } else {
            throw new ParseException("Expected operator", d_currOffset);
        }
    }

    private List<Pair<Op, Expr>> parseManyOpAexprs() throws ParseException {
        List<Pair<Op, Expr>> opExprs = new LinkedList();

        do {
            consumeWhitespace();
            if (d_input.length() == 0 || d_input.charAt(0) == ')') break;
            Op op = parseOp();
            consumeWhitespace();
            Expr e = parseAexpr();
            consumeWhitespace();

            Pair<Op, Expr> p = Pair.of(op, e);
            opExprs.add(p);

            // The termination condition is either we hit the end of
            // the input, or we hit a closing parenthesis
        } while (d_input.length() != 0 && d_input.charAt(0) != ')');

        return opExprs;
    }

    // Precondition: d_input does not start with whitespace.
    private Expr parseAexpr() throws ParseException {
        Expr top = new Expr();

        try {
            int num = parseNum();
            top.d_num = num;

        } catch (ParseException e) {
            // If not num, then pexpr
            try {
                top = parsePexpr();
            } catch (ParseException e2) {
                System.out.println(e2.getMessage());
                throw
                        new ParseException("Expected either number or parenthesized expression",
                                d_currOffset);
            }
        }

        return top;
    }

    private Expr parseExpr() throws ParseException {
        Expr top = parseAexpr();

        List<Pair<Op, Expr>> opExprs = parseManyOpAexprs();

        while (!opExprs.isEmpty()) {
            Pair<Op, Expr> opExpr = opExprs.remove(0);

            Expr newtop = new Expr();
            newtop.d_lhs = top;
            newtop.d_op = opExpr.getLeft();
            newtop.d_rhs = opExpr.getRight();

            top = newtop;
        }

        return top;
    }

    // Precondition: d_input does not start with whitespace.
    private Expr parsePexpr() throws ParseException {
        if (d_input.length() == 0) {
            throw new ParseException("Expected (, got EOL", d_currOffset);
        }

        if (d_input.charAt(0) != '(') {
            throw new ParseException("Expected (", d_currOffset);
        }

        // Consume (
        consume(1);

        consumeWhitespace();
        Expr e = parseExpr();
        consumeWhitespace();

        if (d_input.length() == 0) {
            throw new ParseException("Expected ), got EOL", d_currOffset);
        }

        if (d_input.charAt(0) != ')') {
            throw new ParseException("Expected )", d_currOffset);
        }

        // Consume )
        consume(1);

        return e;
    }

    public Expr parse() throws ParseException {
        consumeWhitespace();
        return parseExpr();
    }

    public ParserTest(String input) {
        d_input = input;
        d_currOffset = 0;
    }
}
