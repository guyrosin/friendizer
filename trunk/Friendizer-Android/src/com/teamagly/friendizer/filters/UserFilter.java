package com.teamagly.friendizer.filters;

import com.teamagly.friendizer.model.User;

public class UserFilter {
	private String name;
	private String gender;
	private int minAgeValue;
	private int maxAgeValue;

	public UserFilter(String name, String gender, int minAgeValue, int maxAgeValue) {
		this.name = name.toLowerCase();
		this.gender = gender.toLowerCase();
		this.minAgeValue = minAgeValue;
		this.maxAgeValue = maxAgeValue;
	}

	public UserFilter() {
		name = "";
		gender = "all";
		minAgeValue = 0;
		maxAgeValue = 100;
	}

	public boolean satistfies(User user) {
		String userGender = user.getGender();
		if (!userGender.equals("") && !gender.equals("all") && !gender.equals(user.getGender()))
			return false;
		if (minAgeValue != 0 || maxAgeValue != 100) { // If the user changed the range
			String userAgeStr = user.getAge();
			if (userAgeStr == null || userAgeStr.length() == 0)
				return false;
			int userAge = Integer.parseInt(user.getAge());
			if (userAge < minAgeValue || userAge > maxAgeValue)
				return false;
		}
		if (!user.getName().toLowerCase().contains(name))
			return false;
		return true;
	}

	public String getName() {
		return name;
	}

	public String getGender() {
		return gender;
	}

	public int getMinAgeValue() {
		return minAgeValue;
	}

	public int getMaxAgeValue() {
		return maxAgeValue;
	}

	public boolean isBlank() {
		UserFilter blankFilter = new UserFilter();
		return (name.equals(blankFilter.name) && gender.equals(blankFilter.gender) && minAgeValue == blankFilter.minAgeValue && maxAgeValue == blankFilter.maxAgeValue);
	}
}
