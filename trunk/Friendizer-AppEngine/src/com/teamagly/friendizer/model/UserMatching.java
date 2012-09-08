package com.teamagly.friendizer.model;

public class UserMatching {
	private User user;
	private int matching;

	public UserMatching(User user, int matching) {
		this.user = user;
		this.matching = matching;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public int getMatching() {
		return matching;
	}

	public void setMatching(int matching) {
		this.matching = matching;
	}
}
