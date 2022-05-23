package com.sobolv.exception;

import com.sobolv.enums.ErrorType;

public class LexerException extends RuntimeException {
    private final char currentChar;
    private final ErrorType errorType;
    private final int numLine;

    public LexerException(char currentChar, ErrorType errorType, int numLine) {
        this.currentChar = currentChar;
        this.errorType = errorType;
        this.numLine = numLine;
    }

    @Override
    public String toString() {
        String str = "";
        if(errorType == ErrorType.LEXER){
            str = "LexerException: unexpected symbol " + currentChar + " on line " + numLine;
        }
        return str;
    }
}