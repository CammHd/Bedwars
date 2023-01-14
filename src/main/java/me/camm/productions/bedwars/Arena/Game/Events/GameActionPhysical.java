package me.camm.productions.bedwars.Arena.Game.Events;

public abstract class GameActionPhysical extends Action{
    protected boolean spent;
    protected String header;
    abstract String getHeader();
    //the header is the string to show on the scoreboard AS the time approaches activation time
}
