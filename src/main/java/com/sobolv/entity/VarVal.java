package com.sobolv.entity;

import com.sobolv.enums.Token;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VarVal {

    private int index;
    private Token token;
    private Double value;

    public VarVal(int index) {
        this.index = index;
    }
}
