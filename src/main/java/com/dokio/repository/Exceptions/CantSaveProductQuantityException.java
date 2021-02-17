package com.dokio.repository.Exceptions;

public class CantSaveProductQuantityException extends Exception {
    @Override
    public void printStackTrace() {
        System.err.println("Can't insert product_quantity table row");
    }
}
