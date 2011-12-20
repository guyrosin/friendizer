package com.teamagly.friendizer.shared;

import java.util.List;

import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServiceName;

@ServiceName(value = "com.teamagly.friendizer.server.FriendizerService", locator = "com.teamagly.friendizer.server.FriendizerServiceLocator")
public interface FriendizerRequest extends RequestContext {

	Request<OnlineUserProxy> createOnlineUser();

	Request<OnlineUserProxy> readOnlineUser(Long id);

	Request<OnlineUserProxy> updateOnlineUser(OnlineUserProxy onlineuser);

	Request<Void> deleteOnlineUser(OnlineUserProxy onlineuser);

	Request<List<OnlineUserProxy>> queryOnlineUsers();

	Request<UserProxy> createUser();

	Request<UserProxy> readUser(Long id);

	Request<UserProxy> updateUser(UserProxy user);

	Request<Void> deleteUser(UserProxy user);

	Request<List<UserProxy>> queryUsers();

}
