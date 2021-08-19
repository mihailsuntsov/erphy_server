package com.dokio.repository.Exceptions;

public class NotEnoughPermissionsException  extends Exception {
    @Override
    public void printStackTrace() {
        System.err.println("Not enough permissions for the operation");
    }
}
