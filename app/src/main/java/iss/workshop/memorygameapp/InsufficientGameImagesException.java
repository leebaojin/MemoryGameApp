package iss.workshop.memorygameapp;

public class InsufficientGameImagesException extends Exception{

    public InsufficientGameImagesException(){
        super("Insufficent Number Of Game Images");
    }
}
