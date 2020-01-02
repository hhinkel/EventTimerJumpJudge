package com.example.jumpjudge;

public final class Rider {
    private int riderNumber;
    private String division;
    private int fenceNumber;
    private int refusals;
    private long jumpTime;
    private String other;
    private long holdTime;
    private String edit;

    Rider(int riderNumber, String division, int fenceNumber, int refusals, long jumpTime, String other, long holdTime, String edit) {
        this.riderNumber = riderNumber;
        this.division = division;
        this.fenceNumber = fenceNumber;
        this.refusals = refusals;
        this.jumpTime = jumpTime;
        this.other = other;
        this.holdTime = holdTime;
        this.edit = edit;
    }

    public int getRiderNumber (){
        return riderNumber;
    }

    public String getDivision () { return division; }

    public int getFenceNumber () { return fenceNumber; }

    public int getRefusals () { return refusals; }

    public long getJumpTime() {
        return jumpTime;
    }

    public String getOther() {
        return other;
    }

    public long getHoldTime() { return holdTime; }

    public String getEdit() { return edit; }
}

