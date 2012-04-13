package com.teamagly.friendizer;

import javax.jdo.annotations.*;

@PersistenceCapable
public class UserAchievement {
	@Persistent
	private long userID;
	
	@Persistent
	private long achievementID;

	public UserAchievement(long userID, long achievementID) {
		this.userID = userID;
		this.achievementID = achievementID;
	}

	public long getUserID() {
		return userID;
	}

	public void setUserID(long userID) {
		this.userID = userID;
	}

	public long getAchievementID() {
		return achievementID;
	}

	public void setAchievementID(long achievementID) {
		this.achievementID = achievementID;
	}
}
