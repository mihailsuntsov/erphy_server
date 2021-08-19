package com.dokio.repository.Exceptions;

public class InsertProductHistoryExceprions  extends Exception {
    @Override
    public void printStackTrace() {
        System.err.println("Can't insert table row in products_history - operation error");
    }
}

