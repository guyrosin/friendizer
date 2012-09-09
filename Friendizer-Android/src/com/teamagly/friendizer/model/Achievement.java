/**
 * 
 */
package com.teamagly.friendizer.model;

public class Achievement {
	private Long id;

	private String title;

	private String description;

	private String iconRes;

	private int reward;

	private long points;

	public boolean earned;

	public Achievement(Achievement achv, boolean earned) {
		id = achv.id;
		title = achv.title;
		description = achv.description;
		iconRes = achv.iconRes;
		reward = achv.reward;
		points = achv.points;
		this.earned = earned;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIconRes() {
		return iconRes;
	}

	public void setIconRes(String iconRes) {
		this.iconRes = iconRes;
	}

	public int getReward() {
		return reward;
	}

	public void setReward(int reward) {
		this.reward = reward;
	}

	public long getPoints() {
		return points;
	}

	public void setPoints(long points) {
		this.points = points;
	}

	/**
	 * @return the earned
	 */
	public boolean isEarned() {
		return earned;
	}

	/**
	 * @param earned
	 *            the earned to set
	 */
	public void setEarned(boolean earned) {
		this.earned = earned;
	}

}
