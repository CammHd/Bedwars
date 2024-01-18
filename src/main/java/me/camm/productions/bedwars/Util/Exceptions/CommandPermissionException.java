package me.camm.productions.bedwars.Util.Exceptions;

public class CommandPermissionException extends CommandException {
    public CommandPermissionException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return "Permission denied: "+exceptionMessage;
    }
}
