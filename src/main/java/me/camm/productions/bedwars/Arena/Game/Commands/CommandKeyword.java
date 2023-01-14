package me.camm.productions.bedwars.Arena.Game.Commands;


/*
This enum is used for permissions and their names.
@author CAMM
 */
public enum CommandKeyword
{
    SETUP("setup","setup.do"),
    REGISTER("register","register.do"),
    SHOUT("shout","shout.do"),
    UNREGISTER("unregister","unregister.do"),
    END("endgame","endgame.do"),
    START("start","start.do");

    //word is the label of the command, perm is the permission name
    private final String word;
    private final String perm;


    //constructor
    CommandKeyword(String word, String perm)
    {
        this.word = word;
        this.perm = perm;
    }

    //Getters
    public String getWord() {
        return word;
    }

    public String getPerm() {
        return perm;
    }
}
