package com.dokio.repository.Exceptions;

public class CantGetProductsTableException  extends Exception{
    @Override
    public void printStackTrace() {
        System.err.println("Can't get products table");
    }
}
