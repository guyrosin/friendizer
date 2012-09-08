package com.teamagly.friendizer;

/**
 * Utility methods for getting the base URL for client-server communication and retrieving shared preferences.
 */
public class Util {
	/**
	 * 
	 * The function calculates the level according to the given points
	 * 
	 * @param currentLevel
	 *            - the current level of the user
	 * @param points
	 *            - the updated points of the user
	 * @return the new level of the user (his current level if the points are not enough and the next level otherwise)
	 */
	public static int calculateLevel(int currentLevel, long points) {
		// Calculate the threshold for next level
		double threshold = 200 * Math.pow(currentLevel, 1.5);

		// If the user has enough points for the next level - return the next level
		if (points >= threshold)
			return currentLevel + 1;
		else
			return currentLevel;
	}
}
