package com.sobolv.interpreter;

import com.sobolv.Main;
import com.sobolv.analyzer.Lexer;
import com.sobolv.entity.Symbol;
import com.sobolv.entity.VarVal;
import com.sobolv.enums.Token;
import com.sobolv.translator.PostfixTranslator;

import java.util.*;

public class PostfixInterpreter {
    private final List<Symbol> postfixCode;
    private final Stack<Symbol> stack;
    private final Lexer lexAnalyzer;
    private final Map<String, Integer> mapOfLabel;

    public PostfixInterpreter(PostfixTranslator translator, Lexer lexAnalyzer) {
        this.postfixCode = translator.parse();
        System.out.println(postfixCode);
        this.lexAnalyzer = lexAnalyzer;
        this.mapOfLabel = translator.getMapOfLabel();
        stack = new Stack<>();
    }

    public void postfixProcess() {
        for (int i = 0; i < postfixCode.size(); i++) {
            Symbol symbol = postfixCode.get(i);
            if (Arrays.asList(Token.INTNUM, Token.DOUBLENUM, Token.IDENT, Token.BOOLVAL, Token.LABEL).contains(symbol.getToken())) {
                stack.push(symbol);
            } else if (symbol.getLexeme().equals("JF") || symbol.getLexeme().equals("JUMP") || symbol.getLexeme().equals(":")) {
                i = doJump(symbol, i);
            } else if (symbol.getLexeme().equals("print")) {
                processPrint();
            } else if (symbol.getLexeme().equals("scan")) {
                processScan();
            } else {
                perform(symbol);
            }
        }
    }

    private int doJump(Symbol symbol, int i) {
        if (symbol.getLexeme().equals("JUMP")) {
            return mapOfLabel.get(stack.pop().getLexeme());
        } else if (symbol.getLexeme().equals("JF")) {
            Symbol m = stack.pop();
            Symbol b = stack.pop();
            if (b.getLexeme().equals("true")) {
                return i;
            }
            return mapOfLabel.get(m.getLexeme());
        } else {
            stack.pop();
            return i;
        }
    }

    private void processPrint() {
        Symbol toPrint = stack.pop();
        if (toPrint.getToken().equals(Token.IDENT)) {
            if (mapOfVar().get(toPrint.getLexeme()).getValue() == null) {
                System.err.println("Variable is not initialized");
                throw new RuntimeException();
            } else {
                checkValue(toPrint);
            }
        } else {
            if(mapOfConst().get(toPrint.getLexeme()).getValue() % 1 == 0){
                int value = mapOfConst().get(toPrint.getLexeme()).getValue().intValue();
                System.out.println(value);
            }else{
                System.out.println(mapOfConst().get(toPrint.getLexeme()).getValue());
            }
        }
    }
    public void checkValue(Symbol s){
            if(Main.tableOfTypes.get(s.getLexeme()).equals(Token.DOUBLENUM.toString())){
                System.out.println(mapOfVar().get(s.getLexeme()).getValue());
            }else if(Main.tableOfTypes.get(s.getLexeme()).equals(Token.INTNUM.toString())){
                if(mapOfVar().get(s.getLexeme()).getValue() % 1 == 0){
                    int value = mapOfVar().get(s.getLexeme()).getValue().intValue();
                    System.out.println(value);
                }else{
                    System.out.println(mapOfVar().get(s.getLexeme()).getValue());
                }
            }
    }

    private void processScan() {
        Scanner scanner = new Scanner(System.in);
        String str = scanner.nextLine();
        try {
            if (str.contains(".")) {
                Double num = Double.parseDouble(str);
                mapOfConst().put(num.toString(), new VarVal(0, Token.DOUBLENUM, num));
                stack.push(new Symbol(0, num.toString(), Token.DOUBLENUM, 0));
            } else {
                Integer num = Integer.parseInt(str);
                mapOfConst().put(num.toString(), new VarVal(0, Token.INTNUM, num.doubleValue()));
                stack.push(new Symbol(0, num.toString(), Token.DOUBLENUM, 0));
            }

        } catch (Exception e) {
            System.err.println("You can enter only 1 number");
            throw new RuntimeException();
        }
    }

