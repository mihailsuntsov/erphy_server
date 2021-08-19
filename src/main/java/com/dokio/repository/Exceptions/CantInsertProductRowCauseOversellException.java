package com.dokio.repository.Exceptions;

//используем когда кол-во товара на складе меньше чем продаваемое или списываемое количество
public class CantInsertProductRowCauseOversellException extends Exception {
    @Override
    public void printStackTrace() {
        System.err.println("Can't do operation because of available quantity less than operation quantity");
    }
}
