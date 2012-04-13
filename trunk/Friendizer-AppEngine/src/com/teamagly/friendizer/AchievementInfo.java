package com.teamagly.friendizer;

import org.json.*;

public class AchievementInfo {
	private Achievement achv;
	private boolean earned;
	
	public AchievementInfo(Achievement achv, boolean earned) {
		this.achv = achv;
		this.earned = earned;
	}
	
	public AchievementInfo(JSONObject obj) throws JSONException {
		long id = obj.getLong("id");
		String title = obj.getString("title");
		String description = obj.getString("description");
		String iconRes = obj.getString("iconRes");
		int reward = obj.getInt("reward");
		achv = new Achievement(id, title, description, iconRes, reward);
		earned = obj.getBoolean("earned");
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
	
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("id", achv.getId());
			obj.put("title", achv.getTitle());
			obj.put("description", achv.getDescription());
			obj.put("iconRes", achv.getIconRes());
			obj.put("reward", achv.getReward());
			obj.put("earned", earned);
		} catch (JSONException e) {
		}
		return obj;
	}
	
	@Override
	public String toString() {
		return toJSONObject().toString();
	}
}
