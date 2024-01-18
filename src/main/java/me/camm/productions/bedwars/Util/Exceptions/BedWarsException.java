package me.camm.productions.bedwars.Util.Exceptions;

public abstract class BedWarsException extends RuntimeException {

    protected String exceptionMessage;
    public BedWarsException(String message) {
        super(message);
        this.exceptionMessage = message;
    }
}
