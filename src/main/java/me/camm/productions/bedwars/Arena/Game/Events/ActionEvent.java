package me.camm.productions.bedwars.Arena.Game.Events;


/**
 * @author CAMM
 * This class is a wrapper class for an action event. It includes the time
 * of when it should activate, as well as an action to do.
 */
public class ActionEvent {


    private final int time;
    private final Action action;


    //constructor
    public ActionEvent(int time, Action action) {
        this.action = action;
        this.time = time;
    }

    //activate the event.
    public void activateEvent() {
            action.activate();
    }

    //get the header for the scoreboard of the players if the action is a physical event
    public String getHeader(){
        if (action instanceof GameActionPhysical)
            return ((GameActionPhysical)action).getHeader();
        return null;
    }

    //get the time of which the event should activate
    public int getActivationTime(){
        return time;
    }

}
