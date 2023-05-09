package com.dokio.repository.Exceptions;

//Variable product can't be selected as a variation (throws if a variable product is selected as a variation of another product)
//Вариативный товар нельзя использовать в качестве вариации (бросается если вариативный товар выбрали в качестве вариации у другого товара)

public class VariableProductCantBeUsedAsVariation extends Exception{
    @Override
    public void printStackTrace() {
        System.err.println("Variable product can't be used as a variation");
    }
}
