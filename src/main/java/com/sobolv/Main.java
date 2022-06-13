package com.sobolv;

import com.sobolv.analyzer.Lexer;
import com.sobolv.analyzer.RecursiveSyntaxAnalyzer;
import com.sobolv.entity.VarVal;
import com.sobolv.interpreter.PostfixInterpreter;
import com.sobolv.translator.PostfixTranslator;
import com.sobolv.util.TableOfSymbols;

import java.util.HashMap;
import java.util.Map;

public class Main {
    private static final TableOfSymbols TABLE_OF_SYMBOLS = TableOfSymbols.getInstance();
    public static final HashMap<String, String> tableOfTypes = new HashMap<>();

    public static void main(String[] args) throws InterruptedException {
        lab3();
    }

    private static void lab1_2() {
        Lexer analyzer = new Lexer(TABLE_OF_SYMBOLS);
//        analyzer.analyze("src/main/resources/MyLang");
        analyzer.analyze("src/main/resources/Lab5");
        RecursiveSyntaxAnalyzer syntaxAnalyzer = new RecursiveSyntaxAnalyzer(TABLE_OF_SYMBOLS, analyzer.getMapOfVar());
        syntaxAnalyzer.parse();
    }
    private static void lab3() throws InterruptedException {
        Lexer analyzer = new Lexer(TABLE_OF_SYMBOLS);
        //        analyzer.analyze("src/main/resources/MyLang");
        analyzer.analyze("src/main/resources/Lab5");
        Thread.sleep(1000);
        PostfixTranslator postfixTranslator = new PostfixTranslator(analyzer, TABLE_OF_SYMBOLS);
        PostfixInterpreter postfixInterpreter = new PostfixInterpreter(postfixTranslator, analyzer);
        postfixInterpreter.postfixProcess();
        postfixTranslator.checkValue(postfixInterpreter.mapOfVar());
//        for (Map.Entry<String, VarVal> entry : postfixInterpreter.mapOfVar().entrySet()) {
//            System.out.println(entry.getKey() + " = " + entry.getValue().getValue());
//        }
//        System.out.println(tableOfTypes);
    }

}
