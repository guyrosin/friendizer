package com.teamagly.friendizer;

import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.android.c2dm.server.PMF;
import com.teamagly.friendizer.model.DeviceInfo;

public class DatastoreHelper {

	private static final DatastoreHelper instance = new DatastoreHelper();

	private DatastoreHelper() {

	}

	public static DatastoreHelper getInstance() {
		return instance;
	}

	public DeviceInfo getDeviceInfo(long id) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(DeviceInfo.class);

		query.setFilter("userID == " + id);

		List<DeviceInfo> devices = (List<DeviceInfo>) query.execute();
		DeviceInfo device = devices.get(0);

		for (int i = 1; i < devices.size(); i++) {
			Date newest = device.getRegistrationTimestamp();
			Date cur = devices.get(i).getRegistrationTimestamp();
			if (cur.compareTo(newest) > 0)
				device = devices.get(i);
		}

		return device;
	}

}
