package com.example.chat_real_time;

public class Recent_Contact {
    private String name, dp, ID;

    public Recent_Contact(String name, String dp, String ID) {
        this.name = name;
        this.dp = dp;
        this.ID = ID;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDp() {
        return dp;
    }

    public void setDp(String dp) {
        this.dp = dp;
    }
}
