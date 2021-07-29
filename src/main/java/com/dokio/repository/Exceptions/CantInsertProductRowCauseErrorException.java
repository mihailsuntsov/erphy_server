package com.dokio.repository.Exceptions;

public class CantInsertProductRowCauseErrorException extends Exception {
    @Override
    public void printStackTrace() {
        System.err.println("Can't insert products table row - operation error");
    }
}
