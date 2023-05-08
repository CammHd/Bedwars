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
    ALL((byte)-1), //player placed block tag.
    NONE((byte)0);  //generators, base air, basically restricted blocks


    /*

    byte 1 --> index 0

      byte x = 0000 0001
      [red][blue][...][][][][]


       if (x != all && x != none)


         Team team = getTeam(x)




         Array[Battleteam] = new BattleTeam[8];

         TeamColor



      public static int getIndex(byte number) {
        int index;
        if (getValueAtPosition(7, number) == 1) {
            number = (byte) (Math.abs(number) - 1);  //getting how big it is
            index = (int) (Math.log(number) / Math.log(2) + 1); /// change of base formula
        }
        else index = (int) (Math.log(number) / Math.log(2));

     return index;
    }

      2^7 - > index 7

     1000 0000




       public static int getValueAtPosition(int shift, byte number) {
        byte result = (byte) (number >> shift);
        return Math.abs(result % 2);
    }


     */

    final byte tag;
    BlockTag(byte tag){
        this.tag = tag;
    }
    public byte getTag(){
        return tag;
    }
}
