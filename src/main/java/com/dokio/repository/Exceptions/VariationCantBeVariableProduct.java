package com.dokio.repository.Exceptions;

//Variation can't be variable product (thrown when trying to make a product variable if it is already used as a variation in another variable product)
//Вариация не может быть вариативным товаром (бросается при попытке сделать товар вариативным, если он уже используется как вариация в другом вариативном товаре)

public class VariationCantBeVariableProduct extends Exception{
    @Override
    public void printStackTrace() {
        System.err.println("Variation can't be variable product");
    }
}