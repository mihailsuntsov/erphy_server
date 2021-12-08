package com.dokio.repository.Exceptions;

//используем при отрицательной сумме у объектов (касс, расч. счетов)
public class CantSetHistoryCauseNegativeSumException extends Exception {
    @Override
    public void printStackTrace() {
        System.err.println("Can't do operation because of an negative sum");
    }
}