    private void perform(Symbol symbol) {
        if (symbol != null && symbol.getToken().equals(Token.UNARY_MINUS)) {
            Symbol negativeNumber = stack.pop();
            if(mapOfVar().get(negativeNumber.getLexeme()) != null){
                Double value = mapOfVar().get(negativeNumber.getLexeme()).getValue();
                negativeNumber.setLexeme("-" + value);
            }else{
                negativeNumber.setLexeme("-" + negativeNumber.getLexeme());
            }
            stack.push(negativeNumber);
            mapOfConst().put(negativeNumber.getLexeme(), new VarVal(negativeNumber.getIndex(), negativeNumber.getToken(), Double.parseDouble(negativeNumber.getLexeme())));
            return;
        }
        if (symbol.getLexeme().equals("=") && symbol.getToken().equals(Token.ASSIGN_OP)) {
            Symbol symbolR = stack.pop();
            Symbol symbolL = stack.pop();
            VarVal varVal;
            try {
                varVal = new VarVal(
                        mapOfVar().get(symbolL.getLexeme()).getIndex(),
                        mapOfConst().get(symbolR.getLexeme()).getToken(),
                        mapOfConst().get(symbolR.getLexeme()).getValue()
                );
                if(!Main.tableOfTypes.containsKey(symbolL.getLexeme())){
                    Main.tableOfTypes.put(symbolL.getLexeme(),symbolR.getToken().toString());
                }

            } catch (NullPointerException e) {
                try {
                    varVal = new VarVal(
                            mapOfVar().get(symbolL.getLexeme()).getIndex(),
                            mapOfVar().get(symbolR.getLexeme()).getToken(),
                            mapOfVar().get(symbolR.getLexeme()).getValue()
                    );
                    if(!Main.tableOfTypes.containsKey(symbolL.getLexeme())){
                        Main.tableOfTypes.put(symbolL.getLexeme(),symbolR.getToken().toString());
                    }
                } catch (NullPointerException ex) {
                    System.err.println("Variable is not initialized \"" + symbolR.getLexeme() + "\"");
                    throw new RuntimeException();
                }
            }
            mapOfVar().put(symbolL.getLexeme(), varVal);
        } else if (Arrays.asList(Token.ADD_OP, Token.MULT_OP, Token.REL_OP).contains(symbol.getToken())) {
            Symbol symbolR = stack.pop();
            Symbol symbolL = stack.pop();
            processOperation(symbolL, symbol, symbolR);
        }
    }

    private void processOperation(Symbol symbolL, Symbol symbol, Symbol symbolR) {
        double valueL;
        double valueR;
        if (symbolL.getToken().equals(Token.IDENT)) {
            if (mapOfVar().get(symbolL.getLexeme()).getValue() == null) {
                System.err.println("Variable is not initialized \"" + symbolL.getLexeme() + "\"");
                throw new RuntimeException();
            } else {
                valueL = mapOfVar().get(symbolL.getLexeme()).getValue();
                Token tokenL = mapOfVar().get(symbolL.getLexeme()).getToken();
            }
        } else {
            valueL = mapOfConst().get(symbolL.getLexeme()).getValue();
        }
        if (symbolR.getToken().equals(Token.IDENT)) {
            if (mapOfVar().get(symbolR.getLexeme()).getValue() == null) {
                System.err.println("Variable is not initialized \"" + symbolR.getLexeme() + "\"");
                throw new RuntimeException();
            } else {
                valueR = mapOfVar().get(symbolR.getLexeme()).getValue();
                Token tokenR = mapOfVar().get(symbolR.getLexeme()).getToken();
            }
        } else {
            valueR = mapOfConst().get(symbolR.getLexeme()).getValue();
        }
        if (symbol.getToken().equals(Token.REL_OP)) {
            compare(symbol, valueL, valueR);
        } else {
            calculate(symbolL, symbol, symbolR, valueL, valueR);
        }
    }

    private void calculate(Symbol symbolL, Symbol opp, Symbol symbolR, Double valueL, Double valueR) {
        double value;
        if (opp.getLexeme().equals("+")) {
            value = valueL + valueR;
        } else if (opp.getLexeme().equals("-")) {
            value = valueL - valueR;
        } else if (opp.getLexeme().equals("*")) {
            value = valueL * valueR;
        } else if (opp.getLexeme().equals("/") && valueR.equals(0D)) {
            System.err.println("Division by zero at line " + opp.getNumLine());
            throw new RuntimeException();
        }else if (opp.getLexeme().equals("/")) {
            value = valueL / valueR;
        } else if (opp.getLexeme().equals("^")) {
            value = Math.pow(valueL, valueR);
        } else {
            return;
        }
        stack.push(new Symbol(0, Double.toString(value),
                Arrays.asList(symbolL.getToken(), symbolR.getToken()).contains(Token.DOUBLENUM)
                        ? Token.DOUBLENUM
                        : Token.INTNUM, 0));
        toMapOfConst(value, symbolL.getToken());
    }

    private void compare(Symbol opp, Double valueL, Double valueR) {
        boolean value;

        if (opp.getLexeme().equals("==")) {
            value = valueL.equals(valueR);

        } else if (opp.getLexeme().equals(">")) {
            value = valueL > valueR;
        } else if (opp.getLexeme().equals("<")) {
            value = valueL < valueR;
        } else if (opp.getLexeme().equals(">=")) {
            value = valueL >= valueR;
        } else if (opp.getLexeme().equals("<=")) {
            value = valueL <= valueR;
        } else {
            return;
        }
        stack.push(new Symbol(0, "" + value, Token.BOOLVAL, 0));
    }

    private void toMapOfConst(double value, Token token) {
        String lexeme = "" + value;
        try {
            int indx = mapOfConst().get(lexeme).getIndex();
        } catch (Exception e) {
            int indx = mapOfConst().size() + 1;
            mapOfConst().put(lexeme, new VarVal(indx, token, value));
        }

    }

    public Map<String, VarVal> mapOfVar() {
        return lexAnalyzer.getMapOfVar();
    }

    private Map<String, VarVal> mapOfConst() {
        return lexAnalyzer.getMapOfConst();
    }
}
