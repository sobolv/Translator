package com.sobolv.entity;

import com.sobolv.enums.Token;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


@EqualsAndHashCode
@Getter
@Setter
@AllArgsConstructor
public class Symbol {
    private int numLine;
    private String lexeme;
    private Token token;
    private int index;

    @Override
    public String toString() {
        return lexeme;
    }
}
