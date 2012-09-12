package com.teamagly.friendizer.model;

import java.util.Date;

public class ActionInfo {
	private User user;
	private boolean youBoughtHim;
	private Date date;
	
	public ActionInfo(User user, boolean youBoughtHim, Date date) {
		this.user = user;
		this.youBoughtHim = youBoughtHim;
		this.date = date;
	}

	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public boolean isYouBoughtHim() {
		return youBoughtHim;
	}
	
	public void setYouBoughtHim(boolean youBoughtHim) {
		this.youBoughtHim = youBoughtHim;
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
}
