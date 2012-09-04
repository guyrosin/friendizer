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
package com.teamagly.friendizer.model;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.teamagly.friendizer.PMF;

/**
 * An account may be associated with multiple devices, but each device may be associated with a single account.
 */
@PersistenceCapable(
		identityType = IdentityType.APPLICATION)
public class UserDevice {

	@PrimaryKey
	@Persistent(
			valueStrategy = IdGeneratorStrategy.IDENTITY)
	Long id;

	@Persistent
	private String regID;

	/**
	 * The ID used for sending messages to.
	 */
	@Persistent
	private Long userID;

	public UserDevice(String regID, long userID) {
		this.regID = regID;
		this.userID = userID;
	}

	/**
	 * Helper function - will query all registrations for a user.
	 */
	@SuppressWarnings("unchecked")
	public static List<UserDevice> getDeviceInfoForUser(long userID) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query query = pm.newQuery(UserDevice.class);
			query.setFilter("userID == " + userID);
			List<UserDevice> result = (List<UserDevice>) query.execute();
			query.closeAll();
			return result;
		} finally {
			pm.close();
		}
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the regID
	 */
	public String getRegID() {
		return regID;
	}

	/**
	 * @param regID
	 *            the regID to set
	 */
	public void setRegID(String regID) {
		this.regID = regID;
	}

	/**
	 * @return the userID
	 */
	public Long getUserID() {
		return userID;
	}

	/**
	 * @param userID
	 *            the userID to set
	 */
	public void setUserID(long userID) {
		this.userID = userID;
	}
}
