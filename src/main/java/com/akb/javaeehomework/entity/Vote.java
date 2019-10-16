package com.akb.javaeehomework.entity;

public class Vote {
    private int votecount=0;
    private String name="";

    public void setVotecount(int votecount) {
        this.votecount = votecount;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getVotecount() {
        return votecount;
    }
}
