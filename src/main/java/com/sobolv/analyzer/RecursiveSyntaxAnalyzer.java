package com.sobolv.analyzer;

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
import java.util.stream.Collectors;

public class RecursiveSyntaxAnalyzer {
    private final List<String> startStatementSymbols;
    private final TableOfSymbols tableOfSymbols;
    private final Map<String, VarVal> mapOfVar;
    private int numRow = 1;

    public RecursiveSyntaxAnalyzer(TableOfSymbols tableOfSymbols, Map<String, VarVal> mapOfVar) {
        this.tableOfSymbols = tableOfSymbols;
        this.mapOfVar = mapOfVar;
        startStatementSymbols = new ArrayList<>();
        startStatementSymbols.add("for");
        startStatementSymbols.add("int");
        startStatementSymbols.add("double");
        startStatementSymbols.add("print");
        startStatementSymbols.add("scan");
        startStatementSymbols.add("if");
    }

    public void parse() {
        try {
            getToken("start", Token.KEYWORD);
            parseStatementList();
            getToken("end", Token.KEYWORD);
            System.out.println("Syntax Analyzer is ok");
        } catch (ParserException e) {
            System.err.println(e.toString());
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
        if (!tableOfSymbols.get(numRow).getToken().equals(Token.IDENT) &&
                !startStatementSymbols.contains(tableOfSymbols.get(numRow).getLexeme())) {
            throw new ParserException(tableOfSymbols.get(numRow), ErrorType.STATELIST_MISMATCH, "IDENT or KEYWORD");
        } else {
            while (
                    tableOfSymbols.get(numRow).getToken().equals(Token.IDENT)
                            || (startStatementSymbols.contains(tableOfSymbols.get(numRow).getLexeme()) &&
                            tableOfSymbols.get(numRow).getToken().equals(Token.KEYWORD) ||
                            tableOfSymbols.get(numRow).getToken().equals(Token.TYPE))
            ) {
                System.out.println("parseStatementList()");
                parseStatement();
            }
        }
    }

    private void parseStatement() {
        Symbol symbol = tableOfSymbols.get(numRow);
        System.out.println("parseStatement()  " + symbol.getLexeme());
        if (symbol.getToken().equals(Token.IDENT)) {
            parseAssign();
            getToken(";", Token.OP_END);
        } else if (symbol.getToken().equals(Token.KEYWORD) && symbol.getLexeme().equals("for")) {
            parseFor();
        } else if (symbol.getToken().equals(Token.KEYWORD) && symbol.getLexeme().equals("if")) {
            parseIf();
        } else if (symbol.getLexeme().equals("int") || symbol.getLexeme().equals("double")) {
            parseDeclare();
            getToken(";", Token.OP_END);
        } else if (symbol.getToken().equals(Token.KEYWORD) && symbol.getLexeme().equals("print")) {
            getToken("print", Token.KEYWORD);
            parsePrint();
            getToken(";", Token.OP_END);
        } else if (symbol.getToken().equals(Token.KEYWORD) && symbol.getLexeme().equals("scan")) {
            getToken("scan", Token.KEYWORD);
            parseScan();
//            getToken(";", Token.OP_END);
        } else if (symbol.getLexeme().equals("end")) {
            numRow++;
        }

    }

    private void parseScan() {
        getToken("(", Token.BRACKETS_OP);
        getToken(")", Token.BRACKETS_OP);
    }

    private void parsePrint() {
        getToken("(", Token.BRACKETS_OP);
        //parseExpr();
        numRow++;
        getToken(")", Token.BRACKETS_OP);
    }

    private void parseAssign() {
        Symbol symbol = tableOfSymbols.get(numRow);
        System.out.println("parseAssign()  " + symbol.getLexeme());
        if (!symbol.getToken().equals(Token.IDENT)) {
            throw new ParserException(symbol, ErrorType.IDENT);
        } else {
            numRow++;
            getToken("=", Token.ASSIGN_OP);
            parseExpr();
        }
    }

    private void parseTerm() {
        parseFactor();
        Symbol symbol = tableOfSymbols.get(numRow);
        if (symbol.getToken().equals(Token.MULT_OP)) {
            numRow++;
            parseFactor();
            symbol = tableOfSymbols.get(numRow);
        }
        if (symbol.getToken().equals(Token.ADD_OP)) {
            numRow++;
            parseFactor();
        }
    }

    private void parseArithmExpr() {
        parseTerm();
        Symbol symbol = tableOfSymbols.get(numRow);
        if (symbol.getToken().equals(Token.ADD_OP)) {
            numRow++;
            symbol = tableOfSymbols.get(numRow);
            if (!symbol.getToken().equals(Token.INTNUM) && !symbol.getToken().equals(Token.DOUBLENUM) &&
                    !symbol.getToken().equals(Token.IDENT) && !symbol.getLexeme().equals("-")) {
                throw new ParserException(symbol, ErrorType.STATELIST_MISMATCH, "ident or num");
            }
            parseArithmExpr();
        }
    }

    private void parseFactor() {
        Symbol symbol = tableOfSymbols.get(numRow);
        if (symbol.getToken().equals(Token.IDENT)) {
            numRow++;
        } else if (symbol.getToken().equals(Token.INTNUM) || symbol.getToken().equals(Token.DOUBLENUM)) {
            checkNumbers();
            numRow++;
        } else if (symbol.getLexeme().equals("(")) {
            numRow++;
            parseArithmExpr();
            getToken(")", Token.BRACKETS_OP);
            symbol = tableOfSymbols.get(numRow);
            if (symbol.getToken().equals(Token.MULT_OP) || symbol.getToken().equals(Token.ADD_OP)) {
                parseTerm();
            }
        }
//        else if (symbol.getToken().equals(Token.MULT_OP) || symbol.getToken().equals(Token.ADD_OP)){
//            throw new ParserException(symbol, ErrorType.IDENT);
//        }
    }

    private void parseExpr() {
        Symbol symbol = tableOfSymbols.get(numRow);
        System.out.println("parseExpr():  " + symbol.getLexeme());
        parseTerm();
        symbol = tableOfSymbols.get(numRow);
        if (symbol.getToken().equals(Token.KEYWORD)){
            parseStatement();
        }
        if (symbol.getToken().equals(Token.ADD_OP)) {
            numRow++;
            parseTerm();
        }
        if (symbol.getToken().equals(Token.MULT_OP)) {
            numRow++;
            parseTerm();
        }
    }

    private void checkNumbers() {
        Symbol symbol = tableOfSymbols.get(numRow);
        int numLine = symbol.getNumLine();
        List<Symbol> symbolsInLine = tableOfSymbols.getEntrySet().stream() // All symbols in line from table
                .filter(x -> x.getValue().getNumLine() == numLine)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        List<Token> tokensInLine = symbolsInLine.stream().map(Symbol::getToken).collect(Collectors.toList());

        if (tokensInLine.contains(Token.TYPE) && tokensInLine.contains(Token.IDENT)) {
            if (symbol.getToken().equals(Token.INTNUM)) {
                symbolsInLine.stream()
                        .filter(x -> x.getLexeme().equalsIgnoreCase("int"))
                        .findAny()
                        .orElseThrow(() -> new ParserException(symbol, ErrorType.STATELIST_MISMATCH, "double"));
                return;
            }
            if (symbol.getToken().equals(Token.DOUBLENUM)) {
                symbolsInLine.stream()
                        .filter(x -> x.getLexeme().equalsIgnoreCase("double"))
                        .findAny()
                        .orElseThrow(() -> new ParserException(symbol, ErrorType.STATELIST_MISMATCH, "integer"));
                return;
            }
        } else if (tokensInLine.contains(Token.IDENT)) {
            return;
        }
    }

    private void parseDeclare() {
        Symbol symbol = tableOfSymbols.get(numRow);
        System.out.println("parseDeclare():  " + symbol.getLexeme());
        if (!symbol.getLexeme().equals("int") && !symbol.getLexeme().equals("double")) {
            throw new ParserException(symbol, ErrorType.STATELIST_MISMATCH, "double or integer");
        }
        numRow++;
        symbol = tableOfSymbols.get(numRow);
        if (!symbol.getToken().equals(Token.IDENT)) {
            throw new ParserException(symbol, ErrorType.IDENT);
        }
        symbol = tableOfSymbols.get(numRow + 1);
        if (symbol.getToken().equals(Token.ASSIGN_OP)) {
            parseAssign();
        } else numRow++;
    }

    private void parseFor() {
        System.out.println("parseFor():  " + "for");
        numRow++;
        getToken("(", Token.BRACKETS_OP);
        parseAssign();
        getToken(";", Token.OP_END);
        parseBoolExpr();
        getToken(";", Token.OP_END);
        parseExpr();
        getToken(")", Token.BRACKETS_OP);
        parseStatementList();
    }

    private void parseIf() {
        System.out.println("parseIf():  " + "if");
        numRow++;
        parseBoolExpr();
        getToken("then", Token.KEYWORD);
        parseStatementList();
        getToken("fi", Token.KEYWORD);
    }

    private void parseBoolExpr() {
        Symbol symbol = tableOfSymbols.get(numRow);
        System.out.println("parseBoolExpr():  " + symbol.getLexeme());
        if (symbol.getLexeme().equals("(")) {
            numRow++;
            symbol = tableOfSymbols.get(numRow);
        }
        if (!symbol.getToken().equals(Token.IDENT) &&
                !symbol.getLexeme().equals("true") &&
                !symbol.getLexeme().equals("false")) {
            throw new ParserException(symbol, ErrorType.STATELIST_MISMATCH, "bool expression");
        }
        parseRelation();
        symbol = tableOfSymbols.get(numRow);
        if (symbol.getLexeme().equals(")")) {
            numRow++;
        }
    }

    private void parseRelation() {
        Symbol symbol = tableOfSymbols.get(numRow);
        System.out.println("parseRelation():  " + symbol.getLexeme());
        if (symbol.getLexeme().equals("true") || symbol.getLexeme().equals("false")) {
            numRow++;
            return;
        }
        if (!symbol.getToken().equals(Token.IDENT) &&
                !symbol.getToken().equals(Token.INTNUM) &&
                !symbol.getToken().equals(Token.DOUBLENUM)) {
            throw new ParserException(symbol, ErrorType.STATELIST_MISMATCH, "ident or num");
        }
        parseExpr();
        if (!tableOfSymbols.get(numRow).getToken().equals(Token.REL_OP)) {
            throw new ParserException(symbol, ErrorType.STATELIST_MISMATCH, "REL_OP");
        }
        numRow++;
        parseExpr();
    }
}