package me.camm.productions.bedwars.Util.Locations;


public enum BlockRegisterType
{
    GENERATOR("GENERATOR"),
    BASE("BASE"),
    ARENA("ARENA"),
    BED("BED"),
    CHEST("CHEST"),
    MAP("MAP"),
    AURA("AURA"),
    TRAP("TRAP");

   private final String data;

   BlockRegisterType(String data)
   {
       this.data = data;
   }

   public String getData()
   {
       return data;
   }
}
