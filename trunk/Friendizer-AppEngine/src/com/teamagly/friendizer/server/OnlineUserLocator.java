package com.teamagly.friendizer.server;

import com.google.web.bindery.requestfactory.shared.Locator;


public class OnlineUserLocator extends Locator<OnlineUser, Void> {

	@Override
	public OnlineUser create(Class<? extends OnlineUser> clazz) {
		return new OnlineUser();
	}

	@Override
	public OnlineUser find(Class<? extends OnlineUser> clazz, Void id) {
		return create(clazz);
	}

	@Override
	public Class<OnlineUser> getDomainType() {
		return OnlineUser.class;
	}

	@Override
	public Void getId(OnlineUser domainObject) {
		return null;
	}

	@Override
	public Class<Void> getIdType() {
		return Void.class;
	}

	@Override
	public Object getVersion(OnlineUser domainObject) {
		return null;
	}

}
