package com.sobolv;

import com.sobolv.analyzer.Lexer;
import com.sobolv.analyzer.RecursiveSyntaxAnalyzer;
import com.sobolv.analyzer.SyntaxAnalyzer;
import com.sobolv.util.TableOfSymbols;
import lombok.SneakyThrows;

import java.util.Map;

public class Main {
    private static final TableOfSymbols TABLE_OF_SYMBOLS = TableOfSymbols.getInstance();

    public static void main(String[] args) {
        lab1();
    }

    private static void lab1() {
        Lexer analyzer = new Lexer(TABLE_OF_SYMBOLS);
        analyzer.analyze("src/main/resources/MyLang");
        SyntaxAnalyzer syntaxAnalyzer = new RecursiveSyntaxAnalyzer(TABLE_OF_SYMBOLS, analyzer.getMapOfVar());
        syntaxAnalyzer.parse();
    }

}
