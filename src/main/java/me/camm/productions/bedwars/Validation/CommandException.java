package me.camm.productions.bedwars.Validation;

public class CommandException extends BedWarsException {
    public CommandException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return "Command Error: "+exceptionMessage;
    }
}
