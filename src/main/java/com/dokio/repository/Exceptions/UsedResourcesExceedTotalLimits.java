package com.dokio.repository.Exceptions;

//Нельзя сохранить, если используемые ресурсы будут выходить за суммарные лимиты
//Cannot be saved if the used resources will exceed the total limits

public class UsedResourcesExceedTotalLimits  extends Exception {

    @Override
    public void printStackTrace() {
        System.err.println("Cannot be saved if the used resources will exceed the total limits");
    }


}
