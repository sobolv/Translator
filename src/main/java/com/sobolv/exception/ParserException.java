package com.sobolv.exception;

import com.sobolv.entity.Symbol;
import com.sobolv.enums.ErrorType;
import com.sobolv.enums.Token;
import com.sobolv.util.TableOfSymbols;

import java.util.Map;

public class ParserException extends RuntimeException {
    private final Symbol symbol;
    private final ErrorType errorType;
    private String expectedLex;
    private Token expectedToken;
    private TableOfSymbols tableOfSymbols = TableOfSymbols.getInstance();

    public ParserException(Symbol symbol, ErrorType errorType, String lex, Token token) {
        this.symbol = symbol;
        this.expectedToken = token;
        this.expectedLex = lex;
        this.errorType = errorType;
    }

    public ParserException(Symbol symbol, ErrorType errorType, String lex) {
        this.symbol = symbol;
        this.errorType = errorType;
        this.expectedLex = lex;
    }

    public ParserException(Symbol symbol, ErrorType errorType) {
        this.symbol = symbol;
        this.errorType = errorType;
    }

    @Override
    public String toString() {
        String str = "";
        switch (errorType) {
            case TOKENS_MISMATCH:
                str = "ParserException: unexpected (" + symbol.getLexeme() + " " + symbol.getToken() + ") " +
                        "on line " + symbol.getNumLine() + ", expected (" + expectedLex + " " + expectedToken + ")\n" + generate();
                break;
            case STATELIST_MISMATCH:
                str = "ParserException: unexpected (" + symbol.getLexeme() + " " + symbol.getToken() + ") " +
                        "on line " + symbol.getNumLine() + ", expected (" + expectedLex + ")\n" + generate();
                break;
            case IDENT:
                str = "ParserException: unexpected (" + symbol.getLexeme() + " " + symbol.getToken() + ") " +
                        "on line " + symbol.getNumLine() + ", expected (IDENT)\n" + generate();
                break;
        }
        return str;
    }

    private String generate() {
        StringBuilder stringBuilder = new StringBuilder();
        int sizeUntilErr = 0;
        boolean wasErr = false;
        for (Map.Entry<Integer, Symbol> entry : tableOfSymbols.getEntrySet()) {
            if (entry.getValue().getNumLine() == symbol.getNumLine()) {
                if (entry.getValue().equals(symbol)) {
                    wasErr = true;
                }
                if (!wasErr) {
                    sizeUntilErr += entry.getValue().getLexeme().length() + 1;
                }
                stringBuilder.append(entry.getValue().getLexeme()).append(" ");
            }
        }
        stringBuilder.append("\n");
        for (int i = 0; i < sizeUntilErr; i++) {
            stringBuilder.append(" ");
        }
        stringBuilder.append("^");
        return stringBuilder.toString();
    }
}
