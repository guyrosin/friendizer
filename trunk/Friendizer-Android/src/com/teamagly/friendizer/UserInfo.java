package com.teamagly.friendizer;

public class UserInfo {
    private long id;
    private long value;
    private long money;
    private long owner;
    private long[] ownsList;

    public UserInfo(long id, long value, long money, long owner, long[] ownsList) {
	this.id = id;
	this.value = value;
	this.money = money;
	this.owner = owner;
	this.ownsList = ownsList;
    }

    public long getId() {
	return id;
    }

    public long getValue() {
	return value;
    }

    public long getMoney() {
	return money;
    }

    public long getOwner() {
	return owner;
    }

    public long[] getOwnsList() {
	return ownsList;
    }
}
