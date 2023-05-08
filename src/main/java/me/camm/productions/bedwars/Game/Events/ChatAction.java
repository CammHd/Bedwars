package me.camm.productions.bedwars.Game.Events;


/*
 * @author CAMM
 * This class models non physical events (These events only have to do with
 * scoreboards or the chat)
 */
public class ChatAction extends Action
{
    private final String text;

    private boolean spent;


    /*
     *
     * @param text the text to send
     */
    public ChatAction(String text) {
        this.text = text;
        spent = false;
    }

    //We use the chat sender to send messages.
    @Override
    public void activate() {
        if (!spent)
            sender.sendMessage(text);
        spent = true;
    }
}
