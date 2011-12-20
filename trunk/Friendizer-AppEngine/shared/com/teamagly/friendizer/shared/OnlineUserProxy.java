package com.teamagly.friendizer.shared;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

@ProxyForName(value = "com.teamagly.friendizer.server.OnlineUser", locator = "com.teamagly.friendizer.server.OnlineUserLocator")
public interface OnlineUserProxy extends ValueProxy {

	UserProxy getUser();
	
	void setId(Long id);

	void setUser(UserProxy user);

	Long getCoordinateX();

	void setCoordinateX(Long coordinateX);

	Long getCoordinateY();

	void setCoordinateY(Long coordinateY);

	Long getId();

}
