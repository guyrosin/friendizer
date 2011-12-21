package com.teamagly.friendizer.shared;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

@ProxyForName(value = "com.teamagly.friendizer.server.User", locator = "com.teamagly.friendizer.server.UserLocator")
public interface UserProxy extends ValueProxy {

	Long getId();

	void setId(Long id);
	
	String getUsername();

	void setUsername(String username);

	Integer getValue();

	void setValue(Integer value);

	Integer getMoney();

	void setMoney(Integer money);

	Long getCoordinateX();

	void setCoordinateX(Long coordinateX);

	Long getCoordinateY();

	void setCoordinateY(Long coordinateY);

	Boolean getOnline();

	void setOnline(Boolean online);

}
