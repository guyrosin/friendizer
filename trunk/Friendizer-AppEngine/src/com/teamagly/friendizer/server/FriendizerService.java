package com.teamagly.friendizer.server;

import java.util.List;

import com.teamagly.friendizer.annotation.ServiceMethod;


public class FriendizerService {
	static DataStore db = new DataStore();
	static OnlineUserStore ous = new OnlineUserStore();
	
	@ServiceMethod
	public OnlineUser createOnlineUser() {
		// TODO Auto-generated method stub
		return null;
	}

	@ServiceMethod
	public OnlineUser readOnlineUser(Long id) {
		OnlineUser user = ous.find(id); 
		return user;
	}

	@ServiceMethod
	public OnlineUser updateOnlineUser(OnlineUser onlineuser) {
		return ous.update(onlineuser);
	}

	@ServiceMethod
	public void deleteOnlineUser(OnlineUser onlineuser) {
		// TODO Auto-generated method stub

	}

	@ServiceMethod
	public List<OnlineUser> queryOnlineUsers() {
		// TODO Auto-generated method stub
		return null;
	}

	@ServiceMethod
	public User createUser() {
		User user = new User();
		return db.update(user);

	}

	@ServiceMethod
	public User readUser(Long id) {

		User user = db.find(id); 
		return user;
	}

	@ServiceMethod
	public User updateUser(User user) {

		return db.update(user);
	}

	@ServiceMethod
	public void deleteUser(User user) {
		db.delete(user.getId());

	}

	@ServiceMethod
	public List<User> queryUsers() {
		// TODO Auto-generated method stub
		return null;
	}

}
