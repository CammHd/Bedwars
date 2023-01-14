package me.camm.productions.bedwars.Util;

public enum BlockTag {
    RED((byte)1),
    BLUE((byte)2),
    YELLOW((byte)4),
    WHITE((byte)8),
    AQUA((byte)16),
    GRAY((byte)32),
    PINK((byte)64),
    GREEN((byte)-128),
    ALL((byte)-1),
    NONE((byte)0);

    final byte tag;
    BlockTag(byte tag){
        this.tag = tag;
    }
    public byte getTag(){
        return tag;
    }
}
