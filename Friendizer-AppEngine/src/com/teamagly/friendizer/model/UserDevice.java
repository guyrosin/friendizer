package com.teamagly.friendizer.model;

import java.util.List;

import javax.jdo.*;
import javax.jdo.Query;
import javax.jdo.annotations.*;

import com.teamagly.friendizer.PMF;

/**
 * An account may be associated with multiple devices, but each device may be associated with a single account.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class UserDevice {
	@PrimaryKey
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
		Query query = pm.newQuery(UserDevice.class);
		query.setFilter("userID == " + userID);
		List<UserDevice> result = (List<UserDevice>) query.execute();
		result.size(); // App Engine bug workaround
		query.closeAll();
		pm.close();
		return result;
	}

	/**
	 * @return the regID
	 */
	public String getRegID() {
		return regID;
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
