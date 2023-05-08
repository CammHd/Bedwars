package me.camm.productions.bedwars.Game.Scoreboard;

import me.camm.productions.bedwars.Game.BattlePlayer;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;

public class ScoreEntry {
    private final int value;
    private volatile String identifier;
    private volatile String name;
    private final BattlePlayer player;

    private final ScoreboardObjective primary;
    private final ScoreboardObjective buffer;

    private volatile ScoreboardScore primaryScore;
    private volatile ScoreboardScore bufferScore;

    private final Scoreboard board;


    public ScoreEntry(Scoreboard board, int value, String identifier, String name, BattlePlayer player, ScoreboardObjective primary, ScoreboardObjective buffer) {
        this.board = board;
        this.value = value;
        this.identifier = identifier;
        this.name = name;

        this.player = player;


        this.primary = primary;
        this.buffer = buffer;

        this.primaryScore = new ScoreboardScore(board,primary, name);
        this.primaryScore.setScore(value);

        this.bufferScore = new ScoreboardScore(board, buffer, name);
        this.bufferScore.setScore(value);

    }

    public synchronized void setName(String name) {
        this.name = name;
    }

    public synchronized void setIdentifier(String newIdentifier) {
        this.identifier = newIdentifier;
    }

    public synchronized String getIdentifier()
    {
        return identifier;
    }

    public synchronized String getName() {
        return name;
    }


    public void sendPrimary()
    {
      send(new PacketPlayOutScoreboardScore(primaryScore));
    }


    public void sendBuffer()
    {
      send(new PacketPlayOutScoreboardScore(bufferScore));
    }



    private void send(Packet<?> packet)
    {
        ((CraftPlayer)player.getRawPlayer()).getHandle().playerConnection.sendPacket(packet);
    }


    /*
    @Author CAMM
    Updates the score based on the value and name.
     */
    public synchronized void recalculate()
    {
       this.primaryScore = new ScoreboardScore(board,primary,name);
       primaryScore.setScore(value);

       this.bufferScore = new ScoreboardScore(board,buffer,name);
       bufferScore.setScore(value);

    }







}
