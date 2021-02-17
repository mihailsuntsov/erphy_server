package com.dokio.repository.Exceptions;

public class CantInsertProductRowCauseErrorException extends Exception {
    @Override
    public void printStackTrace() {
        System.err.println("Can't insert retail_sales_products table row - operation error");
    }
}
