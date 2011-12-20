package com.teamagly.friendizer.server;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.android.c2dm.server.PMF;

public class OnlineUserStore {
	
	static DataStore db = new DataStore();
	public OnlineUser find(Long id) {
		if (id == null) {
			return null;
		}
		User user = db.find(id);
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query query = pm.newQuery("select from " + OnlineUser.class.getName()
					+ " where id==" + user.getId());
			List<OnlineUser> list = (List<OnlineUser>) query.execute();
			return (list.size() == 0 ? null : list.get(0));
		} catch (RuntimeException e) {
			System.out.println(e);
			throw e;
		} finally {
			pm.close();
		}
	}

	/**
	 * Persist this object in the datastore.
	 */
	public OnlineUser update(OnlineUser user) {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.makePersistent(user);
			return user;
		} finally {
			pm.close();
		}
	}
}
