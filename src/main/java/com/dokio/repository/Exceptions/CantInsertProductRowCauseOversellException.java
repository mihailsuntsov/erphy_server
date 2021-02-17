package com.dokio.repository.Exceptions;

public class CantInsertProductRowCauseOversellException extends Exception {
    @Override
    public void printStackTrace() {
        System.err.println("Can't insert retail_sales_products table row because of available < product_count");
    }
}
