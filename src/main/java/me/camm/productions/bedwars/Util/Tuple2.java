package me.camm.productions.bedwars.Util;

public class Tuple2<A,B> {


    public Tuple2(A a, B b) {
        this.firstElem = a;
        this.secondElem = b;
    }

    A firstElem;
    B secondElem;

    public A getFirstElem() {
        return firstElem;
    }

    public B getSecondElem() {
        return secondElem;
    }

}
