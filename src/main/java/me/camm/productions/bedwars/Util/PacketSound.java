package me.camm.productions.bedwars.Util;

import net.minecraft.server.v1_8_R3.PacketPlayOutNamedSoundEffect;

public enum PacketSound
{
    DRAGON("mob.enderdragon.growl",1F),
    PLING("note.pling",1.4F),
    DING("note.pling",2F),
    RECORD_STAL("records.stal",1F),
    ALARM("note.pling",1.5F),
    ALARM_TWO("note.pling",1.6F),
    WITHER("mob.wither.death",1F),
    ENDERMAN("mob.endermen.portal",1F);

    private final String soundName;
    private final float pitch;

    PacketSound(String soundName, float pitch) {
        this.soundName = soundName;
        this.pitch = pitch;
    }

    public String getSoundName() {
        return soundName;
    }

    public float getPitch() {
        return pitch;
    }

    public PacketPlayOutNamedSoundEffect asPacket(double x, double y, double z){
        return new PacketPlayOutNamedSoundEffect(getSoundName(), x,y,z, 1,getPitch());
    }
}
