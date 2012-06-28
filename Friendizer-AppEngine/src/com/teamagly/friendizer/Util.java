/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse Public
 * License v1.0 which accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 *******************************************************************************/
package com.teamagly.friendizer;

/**
 * Utility methods for getting the base URL for client-server communication and retrieving shared preferences.
 */
public class Util {

	/**
	 * Key for user ID (from Facebook) in shared preferences.
	 */
	public static final String USER_ID = "userID";

	/**
	 * Key for device registration id in shared preferences.
	 */
	public static final String REG_ID = "regID";

	public static final String ACCESS_TOKEN = "accessToken";

	public static final String SENDER_ID = "AIzaSyA52pp613NNTl8BncGIh0wDbCDPb78y5X0"; // GCM sender ID

	/**
	 * 
	 * The function calculates the level according to the given points
	 * 
	 * @param currentLevel
	 *            - the current level of the user
	 * @param points
	 *            - the updated points of the user
	 * @return int - the new level of the user (his current level if the points are not enough and the next level otherwise)
	 */
	public static final int calculateLevel(int currentLevel, long points) {
		// Calculate the threshold for next level
		double threshold = 200 * Math.pow(currentLevel, 1.5);

		// If the user has enough points for the next level - return the next level
		if (points >= threshold)
			return currentLevel + 1;
		else
			return currentLevel;
	}
}
