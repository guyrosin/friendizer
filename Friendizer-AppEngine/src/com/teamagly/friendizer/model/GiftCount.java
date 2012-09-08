package com.teamagly.friendizer.model;

public class GiftCount {
	private Gift gift;
	private int count;

	public GiftCount(Gift gift, int count) {
		this.gift = gift;
		this.count = count;
	}

	public GiftCount(Gift gift) {
		this.gift = gift;
		this.count = 0;
	}

	public Gift getGift() {
		return gift;
	}

	public int getCount() {
		return count;
	}
}
