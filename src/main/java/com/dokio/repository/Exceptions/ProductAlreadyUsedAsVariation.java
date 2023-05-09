package com.dokio.repository.Exceptions;

//Нельзя использовать один товар в нескольких вариациях
//You can not use one product in several variations
public class ProductAlreadyUsedAsVariation  extends Exception{
    @Override
    public void printStackTrace() {
        System.err.println("You can not use one product in several variations");
    }
}
