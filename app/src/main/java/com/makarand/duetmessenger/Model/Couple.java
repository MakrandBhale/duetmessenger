package com.makarand.duetmessenger.Model;

public class Couple {
    private String p1 = null, p2 = null;

    public Couple(String p1, String p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public Couple() {
    }

    public String getP1() {
        return p1;
    }

    public void setP1(String p1) {
        this.p1 = p1;
    }

    public String getP2() {
        return p2;
    }

    public void setP2(String p2) {
        this.p2 = p2;
    }
}
