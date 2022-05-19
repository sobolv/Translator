package com.sobolv.analyzer;

import com.sobolv.entity.State;
import com.sobolv.entity.Symbol;
import com.sobolv.entity.VarVal;
import com.sobolv.enums.Token;
import com.sobolv.util.TableOfSymbols;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Lexer {
    private TableOfSymbols tableOfSymbols;

    private String textCode;
    private Map<String, Token> mapOfLanguageTokens;
    private Map<Integer, Token> mapIdRealInteger;
    private Map<String, VarVal> mapOfVar;
    private Map<String, VarVal> mapOfConst;

    private List<Integer> errStates;

    private String lexeme;
    private int state;
    private char currentChar;

    private List<Integer> finalStates;

    private int numChar = -1;
    private int numLine = 1;

    private Map<State, Integer> mapOfStates;

    public Lexer(TableOfSymbols tableOfSymbols) {
        this.tableOfSymbols = tableOfSymbols;
        initMapOfLanguageTokens();
        initMapIdRealInteger();
        initMapOfStates();
        initFinalState();
        initErrStates();
        initMaps();
        initLexeme();
        initState();
    }

    private void readFile(String path) throws IOException {
        textCode = Files.lines(Paths.get(path)).collect(Collectors.joining("\n", "", "\n"));
    }

    @SneakyThrows
    public void analyze(String path) {
        readFile(path);
        while (numChar < textCode.length() - 1) {
            currentChar = nextChar();

            String classChar = defineChar(currentChar);
            state = nextState(state, classChar);
            if (isFinal(state)) {
                processing();
                if (errStates.contains(state)) {
                    break;
                }
            } else if (state == 0) {
                lexeme = "";
            } else {
                lexeme += currentChar;
            }
        }
    }

    private void processing() {
        Token token;
        if (state == 3) {
            numLine++;
            state = 0;
        }else if (state == 2 || state == 7 || state == 10) {
            token = getToken(state, lexeme);
            if (token != Token.KEYWORD) {
                int index = indexVarConst(state, lexeme, token);
                System.out.println(numLine + " " + lexeme + " " + token + " " + index);
                tableOfSymbols.add(new Symbol(numLine, lexeme, token, index));
            } else {
                System.out.println(numLine + " " + lexeme + " " + token);
                tableOfSymbols.add(new Symbol(numLine, lexeme, token, 0));
            }
            lexeme = "";
            numChar = putCharBack(numChar);
            state = 0;
        }else if (state == 17 || state == 18) {
            lexeme += currentChar;
            token = getToken(state, lexeme);
            System.out.println(numLine + " " + lexeme + " " + token);
            tableOfSymbols.add(new Symbol(numLine, lexeme, token, 0));
            lexeme = "";
            state = 0;
        }else if (state == 101 || state == 102) {
            token = getToken(state, lexeme);
            System.out.println(numLine + " " + lexeme + " " + token);
            tableOfSymbols.add(new Symbol(numLine, lexeme, token, 0));
            numChar = putCharBack(numChar);
            lexeme = "";
            state = 0;
        }
    }

    private int nextState(int state, String classChar) {
        Integer integer = mapOfStates.get(new State(state, classChar));
        if (integer == null) {
            integer = mapOfStates.get(new State(state, "other"));
        }
        return integer;
    }

    public String getTextCode() {
        return textCode;
    }

    private int indexVarConst(int state, String lexeme, Token token) {
        int indx = 0;

        if (lexeme.equalsIgnoreCase("int") || lexeme.equalsIgnoreCase("double")) {
            return indx;
        }
        if (state == 2) {
            try {
                indx = mapOfVar.get(lexeme).getIndex();
            } catch (Exception e) {
                indx = mapOfVar.size() + 1;
                mapOfVar.put(lexeme, new VarVal(indx));
            }
        }else if (state == 7 || state == 10) {
            try {
                indx = mapOfConst.get(lexeme).getIndex();
            } catch (Exception e) {
                indx = mapOfConst.size() + 1;
                mapOfConst.put(lexeme, new VarVal(indx, token, Double.parseDouble(lexeme)));
            }
        }
        return indx;
    }

    private void initLexeme() {
        lexeme = "";
    }

    private void initState() {
        state = 0;
    }

    private boolean isFinal(int state) {
        return finalStates.contains(state);
    }

    private int putCharBack(int numChar) {
        return numChar - 1;
    }

    public Map<String, VarVal> getMapOfVar() {
        return mapOfVar;
    }

    public Map<String, VarVal> getMapOfConst() {
        return mapOfConst;
    }

    private void initMapOfLanguageTokens() {
        mapOfLanguageTokens = new HashMap<>();
        mapOfLanguageTokens.put("if", Token.KEYWORD);
        mapOfLanguageTokens.put("true", Token.KEYWORD);
        mapOfLanguageTokens.put("false", Token.KEYWORD);
        mapOfLanguageTokens.put("int", Token.TYPE);
        mapOfLanguageTokens.put("double", Token.TYPE);
        mapOfLanguageTokens.put("start", Token.KEYWORD);
        mapOfLanguageTokens.put("end", Token.KEYWORD);
        mapOfLanguageTokens.put("boolean", Token.KEYWORD);
        mapOfLanguageTokens.put("scan", Token.KEYWORD);
        mapOfLanguageTokens.put("print", Token.KEYWORD);
        mapOfLanguageTokens.put("for", Token.KEYWORD);
        mapOfLanguageTokens.put("then", Token.KEYWORD);
        mapOfLanguageTokens.put("fi", Token.KEYWORD);
        mapOfLanguageTokens.put("=", Token.ASSIGN_OP);
        mapOfLanguageTokens.put("+", Token.ADD_OP);
        mapOfLanguageTokens.put("-", Token.ADD_OP);
        mapOfLanguageTokens.put("*", Token.MULT_OP);
        mapOfLanguageTokens.put("/", Token.MULT_OP);
        mapOfLanguageTokens.put("<", Token.REL_OP);
        mapOfLanguageTokens.put(">", Token.REL_OP);
        mapOfLanguageTokens.put("<=", Token.REL_OP);
        mapOfLanguageTokens.put(">=", Token.REL_OP);
        mapOfLanguageTokens.put("==", Token.REL_OP);
        mapOfLanguageTokens.put("!=", Token.REL_OP);
        mapOfLanguageTokens.put("(", Token.BRACKETS_OP);
        mapOfLanguageTokens.put(")", Token.BRACKETS_OP);
        mapOfLanguageTokens.put(".", Token.PUNCT);
        mapOfLanguageTokens.put(",", Token.PUNCT);
        mapOfLanguageTokens.put(";", Token.OP_END);
        mapOfLanguageTokens.put("{", Token.START_BLOCK);
        mapOfLanguageTokens.put("}", Token.END_BLOCK);
        mapOfLanguageTokens.put("^", Token.MULT_OP);
    }

    private void initMaps() {
        mapOfVar = new LinkedHashMap<>();
        mapOfConst = new HashMap<>();
    }

    private void initMapIdRealInteger() {
        mapIdRealInteger = new HashMap<>();
        mapIdRealInteger.put(2, Token.IDENT);
        mapIdRealInteger.put(10, Token.DOUBLENUM);
        mapIdRealInteger.put(7, Token.INTNUM);
    }

    private void initErrStates() {
        errStates = new ArrayList<>();
        errStates.add(101);
        errStates.add(102);
    }

    private void initFinalState() {
        finalStates = new ArrayList<>();
        finalStates.add(2);
        finalStates.add(3);
        finalStates.add(7);
        finalStates.add(10);
        finalStates.add(17);
        finalStates.add(18);
        finalStates.add(101);
        finalStates.add(102);
    }

    private void initMapOfStates() {
        mapOfStates = new HashMap<>();
        mapOfStates.put(new State(0, ""), 0);
        mapOfStates.put(new State(0, "ws"), 0);
        mapOfStates.put(new State(0, "nl"), 3);
        mapOfStates.put(new State(0, "other"), 101);
        mapOfStates.put(new State(0, "Letter"), 1);
        mapOfStates.put(new State(1, "Letter"), 1);
        mapOfStates.put(new State(1, "Digit"), 1);
        mapOfStates.put(new State(1, "other"), 2);
        mapOfStates.put(new State(0, "Digit"), 5);
        mapOfStates.put(new State(0, "Dot"), 8);
        mapOfStates.put(new State(5, "Digit"), 5);
        mapOfStates.put(new State(5, "Dot"), 12);
        mapOfStates.put(new State(5, "other"), 7);
        mapOfStates.put(new State(8, "Digit"), 9);
        mapOfStates.put(new State(9, "Digit"), 9);
        mapOfStates.put(new State(9, "other"), 10);
        mapOfStates.put(new State(12, "other"), 10);
        mapOfStates.put(new State(12, "Digit"), 13);
        mapOfStates.put(new State(13, "Digit"), 13);
        mapOfStates.put(new State(13, "other"), 10);
        mapOfStates.put(new State(0, ","), 18);
        mapOfStates.put(new State(0, ";"), 18);
        mapOfStates.put(new State(0, "+"), 18);
        mapOfStates.put(new State(0, "-"), 18);
        mapOfStates.put(new State(0, "*"), 18);
        mapOfStates.put(new State(0, "/"), 18);
        mapOfStates.put(new State(0, "^"), 18);
        mapOfStates.put(new State(0, "("), 18);
        mapOfStates.put(new State(0, ")"), 18);
        mapOfStates.put(new State(0, "{"), 18);
        mapOfStates.put(new State(0, "}"), 18);
        mapOfStates.put(new State(0, ">"), 19);
        mapOfStates.put(new State(0, "<"), 19);
        mapOfStates.put(new State(19, "="), 18);
        mapOfStates.put(new State(19, "other"), 102);
        mapOfStates.put(new State(0, "="), 16);
        mapOfStates.put(new State(16, "="), 17);
        mapOfStates.put(new State(16, "other"), 102);
    }

    private Token getToken(int state, String lexeme) {
        Token token = mapOfLanguageTokens.get(lexeme);
        if (token == null) {
            token = mapIdRealInteger.get(state);
        }
        return token;
    }

    private char nextChar() {
        numChar++;
        return textCode.charAt(numChar);
    }

    private String defineChar(char c) {
        String str = String.valueOf(c);
        String res = "";
        if (str.matches("[A-Za-z]")) {
            res = "Letter";
        } else if (str.equals(".")) {
            res = "Dot";
        } else if (str.matches("[0-9]")) {
            res = "Digit";
        } else if (str.equals(" ")) {
            res = "ws";
        } else if (str.equals("\n")) {
            res = "nl";
        }
        if ("+-*/=^E()><{},;:?".contains(str)) {
            res = str;
        }
        return res;
    }

    public Map<Integer, Token> getMapIdRealInteger() {
        return mapIdRealInteger;
    }

    public void setMapIdRealInteger(Map<Integer, Token> mapIdRealInteger) {
        this.mapIdRealInteger = mapIdRealInteger;
    }

}

