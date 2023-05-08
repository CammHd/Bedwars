package me.camm.productions.bedwars.Util.Randoms;

import java.util.List;
import java.util.Random;


//@author CAMM
public class WeightedRandom<T extends WeightedItem<?>>
{
    private final List<T> items;
    private final static Random rand;

    static{
        rand = new Random();
    }
    public WeightedRandom(List<T> items)
    {
        this.items = items;
        if (items == null || items.size() == 0)
            throw new IllegalArgumentException("Items must not be null and must at least have 1 item within");
    }

    public T getNext(){

        double weight = 0;
        for (T item: items)
            weight += item.getWeight();

        T chosen = null;

        for (int slot=0;slot<items.size();slot++)
        {
            double chance = rand.nextDouble() * weight;
            if (chance < items.get(slot).getWeight())
            {
                chosen = items.get(slot);
                break;
            }
            else
                weight -= items.get(slot).getWeight();


            if (slot == items.size() -1)
            {
                chosen = items.get(slot);
                break;
            }
        }
        return chosen;
    }
}