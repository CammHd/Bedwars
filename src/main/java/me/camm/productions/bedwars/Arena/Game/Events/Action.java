package me.camm.productions.bedwars.Arena.Game.Events;

import me.camm.productions.bedwars.Util.Helpers.ChatSender;

/**
 * @author CAMM
 * This abstract class is used for events that occur in game (E.g dragon spawn action, etc)
 */
public abstract class Action{
    protected ChatSender sender;


    public Action(){
        sender = ChatSender.getInstance();
    }

    public abstract void activate();
}
