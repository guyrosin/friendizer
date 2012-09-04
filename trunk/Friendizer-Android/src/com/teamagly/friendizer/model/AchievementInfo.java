package com.teamagly.friendizer.model;

public class AchievementInfo {
	private Achievement achv;
	private boolean earned;

	public AchievementInfo(Achievement achv, boolean earned) {
		this.achv = achv;
		this.earned = earned;
	}

	public Achievement getAchv() {
		return achv;
	}

	public void setAchv(Achievement achv) {
		this.achv = achv;
	}

	public boolean isEarned() {
		return earned;
	}

	public void setEarned(boolean earned) {
		this.earned = earned;
	}
}
