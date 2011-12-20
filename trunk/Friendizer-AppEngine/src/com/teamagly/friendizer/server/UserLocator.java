package com.teamagly.friendizer.server;

import com.google.web.bindery.requestfactory.shared.Locator;


public class UserLocator extends Locator<User, Void> {

	@Override
	public User create(Class<? extends User> clazz) {
		return new User();
	}

	@Override
	public User find(Class<? extends User> clazz, Void id) {
		return create(clazz);
	}

	@Override
	public Class<User> getDomainType() {
		return User.class;
	}

	@Override
	public Void getId(User domainObject) {
		return null;
	}

	@Override
	public Class<Void> getIdType() {
		return Void.class;
	}

	@Override
	public Object getVersion(User domainObject) {
		return null;
	}

}
