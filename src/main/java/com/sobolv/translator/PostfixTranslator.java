package com.sobolv.translator;

import com.sobolv.Main;
import com.sobolv.analyzer.Lexer;
import com.sobolv.entity.Symbol;
import com.sobolv.entity.VarVal;
import com.sobolv.enums.ErrorType;
import com.sobolv.enums.Token;
import com.sobolv.exception.ParserException;
import com.sobolv.util.TableOfSymbols;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostfixTranslator {
    private final TableOfSymbols tableOfSymbols;

    private int labelCount = 0;
    private int rCount = 0;
    private int numRow = 1;
    private Lexer analyzer;
    private List<Symbol> postfixCode;

    private Map<String, Integer> mapOfLabel;

    public PostfixTranslator(Lexer analyzer, TableOfSymbols tableOfSymbols) {
        this.tableOfSymbols = tableOfSymbols;
        this.analyzer = analyzer;
        postfixCode = new ArrayList<>();
        mapOfLabel = new HashMap<>();
    }

    public List<Symbol> parse() {
        try {
            getToken("start", Token.KEYWORD);
            parseStatementList();
            getToken("end", Token.KEYWORD);
            return postfixCode;
        }catch (Exception e){
            System.err.println(e);
            System.exit(1);
            return postfixCode;
        }
    }

    private void getToken(String lexeme, Token token) {
        Symbol symbol = tableOfSymbols.get(numRow);
        if (lexeme.equals(symbol.getLexeme()) && token.equals(symbol.getToken())) {
            numRow++;
        } else {
            throw new ParserException(symbol, ErrorType.STATELIST_MISMATCH, lexeme, token);
        }
    }

    private void parseStatementList() {
        while (parseStatement()) {

        }
    }

    private boolean parseStatement() {
        Symbol symbol = tableOfSymbols.get(numRow);
        if (symbol.getLexeme().equalsIgnoreCase("int")){
            parseInt();
            return true;
        }
        if (symbol.getLexeme().equalsIgnoreCase("double")){
            parseDouble();
            return true;
        }
        if (symbol.getToken().equals(Token.IDENT)) {
            parseAssign();
            getToken(";", Token.OP_END);
            return true;
        } else if (symbol.getLexeme().equals("if")) {
            parseIf();
            return true;
        } else if (symbol.getLexeme().equals("for")) {
            parseFor();
            return true;
        } else if (symbol.getLexeme().equals("print")) {
            parsePrint();
            getToken(";", Token.OP_END);
            return true;
        } else if (symbol.getLexeme().equals("end") || symbol.getLexeme().equals("}") || symbol.getLexeme().equals("fi")) {
            return false;
        } else {
            System.err.println("Unexpected symbol " + symbol.getLexeme() + " at line " + symbol.getNumLine());
            throw new RuntimeException();
        }
    }
    private void parseInt(){
        numRow++;
        Symbol symbol = tableOfSymbols.get(numRow);
        Main.tableOfTypes.put(symbol.getLexeme(),"INTNUM");
    }
    private void parseDouble(){
        numRow++;
        Symbol symbol = tableOfSymbols.get(numRow);
        Main.tableOfTypes.put(symbol.getLexeme(),"DOUBLENUM");
    }
    private Symbol createLabel() {
        return new Symbol(numRow, "m" + labelCount++, Token.LABEL, mapOfLabel.size());
    }


    private void parseIf() {
        numRow++;
        parseBoolExpression();
        getToken("then", Token.KEYWORD);
        Symbol m1 = createLabel();
        postfixCode.add(m1);
        postfixCode.add(new Symbol(numRow, "JF", null, 0));
        parseStatementList();
        getToken("fi", Token.KEYWORD);
        mapOfLabel.put(m1.getLexeme(), postfixCode.size());
        postfixCode.add(m1);
        labelCount++;
    }

    private void parseBoolExpression() {
        parseExpression();
        Symbol symbol = tableOfSymbols.get(numRow);
        if (symbol.getToken().equals(Token.REL_OP)) {
            numRow++;
            parseExpression();
            postfixCode.add(symbol);
        } else {
            System.err.println("REL_OP expected");
            throw new RuntimeException();
        }
    }

    private void parseFor() {
        numRow++;
        getToken("(", Token.BRACKETS_OP);
        Symbol symbol = tableOfSymbols.get(numRow);
        parseAssign();
        getToken(";", Token.OP_END);
        Symbol m0 = createLabel();
        mapOfLabel.put(m0.getLexeme(), postfixCode.size());
        postfixCode.add(m0);
        parseBoolExpression();
        getToken(";", Token.OP_END);
        Symbol r1 = new Symbol(0, "r"+rCount, Token.IDENT, 0);
        mapOfVar().put("r"+rCount, new VarVal(0, Token.IDENT, 1d));
        postfixCode.add(r1);
        rCount++;
        parseExpression();
        postfixCode.add(new Symbol(0, "=", Token.ASSIGN_OP, 0));
        getToken(")", Token.BRACKETS_OP);
        Symbol m1 = createLabel();
        mapOfLabel.put(m1.getLexeme(), postfixCode.size());
        postfixCode.add(m1);
        postfixCode.add(new Symbol(numRow, "JF", null, 0));
        getToken("{", Token.START_BLOCK);
        parseStatementList();
        getToken("}", Token.END_BLOCK);
        postfixCode.add(symbol);
        postfixCode.add(r1);
        postfixCode.add(new Symbol(0, "=", Token.ASSIGN_OP, 0));
        postfixCode.add(m0);
        postfixCode.add(new Symbol(numRow, "JUMP", null, 0));
        postfixCode.add(m1);
        postfixCode.add(new Symbol(0, ":", null, 0));
        mapOfLabel.put(m1.getLexeme(), postfixCode.size()-2);

    }

    private void parsePrint() {
        numRow++;
        getToken("(", Token.BRACKETS_OP);
        parseExpression();
        getToken(")", Token.BRACKETS_OP);
        postfixCode.add(new Symbol(0, "print", Token.KEYWORD, 0));
    }

    private void parseScan() {
        postfixCode.add(tableOfSymbols.get(numRow));
        numRow++;
        getToken("(", Token.BRACKETS_OP);
        getToken(")", Token.BRACKETS_OP);
    }

    private void parseAssign() {
        Symbol symbol = tableOfSymbols.get(numRow);
        postfixCode.add(symbol);

        printStep(symbol.getLexeme());

        numRow++;

        getToken("=", Token.ASSIGN_OP);
        parseExpression();

        postfixCode.add(new Symbol(0, "=", Token.ASSIGN_OP, 0));

        printStep("=");

    }

    private void printStep(String lex) {
        String step = "Step:\n" +
                "Token: " + lex + "\n" +
                "TableOfSymbol[" + numRow + "] = " + tableOfSymbols.get(numRow) + "\n" +
                "postfixCode = " + postfixCode.toString() + "\n" +
                tableOfSymbols.get(numRow).getToken();
        System.out.println(step);
    }

    private void parseExpression() {
        Symbol symbol = tableOfSymbols.get(numRow);
        if (symbol.getLexeme().equals("scan")) {
            parseScan();
            return;
        }
        parseTerm();
        if (symbol.getLexeme().equals("-")) {
            postfixCode.add(new Symbol(0, "NEG", Token.UNARY_MINUS, 0));
        }
        boolean flag = true;
        while (flag) {
            symbol = tableOfSymbols.get(numRow);
            if (symbol.getToken().equals(Token.ADD_OP)) {
                numRow++;
                parseTerm();
                postfixCode.add(symbol);
                printStep(symbol.getLexeme());
            } else {
                flag = false;
            }
        }
    }

    private void parseTerm() {
        Symbol symbol = tableOfSymbols.get(numRow);
        if (symbol.getLexeme().equals("-")) {
            numRow++;
        }
        parsePowTerm();

        boolean flag = true;

        while (flag) {
            symbol = tableOfSymbols.get(numRow);
            if (symbol.getToken().equals(Token.MULT_OP)) {
                numRow++;
                parsePowTerm();
                postfixCode.add(symbol);
                printStep(symbol.getLexeme());
            } else {
                flag = false;
            }
        }
    }
    private void parsePowTerm(){
        Symbol symbol = tableOfSymbols.get(numRow);
        if (symbol.getLexeme().equals("-")) {
            numRow++;
        }
        parseFactor();

        boolean flag = true;

        while (flag) {
            symbol = tableOfSymbols.get(numRow);
            if(symbol.getLexeme().equals("^")) {
                numRow++;
                parsePow();
            }else {
                flag = false;
            }
        }
    }
    private void parsePow(){
        parseFactor();
        Symbol symbol = tableOfSymbols.get(numRow);
        if(symbol.getLexeme().equals("^")){
            numRow++;
            parsePow();
        }
        postfixCode.add(new Symbol(symbol.getNumLine(), "^", Token.MULT_OP, 0));
    }

    private void parseFactor(){
        Symbol symbol = tableOfSymbols.get(numRow);
        if (symbol.getToken().equals(Token.IDENT) || symbol.getToken().equals(Token.INTNUM) || symbol.getToken().equals(Token.DOUBLENUM)) {
            postfixCode.add(symbol);
            printStep(symbol.getLexeme());
            numRow++;
        } else if (symbol.getLexeme().equals("(")) {
            numRow++;
            parseExpression();
            getToken(")", Token.BRACKETS_OP);
        } else {
            System.err.println("Unexpected symbol " + symbol.getLexeme() + " at line " + symbol.getNumLine() + " expected IDENT, INT, DOUBLE, BRACKETS_OP");
            throw new RuntimeException();
        }
    }

    public List<Symbol> getPostfixCode() {
        return postfixCode;
    }

    public Map<String, Integer> getMapOfLabel() {
        return mapOfLabel;
    }

    public Map<String, VarVal> mapOfVar() {
        return analyzer.getMapOfVar();
    }

    public void checkValue(Map<String, VarVal> map){
        for (Map.Entry<String, VarVal> entry : map.entrySet()) {
            if(Main.tableOfTypes.get(entry.getKey()).equals(Token.DOUBLENUM.toString())){
                System.out.println(entry.getKey() + " = " + entry.getValue().getValue());
            }else if(Main.tableOfTypes.get(entry.getKey()).equals(Token.INTNUM.toString())){
                if(entry.getValue().getValue() % 1 == 0){
                    int value = entry.getValue().getValue().intValue();
                    System.out.println(entry.getKey() + " = " + (int)entry.getValue().getValue().intValue());
                }else{
                    System.out.println(entry.getKey() + " = " + entry.getValue().getValue());
                }
            }

        }
    }

}
